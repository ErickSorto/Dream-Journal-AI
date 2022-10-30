package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.components.TransparentHintTextField

@Composable
fun DescriptionPage(
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {
    val titleState = viewModel.dreamUiState.value.dreamTitle
    val contentState = viewModel.dreamUiState.value.dreamContent

    val dreamUiState = viewModel.dreamUiState

    Column(
        modifier = Modifier
            .background(color = Color.Transparent)
            .padding(16.dp)
    ) {

        TransparentHintTextField(
            text = titleState,
            hint = "Enter Title...", //TODO
            onValueChange = {
                viewModel.onEvent(AddEditDreamEvent.EnteredTitle(it))
            },
            onFocusChange = {
                            if (it.isFocused) {
                                viewModel.onEvent(AddEditDreamEvent.EnteredTitle(titleState))
                            } else {
                                viewModel.onEvent(AddEditDreamEvent.EnteredTitle(titleState))
                            }
            },
            isHintVisible = titleState.isBlank(),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(16.dp)

        )

        Spacer(modifier = Modifier.height(16.dp))
        TransparentHintTextField(
            text = dreamUiState.value.dreamContent,
            hint = LocalContext.current.getString(org.ballistic.dreamjournalai.R.string.hint_title), //TODO
            onValueChange = {
                viewModel.onEvent(AddEditDreamEvent.EnteredContent(it))
            },
            onFocusChange = {
                if (it.isFocused) {
                    viewModel.onEvent(AddEditDreamEvent.EnteredContent(contentState))
                } else {
                    viewModel.onEvent(AddEditDreamEvent.EnteredContent(contentState))
                }
            },
            isHintVisible = contentState.isBlank(),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(16.dp)
        )

    }

}