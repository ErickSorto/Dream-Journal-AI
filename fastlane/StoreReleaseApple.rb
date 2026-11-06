# Focused App Store Connect adapter. All clients are injected; loading is inert.
module StoreRelease
  class Apple
    PLATFORM = "IOS".freeze
    WRITE_STATES = %w[PREPARE_FOR_SUBMISSION DEVELOPER_REJECTED REJECTED METADATA_REJECTED INVALID_BINARY].freeze
    SUBMIT_STATES = (WRITE_STATES + ["READY_FOR_REVIEW"]).freeze
    INFO_WRITE_STATES = %w[PREPARE_FOR_SUBMISSION DEVELOPER_REJECTED REJECTED READY_FOR_REVIEW].freeze
    INFO_PENDING_STATES = %w[PENDING_RELEASE ACCEPTED].freeze
    VERSION_FIELDS = {
      "description" => :description, "keywords" => :keywords, "support_url" => :support_url,
      "marketing_url" => :marketing_url, "promotional_text" => :promotional_text
    }.freeze
    INFO_FIELDS = { "name" => :name, "subtitle" => :subtitle, "privacy_url" => :privacy_policy_url }.freeze
    CONTACT_FIELDS = { "first_name" => :contact_first_name, "last_name" => :contact_last_name,
                       "email" => :contact_email, "phone" => :contact_phone }.freeze
    REQUIRED_VERSION_FIELDS = %i[description keywords support_url].freeze
    REQUIRED_INFO_FIELDS = %i[name privacy_policy_url].freeze

    def initialize(plan, config, client:, api:)
      @plan, @config, @client, @api = plan, config, client, api
      @version_name = plan.fetch("version")
      @build_number = plan.fetch("ios_build_number").to_s
      @bundle = config.fetch("ios_bundle_id")
      @defaults = plan.fetch("defaults").fetch("ios")
      @updates = plan.fetch("metadata_updates").fetch("ios", {})
      @notes = plan.fetch("localized_notes").fetch("ios", {})
      @completed = []
    end

    def prepare!
      perform do
        resolve_app_and_build!
        versions = versions_for_app
        live_versions = versions.select { |version| state(version) == "READY_FOR_DISTRIBUTION" && version.version_string != @version_name }
        unless live_versions.length == 1
          raise Error, "Routine preparation requires one currently released iOS predecessor; first publication or ambiguous release state requires Console setup."
        end
        @version = exact_version(versions)
        guard_review_submissions!
        if @version.nil?
          open_versions = versions.reject { |version| %w[READY_FOR_DISTRIBUTION REPLACED_WITH_NEW_VERSION].include?(state(version)) }
          raise Error, "Another iOS version is outstanding; resolve it before creating this exact version." unless open_versions.empty?
          mutate("Create exact iOS version #{@version_name}") do
            @client.post_app_store_version(app_id: @app.id, attributes: { versionString: @version_name, platform: PLATFORM, releaseType: "MANUAL" })
          end
          @version = exact_version(versions_for_app)
          raise Error, "Created iOS version could not be verified; inspect App Store Connect before retrying." unless @version
        end
        require_state!(@version, WRITE_STATES, "preparation")
        reject_scheduled_date!
        @info = target_info(publish: false)
        # Compute every locale's required content before any localization write.
        locale_jobs = plan_localizations
        review = review_detail
        review_patch = desired_review_patch
        if review.nil? && review_patch.empty?
          # Reuse mode deliberately leaves a missing detail untouched for submission to report.
        elsif review.nil?
          validate_review_values!(review_patch)
        end
        mutate("Set manual release on iOS version #{@version_name}") do
          @version.update(client: @client, attributes: { release_type: "MANUAL" })
        end unless @version.release_type == "MANUAL"
        attached = call("Read selected iOS build") { @version.get_build(client: @client) }
        unless attached && attached.id == @build.id
          mutate("Attach exact iOS build #{@build_number}") { @version.select_build(client: @client, build_id: @build.id) }
        end
        apply_localizations(locale_jobs)
        apply_review_patch(review, review_patch) unless review_patch.empty?
        apply_explicit_encryption
        refresh_version!
        verify_prepared!(publish: false, require_review: false)
        "App Store Connect version #{@version_name} prepared with build #{@build_number} and manual release. No review submission or public release was requested."
      end
    end

    def submit!
      perform do
        resolve_app_and_build!
        @version = exact_version(versions_for_app)
        raise Error, "Exact iOS version is missing; run preparation first." unless @version
        require_state!(@version, SUBMIT_STATES, "submission")
        verify_prepared!(publish: false, require_review: true)
        submission = guard_review_submissions!
        if submission.nil?
          submission = mutate("Create iOS review submission") { @app.create_review_submission(client: @client, platform: PLATFORM) }
        end
        items = submission_items(submission)
        if items.empty?
          mutate("Add exact iOS version to review submission #{submission.id}") do
            submission.add_app_store_version_to_review_items(client: @client, app_store_version_id: @version.id)
          end
        else
          verify_submission_items!(items)
        end
        # Apple may transition asynchronously. Stop without submitting if not ready;
        # a later explicit invocation safely reuses this exact single-item draft.
        refresh_version!
        require_state!(@version, ["READY_FOR_REVIEW"], "review readiness; retry only after Apple reports Ready for Review")
        verify_prepared!(publish: false, require_review: true)
        submission = call("Refresh exact iOS review submission") do
          @api::ReviewSubmission.get(client: @client, review_submission_id: submission.id, includes: "items")
        end
        raise Error, "Exact review submission is not Ready for Review." unless submission && submission.state == "READY_FOR_REVIEW" && submission.platform == PLATFORM
        verify_submission_items!(submission_items(submission))
        mutate("Submit exact iOS version #{@version_name} for review") { submission.submit_for_review(client: @client) }
        "App Store Connect review submission requested for #{@version_name} (#{@build_number}) with manual release. Approval will require a separate public-release action."
      end
    end

    def publish!
      perform do
        resolve_app_and_build!
        @version = exact_version(versions_for_app)
        raise Error, "Exact approved iOS version is missing." unless @version
        require_state!(@version, ["PENDING_DEVELOPER_RELEASE"], "manual release")
        verify_prepared!(publish: true, require_review: true)
        mutate("Request public release of exact iOS version #{@version_name}") do
          @version.create_app_store_version_release_request(client: @client)
        end
        "Public release requested for iOS #{@version_name} (#{@build_number}). Storefront availability is not yet confirmed."
      end
    end

    private

    def perform
      yield
    rescue Error => error
      detail = @completed.empty? ? "" : " Completed before stopping: #{@completed.join('; ')}. Apple writes are not transactional; inspect these targets before retrying."
      raise Error, error.message + detail, cause: nil
    rescue StandardError => error
      detail = @completed.empty? ? "" : " Some Apple writes completed; inspect the exact version before retrying."
      raise Error, "Apple operation stopped (#{error.class.name}); response content was not logged.#{detail}", cause: nil
    end

    def call(label, &block)
      StoreRelease.safe_call(label, &block)
    end

    def mutate(label, &block)
      result = StoreRelease.safe_call(label, mutation: true, &block)
      @completed << label
      result
    end

    def all_models(response)
      response.all_pages.flat_map(&:to_models)
    end

    def state(version)
      version.app_version_state
    end

    def require_state!(version, allowed, purpose)
      raise Error, "Exact iOS version has an unsupported state for #{purpose}." unless allowed.include?(state(version))
    end

    def resolve_app_and_build!
      apps = call("Resolve exact App Store Connect app") do
        @api::App.all(client: @client, filter: { bundleId: @bundle }, includes: nil)
      end.select { |app| app.bundle_id == @bundle }
      raise Error, "Expected exactly one existing App Store Connect app for this bundle ID." unless apps.length == 1
      @app = apps.first
      builds = call("Resolve exact uploaded iOS build") do
        @api::Build.all(client: @client, app_id: @app.id, version: @version_name, build_number: @build_number, platform: PLATFORM)
      end.select do |build|
        build.app_id == @app.id && build.bundle_id == @bundle && build.app_version == @version_name && build.version.to_s == @build_number && build.platform == PLATFORM
      end
      raise Error, "Expected exactly one uploaded build matching app, platform, version and build number." unless builds.length == 1
      @build = builds.first
      raise Error, "Exact uploaded iOS build is not VALID." unless @build.processing_state == "VALID"
    end

    def versions_for_app
      call("Read iOS versions") { @app.get_app_store_versions(client: @client, filter: { platform: PLATFORM }, includes: nil) }
        .select { |version| version.platform == PLATFORM }
    end

    def exact_version(versions)
      matches = versions.select { |version| version.version_string == @version_name }
      raise Error, "More than one exact iOS version matched." if matches.length > 1
      matches.first
    end

    def refresh_version!
      fresh = call("Refresh exact iOS version") { @api::AppStoreVersion.get(client: @client, app_store_version_id: @version.id, includes: nil) }
      unless fresh && fresh.id == @version.id && fresh.version_string == @version_name && fresh.platform == PLATFORM
        raise Error, "Exact iOS version identity changed."
      end
      @version = fresh
    end

    def app_infos
      call("Read all AppInfo records") { all_models(@client.get_app_infos(app_id: @app.id, includes: nil)) }
    end

    def target_info(publish:)
      allowed = publish ? INFO_PENDING_STATES : INFO_WRITE_STATES
      infos = app_infos
      matches = infos.select { |info| allowed.include?(info.state) }
      # An unchanged AppInfo may still be the sole current live record. This
      # read-only exception is restricted to plans selecting no AppInfo fields;
      # it never supplies a target for a metadata write or selected-field check.
      info_fields_selected = @updates.values.any? { |fields| (fields.keys & INFO_FIELDS.keys).any? }
      if publish && matches.empty? && !info_fields_selected
        current = infos.reject { |info| info.state == "REPLACED_WITH_NEW_INFO" }
        return current.first if current.length == 1 && current.first.state == "READY_FOR_DISTRIBUTION"
      end
      raise Error, "Expected one #{publish ? 'pending-release' : 'editable'} AppInfo; no live/latest fallback is allowed." unless matches.length == 1
      matches.first
    end

    def version_locales(version = @version)
      models = call("Read all version localizations") do
        all_models(@client.get_app_store_version_localizations(app_store_version_id: version.id, includes: nil))
      end
      unique_locales(models)
    end

    def info_locales(info = @info)
      models = call("Read all AppInfo localizations") do
        all_models(@client.get_app_info_localizations(app_info_id: info.id, includes: nil))
      end
      unique_locales(models)
    end

    def unique_locales(models)
      raise Error, "Duplicate remote localization records; resolve in App Store Connect." unless models.map(&:locale).uniq.length == models.length
      models.to_h { |model| [model.locale, model] }
    end

    def fields_for(locale)
      fields = @updates.fetch(locale, {})
      raise Error, "Unsupported selected iOS metadata field." unless (fields.keys - VERSION_FIELDS.keys - INFO_FIELDS.keys).empty?
      raise Error, "Blank selected iOS metadata would clear a field." if fields.values.any? { |value| StoreRelease.blank?(value) }
      fields
    end

    def plan_localizations
      current_version = version_locales
      current_info = info_locales
      live_versions = versions_for_app.select { |version| state(version) == "READY_FOR_DISTRIBUTION" && version.id != @version.id }
      raise Error, "Ambiguous live iOS version for missing-field reuse." if live_versions.length > 1
      live_version_locales = live_versions.empty? ? {} : version_locales(live_versions.first)
      live_infos = app_infos.select { |info| info.state == "READY_FOR_DISTRIBUTION" }
      raise Error, "Ambiguous live AppInfo for missing-field reuse." if live_infos.length > 1
      live_info_locales = live_infos.empty? ? {} : info_locales(live_infos.first)
      (@notes.keys | @updates.keys).map do |locale|
        selected = fields_for(locale)
        version_patch = selected.filter_map { |key, value| [VERSION_FIELDS[key], value] if VERSION_FIELDS.key?(key) }.to_h
        info_patch = selected.filter_map { |key, value| [INFO_FIELDS[key], value] if INFO_FIELDS.key?(key) }.to_h
        version_patch[:whats_new] = @notes.fetch(locale) if @notes.key?(locale)
        fill_missing!(version_patch, current_version[locale], live_version_locales[locale], REQUIRED_VERSION_FIELDS, locale)
        # Reuse an existing same-locale optional URL/text only when the prepared
        # version has none. Explicit reviewed selections always take precedence.
        (VERSION_FIELDS.values - REQUIRED_VERSION_FIELDS).each do |field|
          next if version_patch.key?(field) || (current_version[locale] && !StoreRelease.blank?(current_version[locale].public_send(field)))
          previous = live_version_locales[locale]&.public_send(field)
          version_patch[field] = previous unless StoreRelease.blank?(previous)
        end
        fill_missing!(info_patch, current_info[locale], live_info_locales[locale], REQUIRED_INFO_FIELDS, locale)
        { locale: locale, version_patch: version_patch, info_patch: info_patch }
      end
    end

    def fill_missing!(patch, target, source, required, locale)
      required.each do |field|
        next if patch.key?(field) || (target && !StoreRelease.blank?(target.public_send(field)))
        value = source&.public_send(field)
        raise Error, "Missing required #{field} for #{locale}; select reviewed localized metadata before preparing." if StoreRelease.blank?(value)
        patch[field] = value
      end
    end

    def api_attributes(patch)
      patch.to_h { |key, value| [key.to_s.gsub(/_([a-z])/) { Regexp.last_match(1).upcase }.to_sym, value] }
    end

    def apply_localizations(jobs)
      jobs.each do |job|
        locale = job.fetch(:locale)
        info = info_locales[locale]
        info_patch = job.fetch(:info_patch)
        if info
          changed = info_patch.reject { |key, value| info.public_send(key) == value }
          mutate("Patch selected AppInfo fields for #{locale}") { info.update(client: @client, attributes: changed) } unless changed.empty?
        else
          mutate("Create reviewed AppInfo locale #{locale}") do
            @info.create_app_info_localization(client: @client, attributes: api_attributes(info_patch).merge(locale: locale))
          end
        end
        # Apple can propagate creation; re-read both collections before another create.
        raise Error, "AppInfo locale creation could not be verified." unless info_locales.key?(locale)
        version_locale = version_locales[locale]
        patch = job.fetch(:version_patch)
        if version_locale
          changed = patch.reject { |key, value| version_locale.public_send(key) == value }
          mutate("Patch selected version fields for #{locale}") { version_locale.update(client: @client, attributes: changed) } unless changed.empty?
        else
          mutate("Create reviewed version locale #{locale}") do
            @version.create_app_store_version_localization(client: @client, attributes: api_attributes(patch).merge(locale: locale))
          end
        end
      end
    end

    def review_detail
      call("Read exact version App Review details") { @version.fetch_app_store_review_detail(client: @client) }
    end

    def desired_review_patch
      mode = @defaults.fetch("review_details_mode")
      raise Error, "Unsupported review details mode." unless %w[reuse replace].include?(mode)
      patch = {}
      if mode == "replace"
        CONTACT_FIELDS.each do |key, attribute|
          value = @defaults.fetch("review_contact").fetch(key)
          raise Error, "Missing replacement App Review contact field #{key}." if StoreRelease.blank?(value)
          patch[attribute] = value
        end
        required = @defaults["demo_account_required"]
        raise Error, "Replacement reviewer login requirement must be explicit." unless [true, false].include?(required)
        patch[:demo_account_required] = required
        if required
          %w[demo_user_env demo_password_env].zip(%i[demo_account_name demo_account_password]).each do |key, attribute|
            env_name = @defaults[key]
            raise Error, "Invalid review credential environment variable name." unless env_name.is_a?(String) && env_name.match?(/\A[A-Z][A-Z0-9_]*\z/)
            patch[attribute] = StoreRelease.require_env(env_name)
          end
        end
      end
      patch[:notes] = @defaults["review_notes"] unless @defaults["review_notes"].nil?
      patch
    end

    def apply_review_patch(review, patch)
      if review
        changed = patch.reject { |key, value| review.public_send(key) == value }
        mutate("Patch explicitly selected App Review details") { review.update(client: @client, attributes: changed) } unless changed.empty?
      else
        mutate("Create explicitly supplied App Review details") { @version.create_app_store_review_detail(client: @client, attributes: patch) }
      end
    end

    def validate_review_values!(values)
      CONTACT_FIELDS.values.each { |field| raise Error, "App Review requires #{field}; configure the exact target version." if StoreRelease.blank?(values[field]) }
      raise Error, "App Review email is invalid." unless values[:contact_email].to_s.match?(/\A[^\s@]+@[^\s@]+\.[^\s@]+\z/)
      raise Error, "App Review phone must include an international country code." unless values[:contact_phone].to_s.match?(/\A\+[1-9][0-9 ()-]+\z/)
      unless [true, false].include?(values[:demo_account_required])
        raise Error, "App Review demo-account requirement is unresolved."
      end
      if values[:demo_account_required]
        %i[demo_account_name demo_account_password].each { |field| raise Error, "Required App Review login is missing; configure it without logging credentials." if StoreRelease.blank?(values[field]) }
      end
    end

    def apply_explicit_encryption
      explicit = @defaults["uses_non_exempt_encryption"]
      return if explicit.nil?
      raise Error, "Export-compliance answer must be a boolean." unless [true, false].include?(explicit)
      existing = @build.uses_non_exempt_encryption
      raise Error, "Reviewed encryption answer conflicts with the selected build; resolve in App Store Connect." unless existing.nil? || existing == explicit
      if existing.nil?
        mutate("Set explicitly reviewed encryption answer on exact build") { @build.update(client: @client, attributes: { uses_non_exempt_encryption: explicit }) }
        @build = call("Refresh exact iOS build") { @api::Build.get(client: @client, build_id: @build.id) }
      end
    end

    def verify_prepared!(publish:, require_review:)
      raise Error, "Exact iOS version is not configured for manual release." unless @version.release_type == "MANUAL"
      reject_scheduled_date!
      attached = call("Verify exact attached iOS build") { @version.get_build(client: @client) }
      raise Error, "Attached build differs from reviewed candidate; run preparation." unless attached && attached.id == @build.id
      @info = target_info(publish: publish)
      versions = version_locales
      infos = info_locales
      (@notes.keys | @updates.keys).each do |locale|
        raise Error, "Reviewed locale #{locale} is missing remotely." unless versions[locale] && infos[locale]
        fields_for(locale).each do |key, expected|
          model, field = VERSION_FIELDS.key?(key) ? [versions[locale], VERSION_FIELDS.fetch(key)] : [infos[locale], INFO_FIELDS.fetch(key)]
          raise Error, "Remote selected field #{locale}/#{key} differs from the reviewed plan; prepare and verify again." unless model.public_send(field) == expected
        end
        if @notes.key?(locale) && versions[locale].whats_new != @notes[locale]
          raise Error, "Remote What's New for #{locale} differs from the reviewed notes."
        end
        REQUIRED_VERSION_FIELDS.each { |field| raise Error, "Remote required #{field} is blank for #{locale}." if StoreRelease.blank?(versions[locale].public_send(field)) }
        REQUIRED_INFO_FIELDS.each { |field| raise Error, "Remote required #{field} is blank for #{locale}." if StoreRelease.blank?(infos[locale].public_send(field)) }
      end
      detail = review_detail
      desired_review_patch.each do |field, expected|
        raise Error, "Remote reviewed App Review details differ; run preparation first." unless detail && detail.public_send(field) == expected
      end
      return unless require_review
      raise Error, "Exact version has no App Review details; complete them before submission." unless detail
      values = (CONTACT_FIELDS.values + %i[demo_account_required demo_account_name demo_account_password]).to_h { |field| [field, detail.public_send(field)] }
      validate_review_values!(values)
      raise Error, "Selected build has no export-compliance answer; obtain the actual answer before submission." if @build.uses_non_exempt_encryption.nil?
      explicit = @defaults["uses_non_exempt_encryption"]
      raise Error, "Selected build's encryption answer differs from the reviewed plan." unless explicit.nil? || explicit == @build.uses_non_exempt_encryption
    end

    def reject_scheduled_date!
      unless StoreRelease.blank?(@version.earliest_release_date)
        raise Error, "Exact iOS version retains a scheduled release date; clear or reconcile that schedule in App Store Connect before this manual-release workflow."
      end
    end

    def guard_review_submissions!
      submissions = call("Read all iOS review submissions") { @app.get_review_submissions(client: @client, filter: { platform: PLATFORM }, includes: nil) }
        .select { |submission| submission.platform == PLATFORM && submission.state != "COMPLETE" }
      raise Error, "An active, unresolved, or unknown iOS review submission blocks this action." if submissions.any? { |submission| submission.state != "READY_FOR_REVIEW" }
      raise Error, "Multiple ready iOS review submissions require Console resolution." if submissions.length > 1
      ready = submissions.first
      if ready
        items = submission_items(ready)
        verify_submission_items!(items) unless items.empty?
      end
      ready
    end

    def submission_items(submission)
      call("Read exact review-submission items") do
        @api::ReviewSubmissionItem.all(client: @client, review_submission_id: submission.id,
                                       includes: "appStoreVersion,appEvent,appStoreVersionExperiment,appCustomProductPageVersion")
      end
    end

    def verify_submission_items!(items)
      unless @version && items.length == 1 && items.first.app_store_version&.id == @version.id
        raise Error, "Review submission contains unrelated or ambiguous items; resolve it in App Store Connect."
      end
      item = items.first
      if item.app_event || item.app_store_version_experiment || item.app_store_product_page_version
        raise Error, "Review item contains unrelated content."
      end
    end
  end
end
