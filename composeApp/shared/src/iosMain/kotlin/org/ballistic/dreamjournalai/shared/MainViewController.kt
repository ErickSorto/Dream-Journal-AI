package org.ballistic.dreamjournalai.shared

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import com.revenuecat.purchases.kmp.Purchases
import org.ballistic.dreamjournalai.shared.di.initKoin
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.configure
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        initKoin(
            null
        )
        enableCrashlytics()
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            apiKey = "appl_GACgngYvBvuSSIuemglFOKzRvDA" //TODO: Add your RevenueCat API Key
        ) {
            appUserId = Firebase.auth.currentUser?.uid
        }
    }
) {
    App()
}