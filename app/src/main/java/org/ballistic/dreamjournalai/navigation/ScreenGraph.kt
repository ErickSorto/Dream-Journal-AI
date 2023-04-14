package org.ballistic.dreamjournalai.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.DreamJournalScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.DreamsEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.viewmodel.DreamsViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.store_billing.presentation.store_screen.StoreEvent
import org.ballistic.dreamjournalai.store_billing.presentation.store_screen.StoreScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScreenGraph(
    navController: NavHostController,
    mainScreenViewModelState: MainScreenViewModelState,
    dreamsViewModel: DreamsViewModel,
    innerPadding: PaddingValues,
    onMainEvent: (MainScreenEvent) -> Unit = {},
    onDreamsEvent: (DreamsEvent) -> Unit = {},
    onStoreEvent: (StoreEvent) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screens.DreamJournalScreen.route,
    ) {


        composable(route = Screens.DreamJournalScreen.route) {
            DreamJournalScreen(
                navController = navController,
                mainScreenViewModelState = mainScreenViewModelState,
                dreamsViewModel = dreamsViewModel,
                innerPadding = innerPadding,
                onMainEvent = { onMainEvent(it) },
                onDreamsEvent = { onDreamsEvent(it) }
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
    }
}