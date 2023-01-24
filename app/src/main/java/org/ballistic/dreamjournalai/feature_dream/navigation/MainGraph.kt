package org.ballistic.dreamjournalai.feature_dream.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.DreamsScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.Sign_In_Screen
import org.ballistic.dreamjournalai.feature_dream.presentation.store_screen.StoreScreen

@Composable
fun MainGraph(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screens.DreamListScreen.route
    ) {
        composable(route = Screens.DreamListScreen.route) {
            DreamsScreen(navController = navController)
        }
        //store
        composable(route = Screens.StoreScreen.route) {
            StoreScreen(navController = navController)
        }
        composable(route = Screens.SignInScreen.route) {
            Sign_In_Screen(
                navController = navController,
            )
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
                dreamImage = image
            )
        }
    }
}