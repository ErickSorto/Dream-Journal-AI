package org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.components.DreamItem
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.viewmodel.DreamsViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.navigation.Screens

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DreamJournalScreen(
    navController: NavController,
    mainScreenViewModelState: MainScreenViewModelState,
    dreamsViewModel: DreamsViewModel,
    innerPadding: PaddingValues = PaddingValues(),
    onMainEvent : (MainScreenEvent) -> Unit = {},
    onDreamsEvent : (DreamsEvent) -> Unit = {}
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val state by dreamsViewModel.state.collectAsStateWithLifecycle()


    onMainEvent(MainScreenEvent.SetBottomBarState(true))
    onMainEvent(MainScreenEvent.SetFloatingActionButtonState(true))
    onMainEvent(MainScreenEvent.SetTopBarState(true))

//    mainScreenViewModelState.setBottomBarState(true)
//    mainScreenViewModelState.setFloatingActionButtonState(true)
//    mainScreenViewModelState.setTopBarState(true)



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentPadding = innerPadding,
    ) {
        items(state.dreams) { dream ->
            DreamItem(
                dream = dream,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp)
                    .clickable {
                        navController.navigate(
                            Screens.AddEditDreamScreen.route +
                                    "?dreamId=${dream.id}&dreamImageBackground=${dream.backgroundImage}"
                        )
                    },
                onDeleteClick = {
                    scope.launch {
                        dreamsViewModel.onEvent(DreamsEvent.DeleteDream(dream, context))

                        val result = mainScreenViewModelState.scaffoldState.snackBarHostState.value.showSnackbar(
                            message = "Dream deleted",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Long
                        )

                        mainScreenViewModelState.scaffoldState.snackBarHostState.value.currentSnackbarData?.dismiss()

                        if (result == SnackbarResult.ActionPerformed) {
                            dreamsViewModel.onEvent(DreamsEvent.RestoreDream)
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}