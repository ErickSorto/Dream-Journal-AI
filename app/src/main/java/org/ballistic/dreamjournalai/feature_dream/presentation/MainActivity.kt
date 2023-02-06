package org.ballistic.dreamjournalai.feature_dream.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.feature_dream.navigation.MainGraph
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenView
import org.ballistic.dreamjournalai.onboarding.data.DataStoreRepository
import org.ballistic.dreamjournalai.onboarding.presentation.WelcomeScreen
import org.ballistic.dreamjournalai.onboarding.presentation.viewmodel.SplashViewModel
import org.ballistic.dreamjournalai.ui.theme.DreamCatcherAITheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    var keepSplashOpened = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }



        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DreamCatcherAITheme {
                // A surface container using the 'background' color from the theme
                ProvideWindowInsets(
                    windowInsetsAnimationsEnabled = true, consumeWindowInsets = false
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        //  val screen by splashViewModel.state

                        MainScreenView(onDataLoaded = {
                            keepSplashOpened = false
                        })
                    }
                }
            }
        }
    }
}