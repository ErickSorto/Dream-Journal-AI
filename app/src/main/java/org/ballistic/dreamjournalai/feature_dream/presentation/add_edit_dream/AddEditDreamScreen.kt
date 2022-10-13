package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream


import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.components.TabLayout
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.components.TransparentHintTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDreamScreen(
    navController: NavController,
    dreamColor: Int,
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {
    val titleState = viewModel.dreamTitle.value
    val contentState = viewModel.dreamContent.value

    val snackbarHostState = remember { SnackbarHostState() }

    val dreamBackGroundAnimatable = remember {
        Animatable(
            Color(if (dreamColor != -1) dreamColor else viewModel.dreamColor.value)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(dreamBackGroundAnimatable.value)
                .padding(16.dp)
        ) {
            TabLayout()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Dream.dreamColors.forEach { color ->
                    val colorInt = color.toArgb()
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(15.dp, CircleShape)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                if (viewModel.dreamColor.value == colorInt) 5.dp else 0.dp,
                                Color.Black,
                                CircleShape
                            )
                            .clickable {
                                scope.launch {
                                    dreamBackGroundAnimatable.animateTo(
                                        targetValue = color,
                                        animationSpec = tween(
                                            durationMillis = 500
                                        )
                                    )


                                }
                                viewModel.onEvent(AddEditDreamEvent.ChangeColor(colorInt))
                            }


                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TransparentHintTextField(
                text = titleState.text,
                hint = titleState.hint,
                onValueChange = {
                    viewModel.onEvent(AddEditDreamEvent.EnteredTitle(it))
                },
                onFocusChange = {
                    viewModel.onEvent(AddEditDreamEvent.ChangeTitleFocus(it))
                },
                isHintVisible = titleState.isHintVisible,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))
            TransparentHintTextField(
                text = contentState.text,
                hint = contentState.hint,
                onValueChange = {
                    viewModel.onEvent(AddEditDreamEvent.EnteredContent(it))
                },
                onFocusChange = {
                    viewModel.onEvent(AddEditDreamEvent.ChangeContentFocus(it))
                },
                isHintVisible = contentState.isHintVisible,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxHeight()
            )
        }

    }

}
