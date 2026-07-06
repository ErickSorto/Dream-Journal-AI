import SwiftUI
import FirebaseCore
import FirebaseAuth
import FirebaseFirestore
import FirebaseMessaging
import ComposeApp
import GoogleSignIn
import GoogleMobileAds
import UserNotifications

private final class DreamNorthRewardedAdHandler: NSObject, IosRewardedAdHandler, FullScreenContentDelegate {
    #if DEBUG
    private let adUnitID = "ca-app-pub-3940256099942544/1712485313"
    #else
    private let adUnitID = "ca-app-pub-8710979310678386/7814480218"
    #endif
    private var rewardedAd: RewardedAd?
    private var onAdFailedToLoad: (() -> Void)?

    func showRewardedAd(
      onAdLoaded: @escaping () -> Void,
      onAdFailedToLoad: @escaping () -> Void,
      onAdReward: @escaping () -> Void
    ) {
      self.onAdFailedToLoad = onAdFailedToLoad

      Task { @MainActor in
        do {
          let ad = try await RewardedAd.load(with: adUnitID, request: Request())
          rewardedAd = ad
          ad.fullScreenContentDelegate = self
          onAdLoaded()

          guard let rootViewController = UIApplication.shared.dreamNorthTopViewController() else {
            rewardedAd = nil
            onAdFailedToLoad()
            return
          }

          ad.present(from: rootViewController) {
            onAdReward()
          }
        } catch {
          rewardedAd = nil
          onAdFailedToLoad()
        }
      }
    }

    func ad(
      _ ad: FullScreenPresentingAd,
      didFailToPresentFullScreenContentWithError error: Error
    ) {
      rewardedAd = nil
      onAdFailedToLoad?()
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
      rewardedAd = nil
      onAdFailedToLoad = nil
    }
}

private extension UIApplication {
    func dreamNorthTopViewController() -> UIViewController? {
      let root = connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .flatMap(\.windows)
        .first { $0.isKeyWindow }?
        .rootViewController ?? keyWindow?.rootViewController

      return root?.dreamNorthTopPresentedViewController()
    }
}

private extension UIViewController {
    func dreamNorthTopPresentedViewController() -> UIViewController {
      if let presentedViewController {
        return presentedViewController.dreamNorthTopPresentedViewController()
      }
      return self
    }
}

private enum FirebaseBootstrap {
    private static var isConfigured = false

    static func configureIfNeeded() {
      guard !isConfigured else { return }
      FirebaseApp.configure()
      isConfigured = true
    }
}

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    private let rewardedAdHandler = DreamNorthRewardedAdHandler()
    private var authStateHandle: AuthStateDidChangeListenerHandle?

    override init() {
      super.init()
      FirebaseBootstrap.configureIfNeeded()
      IosRewardedAdBridge.shared.handler = rewardedAdHandler
    }

    func application(
      _ application: UIApplication,
      didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
      UNUserNotificationCenter.current().delegate = self
      Messaging.messaging().delegate = self
      requestGeneratedArtNotificationPermission(application)
      authStateHandle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
        guard user != nil else { return }
        self?.refreshGeneratedArtMessagingToken()
      }
      MobileAds.shared.start()
      #if DEBUG
      scheduleNotificationSmokeTestIfRequested()
      #endif
      return true
    }

    func application(
      _ app: UIApplication,
      open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]
    ) -> Bool {
      var handled: Bool

      handled = GIDSignIn.sharedInstance.handle(url)
      if handled {
        return true
      }

      // Handle other custom URL types.

      // If not handled by this app, return false.
      return false
    }

    func userNotificationCenter(
      _ center: UNUserNotificationCenter,
      didReceive response: UNNotificationResponse,
      withCompletionHandler completionHandler: @escaping () -> Void
    ) {
	      let userInfo = response.notification.request.content.userInfo
	      let destination = userInfo[NotificationNavigationController.shared.EXTRA_DESTINATION] as? String
	      let dreamId = userInfo[NotificationNavigationController.shared.EXTRA_DREAM_ID] as? String
	      NotificationNavigationController.shared.openRawDestination(rawDestination: destination, dreamId: dreamId)
	      completionHandler()
	    }

	    func application(
	      _ application: UIApplication,
	      didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
	    ) {
	      Messaging.messaging().apnsToken = deviceToken
	      refreshGeneratedArtMessagingToken()
	    }

	    func application(
	      _ application: UIApplication,
	      didReceiveRemoteNotification userInfo: [AnyHashable : Any]
	    ) async -> UIBackgroundFetchResult {
	      return UIBackgroundFetchResult.newData
	    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
      registerGeneratedArtDeviceToken(fcmToken)
    }

    func userNotificationCenter(
      _ center: UNUserNotificationCenter,
      willPresent notification: UNNotification,
      withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
      completionHandler([.banner, .list, .sound, .badge])
    }

    deinit {
      if let authStateHandle {
        Auth.auth().removeStateDidChangeListener(authStateHandle)
      }
    }

    private func requestGeneratedArtNotificationPermission(_ application: UIApplication) {
      UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
        guard granted else { return }
        DispatchQueue.main.async {
          application.registerForRemoteNotifications()
        }
      }
    }

    private func refreshGeneratedArtMessagingToken() {
      Messaging.messaging().token { [weak self] token, error in
        guard error == nil else { return }
        self?.registerGeneratedArtDeviceToken(token)
      }
    }

    private func registerGeneratedArtDeviceToken(_ token: String?) {
      guard let token = token?.trimmingCharacters(in: .whitespacesAndNewlines),
            !token.isEmpty,
            let uid = Auth.auth().currentUser?.uid
      else {
        return
      }
      let tokenId = String(token.replacingOccurrences(of: "/", with: "_").prefix(160))
      Firestore.firestore()
        .collection("users")
        .document(uid)
        .collection("device_tokens")
        .document(tokenId)
        .setData(
          [
            "token": token,
            "active": true,
            "platform": "ios",
            "updatedAt": FieldValue.serverTimestamp()
          ],
          merge: true
        )
    }

    #if DEBUG
    private func scheduleNotificationSmokeTestIfRequested() {
      let arguments = ProcessInfo.processInfo.arguments
      guard let flagIndex = arguments.firstIndex(of: "-DreamNorthNotificationSmokeTest"),
            arguments.indices.contains(flagIndex + 1)
      else {
        return
      }

      let kind = arguments[flagIndex + 1]
      let payload: (identifier: String, title: String, body: String, attachment: String?)
      switch kind {
      case "plain":
        payload = (
          identifier: "debug_smoke_plain_notification",
          title: "Reality check",
          body: "Pause and ask: am I dreaming?",
          attachment: nil
        )
      case "token":
        payload = (
          identifier: "debug_smoke_dream_token_notification",
          title: "Claim your daily token",
          body: "Your DreamNorth token is ready.",
          attachment: "daily_token_notification_attachment"
        )
      case "reality":
        payload = (
          identifier: "debug_smoke_reality_check_notification",
          title: "Reality check",
          body: "Pause and ask: am I dreaming?",
          attachment: "reality_check_notification_attachment"
        )
      default:
        payload = (
          identifier: "debug_smoke_dream_journal_notification",
          title: "Write in your dream journal",
          body: "Take a minute to save what you remember.",
          attachment: "dream_journal_notification_attachment"
        )
      }

      let center = UNUserNotificationCenter.current()
      center.requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
        guard granted else { return }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
          let content = UNMutableNotificationContent()
          content.title = payload.title
          content.body = payload.body
          content.sound = .default

          let attachmentResource = payload.attachment.flatMap { attachment -> (url: URL, typeHint: String)? in
            var candidates: [(name: String, extension: String, typeHint: String)] = [
              (attachment, "jpg", "public.jpeg"),
              (attachment, "png", "public.png")
            ]
            if attachment.hasSuffix("_attachment") {
              let artFallback = String(attachment.dropLast("_attachment".count)) + "_art"
              candidates.append((artFallback, "jpg", "public.jpeg"))
              candidates.append((artFallback, "png", "public.png"))
            }
            for candidate in candidates {
              if let url = Bundle.main.url(forResource: candidate.name, withExtension: candidate.extension) {
                return (url, candidate.typeHint)
              }
            }
            return nil
          }
          if let resource = attachmentResource,
             let attachmentIdentifier = payload.attachment,
             let attachment = try? UNNotificationAttachment(
              identifier: attachmentIdentifier,
              url: resource.url,
              options: [UNNotificationAttachmentOptionsTypeHintKey: resource.typeHint]
             ) {
            content.attachments = [attachment]
          }

          let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1.0, repeats: false)
          let request = UNNotificationRequest(identifier: payload.identifier, content: content, trigger: trigger)
          center.add(request)
        }
      }
    }
    #endif
}

@main
struct iosAppApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    init(){
        // Avoid configuring Firebase twice. AppDelegate configures early for Messaging/Crashlytics.
        FirebaseBootstrap.configureIfNeeded()
      }

    var body: some Scene {
        WindowGroup {
            ContentView().onOpenURL(perform: { url in
                GIDSignIn.sharedInstance.handle(url)
            })
        }
    }
}
