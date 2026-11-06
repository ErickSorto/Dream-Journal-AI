# Narrow, explicitly authorized store actions. Loading this file never authenticates.
require "json"
require "logger"
require "openssl"

module StoreRelease
  class Error < StandardError; end
  ATTESTATION = "managed-publishing-on-no-unrelated-changes".freeze

  def self.blank?(value)
    value.nil? || value.to_s.strip.empty?
  end

  # Store errors can contain review credentials. Never pass response bodies to CI logs.
  def self.safe_call(label, mutation: false)
    yield
  rescue Error
    raise
  rescue StandardError => error
    outcome = mutation ? " The outcome may be uncertain; inspect this exact target before retrying." : ""
    raise Error, "#{label} failed (#{error.class.name}).#{outcome}", cause: nil
  end

  def self.require_env(name)
    value = ENV[name]
    raise Error, "Missing environment variable #{name}." if blank?(value)
    value
  end

  def self.validate_gate!(platform, stage, version)
    expected = "#{platform}:#{stage}:#{version}"
    raise Error, "Store action requires explicit RELEASE_STORE_ACTION_CONFIRMED=#{expected}." unless ENV["RELEASE_STORE_ACTION_CONFIRMED"] == expected
  end

  def self.run(context, platform:, stage:, version:)
    validate_gate!(platform, stage, version)
    context.sh("python3", File.join(ReleasePipeline.root, "scripts", "release.py"), "prepare", version,
               "--platform", platform, "--stage", stage, "--reviewed")
    plan = JSON.parse(File.read(File.join(ReleasePipeline.output_dir(version), "store-plan.json")))
    unless plan["schema_version"] == 1 && plan["platform"] == platform && plan["stage"] == stage && plan["version"] == version && plan["reviewed"] == true
      raise Error, "Store plan does not match this reviewed action."
    end
    raise Error, "Store plan has unresolved requirements; inspect review.md." unless plan.fetch("missing").empty?
    if platform == "android" && stage == "publish"
      raise Error, "Google Play public release requires Play Console > Publishing overview > Publish changes. Verify the exact approved version and all pending changes there; no public-release API is invoked."
    end
    config = ReleasePipeline.config
    raise Error, "Store plan app mismatch." unless plan["app_name"] == config.fetch("app_name")
    executor = platform == "android" ? Android.new(plan, config, client: google_client, models: Google::Apis::AndroidpublisherV3) : Apple.new(plan, config, client: apple_client, api: Spaceship::ConnectAPI)
    result = executor.public_send({ "prepare" => :prepare!, "submit" => :submit!, "publish" => :publish! }.fetch(stage))
    FastlaneCore::UI.success(result)
  rescue Error => error
    FastlaneCore::UI.user_error!(error.message)
  end

  def self.google_client
    require "google/apis/androidpublisher_v3"
    require "googleauth"
    path = require_env("PLAY_SERVICE_ACCOUNT_JSON")
    raise Error, "Play service account JSON path is not a file." unless File.file?(path)
    Google::Apis.logger = Logger.new(File::NULL)
    client = Google::Apis::AndroidpublisherV3::AndroidPublisherService.new
    client.request_options.retries = 0
    client.client_options.log_http_requests = false
    File.open(path) do |file|
      client.authorization = Google::Auth::ServiceAccountCredentials.make_creds(json_key_io: file, scope: "https://www.googleapis.com/auth/androidpublisher")
    end
    client
  rescue Error
    raise
  rescue StandardError => error
    raise Error, "Google credential initialization failed (#{error.class.name}); credential content was not logged.", cause: nil
  end

  def self.apple_client
    debug_overrides = %w[SPACESHIP_DEBUG SPACESHIP_PROXY SPACESHIP_PROXY_SSL_VERIFY_NONE].reject { |key| blank?(ENV[key]) }
    raise Error, "Remove Apple debug/proxy overrides before a store action: #{debug_overrides.join(', ')}." unless debug_overrides.empty?
    require "spaceship"
    key_id = require_env("ASC_KEY_ID")
    path = require_env("ASC_KEY_PATH")
    raise Error, "App Store Connect key path is not a file." unless File.file?(path)
    # Construct directly so inherited Spaceship key environment cannot override the selected key.
    raw = File.binread(path)
    token = Spaceship::ConnectAPI::Token.new(key_id: key_id, issuer_id: ENV["ASC_ISSUER_ID"].to_s.empty? ? nil : ENV["ASC_ISSUER_ID"],
                                            key: OpenSSL::PKey::EC.new(raw), key_raw: raw, in_house: false)
    client = Spaceship::ConnectAPI::Client.new(token: token)
    harden_apple_client(client)
  rescue Error
    raise
  rescue StandardError => error
    raise Error, "Apple credential initialization failed (#{error.class.name}); credential content was not logged.", cause: nil
  end

  def self.harden_apple_client(client)
    %i[tunes_request_client test_flight_request_client provisioning_request_client users_request_client].each do |name|
      request_client = client.public_send(name)
      next unless request_client
      # Default Spaceship logs full reviewer detail payloads, including demo passwords.
      request_client.logger = Logger.new(File::NULL)
      # A transport retry of a mutation can duplicate an uncertain store action.
      request_client.define_singleton_method(:with_asc_retry) { |*_, &block| block.call }
      request_client.define_singleton_method(:with_retry) { |*_, &block| block.call }
      request_client.define_singleton_method(:log_request) { |*_| nil }
      request_client.define_singleton_method(:log_response) { |*_| nil }
    end
    client
  end

  # Pinned Supply omits changesInReviewBehavior and its default can cancel a live
  # review. Scope this override to the internal-upload lane and never fallback.
  module SafeInternalCommit
    def commit_current_edit!
      return super unless Thread.current[:release_internal_commit_guard]
      ensure_active_edit!
      StoreRelease.safe_call("Commit internal test edit #{current_edit.id}", mutation: true) do
        client.commit_edit(current_package_name, current_edit.id,
                           changes_in_review_behavior: "ERROR_IF_IN_REVIEW", changes_not_sent_for_review: true,
                           options: { retries: 0 })
      end
      self.current_edit = nil
      self.current_package_name = nil
    end
  end

  def self.with_internal_upload_guard
    require "supply" unless defined?(Supply::Client)
    Supply::Client.prepend(SafeInternalCommit) unless Supply::Client.ancestors.include?(SafeInternalCommit)
    previous = Thread.current[:release_internal_commit_guard]
    Thread.current[:release_internal_commit_guard] = true
    yield
  ensure
    Thread.current[:release_internal_commit_guard] = previous
  end

  class Android
    LISTING_FIELDS = %w[title short_description full_description video].freeze
    PUBLISHED = "RELEASE_LIFECYCLE_STATE_PUBLISHED".freeze
    def initialize(plan, config, client:, models:)
      @plan, @config, @client, @models = plan, config, client, models
      @package = config.fetch("android_package")
      @code = plan.fetch("android_version_code").to_s
    end

    def prepare!
      with_edit do |edit|
        populate_edit(edit, status: "draft")
        call("Validate Play preparation edit #{edit.id}") { @client.validate_edit(@package, edit.id) }
      end
      "Google Play preparation validated and its temporary edit discarded. No changes were committed; this is not a saved Console draft."
    end

    def submit!
      unless @plan.dig("defaults", "android", "managed_publishing_confirmed") == true && ENV["RELEASE_GOOGLE_MANAGED_PUBLISHING_ATTESTATION"] == ATTESTATION
        raise Error, "Submitting requires Managed Publishing enabled and fresh confirmation that there are no unrelated pending changes. The API cannot verify this setting."
      end
      with_edit do |edit|
        populate_edit(edit, status: "completed")
        call("Validate Play submission edit #{edit.id}") { @client.validate_edit(@package, edit.id) }
        # Mark before the call: a transport error could follow a successful commit.
        @commit_attempted = true
        call("Commit production review edit #{edit.id}", mutation: true) do
          @client.commit_edit(@package, edit.id, changes_in_review_behavior: "ERROR_IF_IN_REVIEW", options: { retries: 0 })
        end
        @committed = true
      end
      "Google Play production edit committed for review with Managed Publishing attested on. Check review status in Console; public release remains a separate Console action."
    end

    private

    def call(label, mutation: false, &block)
      StoreRelease.safe_call(label, mutation: mutation, &block)
    end

    def with_edit
      @committed = false
      @commit_attempted = false
      edit = call("Create temporary Play edit for #{@package}", mutation: true) { @client.insert_edit(@package) }
      primary = nil
      begin
        yield edit
      rescue StandardError => error
        primary = error
      ensure
        # Do not retry or delete an edit after an uncertain commit; inspect first.
        unless @committed || @commit_attempted
          begin
            call("Discard temporary Play edit #{edit.id}", mutation: true) { @client.delete_edit(@package, edit.id) }
          rescue StandardError => cleanup
            expiry = edit.respond_to?(:expiry_time_seconds) ? edit.expiry_time_seconds : "unknown"
            message = "Temporary edit #{edit.id} could not be confirmed discarded (expiry #{expiry}). No commit was attempted. #{cleanup.message}"
            primary = Error.new(primary ? "#{primary.message} #{message}" : message)
          end
        end
      end
      raise primary if primary
    end

    def codes(release)
      Array(release.version_codes).map(&:to_s)
    end

    def lifecycle(track)
      response = call("Read #{@package} #{track} release lifecycle") do
        @client.list_application_track_releases("applications/#{@package}/tracks/#{track}")
      end
      Array(response.releases)
    end

    def lifecycle_codes(release)
      Array(release.active_artifacts).map { |artifact| artifact.version_code.to_s }
    end

    def populate_edit(edit, status:)
      tracks = Array(call("Read Play edit tracks") { @client.list_edit_tracks(@package, edit.id) }.tracks)
      sources = tracks.select { |track| track.track == "internal" }
      raise Error, "Expected exactly one existing internal test track." unless sources.length == 1
      candidates = Array(sources.first.releases).select { |release| codes(release).include?(@code) }
      unless candidates.length == 1 && codes(candidates.first) == [@code] && candidates.first.status == "completed"
        raise Error, "Candidate #{@code} must be the exact single-version completed internal release."
      end
      source_states = lifecycle("internal").select { |release| lifecycle_codes(release).include?(@code) }
      unless source_states.length == 1 && lifecycle_codes(source_states.first) == [@code] && source_states.first.release_lifecycle_state == PUBLISHED
        raise Error, "Candidate #{@code} is not confirmed published to the internal test track."
      end
      destinations = tracks.select { |track| track.track == "production" }
      raise Error, "Ambiguous production track." if destinations.length > 1
      existing = Array(destinations.first&.releases)
      raise Error, "Candidate #{@code} already exists in production; no automatic resubmission or rollout is allowed." if existing.any? { |release| codes(release).include?(@code) }
      raise Error, "Production has an existing draft, staged, halted, or unknown release; resolve it in Console first." if existing.any? { |release| release.status != "completed" }
      production_states = lifecycle("production")
      if existing.empty? || production_states.none? { |release| release.release_lifecycle_state == PUBLISHED }
        raise Error, "Initial production publication must be completed in Play Console before this Managed Publishing workflow can be used."
      end
      raise Error, "Candidate #{@code} already has a production lifecycle entry." if production_states.any? { |release| lifecycle_codes(release).include?(@code) }
      # Lifecycle results omit obsolete releases; only known published entries are safe to preserve.
      if production_states.any? { |release| release.release_lifecycle_state != PUBLISHED }
        raise Error, "Production has an outstanding or unknown review lifecycle; resolve it in Console first."
      end
      notes = Array(candidates.first.release_notes).to_h { |note| [note.language, note.text] }
      notes.merge!(@plan.fetch("localized_notes").fetch("android", {}))
      release = @models::TrackRelease.new(name: @plan.fetch("version"), version_codes: [@code], status: status,
                                          release_notes: notes.map { |language, text| @models::LocalizedText.new(language: language, text: text) })
      @plan.fetch("metadata_updates").fetch("android", {}).each do |locale, fields|
        unknown = fields.keys - LISTING_FIELDS
        raise Error, "Unsupported Play listing fields: #{unknown.join(', ')}." unless unknown.empty?
        attributes = fields.reject { |_, value| StoreRelease.blank?(value) }.transform_keys(&:to_sym)
        next if attributes.empty?
        call("Patch selected Play listing fields for #{locale}", mutation: true) do
          @client.patch_edit_listing(@package, edit.id, locale, @models::Listing.new(**attributes))
        end
      end
      call("Stage exact production candidate #{@code} in temporary edit #{edit.id}", mutation: true) do
        @client.update_edit_track(@package, edit.id, "production", @models::Track.new(track: "production", releases: existing + [release]))
      end
    end
  end
end

require_relative "StoreReleaseApple"
