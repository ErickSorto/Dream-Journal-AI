package org.ballistic.dreamjournalai.shared

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.configure
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook
import org.ballistic.dreamjournalai.shared.core.domain.ReviewComponent
import org.ballistic.dreamjournalai.shared.di.initKoin
import org.koin.mp.KoinPlatform.getKoin
import platform.UIKit.UIColor
import platform.UIKit.UIViewController

private var didInstallUnhandledExceptionHook = false

@OptIn(ExperimentalNativeApi::class)
private fun installUnhandledExceptionLogger() {
    if (didInstallUnhandledExceptionHook) return
    didInstallUnhandledExceptionHook = true
    setUnhandledExceptionHook { throwable ->
        println("DreamJournal unhandled Kotlin exception: ${throwable::class.simpleName}: ${throwable.message}")
        throwable.printStackTrace()
    }
}

fun MainViewController(): UIViewController {
    val controller = ComposeUIViewController(
        configure = {
            installUnhandledExceptionLogger()
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
        App(
            requestInAppReview = {
                CoroutineScope(Dispatchers.Main).launch {
                    getKoin()
                        .get<ReviewComponent>()
                        .requestInAppReview()
                        .collect { }
                }
            }
        )
    }
    controller.view.backgroundColor = UIColor.blackColor
    return controller
}
