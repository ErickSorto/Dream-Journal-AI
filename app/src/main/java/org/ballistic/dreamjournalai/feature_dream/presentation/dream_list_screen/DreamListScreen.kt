package org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.insets.navigationBarsWithImePadding
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.components.DreamItem
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.viewmodel.DreamsViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamsScreen(
    navController: NavController,
    viewModel: DreamsViewModel = hiltViewModel(),
    mainScreenViewModel: MainScreenViewModel
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    mainScreenViewModel.setBottomBarState(true)
    mainScreenViewModel.setFloatingActionButtonState(true)

    Box(){

        Image(modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.blue_lighthouse),
            contentDescription = "Background", contentScale = ContentScale.Crop)

        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            containerColor = Color.Transparent,
            modifier = Modifier.navigationBarsWithImePadding()
        )
        { padding ->
            Column(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
                    .fillMaxSize()
            ){
                LazyColumn(modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding())){
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
                                    viewModel.onEvent(DreamsEvent.DeleteDream(dream))
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Dream deleted",
                                        actionLabel = "Undo"
                                    )
                                    if(result == SnackbarResult.ActionPerformed){
                                        viewModel.onEvent(DreamsEvent.RestoreDream)
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

        }
    }
}