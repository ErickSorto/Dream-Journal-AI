package org.ballistic.dreamjournalai.shared

import android.content.res.Configuration
import android.graphics.Color
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.extensions.onCreateOrOnNewIntent
import com.mikhailovskii.inappreview.InAppReviewDelegate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationNavigationController
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.StoreScreen
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.viewmodel.StoreScreenViewModelState
import org.ballistic.dreamjournalai.shared.theme.DreamJournalAITheme
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin

object Static {
    init {
        System.setProperty("kotlin-logging-to-android-native", "true")
    }
}
private val static = Static

class MainActivity : ComponentActivity() {


    private var keepSplashOpened = true

    private val inAppReviewManager: InAppReviewDelegate by lazy {
        getKoin().get<InAppReviewDelegate> { parametersOf(this) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KMPNotifier.onCreateOrOnNewIntent(intent)
        handleNotificationIntent(intent)

        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val navigationBarStyle = if (isNightMode) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
            navigationBarStyle = navigationBarStyle
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }
        lifecycleScope.launch {
            delay(SPLASH_FAILSAFE_TIMEOUT_MS)
            keepSplashOpened = false
        }
        if (isDebugStorePreviewIntent(intent)) {
            keepSplashOpened = false
            setContent {
                DreamJournalAITheme {
                    StoreScreen(
                        storeScreenViewModelState = StoreScreenViewModelState(
                            isUserAnonymous = false,
                            dreamTokens = 500,
                        ),
                        bottomPaddingValue = 0.dp,
                    )
                }
            }
            return
        }
        setContent {
            App(
                onSplashFinished = {
                    keepSplashOpened = false
                },
                context = this@MainActivity,
                requestInAppReview = ::showInAppReview,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        KMPNotifier.onCreateOrOnNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        NotificationNavigationController.open(
            rawDestination = intent?.getStringExtra(NotificationNavigationController.EXTRA_DESTINATION),
            dreamId = intent?.getStringExtra(NotificationNavigationController.EXTRA_DREAM_ID)
        )
    }

    private fun isDebugStorePreviewIntent(intent: Intent?): Boolean {
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return isDebuggable && intent?.getBooleanExtra(DEBUG_STORE_PREVIEW_EXTRA, false) == true
    }

    private fun showInAppReview() {
        lifecycleScope.launch {
            inAppReviewManager.requestInAppReview().collect { reviewCode ->
                // handle the result
            }
        }
    }

    private companion object {
        const val DEBUG_STORE_PREVIEW_EXTRA = "dreamnorth.debug.open_store_preview"
        const val SPLASH_FAILSAFE_TIMEOUT_MS = 2_500L
    }
}
