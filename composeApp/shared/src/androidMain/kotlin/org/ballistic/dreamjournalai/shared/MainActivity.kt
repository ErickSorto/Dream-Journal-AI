package org.ballistic.dreamjournalai.shared

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.mikhailovskii.inappreview.InAppReviewDelegate
import kotlinx.coroutines.launch
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
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }
        setContent {
            App(
                onSplashFinished = {
                    keepSplashOpened = false
                },
                context = this@MainActivity
            )
        }
    }

    private fun showInAppReview() {
        lifecycleScope.launch {
            inAppReviewManager.requestInAppReview().collect { reviewCode ->
                // handle the result
            }
        }
    }
}
