package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen


import androidx.activity.compose.BackHandler
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
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.ImageGenerationPopUp
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.TabLayout
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDreamScreen(
    navController: NavController,
    dreamImage: Int,
    viewModel: AddEditDreamViewModel = hiltViewModel(),
    mainScreenViewModel: MainScreenViewModel
) {

    val snackbarHostState = remember { SnackbarHostState() }




    mainScreenViewModel.setBottomBarState(false)
    mainScreenViewModel.setFloatingActionButtonState(false)

    BackHandler {
        viewModel.dialogState.value = !viewModel.dialogState.value
    }

    val dreamBackgroundImage = remember {
        mutableStateOf(
            if (dreamImage != -1) dreamImage else viewModel.dreamUiState.value.dreamInfo.dreamBackgroundImage
        )
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
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

    if (viewModel.dialogState.value) {
        AlertSave(
            onDismiss = {
                navController.navigateUp()
                viewModel.dialogState.value = false
            },
            onConfirm = {
                viewModel.dialogState.value = false
                viewModel.onEvent(AddEditDreamEvent.SaveDream)
                if (viewModel.saveSuccess.value) {
                    navController.navigateUp()
                }
            },
            onClickOutside = {
                viewModel.dialogState.value = false
            }
        )
    }



    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Dream Journal AI") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.dialogState.value = true
                    }) {
                        Icon(
                            modifier = Modifier.rotate(180f),
                            imageVector = Icons.Filled.ArrowRightAlt,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(AddEditDreamEvent.SaveDream) }) {
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