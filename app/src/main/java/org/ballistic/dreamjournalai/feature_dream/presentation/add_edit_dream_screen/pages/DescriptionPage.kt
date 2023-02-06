package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.GenerateButton
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.TransparentHintTextField

@OptIn(DelicateCoroutinesApi::class, ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun DescriptionPage(
    state: PagerState
) {
    val viewModel: AddEditDreamViewModel = hiltViewModel()
    val titleState = viewModel.dreamUiState.value.dreamTitle
    val contentState = viewModel.dreamUiState.value.dreamContent
    val detailState = viewModel.dreamUiState.value.dreamGeneratedDetails
    val dreamUiState = viewModel.dreamUiState




    Column(
        modifier = Modifier
            .background(color = Color.Transparent)
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 16.dp)
    ) {

        TransparentHintTextField(
            text = titleState,
            hint = LocalContext.current.getString(org.ballistic.dreamjournalai.R.string.hint_title), //TODO
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
                .onFocusEvent {
                }

        )

        Spacer(modifier = Modifier.height(16.dp))

        TransparentHintTextField(
            text = dreamUiState.value.dreamContent,
            hint = LocalContext.current.getString(org.ballistic.dreamjournalai.R.string.hint_description), //TODO
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
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(8.dp)
                .onFocusEvent {
                }
        )


            GenerateButton(viewModel = viewModel, state = state)

    }
}