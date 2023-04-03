package org.ballistic.dreamjournalai.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.DreamJournalScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.viewmodel.DreamsViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.store_screen.StoreScreen


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun ScreenGraph(
    navController: NavHostController,
    mainScreenViewModel: MainScreenViewModel,
    dreamsViewModel: DreamsViewModel,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screens.DreamJournalScreen.route,
    ) {


        composable(route = Screens.DreamJournalScreen.route) {
            DreamJournalScreen(
                navController = navController,
                mainScreenViewModel = mainScreenViewModel,
                dreamsViewModel = dreamsViewModel,
                innerPadding = innerPadding
            )
        }
        //store
        composable(route = Screens.StoreScreen.route) {
            StoreScreen(mainScreenViewModel = mainScreenViewModel)
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