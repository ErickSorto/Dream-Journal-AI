package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.pages

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.viewmodel.AddEditDreamViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.*


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun AIPage(
    state: PagerState,
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {

    val responseState = viewModel.dreamUiState.value.dreamAIExplanation
    val imageState = viewModel.dreamUiState.value.dreamAIImage
    val contentState = viewModel.dreamUiState.value.dreamContent
    val detailState = viewModel.dreamUiState.value.dreamGeneratedDetails.response
    val infiniteTransition = rememberInfiniteTransition()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as Activity


    if (viewModel.imageGenerationPopUpState.value) {
        ImageGenerationPopUp(

            onDreamTokenClick = {
                viewModel.imageGenerationPopUpState.value = false
                scope.launch {
                    viewModel.onEvent(
                        AddEditDreamEvent.ClickGenerateAIImage(
                            detailState,
                            activity,
                            false
                        )
                    )
                }
            },
            onAdClick = {
                viewModel.imageGenerationPopUpState.value = false
                scope.launch {
                    viewModel.onEvent(
                        AddEditDreamEvent.ClickGenerateAIImage(
                            detailState,
                            activity,
                            true
                        )
                    )
                }
            },
            onClickOutside = {
                viewModel.imageGenerationPopUpState.value = false
            },
            pagerState = state,
        )
    }

    if (viewModel.dreamInterpretationPopUpState.value) {
        DreamInterpretationPopUp(
            onDreamTokenClick = {
                viewModel.dreamInterpretationPopUpState.value = false
                scope.launch {
                    viewModel.onEvent(
                        AddEditDreamEvent.ClickGenerateAIResponse(
                            contentState,
                            activity,
                            false
                        )
                    )
                }
            },
            onAdClick = {
                viewModel.dreamInterpretationPopUpState.value = false
                scope.launch {
                    viewModel.onEvent(
                        AddEditDreamEvent.ClickGenerateAIResponse(
                            contentState,
                            activity,
                            true
                        )
                    )
                }
            },
            onClickOutside = {
                viewModel.dreamInterpretationPopUpState.value = false
            },
            pagerState = state,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .verticalScroll(scrollState, true)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp),
                text = "AI Results",
                style = typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = viewModel.dreamUiState.value.dreamAIExplanation.response,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp, 0.dp, 16.dp, 16.dp),
                textAlign = TextAlign.Center,
                style = typography.bodyLarge
            )

            val painter =
                rememberAsyncImagePainter(model = viewModel.dreamUiState.value.dreamAIImage.image.toString())

            //if painter is null then do not show the image
            AnimatedVisibility(visible = viewModel.dreamUiState.value.dreamAIImage.image != null) {
                Image(
                    painter = painter,
                    contentDescription = "AI Generated Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(16.dp, 0.dp, 16.dp, 16.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (responseState.isLoading || imageState.isLoading) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    ArcRotationAnimation(
                        infiniteTransition = infiniteTransition,
                    )
                }

            }
        }

        GenerateButtonsLayout(viewModel = viewModel, state = state)
    }
}