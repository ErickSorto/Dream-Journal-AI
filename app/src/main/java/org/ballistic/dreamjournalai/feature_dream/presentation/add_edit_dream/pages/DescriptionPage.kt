package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.components.TransparentHintTextField

@Composable
fun DescriptionPage(
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {
    val titleState = viewModel.dreamTitle.value
    val contentState = viewModel.dreamContent.value

    Column(
        modifier = Modifier
            .background(color = Color.Transparent)
            .padding(16.dp)
    ) {

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
            textStyle = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(16.dp)

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
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(16.dp)
        )

    }

}