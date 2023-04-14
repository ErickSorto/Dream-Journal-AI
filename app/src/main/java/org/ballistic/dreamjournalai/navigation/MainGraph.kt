package org.ballistic.dreamjournalai.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenView
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.onboarding.presentation.OnboardingScreen
import org.ballistic.dreamjournalai.store_billing.presentation.store_screen.StoreScreenViewModel
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun MainGraph(
    navController: NavHostController,
    onDataLoaded: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Screens.OnboardingScreen.route,
    ) {
        //welcome
        composable(route = Screens.OnboardingScreen.route) {
            val authViewModel: AuthViewModel = hiltViewModel()

            OnboardingScreen(
                navigateToDreamJournalScreen = {
                    navController.popBackStack()
                    navController.navigate(Screens.MainScreen.route)
                },
                onDataLoaded = {
                    onDataLoaded()
                },
                authViewModelState = authViewModel.state.collectAsStateWithLifecycle().value,
                onEvent = {
                    authViewModel.onEvent(it)
                }
            )
        }

        composable(route = Screens.MainScreen.route) {
            val mainScreenViewModel: MainScreenViewModel = hiltViewModel()
            val storeScreenViewModel: StoreScreenViewModel = hiltViewModel()
            MainScreenView(
                mainScreenViewModelState = mainScreenViewModel.mainScreenViewModelState.collectAsStateWithLifecycle().value,
                onDataLoaded = {
                    onDataLoaded()
                },
                onMainEvent = {
                    mainScreenViewModel.onEvent(it)
                },
                onStoreEvent = {
                    storeScreenViewModel.onEvent(it)
                },
            )
        }
    }
}