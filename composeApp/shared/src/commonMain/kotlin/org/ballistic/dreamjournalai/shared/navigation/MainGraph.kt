package org.ballistic.dreamjournalai.shared.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModel
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModel
import org.ballistic.dreamjournalai.shared.dream_main.presentation.MainScreenView
import org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.OnboardingScreen
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainGraph(
    onDataLoaded: () -> Unit,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.OnboardingScreen,
        enterTransition = { fadeIn(animationSpec = tween(1000)) },
        exitTransition = { fadeOut(animationSpec = tween(1000)) },
        popEnterTransition = { fadeIn(animationSpec = tween(1000)) },
        popExitTransition = { fadeOut(animationSpec = tween(1000)) },
        modifier = Modifier.fillMaxSize()
    ) {
        composable<Route.OnboardingScreen> {
            val loginViewModel = koinViewModel<LoginViewModel>()
            val signupViewModel = koinViewModel<SignupViewModel>()
            val loginViewModelState = loginViewModel.state.collectAsStateWithLifecycle().value
            val signupViewModelState = signupViewModel.state.collectAsStateWithLifecycle().value

            OnboardingScreen(
                navigateToDreamJournalScreen = {
                    navController.popBackStack()
                    navController.navigate(Route.MainScreen)
                },
                onDataLoaded = {
                    onDataLoaded()
                },
                loginViewModelState = loginViewModelState,
                signupViewModelState = signupViewModelState,
                onLoginEvent = {
                    loginViewModel.onEvent(it)
                },
                onSignupEvent = {
                    signupViewModel.onEvent(it)
                },
            )
        }

        composable<Route.MainScreen> {
            val mainScreenViewModel = koinViewModel<MainScreenViewModel>()
            val mainScreenViewModelState = mainScreenViewModel.mainScreenViewModelState
                .collectAsStateWithLifecycle().value

            MainScreenView(
                mainScreenViewModelState = mainScreenViewModelState,
                onDataLoaded = {
                    onDataLoaded()
                },
                onMainEvent = {
                    mainScreenViewModel.onEvent(it)
                },
                onNavigateToOnboardingScreen = {
                    navController.popBackStack()
                    navController.navigate(Route.OnboardingScreen)
                },
            )
        }
    }
}
