# Offline store contract tests: no authentication or network calls.
require "minitest/autorun"
require "ostruct"
require "stringio"
require "timeout"
require_relative "../../fastlane/StoreRelease"

module StoreModels
  TrackRelease = Struct.new(:name, :version_codes, :status, :release_notes, keyword_init: true)
  Track = Struct.new(:track, :releases, keyword_init: true)
  Listing = Struct.new(:title, :short_description, :full_description, :video, keyword_init: true)
  LocalizedText = Struct.new(:language, :text, keyword_init: true)
end

class FakePlay
  attr_reader :calls
  attr_accessor :tracks, :states, :fail_delete, :fail_validate, :fail_commit
  def initialize
    @calls = []
    @tracks = [StoreModels::Track.new(track: "internal", releases: [release("20")]), StoreModels::Track.new(track: "production", releases: [release("19")])]
    @states = { "internal" => [state("20")], "production" => [state("19")] }
  end
  def release(code, status = "completed")
    StoreModels::TrackRelease.new(version_codes: [code], status: status, release_notes: [StoreModels::LocalizedText.new(language: "fr-FR", text: "Previously reviewed French")])
  end
  def state(code, status = "RELEASE_LIFECYCLE_STATE_PUBLISHED")
    OpenStruct.new(active_artifacts: [OpenStruct.new(version_code: code)], release_lifecycle_state: status)
  end
  def insert_edit(package)
    @calls << [:insert, package]
    OpenStruct.new(id: "edit-123", expiry_time_seconds: "123456")
  end
  def list_edit_tracks(*args)
    @calls << [:tracks, args]
    OpenStruct.new(tracks: @tracks)
  end
  def list_application_track_releases(parent)
    @calls << [:lifecycle, parent]
    OpenStruct.new(releases: @states.fetch(parent.split("/").last))
  end
  def patch_edit_listing(*args)
    @calls << [:listing, args]
  end
  def update_edit_track(*args)
    @calls << [:track_update, args]
  end
  def validate_edit(*args)
    @calls << [:validate, args]
    raise "SECRET response body" if fail_validate
  end
  def delete_edit(*args)
    @calls << [:delete, args]
    raise "SECRET delete body" if fail_delete
  end
  def commit_edit(*args, **kwargs)
    @calls << [:commit, args, kwargs]
    raise "SECRET commit body" if fail_commit
  end
end

class StoreReleaseTest < Minitest::Test
  def setup
    @env = ENV.to_h
    @play = FakePlay.new
    @plan = { "version" => "1.2.3", "android_version_code" => 20,
              "defaults" => { "android" => { "managed_publishing_confirmed" => true } },
              "localized_notes" => { "android" => { "en-US" => "Reviewed notes" } },
              "metadata_updates" => { "android" => { "en-US" => { "short_description" => "Reviewed short description" } } } }
    @config = { "android_package" => "example.app" }
  end
  def teardown
    ENV.replace(@env)
  end
  def executor
    StoreRelease::Android.new(@plan, @config, client: @play, models: StoreModels)
  end
  def authorize
    ENV["RELEASE_GOOGLE_MANAGED_PUBLISHING_ATTESTATION"] = StoreRelease::ATTESTATION
  end
  def test_action_gate_exact_platform_stage_version
    ENV["RELEASE_STORE_ACTION_CONFIRMED"] = "ios:prepare:1.2.3"
    assert_raises(StoreRelease::Error) { StoreRelease.validate_gate!("ios", "submit", "1.2.3") }
    assert_raises(StoreRelease::Error) { StoreRelease.validate_gate!("android", "prepare", "1.2.3") }
    StoreRelease.validate_gate!("ios", "prepare", "1.2.3")
  end
  def test_prepare_validates_and_discards_without_commit
    assert_includes executor.prepare!, "No changes were committed"
    assert_equal [:validate, :delete], @play.calls.last(2).map(&:first)
    refute @play.calls.any? { |call| call.first == :commit }
    destination = @play.calls.find { |call| call.first == :track_update }[1].last
    assert_same @play.tracks.last.releases.first, destination.releases.first
    assert_equal "draft", destination.releases.last.status
    assert_equal ["20"], destination.releases.last.version_codes
    assert_equal({ "fr-FR" => "Previously reviewed French", "en-US" => "Reviewed notes" }, destination.releases.last.release_notes.to_h { |note| [note.language, note.text] })
    patch = @play.calls.find { |call| call.first == :listing }[1].last
    assert_equal "Reviewed short description", patch.short_description
    assert_nil patch.title
    assert_nil patch.full_description
  end
  def test_submit_requires_fresh_managed_publishing_attestation_before_any_api
    assert_raises(StoreRelease::Error) { executor.submit! }
    assert_empty @play.calls
    authorize
    @plan["defaults"]["android"]["managed_publishing_confirmed"] = false
    assert_raises(StoreRelease::Error) { executor.submit! }
    assert_empty @play.calls
  end
  def test_submit_commits_exactly_once_without_review_cancellation_or_fallback
    authorize
    executor.submit!
    commits = @play.calls.select { |call| call.first == :commit }
    assert_equal 1, commits.length
    assert_equal({ changes_in_review_behavior: "ERROR_IF_IN_REVIEW", options: { retries: 0 } }, commits.first[2])
    assert_equal "completed", @play.calls.find { |call| call.first == :track_update }[1].last.releases.last.status
    refute @play.calls.any? { |call| call.first == :delete }
  end
  def test_uncertain_commit_is_not_retried_or_deleted_and_error_is_redacted
    authorize
    @play.fail_commit = true
    error = assert_raises(StoreRelease::Error) { executor.submit! }
    assert_includes error.message, "edit-123"
    assert_includes error.message, "uncertain"
    refute_includes error.message, "SECRET"
    assert_equal 1, @play.calls.count { |call| call.first == :commit }
    refute @play.calls.any? { |call| call.first == :delete }
  end
  def test_failed_validation_discards_edit_and_redacts_body
    @play.fail_validate = true
    error = assert_raises(StoreRelease::Error) { executor.prepare! }
    refute_includes error.message, "SECRET"
    assert_equal :delete, @play.calls.last.first
    refute @play.calls.any? { |call| call.first == :commit }
  end
  def test_failed_cleanup_never_reports_discarded
    @play.fail_delete = true
    error = assert_raises(StoreRelease::Error) { executor.prepare! }
    assert_includes error.message, "could not be confirmed discarded"
    assert_includes error.message, "edit-123"
    assert_includes error.message, "123456"
  end
  def test_primary_failure_survives_cleanup_failure
    @play.fail_delete = @play.fail_validate = true
    error = assert_raises(StoreRelease::Error) { executor.prepare! }
    assert_includes error.message, "Validate Play preparation"
    assert_includes error.message, "could not be confirmed discarded"
    refute_includes error.message, "SECRET"
  end
  def test_candidate_must_be_single_published_internal_build
    @play.tracks.first.releases.first.version_codes << "18"
    assert_raises(StoreRelease::Error) { executor.prepare! }
    refute @play.calls.any? { |call| call.first == :track_update }
  end
  def test_pending_internal_release_cannot_be_promoted
    @play.states["internal"].first.release_lifecycle_state = "RELEASE_LIFECYCLE_STATE_IN_REVIEW"
    assert_raises(StoreRelease::Error) { executor.prepare! }
    assert_equal :delete, @play.calls.last.first
  end
  def test_live_or_pending_candidate_and_unrelated_rollout_are_blocked
    @play.states["production"] << @play.state("20")
    assert_raises(StoreRelease::Error) { executor.prepare! }
    @play.states["production"].pop
    @play.tracks.last.releases.first.status = "inProgress"
    assert_raises(StoreRelease::Error) { executor.prepare! }
  end
  def test_first_publication_requires_console
    @play.states["production"] = []
    @play.tracks.last.releases = []
    error = assert_raises(StoreRelease::Error) { executor.prepare! }
    assert_includes error.message, "Initial production publication"
  end
  def test_unrelated_pending_review_is_blocked
    @play.states["production"] << @play.state("18", "RELEASE_LIFECYCLE_STATE_APPROVED_NOT_PUBLISHED")
    assert_raises(StoreRelease::Error) { executor.prepare! }
    refute @play.calls.any? { |call| call.first == :listing }
  end
end

module Supply
  class Client
    attr_accessor :current_edit, :current_package_name, :client
    def ensure_active_edit!
      raise "missing edit" unless current_edit
    end
    def commit_current_edit!
      raise "unguarded default must not run"
    end
  end
end

class InternalCommitSafetyTest < Minitest::Test
  def test_scoped_commit_uses_fail_closed_review_behavior_once
    client = Supply::Client.new
    client.client = FakePlay.new
    client.current_edit = OpenStruct.new(id: "internal-edit")
    client.current_package_name = "example.app"
    StoreRelease.with_internal_upload_guard { client.commit_current_edit! }
    assert_equal [[:commit, ["example.app", "internal-edit"], { changes_in_review_behavior: "ERROR_IF_IN_REVIEW", changes_not_sent_for_review: true, options: { retries: 0 } }]], client.client.calls
    assert_nil client.current_edit
    assert_nil Thread.current[:release_internal_commit_guard]
  end

  def test_internal_commit_failure_has_no_fallback_and_retains_exact_edit
    client = Supply::Client.new
    client.client = FakePlay.new
    client.client.fail_commit = true
    client.current_edit = OpenStruct.new(id: "internal-edit")
    client.current_package_name = "example.app"
    error = assert_raises(StoreRelease::Error) do
      StoreRelease.with_internal_upload_guard { client.commit_current_edit! }
    end
    assert_equal 1, client.client.calls.length
    assert_equal "internal-edit", client.current_edit.id
    refute_includes error.message, "SECRET"
    assert_nil Thread.current[:release_internal_commit_guard]
  end

  def test_apple_transport_hardening_disables_both_retry_layers_and_payload_logs
    log = StringIO.new
    request_client = Object.new
    request_client.define_singleton_method(:logger=) { |logger| @logger = logger }
    request_client.define_singleton_method(:log_request) { |*args| log.write(args.inspect) }
    request_client.define_singleton_method(:log_response) { |*args| log.write(args.inspect) }
    client = Object.new
    %i[tunes_request_client test_flight_request_client provisioning_request_client users_request_client].each do |name|
      client.define_singleton_method(name) { request_client }
    end
    StoreRelease.harden_apple_client(client)
    attempts = 0
    assert_raises(Timeout::Error) do
      request_client.with_asc_retry do
        request_client.with_retry do
          attempts += 1
          request_client.log_request(:post, "/review", { demoAccountPassword: "SENTINEL_SECRET" })
          request_client.log_response(:post, "/review", "SENTINEL_SECRET")
          raise Timeout::Error
        end
      end
    end
    assert_equal 1, attempts
    assert_empty log.string
  end
end
