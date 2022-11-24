package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream


import androidx.compose.animation.Animatable
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsWithImePadding
import kotlinx.coroutines.flow.collectLatest
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.components.TabLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDreamScreen(
    navController: NavController,
    dreamImage: Int,
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    val dreamBackGroundAnimatable = remember {
        Animatable(
            Color(if (dreamImage != -1) dreamImage else viewModel.dreamUiState.value.dreamInfo.dreamBackgroundImage)
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

    Crossfade(targetState = viewModel.dreamUiState.value.dreamInfo.dreamBackgroundImage) { image ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dreamBackGroundAnimatable.value)
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


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Dream Journal AI") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
                ))
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        containerColor = Color.Transparent,

        ) { padding ->
        //crossfade between imagebackgrounds

//
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)

        ) {

            TabLayout(dreamImage)

        }
    }
}