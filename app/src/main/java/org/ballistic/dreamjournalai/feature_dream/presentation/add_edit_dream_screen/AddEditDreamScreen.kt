package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen


import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.AlertSave
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.TabLayout
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.viewmodel.AddEditDreamViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModelState

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDreamScreen(
    navController: NavController,
    dreamImage: Int,
    addEditDreamViewModel: AddEditDreamViewModel = hiltViewModel(),
    mainScreenViewModelState: MainScreenViewModelState,
    onMainEvent: (MainScreenEvent) -> Unit = {}
) {

    val snackbarHostState = remember { SnackbarHostState() }

    onMainEvent(MainScreenEvent.SetBottomBarState(false))
    onMainEvent(MainScreenEvent.SetFloatingActionButtonState(false))
    onMainEvent(MainScreenEvent.SetTopBarState(false))
    onMainEvent(MainScreenEvent.SetSearchingState(false))
    
    BackHandler {
        addEditDreamViewModel.dialogState.value = !addEditDreamViewModel.dialogState.value
    }

    val dreamBackgroundImage = remember {
        mutableStateOf(
            if (dreamImage != -1) dreamImage else addEditDreamViewModel.dreamUiState.value.dreamInfo.dreamBackgroundImage
        )
    }

    LaunchedEffect(key1 = true) {
        addEditDreamViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditDreamViewModel.UiEvent.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
                is AddEditDreamViewModel.UiEvent.SaveDream -> {
                    navController.navigateUp()
                }

                else -> {}
            }
        }
    }

    Crossfade(targetState = dreamBackgroundImage.value) { image ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = "Dream Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(10.dp)
            )
        }
    }

    if (addEditDreamViewModel.dialogState.value) {
        AlertSave(
            onDismiss = {
                navController.navigateUp()
                addEditDreamViewModel.dialogState.value = false
            },
            onConfirm = {
                addEditDreamViewModel.dialogState.value = false
                addEditDreamViewModel.onEvent(AddEditDreamEvent.SaveDream)
                if (addEditDreamViewModel.saveSuccess.value) {
                    navController.navigateUp()
                }
            },
            onClickOutside = {
                addEditDreamViewModel.dialogState.value = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Dream Journal AI") },
                navigationIcon = {
                    IconButton(onClick = {
                        addEditDreamViewModel.dialogState.value = true
                    }) {
                        Icon(
                            modifier = Modifier.rotate(180f),
                            imageVector = Icons.Filled.ArrowRightAlt,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { addEditDreamViewModel.onEvent(AddEditDreamEvent.SaveDream) }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save Dream"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.4f),
                    navigationIconContentColor = Color.Black,
                    titleContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                ),
                modifier = Modifier.padding()
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        containerColor = Color.Transparent,

        ) { padding ->
        //change bottom to 0.dp in padding

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
        ) {
            TabLayout(dreamBackgroundImage)
        }
    }
}