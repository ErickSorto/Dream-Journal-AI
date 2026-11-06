# Offline Apple orchestration tests. All models and clients below are in memory.
# These fixtures contain only invented review credentials; no account is accessed.
require "minitest/autorun"
require_relative "../../fastlane/StoreRelease"

module StoreAppleFakes
  class Record
    FIELDS = %i[id bundle_id app_id app_version version platform processing_state uses_non_exempt_encryption
                version_string app_version_state release_type earliest_release_date attached_build_id state locale
                description keywords support_url marketing_url promotional_text whats_new name subtitle privacy_policy_url
                contact_first_name contact_last_name contact_email contact_phone demo_account_required
                demo_account_name demo_account_password notes app_store_version app_event app_store_version_experiment
                app_store_product_page_version].freeze
    attr_accessor(*FIELDS)
    attr_reader :store, :kind

    def initialize(store, kind, **attributes)
      @store, @kind = store, kind
      attributes.each { |key, value| public_send("#{key}=", value) }
    end

    def update(client:, attributes:)
      check_client(client)
      store.write([:update, kind, id, attributes.dup]) do
        attributes.each { |key, value| public_send("#{key}=", value) }
        self
      end
    end

    def check_client(client)
      raise "Injected client was lost" unless client.equal?(store)
    end
  end

  class App < Record
    def get_app_store_versions(client:, filter:, includes:)
      check_client(client)
      raise "Wrong platform filter" unless filter == { platform: "IOS" } && includes.nil?
      store.versions
    end

    def get_review_submissions(client:, filter:, includes:)
      check_client(client)
      raise "Wrong submission filter" unless filter == { platform: "IOS" } && includes.nil?
      store.submissions
    end

    def create_review_submission(client:, platform:)
      check_client(client)
      store.write([:create_submission, id, platform]) do
        submission = Submission.new(store, :submission, id: "submission-new", platform: platform, state: "READY_FOR_REVIEW")
        store.submissions << submission
        submission
      end
    end
  end

  class Version < Record
    def get_build(client:)
      check_client(client)
      store.builds.find { |build| build.id == attached_build_id }
    end

    def select_build(client:, build_id:)
      check_client(client)
      store.write([:select_build, id, build_id]) { self.attached_build_id = build_id }
    end

    def fetch_app_store_review_detail(client:)
      check_client(client)
      store.reviews[id]
    end

    def create_app_store_review_detail(client:, attributes:)
      check_client(client)
      store.write([:create_review, id, attributes.dup]) do
        store.reviews[id] = Record.new(store, :review, id: "review-#{id}", **attributes)
      end
    end

    def create_app_store_version_localization(client:, attributes:)
      check_client(client)
      store.write([:create_version_locale, id, attributes.dup]) do
        store.version_localizations[id] << Record.new(store, :version_locale, id: "vl-#{id}-#{attributes.fetch(:locale)}", **store.snake_attributes(attributes))
      end
    end

    def create_app_store_version_release_request(client:)
      check_client(client)
      store.write([:release, id]) { :requested }
    end
  end

  class Info < Record
    def create_app_info_localization(client:, attributes:)
      check_client(client)
      store.write([:create_info_locale, id, attributes.dup]) do
        store.info_localizations[id] << Record.new(store, :info_locale, id: "il-#{id}-#{attributes.fetch(:locale)}", **store.snake_attributes(attributes))
      end
    end
  end

  class Submission < Record
    def add_app_store_version_to_review_items(client:, app_store_version_id:)
      check_client(client)
      store.write([:add_item, id, app_store_version_id]) do
        target = store.versions.find { |version| version.id == app_store_version_id }
        store.items[id] << Record.new(store, :item, id: "item-new", app_store_version: target)
        if store.becomes_ready
          target.app_version_state = "READY_FOR_REVIEW"
          store.infos.reject { |info| info.state == "READY_FOR_DISTRIBUTION" }.each { |info| info.state = "READY_FOR_REVIEW" }
        end
      end
    end

    def submit_for_review(client:)
      check_client(client)
      store.write([:submit, id]) { self.state = "WAITING_FOR_REVIEW" }
    end
  end

  class Response
    def initialize(models); @models = models; end
    # Exercise collection pagination even for small fixtures.
    def all_pages; @models.empty? ? [self] : @models.each_slice(1).map { |slice| self.class.new(slice) }; end
    def to_models; @models; end
  end

  class Client
    attr_accessor :apps, :builds, :versions, :infos, :submissions, :fail_event, :fail_after, :becomes_ready
    attr_reader :version_localizations, :info_localizations, :reviews, :items, :writes

    def initialize
      @apps, @builds, @versions, @infos, @submissions, @writes = [], [], [], [], [], []
      @version_localizations = Hash.new { |hash, key| hash[key] = [] }
      @info_localizations = Hash.new { |hash, key| hash[key] = [] }
      @items = Hash.new { |hash, key| hash[key] = [] }
      @reviews = {}
      @becomes_ready = true
    end

    def write(event)
      @writes << event
      failing = @fail_event && @fail_event.call(event)
      raise IOError, "PRIVATE_RESPONSE_SENTINEL" if failing && !@fail_after
      result = yield
      raise IOError, "PRIVATE_RESPONSE_SENTINEL" if failing && @fail_after
      result
    end

    def snake_attributes(attributes)
      attributes.to_h { |key, value| [key.to_s.gsub(/([A-Z])/) { "_#{Regexp.last_match(1).downcase}" }.to_sym, value] }
    end

    def get_app_infos(app_id:, includes:)
      raise "Wrong app" unless apps.any? { |app| app.id == app_id } && includes.nil?
      Response.new(infos)
    end

    def get_app_store_version_localizations(app_store_version_id:, includes:)
      raise "Unexpected include" unless includes.nil?
      Response.new(version_localizations[app_store_version_id])
    end

    def get_app_info_localizations(app_info_id:, includes:)
      raise "Unexpected include" unless includes.nil?
      Response.new(info_localizations[app_info_id])
    end

    def post_app_store_version(app_id:, attributes:)
      raise "Wrong app" unless apps.any? { |app| app.id == app_id }
      write([:create_version, app_id, attributes.dup]) do
        version = Version.new(self, :version, id: "version-new", version_string: attributes.fetch(:versionString),
                              platform: attributes.fetch(:platform), release_type: attributes.fetch(:releaseType), app_version_state: "PREPARE_FOR_SUBMISSION")
        versions << version
        version
      end
    end
  end

  module API
    class App
      def self.all(client:, filter:, includes:)
        raise "Wrong app query" unless filter.keys == [:bundleId] && includes.nil?
        client.apps
      end
    end
    class Build
      def self.all(client:, app_id:, version:, build_number:, platform:)
        raise "Missing exact build constraints" if [app_id, version, build_number, platform].any?(&:nil?)
        client.builds
      end
      def self.get(client:, build_id:); client.builds.find { |build| build.id == build_id }; end
    end
    class AppStoreVersion
      def self.get(client:, app_store_version_id:, includes:)
        raise "Unexpected include" unless includes.nil?
        client.versions.find { |version| version.id == app_store_version_id }
      end
    end
    class ReviewSubmission
      def self.get(client:, review_submission_id:, includes:)
        raise "Wrong review includes" unless includes == "items"
        client.submissions.find { |submission| submission.id == review_submission_id }
      end
    end
    class ReviewSubmissionItem
      def self.all(client:, review_submission_id:, includes:)
        raise "Missing item relationships" unless includes == "appStoreVersion,appEvent,appStoreVersionExperiment,appCustomProductPageVersion"
        client.items[review_submission_id]
      end
    end
  end
end

class StoreAppleTest < Minitest::Test
  F = StoreAppleFakes

  def setup
    @client = F::Client.new
    @config = { "ios_bundle_id" => "example.release.app" }
    @plan = { "version" => "2.0", "ios_build_number" => "20", "defaults" => { "ios" => { "review_details_mode" => "reuse", "uses_non_exempt_encryption" => nil } },
              "metadata_updates" => { "ios" => {} }, "localized_notes" => { "ios" => { "en-US" => "Reviewed release notes." } } }
    @app = F::App.new(@client, :app, id: "app-1", bundle_id: @config.fetch("ios_bundle_id"))
    @build = F::Record.new(@client, :build, id: "build-20", app_id: @app.id, bundle_id: @app.bundle_id, app_version: "2.0", version: "20",
                           platform: "IOS", processing_state: "VALID", uses_non_exempt_encryption: false)
    @version = F::Version.new(@client, :version, id: "version-2", version_string: "2.0", platform: "IOS", app_version_state: "PREPARE_FOR_SUBMISSION",
                              release_type: "MANUAL", attached_build_id: @build.id)
    @live_version = F::Version.new(@client, :version, id: "version-1", version_string: "1.0", platform: "IOS", app_version_state: "READY_FOR_DISTRIBUTION", release_type: "MANUAL")
    @info = F::Info.new(@client, :info, id: "info-next", state: "PREPARE_FOR_SUBMISSION")
    @live_info = F::Info.new(@client, :info, id: "info-live", state: "READY_FOR_DISTRIBUTION")
    @client.apps << @app
    @client.builds << @build
    @client.versions.concat([@live_version, @version])
    @client.infos.concat([@live_info, @info])
    @version_locale = version_locale(@version, "en-US")
    @info_locale = info_locale(@info, "en-US")
    @live_version_locale = version_locale(@live_version, "en-US", description: "Live description", keywords: "live,keywords", support_url: "https://example.test/live-support")
    @live_info_locale = info_locale(@live_info, "en-US", name: "Live name", privacy_policy_url: "https://example.test/live-privacy")
    @review = F::Record.new(@client, :review, id: "review-2", contact_first_name: "Test", contact_last_name: "Reviewer", contact_email: "review@example.test",
                            contact_phone: "+1 202 555 0100", demo_account_required: true, demo_account_name: "invented-demo-user", demo_account_password: "invented-demo-password", notes: "Existing reviewer notes")
    @client.reviews[@version.id] = @review
  end

  def version_locale(version, locale, **attributes)
    record = F::Record.new(@client, :version_locale, **{ id: "vl-#{version.id}-#{locale}", locale: locale, description: "Prepared description", keywords: "prepared,keywords",
                                                       support_url: "https://example.test/support", whats_new: "Reviewed release notes." }.merge(attributes))
    @client.version_localizations[version.id] << record
    record
  end

  def info_locale(info, locale, **attributes)
    record = F::Record.new(@client, :info_locale, **{ id: "il-#{info.id}-#{locale}", locale: locale, name: "Prepared name", privacy_policy_url: "https://example.test/privacy" }.merge(attributes))
    @client.info_localizations[info.id] << record
    record
  end

  def adapter; StoreRelease::Apple.new(@plan, @config, client: @client, api: F::API); end

  def ready_submission(version = @version)
    submission = F::Submission.new(@client, :submission, id: "submission-ready", state: "READY_FOR_REVIEW", platform: "IOS")
    @client.submissions << submission
    @client.items[submission.id] << F::Record.new(@client, :item, id: "item-ready", app_store_version: version)
    submission
  end

  def pending_release!
    @version.app_version_state = "PENDING_DEVELOPER_RELEASE"
    @info.state = "PENDING_RELEASE"
  end

  def assert_read_only_failure(action, pattern)
    error = assert_raises(StoreRelease::Error) { adapter.public_send(action) }
    assert_match pattern, error.message
    assert_empty @client.writes
    error
  end

  def test_prepare_reuses_only_missing_same_locale_required_fields
    @version_locale.keywords = nil
    @version_locale.support_url = " "
    @info_locale.privacy_policy_url = nil
    @version_locale.whats_new = "Old notes"
    adapter.prepare!
    assert_equal "Prepared description", @version_locale.description
    assert_equal "live,keywords", @version_locale.keywords
    assert_equal "https://example.test/live-support", @version_locale.support_url
    assert_equal "Prepared name", @info_locale.name
    assert_equal "https://example.test/live-privacy", @info_locale.privacy_policy_url
    assert_equal "Reviewed release notes.", @version_locale.whats_new
    assert_equal "Live description", @live_version_locale.description
    assert @client.writes.all? { |event| event[0] == :update && %i[version_locale info_locale].include?(event[1]) }
  end

  def test_new_locale_creation_uses_reviewed_same_locale_required_content
    @plan["localized_notes"]["ios"]["es-MX"] = "Notas revisadas."
    version_locale(@live_version, "es-MX", description: "Descripción", keywords: "familia,tareas", support_url: "https://example.test/es/ayuda")
    info_locale(@live_info, "es-MX", name: "Nombre", privacy_policy_url: "https://example.test/es/privacidad")
    adapter.prepare!
    info_create = @client.writes.find { |event| event[0] == :create_info_locale }
    version_create = @client.writes.find { |event| event[0] == :create_version_locale }
    assert_equal @info.id, info_create[1]
    assert_equal({ name: "Nombre", privacyPolicyUrl: "https://example.test/es/privacidad", locale: "es-MX" }, info_create[2])
    assert_equal "Notas revisadas.", version_create[2][:whatsNew]
    assert_equal "https://example.test/es/ayuda", version_create[2][:supportUrl]
    refute version_create[2].key?(:support_url)
  end

  def test_new_locale_never_inherits_different_language
    @plan["localized_notes"]["ios"]["fr-FR"] = "Notes validées."
    @version.release_type = "AFTER_APPROVAL"
    assert_read_only_failure(:prepare!, /Missing required description for fr-FR/)
  end

  def test_reuse_review_mode_preserves_omitted_contact_and_demo_credentials
    @plan["defaults"]["ios"]["review_notes"] = "Selected reviewer note"
    adapter.prepare!
    patch = @client.writes.find { |event| event[0, 2] == [:update, :review] }
    assert_equal({ notes: "Selected reviewer note" }, patch[3])
    assert_equal "invented-demo-user", @review.demo_account_name
    assert_equal "invented-demo-password", @review.demo_account_password
    assert_equal true, @review.demo_account_required
    assert_equal "review@example.test", @review.contact_email
  end

  def test_prepare_creates_only_exact_missing_version_with_manual_release
    @client.versions.delete(@version)
    @client.reviews.clear
    result = adapter.prepare!
    creation = @client.writes.find { |event| event[0] == :create_version }
    assert_equal [:create_version, @app.id, { versionString: "2.0", platform: "IOS", releaseType: "MANUAL" }], creation
    assert_includes result, "No review submission"
    refute @client.writes.any? { |event| %i[submit release].include?(event[0]) }
  end

  def test_prepare_refuses_first_publication_and_other_open_version
    @client.versions.delete(@live_version)
    assert_read_only_failure(:prepare!, /currently released iOS predecessor/)
    setup
    @version.version_string = "1.9"
    assert_read_only_failure(:prepare!, /Another iOS version is outstanding/)
  end

  def test_exact_uploaded_build_must_be_valid
    %w[PROCESSING FAILED INVALID].each do |processing|
      @build.processing_state = processing
      assert_read_only_failure(:prepare!, /not VALID/)
    end
  end

  def test_exact_uploaded_build_must_be_unique_and_match_all_identity_fields
    @client.builds << @build.dup
    assert_read_only_failure(:prepare!, /exactly one uploaded build/)
    @client.builds.pop
    %i[app_id bundle_id app_version version platform].each do |field|
      original = @build.public_send(field)
      @build.public_send("#{field}=", "wrong")
      assert_read_only_failure(:prepare!, /exactly one uploaded build/)
      @build.public_send("#{field}=", original)
    end
  end

  def test_submit_requires_manual_release_and_no_scheduled_date
    @version.release_type = "AFTER_APPROVAL"
    assert_read_only_failure(:submit!, /not configured for manual release/)
    @version.release_type = "MANUAL"
    @version.earliest_release_date = "2030-01-01T00:00:00Z"
    assert_read_only_failure(:submit!, /scheduled release date/)
  end

  def test_submit_rejects_changed_notes_and_selected_metadata
    @version_locale.whats_new = "Unreviewed edit"
    assert_read_only_failure(:submit!, /What's New.*differs/)
    @version_locale.whats_new = @plan["localized_notes"]["ios"]["en-US"]
    @plan["metadata_updates"]["ios"]["en-US"] = { "name" => "Reviewed replacement" }
    assert_read_only_failure(:submit!, /selected field en-US\/name differs/)
  end

  def test_submit_rejects_unrelated_review_items
    ready_submission(@live_version)
    assert_read_only_failure(:submit!, /unrelated or ambiguous items/)
    @client.items["submission-ready"].first.app_store_version = @version
    @client.items["submission-ready"].first.app_event = F::Record.new(@client, :event, id: "unrelated-event")
    assert_read_only_failure(:submit!, /unrelated content/)
  end

  def test_submit_reuses_exact_ready_draft_without_duplicate_items
    @version.app_version_state = "READY_FOR_REVIEW"
    @info.state = "READY_FOR_REVIEW"
    submission = ready_submission
    result = adapter.submit!
    assert_equal [[:submit, submission.id]], @client.writes
    assert_includes result, "separate public-release action"
    assert_equal "MANUAL", @version.release_type
  end

  def test_submit_creates_single_item_then_checks_readiness
    adapter.submit!
    assert_equal %i[create_submission add_item submit], @client.writes.map(&:first)
    assert_equal @version.id, @client.writes[1][2]
    refute @client.writes.any? { |event| event[0] == :release }
  end

  def test_submit_stops_before_submission_when_apple_has_not_transitioned
    @client.becomes_ready = false
    error = assert_raises(StoreRelease::Error) { adapter.submit! }
    assert_includes error.message, "review readiness"
    assert_includes error.message, "Completed before stopping"
    assert_equal %i[create_submission add_item], @client.writes.map(&:first)
  end

  def test_publish_reads_pending_app_info_not_live_app_info
    pending_release!
    @plan["metadata_updates"]["ios"]["en-US"] = { "name" => "Prepared name" }
    result = adapter.publish!
    assert_equal [[:release, @version.id]], @client.writes
    assert_includes result, "availability is not yet confirmed"
  end

  def test_publish_does_not_fallback_to_live_or_editable_info_for_selected_fields
    @version.app_version_state = "PENDING_DEVELOPER_RELEASE"
    @plan["metadata_updates"]["ios"]["en-US"] = { "name" => "Prepared name" }
    assert_read_only_failure(:publish!, /pending-release.*AppInfo/)
  end

  def test_publish_notes_only_can_read_the_single_live_app_info
    @version.app_version_state = "PENDING_DEVELOPER_RELEASE"
    @client.infos.delete(@info)
    result = adapter.publish!
    assert_equal [[:release, @version.id]], @client.writes
    assert_includes result, "Public release requested"
    assert_equal "Live name", @live_info_locale.name
  end

  def test_publish_notes_only_live_fallback_rejects_another_outstanding_info
    @version.app_version_state = "PENDING_DEVELOPER_RELEASE"
    assert_read_only_failure(:publish!, /pending-release.*AppInfo/)
  end

  def test_publish_requires_exact_approved_state_and_reviewed_content
    assert_read_only_failure(:publish!, /unsupported state for manual release/)
    pending_release!
    @version_locale.whats_new = "Changed after review"
    assert_read_only_failure(:publish!, /What's New.*differs/)
  end

  def test_unknown_write_outcome_is_not_retried_or_rolled_back
    @version.release_type = "AFTER_APPROVAL"
    @version.attached_build_id = nil
    @client.fail_event = ->(event) { event[0] == :select_build }
    @client.fail_after = true
    error = assert_raises(StoreRelease::Error) { adapter.prepare! }
    assert_equal %i[update select_build], @client.writes.map(&:first)
    assert_equal "MANUAL", @version.release_type
    assert_equal @build.id, @version.attached_build_id
    assert_includes error.message, "outcome may be uncertain"
    assert_includes error.message, "Completed before stopping: Set manual release"
    refute_includes error.message, "PRIVATE_RESPONSE_SENTINEL"
    assert_nil error.cause
  end

  def test_unknown_public_release_outcome_is_reported_without_retry
    pending_release!
    @client.fail_event = ->(event) { event[0] == :release }
    @client.fail_after = true
    error = assert_raises(StoreRelease::Error) { adapter.publish! }
    assert_equal [[:release, @version.id]], @client.writes
    assert_includes error.message, "outcome may be uncertain"
    refute_includes error.message, "PRIVATE_RESPONSE_SENTINEL"
  end

  def test_explicit_encryption_answer_is_never_inferred
    @build.uses_non_exempt_encryption = nil
    adapter.prepare!
    assert_nil @build.uses_non_exempt_encryption
    assert_empty @client.writes
    assert_read_only_failure(:submit!, /no export-compliance answer/)
    @plan["defaults"]["ios"]["uses_non_exempt_encryption"] = false
    adapter.prepare!
    assert_equal false, @build.uses_non_exempt_encryption
    assert_equal [[:update, :build, @build.id, { uses_non_exempt_encryption: false }]], @client.writes
  end
end
