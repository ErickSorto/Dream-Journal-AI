package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.DelicateCoroutinesApi
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.GenerateButtonsLayout
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.TransparentHintTextField
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.viewmodel.AddEditDreamState

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(DelicateCoroutinesApi::class, ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun DescriptionPage(
    pagerState: PagerState,
    addEditDreamState: AddEditDreamState,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit = {},
) {


    Column(
        modifier = Modifier
            .background(color = Color.Transparent)
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 16.dp)
    ) {

        TransparentHintTextField(
            text = addEditDreamState.dreamTitle,
            hint = LocalContext.current.getString(org.ballistic.dreamjournalai.R.string.hint_title),
            onValueChange = {
                onAddEditDreamEvent(AddEditDreamEvent.EnteredTitle(it))
            },
            onFocusChange = {
                onAddEditDreamEvent(AddEditDreamEvent.EnteredTitle(addEditDreamState.dreamTitle))
            },
            isHintVisible = addEditDreamState.dreamTitle.isBlank(),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(16.dp)
                .onFocusEvent {
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TransparentHintTextField(
            text = addEditDreamState.dreamContent,
            hint = LocalContext.current.getString(org.ballistic.dreamjournalai.R.string.hint_description),
            onValueChange = {
                onAddEditDreamEvent(AddEditDreamEvent.EnteredContent(it))
            },
            onFocusChange = {
                onAddEditDreamEvent(AddEditDreamEvent.EnteredContent(addEditDreamState.dreamContent))
            },
            isHintVisible = addEditDreamState.dreamContent.isBlank(),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(8.dp)
                .onFocusEvent {
                }.focusable()
        )

        if (addEditDreamState.dreamContent.isNotBlank() && addEditDreamState.dreamContent.length > 10) {
            GenerateButtonsLayout(
                addEditDreamState = addEditDreamState,
                pagerState = pagerState
            )
        }
    }
}