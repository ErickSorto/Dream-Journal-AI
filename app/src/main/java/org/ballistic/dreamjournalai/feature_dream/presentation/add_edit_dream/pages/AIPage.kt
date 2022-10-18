package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamViewModel

@Composable
fun AIPage(
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {


    Box(contentAlignment = Alignment.BottomCenter) {
        Button(onClick = {
            viewModel.onEvent(AddEditDreamEvent.AIResponse(viewModel.dreamContent.value.text))
        },
            modifier = Modifier.fillMaxWidth().padding(16.dp)

        ) {
            Text(text = "Generate AI Response", modifier = Modifier
                .padding(16.dp)
                .size(100.dp))
        }
    }





}