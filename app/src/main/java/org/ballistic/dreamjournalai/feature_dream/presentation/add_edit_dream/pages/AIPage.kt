package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.components.ArcRotationAnimation


@Composable
fun AIPage(
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {


    val responseState = viewModel.dreamUiState.value.dreamAIExplanation
    val imageState = viewModel.dreamUiState.value.dreamAIImage
    val infiniteTransition = rememberInfiniteTransition()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState(), true),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f)),
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
                        .clip(RoundedCornerShape(10.dp))
                    ,
                    contentScale = ContentScale.Crop
                )
            }


            if (responseState.isLoading && imageState.isLoading) {
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

        //vertical spacer
        Spacer(modifier = Modifier.weight(1f))

        Box(contentAlignment = Alignment.BottomCenter) {
            Button(
                onClick = {
                    viewModel.onEvent(AddEditDreamEvent.ClickGenerateAIResponse(viewModel.dreamUiState.value.dreamContent))
                    viewModel.onEvent(AddEditDreamEvent.CLickGenerateAIImage(viewModel.dreamUiState.value.dreamAIImage.image.toString()))
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.7f))

            ) {
                Text(
                    text = "Generate AI Response",
                    modifier = Modifier
                        .padding(16.dp),
                    color = Color.Black,
                    style = typography.titleMedium,
                    fontWeight = Bold
                )
            }
        }
    }


}