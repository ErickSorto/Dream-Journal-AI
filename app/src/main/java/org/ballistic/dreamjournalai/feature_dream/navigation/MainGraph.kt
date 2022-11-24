package org.ballistic.dreamjournalai.feature_dream.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.dreams.DreamsScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.Signup_Screen

@Composable
fun MainGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.DreamListScreen.route
    ) {
        composable(route = Screens.DreamListScreen.route) {
            DreamsScreen(navController = navController)
        }
        composable(route = Screens.StoreSignInScreen.route) {
            Signup_Screen(navController = navController)
        }
        composable(
            route = Screens.AddEditDreamScreen.route +
                    "?dreamId={dreamId}&dreamImageBackground={dreamImageBackground}",
            arguments = listOf(
                navArgument(
                    name = "dreamId"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
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
                dreamImage = image
            )
        }
    }
}