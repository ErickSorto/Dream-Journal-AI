# Offline contracts against the pinned gems. No keys, authentication, or network.
require "minitest/autorun"
require "stringio"
require "ostruct"
require "spaceship"
require "google/apis/androidpublisher_v3"
require_relative "../../fastlane/StoreRelease"

class PinnedStoreClientsTest < Minitest::Test
  def setup
    @env = ENV.to_h
    %w[SPACESHIP_DEBUG SPACESHIP_PROXY SPACESHIP_PROXY_SSL_VERIFY_NONE DEBUG].each { |key| ENV.delete(key) }
  end
  def teardown
    ENV.replace(@env)
  end
  def apple_with_transport(mode)
    # The APIClient constructor only needs in_house; the test Faraday transport
    # replaces the original before the first request and contains no JWT/key.
    client = Spaceship::ConnectAPI::Client.new(token: OpenStruct.new(in_house: false))
    StoreRelease.harden_apple_client(client)
    calls = []
    transport = Faraday.new(url: "https://api.appstoreconnect.apple.com") do |connection|
      connection.adapter :test do |stubs|
        stubs.patch("/v1/appStoreReviewDetails/review-only") do |environment|
          calls << environment.body
          raise Faraday::TimeoutError, "SENTINEL_SECRET timeout" if mode == :timeout
          [500, { "Content-Type" => "application/json" }, { "errors" => [{ "detail" => "SENTINEL_SECRET echoed password" }] }]
        end
      end
    end
    client.tunes_request_client.instance_variable_set(:@client, transport)
    [client, calls]
  end
  def assert_no_retry_or_secret_log(mode)
    client, calls = apple_with_transport(mode)
    output = capture_io do
      error = assert_raises(StoreRelease::Error) do
        StoreRelease.safe_call("Update exact review-only detail", mutation: true) do
          client.patch_app_store_review_detail(app_store_review_detail_id: "review-only", attributes: { demoAccountPassword: "SENTINEL_SECRET" })
        end
      end
      refute_includes error.message, "SENTINEL_SECRET"
    end
    assert_equal 1, calls.length
    refute_includes output.join, "SENTINEL_SECRET"
    assert_equal File::NULL, client.tunes_request_client.logger.instance_variable_get(:@logdev)&.filename || File::NULL
  end
  def test_real_apple_500_path_has_no_retry_and_no_body_logs
    assert_no_retry_or_secret_log(:http500)
  end
  def test_real_apple_transport_timeout_has_no_retry_and_no_body_logs
    assert_no_retry_or_secret_log(:timeout)
  end
  def test_real_google_commit_supports_exact_guard_and_zero_retries
    client = Google::Apis::AndroidpublisherV3::AndroidPublisherService.new
    commands = []
    client.define_singleton_method(:execute_or_queue_command) do |command, &_block|
      commands << command
      nil
    end
    client.commit_edit("example.app", "edit-only", changes_in_review_behavior: "ERROR_IF_IN_REVIEW", changes_not_sent_for_review: true, options: { retries: 0 })
    assert_equal 1, commands.length
    assert_equal "ERROR_IF_IN_REVIEW", commands.first.query["changesInReviewBehavior"]
    assert_equal true, commands.first.query["changesNotSentForReview"]
    assert_equal 0, commands.first.options.retries
  end
  def test_google_model_serialization_preserves_listing_omissions
    model = Google::Apis::AndroidpublisherV3::Listing.new(short_description: "Selected text")
    json = JSON.parse(model.to_json)
    assert_equal({ "shortDescription" => "Selected text" }, json)
  end
end
