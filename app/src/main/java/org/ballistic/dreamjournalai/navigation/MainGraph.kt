package org.ballistic.dreamjournalai.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenView
import org.ballistic.dreamjournalai.onboarding.presentation.OnboardingScreen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun MainGraph(
    navController: NavHostController,
    onDataLoaded : () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screens.OnboardingScreen.route,
    ) {
        //welcome
        composable(route = Screens.OnboardingScreen.route) {
            OnboardingScreen(
                navigateToDreamJournalScreen = {
                    navController.popBackStack()
                    navController.navigate(Screens.MainScreen.route)
                },
                onDataLoaded = {
                    onDataLoaded()
                }
            )
        }

        composable(route = Screens.MainScreen.route) {
            MainScreenView(
                onDataLoaded = {
                   onDataLoaded()
                },
            )
        }
    }
}