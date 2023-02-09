package org.ballistic.dreamjournalai.feature_dream.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.DreamsScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.Sign_In_Screen
import org.ballistic.dreamjournalai.feature_dream.presentation.store_screen.StoreScreen
import org.ballistic.dreamjournalai.onboarding.presentation.WelcomeScreen

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun MainGraph(navController: NavHostController, startDestination: String, mainScreenViewModel: MainScreenViewModel) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        //welcome
        composable(route = Screens.Welcome.route) {
            WelcomeScreen(navController = navController, mainScreenViewModel = mainScreenViewModel)
        }

        composable(route = Screens.DreamListScreen.route) {
            DreamsScreen(navController = navController, mainScreenViewModel = mainScreenViewModel)
        }
        //store
        composable(route = Screens.StoreScreen.route) {
            StoreScreen( mainScreenViewModel = mainScreenViewModel)
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
        ) {
            val image = it.arguments?.getInt("dreamImageBackground") ?: -1
            AddEditDreamScreen(
                navController = navController,
                dreamImage = image,
                mainScreenViewModel = mainScreenViewModel
            )
        }
    }
}