package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamViewModel

@Composable
fun AIPage(
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "AI Response", fontSize = 24.sp)
        Text(
            text = viewModel.dreamUiState.value.dreamAIExplanation, modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(16.dp, 0.dp, 16.dp, 0.dp)
        )

        //vertical spacer
        Spacer(modifier = Modifier.weight(1f))



        Box(contentAlignment = Alignment.BottomCenter) {
            Button(
                onClick = {
                    viewModel.onEvent(AddEditDreamEvent.ClickGenerateAIResponse(viewModel.dreamUiState.value.dreamContent))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red.copy(alpha = 0.8f))

            ) {
                Text(
                    text = "Generate AI Response", modifier = Modifier
                        .padding(16.dp), color = Color.White, fontSize = 16.sp
                )
            }
        }
    }
}