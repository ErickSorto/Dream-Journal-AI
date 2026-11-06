# Contract tests only: every build, signing, and upload action is stubbed.
require "minitest/autorun"
require "tmpdir"
require "fileutils"
require "json"

module UI
  def self.user_error!(message)
    raise RuntimeError, message
  end
end

module FastlaneCore
  UI = ::UI
end

# The upload action is stubbed; the scoped commit patch must not load gems or
# authenticate merely to test lane arguments.
module Supply
  class Client; end
end

class FastfileHarness
  attr_reader :lanes, :calls
  attr_accessor :fail_archive, :manifest

  def initialize(path)
    @lanes = {}
    @calls = []
    instance_eval(File.read(path), path)
  end

  def platform(name, &block)
    @platform = name
    instance_exec(&block)
  ensure
    @platform = nil
  end

  def desc(_text); end

  def lane(name, &block)
    @lanes[[@platform, name]] = block
  end

  def run(platform, lane, version = "1.2.3")
    instance_exec({ version: version }, &@lanes.fetch([platform, lane]))
  end

  def sh(*args)
    @calls << [:sh, args]
    raise "Unexpected command" unless args[0] == "python3" && args[2] == "export" && args[4] == "--reviewed"
    output = ReleasePipeline.output_dir(args[3])
    FileUtils.mkdir_p(File.join(output, "metadata", "android"))
    File.write(File.join(output, "manifest.json"), JSON.pretty_generate(@manifest) + "\n")
    File.write(
      File.join(output, "testflight.json"),
      JSON.generate("localized_build_info" => { "en-US" => { "whats_new" => @manifest["notes"]["en-US"] } })
    )
  end

  def gradle(**options)
    @calls << [:gradle, options.merge(environment: ENV.to_h.select { |key, _| key.start_with?("RELEASE_") })]
    if options.fetch(:task).end_with?(":exportStoreReleaseBundle")
      File.write(ReleasePipeline.artifact_path("1.2.3", "android"), "AAB fixture")
    end
  end

  def upload_to_play_store(**options)
    @calls << [:play_upload, options.merge(promotion_env: ENV["SUPPLY_TRACK_PROMOTE_TO"])]
  end

  def cocoapods(**options)
    @calls << [:cocoapods, options]
  end

  def update_code_signing_settings(**options)
    @calls << [:signing, options]
    File.write(File.join(options.fetch(:path), "project.pbxproj"), "temporary signing")
  end

  def build_app(**options)
    @calls << [:archive, options]
    raise "simulated archive failure" if @fail_archive
    File.write(File.join(options.fetch(:output_directory), options.fetch(:output_name)), "IPA fixture")
  end

  def app_store_connect_api_key(**options)
    @calls << [:api_key, options]
    { "fixture" => true }
  end

  def upload_to_testflight(**options)
    @calls << [:testflight_upload, options]
  end
end

class ReleaseFastfileTest < Minitest::Test
  ENV_NAMES = %w[
    ANDROID_KEYSTORE_PATH ANDROID_KEYSTORE_STORE_PASSWORD ANDROID_KEYSTORE_KEY_ALIAS
    ANDROID_KEYSTORE_KEY_PASSWORD PLAY_SERVICE_ACCOUNT_JSON IOS_PROFILE_NAME IOS_TEAM_ID
    ASC_KEY_ID ASC_ISSUER_ID ASC_KEY_PATH RELEASE_UPLOAD_CONFIRMED RELEASE_VERSION_NAME
    RELEASE_VERSION_CODE RELEASE_SIGNING_REQUIRED SUPPLY_TRACK_PROMOTE_TO
  ].freeze

  def setup
    fastfile = File.expand_path("../../fastlane/Fastfile", __dir__)
    @harness = FastfileHarness.new(fastfile)
    @temporary = Dir.mktmpdir("release-fastlane-test")
    @original_root = ReleasePipeline.method(:root)
    temporary = @temporary
    ReleasePipeline.define_singleton_method(:root) { temporary }
    @env = ENV_NAMES.to_h { |name| [name, ENV[name]] }
    ENV_NAMES.each { |name| ENV.delete(name) }
    FileUtils.mkdir_p(File.join(@temporary, "release"))
    FileUtils.mkdir_p(File.join(@temporary, "iosApp", "iosApp.xcodeproj"))
    FileUtils.mkdir_p(File.join(@temporary, "composeApp"))
    @config = {
      "app_name" => "Fixture",
      "android_module" => ":composeApp",
      "android_package" => "example.fixture",
      "android_firebase_path" => "composeApp/google-services.json",
      "ios_bundle_id" => "example.fixture",
      "ios_workspace" => "iosApp/iosApp.xcworkspace",
      "ios_project" => "iosApp/iosApp.xcodeproj",
      "ios_scheme" => "StoreRelease",
      "ios_target" => "iosApp",
      "ios_team_id" => "EXAMPLE1234",
      "ios_firebase_path" => "iosApp/GoogleService-Info.plist"
    }
    File.write(File.join(@temporary, "release", "config.json"), JSON.generate(@config))
    File.write(File.join(@temporary, "iosApp", "iosApp.xcodeproj", "project.pbxproj"), "existing uncommitted work\n")
    %w[composeApp/google-services.json iosApp/GoogleService-Info.plist iosApp/Podfile.lock fake.jks fake.json fake.p8].each do |path|
      File.write(File.join(@temporary, path), "test fixture")
    end
    @harness.manifest = {
      "version" => "1.2.3", "android_version_code" => 123, "ios_build_number" => 45,
      "notes" => { "en-US" => "Reviewed release notes." }
    }
  end

  def teardown
    original_root = @original_root
    ReleasePipeline.define_singleton_method(:root) { original_root.call }
    @env.each { |name, value| value.nil? ? ENV.delete(name) : ENV[name] = value }
    FileUtils.remove_entry(@temporary)
  end

  def android_credentials
    ENV["ANDROID_KEYSTORE_PATH"] = File.join(@temporary, "fake.jks")
    %w[ANDROID_KEYSTORE_STORE_PASSWORD ANDROID_KEYSTORE_KEY_ALIAS ANDROID_KEYSTORE_KEY_PASSWORD].each do |name|
      ENV[name] = "fixture"
    end
  end

  def ios_credentials
    ENV["IOS_PROFILE_NAME"] = "Fixture App Store"
    ENV["IOS_TEAM_ID"] = "EXAMPLE1234"
  end

  def upload_credentials
    ENV["RELEASE_UPLOAD_CONFIRMED"] = "internal"
    ENV["PLAY_SERVICE_ACCOUNT_JSON"] = File.join(@temporary, "fake.json")
    ENV["ASC_KEY_ID"] = "fixture"
    ENV["ASC_ISSUER_ID"] = "fixture"
    ENV["ASC_KEY_PATH"] = File.join(@temporary, "fake.p8")
  end

  def call_options(name)
    @harness.calls.reverse.find { |call| call[0] == name }.fetch(1)
  end

  def test_build_test_and_separately_gated_store_lanes_exist
    expected = [:android, :ios].flat_map do |platform|
      [:build_test, :upload_test, :prepare_store, :submit_store, :release_store].map { |lane| [platform, lane] }
    end
    assert_equal expected.sort, @harness.lanes.keys.sort
  end

  def test_upload_confirmation_fails_before_any_action
    [:android, :ios].each do |platform|
      assert_raises(RuntimeError) { @harness.run(platform, :upload_test) }
    end
    assert_empty @harness.calls
  end

  def test_store_actions_require_separate_confirmation_before_any_action
    ENV.delete("RELEASE_STORE_ACTION_CONFIRMED")
    [:android, :ios].each do |platform|
      [:prepare_store, :submit_store, :release_store].each do |lane|
        assert_raises(RuntimeError) { @harness.run(platform, lane) }
      end
    end
    assert_empty @harness.calls
  end

  def test_invalid_version_is_rejected_before_export
    assert_raises(RuntimeError) { @harness.run(:android, :build_test, "../../secrets") }
    assert_empty @harness.calls
  end

  def test_missing_signing_material_fails_before_gradle
    assert_raises(RuntimeError) { @harness.run(:android, :build_test) }
    refute @harness.calls.any? { |name, _| name == :gradle }
  end

  def test_android_uses_final_bundle_task_and_restores_environment
    android_credentials
    @harness.run(:android, :build_test)
    options = call_options(:gradle)
    assert_equal ":composeApp:exportStoreReleaseBundle", options[:task]
    assert_equal "1.2.3", options[:environment]["RELEASE_VERSION_NAME"]
    assert_equal "123", options[:environment]["RELEASE_VERSION_CODE"]
    assert_equal "true", options[:environment]["RELEASE_SIGNING_REQUIRED"]
    assert_nil ENV["RELEASE_VERSION_NAME"]
    assert File.file?(ReleasePipeline.receipt_path("1.2.3", "android"))
  end

  def test_play_is_internal_without_store_review_or_listing_changes
    android_credentials
    upload_credentials
    @harness.run(:android, :build_test)
    ENV["SUPPLY_TRACK_PROMOTE_TO"] = "production"
    @harness.run(:android, :upload_test)
    options = call_options(:play_upload)
    assert_equal "internal", options[:track]
    assert_nil options[:promotion_env]
    assert_equal "production", ENV["SUPPLY_TRACK_PROMOTE_TO"]
    assert_equal true, options[:changes_not_sent_for_review]
    assert_equal false, options[:rescue_changes_not_sent_for_review]
    assert_equal true, options[:skip_upload_metadata]
    assert_equal true, options[:skip_upload_images]
    assert_equal true, options[:skip_upload_screenshots]
    assert_equal false, options[:skip_upload_changelogs]
  end

  def test_modified_notes_reject_stale_binary
    android_credentials
    upload_credentials
    @harness.run(:android, :build_test)
    @harness.manifest["notes"]["en-US"] = "Different notes."
    assert_raises(RuntimeError) { @harness.run(:android, :upload_test) }
    refute @harness.calls.any? { |name, _| name == :play_upload }
  end

  def test_modified_binary_rejected
    android_credentials
    upload_credentials
    @harness.run(:android, :build_test)
    File.write(ReleasePipeline.artifact_path("1.2.3", "android"), "replaced")
    assert_raises(RuntimeError) { @harness.run(:android, :upload_test) }
    refute @harness.calls.any? { |name, _| name == :play_upload }
  end

  def test_modified_release_configuration_rejects_prior_binary
    android_credentials
    upload_credentials
    @harness.run(:android, :build_test)
    @config["locales"] = { "es" => { "android" => "es-US", "ios" => "es-MX" } }
    File.write(File.join(@temporary, "release", "config.json"), JSON.generate(@config))
    assert_raises(RuntimeError) { @harness.run(:android, :upload_test) }
    refute @harness.calls.any? { |name, _| name == :play_upload }
  end

  def test_android_retains_mapping_from_nested_module
    android_credentials
    @config["android_module"] = ":composeApp:shared"
    File.write(File.join(@temporary, "release", "config.json"), JSON.generate(@config))
    mapping_dir = File.join(@temporary, "composeApp", "shared", "build", "outputs", "mapping", "release")
    FileUtils.mkdir_p(mapping_dir)
    File.write(File.join(mapping_dir, "mapping.txt"), "R8 mapping fixture\n")
    @harness.run(:android, :build_test)
    assert_equal ":composeApp:shared:exportStoreReleaseBundle", call_options(:gradle)[:task]
    assert_equal "R8 mapping fixture\n",
      File.read(File.join(ReleasePipeline.output_dir("1.2.3"), "mapping.txt"))
  end

  def test_android_removes_stale_exported_mapping_when_no_mapping_was_produced
    android_credentials
    FileUtils.mkdir_p(ReleasePipeline.output_dir("1.2.3"))
    mapping = File.join(ReleasePipeline.output_dir("1.2.3"), "mapping.txt")
    File.write(mapping, "stale mapping")
    @harness.run(:android, :build_test)
    refute File.exist?(mapping)
  end

  def test_ios_preserves_dirty_project_and_scopes_signing
    ios_credentials
    @harness.run(:ios, :build_test)
    assert_equal "existing uncommitted work\n",
      File.read(File.join(@temporary, "iosApp", "iosApp.xcodeproj", "project.pbxproj"))
    assert_equal ["iosApp"], call_options(:signing)[:targets]
    assert_equal ["Release"], call_options(:signing)[:build_configurations]
    assert_equal false, call_options(:signing)[:use_automatic_signing]
    assert_equal true, call_options(:cocoapods)[:deployment]
    assert_equal false, call_options(:cocoapods)[:repo_update]
    assert_equal "app-store", call_options(:archive)[:export_method]
    assert_equal "StoreRelease", call_options(:archive)[:scheme]
    refute call_options(:archive)[:xcargs].include?("PROVISIONING_PROFILE")
    refute call_options(:archive)[:xcargs].include?("allowProvisioningUpdates")
    assert_equal false, call_options(:archive)[:export_options][:manageAppVersionAndBuildNumber]
  end

  def test_ios_failure_restores_project_and_leaves_no_build_receipt
    ios_credentials
    @harness.fail_archive = true
    assert_raises(RuntimeError) { @harness.run(:ios, :build_test) }
    assert_equal "existing uncommitted work\n",
      File.read(File.join(@temporary, "iosApp", "iosApp.xcodeproj", "project.pbxproj"))
    refute File.exist?(ReleasePipeline.receipt_path("1.2.3", "ios"))
  end

  def test_testflight_never_submits_review_or_external_distribution
    ios_credentials
    upload_credentials
    @harness.run(:ios, :build_test)
    @harness.run(:ios, :upload_test)
    options = call_options(:testflight_upload)
    assert_equal true, options[:skip_submission]
    assert_equal false, options[:submit_beta_review]
    assert_equal false, options[:distribute_external]
    assert_equal false, options[:notify_external_testers]
    assert_equal [], options[:groups]
    assert_equal false, options[:skip_waiting_for_build_processing]
    assert_equal "Reviewed release notes.", options[:localized_build_info]["en-US"][:whats_new]
    refute options[:localized_build_info]["en-US"].key?("whats_new")
  end
end
