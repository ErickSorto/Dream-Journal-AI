package org.ballistic.dreamjournalai.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ballistic.dreamjournalai.feature_dream.presentation.account_settings.AccountSettingsScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.DreamJournalListScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.viewmodel.DreamJournalListViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.store_billing.presentation.store_screen.StoreEvent
import org.ballistic.dreamjournalai.store_billing.presentation.store_screen.StoreScreen
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScreenGraph(
    navController: NavHostController,
    mainScreenViewModelState: MainScreenViewModelState,
    innerPadding: PaddingValues,
    onMainEvent: (MainScreenEvent) -> Unit = {},
    onStoreEvent: (StoreEvent) -> Unit = {},
    onNavigateToOnboardingScreen: () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = Screens.DreamJournalScreen.route,
    ) {

        composable(route = Screens.DreamJournalScreen.route) {
            val dreamJournalListViewModel: DreamJournalListViewModel = hiltViewModel()
            DreamJournalListScreen(
                navController = navController,
                mainScreenViewModelState = mainScreenViewModelState,
                dreamJournalListState = dreamJournalListViewModel.dreamJournalListState.collectAsStateWithLifecycle().value,
                innerPadding = innerPadding,
                onMainEvent = { onMainEvent(it) },
                onDreamListEvent = { dreamJournalListViewModel.onEvent(it) },
            )
        }
        //store
        composable(route = Screens.StoreScreen.route) {
            StoreScreen(
                mainScreenViewModelState = mainScreenViewModelState,
                onMainEvent = { onMainEvent(it) },
                onStoreEvent = { onStoreEvent(it) })
        }


        composable(
            route = Screens.AddEditDreamScreen.route +
                    "?dreamId={dreamId}&dreamImageBackground={dreamImageBackground}",
            arguments = listOf(
                navArgument(
                    name = "dreamId"
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(
                    name = "dreamImageBackground"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
            )
        ) { value ->
            val image = value.arguments?.getInt("dreamImageBackground") ?: -1
            AddEditDreamScreen(
                navController = navController,
                dreamImage = image,
                mainScreenViewModelState = mainScreenViewModelState,
            ) { onMainEvent(it) }
        }
        // Add your new screens here
        composable(route = Screens.Favorites.route) {
            // Implement your Favorites screen composable here
        }

        composable(route = Screens.AccountSettings.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            AccountSettingsScreen(
                authViewModel.state.collectAsStateWithLifecycle().value,
                paddingValues = innerPadding,
                onNavigateToOnboardingScreen = onNavigateToOnboardingScreen,
                authEvent = { authViewModel.onEvent(it) }
            )
        }

        composable(route = Screens.AboutMe.route) {
            // Implement your About Me screen composable here
        }

        composable(route = Screens.Tools.route) {
            // Implement your Tools screen composable here
        }

        composable(route = Screens.Statistics.route) {
            // Implement your Statistics screen composable here
        }

        composable(route = Screens.NotificationSettings.route) {
            // Implement your Notification Settings screen composable here
        }
        composable(route = Screens.Nightmares.route) {
            // Implement your Nightmares screen composable here
        }

        composable(route = Screens.Dictionary.route) {
            // Implement your Dictionary screen composable here
        }
        composable(route = Screens.DreamSettings.route) {
            // Implement your Dream Settings screen composable here
        }
    }
}