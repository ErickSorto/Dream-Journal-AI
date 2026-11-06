# Pinned Spaceship model serialization contracts. Recorder replaces every API call.
require "minitest/autorun"
require "spaceship"

class ModelRecorder
  Page = Struct.new(:models) do
    def first = models.first
    def to_models = models
    def all_pages = [self]
  end
  attr_reader :calls
  attr_accessor :models
  def initialize
    @calls, @models = [], []
  end
  def method_missing(name, **arguments)
    @calls << [name, arguments]
    Page.new(@models)
  end
  def respond_to_missing?(_name, _private = false) = true
end

class PinnedAppleModelsTest < Minitest::Test
  API = Spaceship::ConnectAPI
  def setup
    @client = ModelRecorder.new
  end
  def test_app_info_update_maps_only_selected_privacy_field
    locale = API::AppInfoLocalization.new("locale", { "locale" => "en-US", "name" => "Existing name" })
    locale.update(client: @client, attributes: { privacy_policy_url: "https://example.com/privacy" })
    assert_equal [:patch_app_info_localization, { app_info_localization_id: "locale", attributes: { "privacyPolicyUrl" => "https://example.com/privacy" } }], @client.calls.last
  end
  def test_version_locale_update_maps_selected_fields_without_unselected_content
    locale = API::AppStoreVersionLocalization.new("version-locale", {})
    locale.update(client: @client, attributes: { support_url: "https://example.com/support", promotional_text: "Reviewed promotion", whats_new: "Reviewed notes" })
    assert_equal [:patch_app_store_version_localization, { app_store_version_localization_id: "version-locale", attributes: { "supportUrl" => "https://example.com/support", "promotionalText" => "Reviewed promotion", "whatsNew" => "Reviewed notes" } }], @client.calls.last
  end
  def test_new_localizations_require_camel_case_attributes_and_keep_exact_parent
    info = API::AppInfo.new("info", {})
    info.create_app_info_localization(client: @client, attributes: { locale: "fr-FR", name: "Nom", privacyPolicyUrl: "https://example.com/confidentialite" })
    assert_equal [:post_app_info_localization, { app_info_id: "info", attributes: { locale: "fr-FR", name: "Nom", privacyPolicyUrl: "https://example.com/confidentialite" } }], @client.calls.last
    version = API::AppStoreVersion.new("version", {})
    version.create_app_store_version_localization(client: @client, attributes: { locale: "fr-FR", description: "Description", supportUrl: "https://example.com/aide" })
    assert_equal [:post_app_store_version_localization, { app_store_version_id: "version", attributes: { locale: "fr-FR", description: "Description", supportUrl: "https://example.com/aide" } }], @client.calls.last
  end
  def test_manual_release_and_exact_build_are_independent_narrow_relationship_updates
    version = API::AppStoreVersion.new("version", {})
    version.update(client: @client, attributes: { release_type: "MANUAL" })
    version.select_build(client: @client, build_id: "exact-build")
    assert_equal [:patch_app_store_version, { app_store_version_id: "version", attributes: { "releaseType" => "MANUAL" } }], @client.calls[0]
    assert_equal [:patch_app_store_version_with_build, { app_store_version_id: "version", build_id: "exact-build" }], @client.calls[1]
  end
  def test_reviewer_notes_update_does_not_serialize_contact_or_login_fields
    detail = API::AppStoreReviewDetail.new("review", { "demoAccountPassword" => "SENTINEL_SECRET" })
    detail.update(client: @client, attributes: { notes: "Reviewed tester instructions" })
    assert_equal [:patch_app_store_review_detail, { app_store_review_detail_id: "review", attributes: { "notes" => "Reviewed tester instructions" } }], @client.calls.last
    refute_includes @client.calls.inspect, "SENTINEL_SECRET"
  end
  def test_build_lookup_filters_exact_app_version_number_and_platform
    app = API::App.new("app", { "bundleId" => "example.app" })
    prerelease = API::PreReleaseVersion.new("pre", { "version" => "1.2.3", "platform" => "IOS" })
    build = API::Build.new("build", { "app" => app, "preReleaseVersion" => prerelease, "version" => "33", "processingState" => "VALID" })
    @client.models = [build]
    results = API::Build.all(client: @client, app_id: "app", version: "1.2.3", build_number: "33", platform: "IOS")
    assert_equal [build], results
    assert_equal "example.app", results.first.bundle_id
    assert_equal "1.2.3", results.first.app_version
    assert_equal "IOS", results.first.platform
    filter = @client.calls.last[1][:filter]
    assert_equal "app", filter[:app]
    assert_equal "1.2.3", filter["preReleaseVersion.version"]
    assert_equal "33", filter[:version]
  end
  def test_review_submission_and_public_release_use_separate_exact_targets
    submission = API::ReviewSubmission.new("submission", {})
    submission.add_app_store_version_to_review_items(client: @client, app_store_version_id: "version")
    submission.submit_for_review(client: @client)
    version = API::AppStoreVersion.new("version", {})
    version.create_app_store_version_release_request(client: @client)
    assert_equal [
      [:post_review_submission_item, { review_submission_id: "submission", app_store_version_id: "version" }],
      [:patch_review_submission, { review_submission_id: "submission", attributes: { submitted: true } }],
      [:post_app_store_version_release_request, { app_store_version_id: "version" }]
    ], @client.calls
  end
  def test_review_items_support_all_unrelated_content_relationships
    includes = "appStoreVersion,appEvent,appStoreVersionExperiment,appCustomProductPageVersion"
    API::ReviewSubmissionItem.all(client: @client, review_submission_id: "submission", includes: includes)
    assert_equal [:get_review_submission_items, { review_submission_id: "submission", includes: includes, limit: nil, sort: nil }], @client.calls.last
  end
end
