package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamViewModel


@OptIn(ExperimentalPagerApi::class, DelicateCoroutinesApi::class)
@Composable
fun GenerateButton(viewModel : AddEditDreamViewModel, state: PagerState) {
    val scope = rememberCoroutineScope()
    val dreamUiState = viewModel.dreamUiState
    if (dreamUiState.value.dreamContent.isNotBlank() && dreamUiState.value.dreamContent.length > 10) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Button(
                onClick = {
                    scope.launch {
                        delay(100)
                        state.animateScrollToPage(1)
                    }
                    GlobalScope.launch {
                        viewModel.onEvent(AddEditDreamEvent.ClickGenerateAIResponse(viewModel.dreamUiState.value.dreamContent))
                        viewModel.onEvent(AddEditDreamEvent.ClickGenerateDetails(viewModel.dreamUiState.value.dreamContent))
                        delay(3000)
                        viewModel.onEvent(AddEditDreamEvent.CLickGenerateAIImage(viewModel.dreamUiState.value.dreamAIImage.image.toString()))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.7f))

            ) {
                Text(
                    text = "Generate AI Response",
                    modifier = Modifier
                        .padding(16.dp),
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
