package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream


import androidx.compose.animation.Animatable
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.components.TabLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDreamScreen(
    navController: NavController,
    dreamColor: Int,
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    val dreamBackGroundAnimatable = remember {
        Animatable(
            Color(if (dreamColor != -1) dreamColor else viewModel.dreamUiState.value.dreamInfo.dreamBackgroundImage)
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

            }
        }
    }
    //listen to color changes and animate
    LaunchedEffect(key1 = viewModel.dreamUiState.value.dreamInfo.dreamBackgroundImage) {
        dreamBackGroundAnimatable.animateTo(//animate to new backgroundimage color
            targetValue = Color(viewModel.dreamUiState.value.dreamInfo.dreamBackgroundImage),
            animationSpec = tween(
                durationMillis = 500
            )
        )
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(AddEditDreamEvent.SaveDream)
                },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = "Save Dream"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },


        ) { padding ->
        //crossfade between imagebackgrounds
        Crossfade(targetState = viewModel.dreamUiState.value.dreamInfo.dreamBackgroundImage) { image ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dreamBackGroundAnimatable.value)
                    .padding(padding)
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
//
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding()
        ) {

            TabLayout(dreamColor)

        }
    }
}