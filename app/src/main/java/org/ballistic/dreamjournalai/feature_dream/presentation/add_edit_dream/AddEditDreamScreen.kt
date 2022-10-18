package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream


import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
            Color(if (dreamColor != -1) dreamColor else viewModel.dreamBackgroundColor.value)
        )

    }

    val scope = rememberCoroutineScope()




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
    LaunchedEffect(key1 = viewModel.dreamBackgroundColor.value) {
        dreamBackGroundAnimatable.animateTo(//animate to new backgroundimage color
            targetValue = Color(viewModel.dreamBackgroundColor.value),
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


    ){ padding ->
        Image(painter = painterResource(id = viewModel.dreamBackgroundColor.value), contentDescription = "Dream Background", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
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
