import SwiftUI
import Firebase
import RevenueCat
import ComposeApp
import GoogleSignIn

class AppDelegate: NSObject, UIApplicationDelegate {

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
}

@main
struct iosAppApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    init(){
        // Avoid configuring Firebase twice. If it's already configured by the shared KMP code,
        // skip configuring here to prevent the crash: "Default app has already been configured."
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        } else {
        }
      }

    var body: some Scene {
        WindowGroup {
            ContentView().onOpenURL(perform: { url in
                GIDSignIn.sharedInstance.handle(url)
            })
        }
    }
}
