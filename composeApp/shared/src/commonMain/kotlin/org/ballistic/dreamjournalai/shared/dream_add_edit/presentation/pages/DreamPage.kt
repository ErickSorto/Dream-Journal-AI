package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.*
import org.ballistic.dreamjournalai.shared.core.components.ActionBottomSheet
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.FleetingAudioDialog
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.GenerateButtonsLayout
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.RecordingLayout
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.TransparentHintTextField
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun DreamPage(
    titleTextFieldState: TextFieldState,
    contentTextFieldState: TextFieldState,
    audioUrl: String,
    audioDuration: Long,
    audioTimestamp: Long,
    isAudioPermanent: Boolean,
    isTranscribing: Boolean,
    isUserAnonymous: Boolean,
    audioTranscription: String,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit,
    animateToPage: (Int) -> Unit,
    snackBarState: () -> Unit
) {
    val isKeyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val fullContentLength = contentTextFieldState.text.length + audioTranscription.length
    val canGenerateAI = fullContentLength >= 20

    var showDeleteAudioDialog by remember { mutableStateOf(false) }
    var showFleetingDialog by remember { mutableStateOf(false) }

    if (showDeleteAudioDialog) {
        ActionBottomSheet(
            title = stringResource(Res.string.delete_audio_title),
            message = stringResource(Res.string.delete_audio_message),
            buttonText = stringResource(Res.string.delete),
            onClick = {
                onAddEditDreamEvent(AddEditDreamEvent.DeleteVoiceRecording)
                showDeleteAudioDialog = false
            },
            onClickOutside = {
                showDeleteAudioDialog = false
            }
        )
    }

    if (showFleetingDialog) {
        FleetingAudioDialog(
            audioTimestamp = audioTimestamp,
            audioDurationSeconds = audioDuration,
            onDismiss = { showFleetingDialog = false },
            onKeepForever = { cost ->
                onAddEditDreamEvent(AddEditDreamEvent.MakeAudioPermanent(cost))
                showFleetingDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .background(color = Color.Transparent)
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        TransparentHintTextField(
            hint = stringResource(Res.string.hint_title),
            isHintVisible = titleTextFieldState.text.isBlank(),
            singleLine = true,
            textStyle = typography.headlineMedium.copy(color = OriginalXmlColors.White),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    OriginalXmlColors.LightBlack.copy(.7f)
                )
                .padding(16.dp),
            textFieldState = titleTextFieldState,
            onEvent = {
                onAddEditDreamEvent(it)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    OriginalXmlColors.LightBlack.copy(.7f)
                ),
        ) {
            TransparentHintTextField(
                hint = stringResource(Res.string.hint_description),
                isHintVisible = contentTextFieldState.text.isBlank(),
                textStyle = typography.bodyMedium.copy(
                    color = OriginalXmlColors.White
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp, 8.dp))
                    .padding(12.dp, 16.dp, 12.dp, 0.dp)
                    .weight(1f)
                    .background(
                        Color.Transparent
                    ),
                modifier2 = Modifier.fillMaxSize(),
                textFieldState = contentTextFieldState,
                onEvent = {
                    onAddEditDreamEvent(it)
                }
            )

            // AI Tools and Microphone button
            GenerateButtonsLayout(
                onAddEditEvent = onAddEditDreamEvent,
                snackBarState = snackBarState,
                animateToPage = animateToPage,
                isUserAnonymous = isUserAnonymous,
                canGenerateAI = canGenerateAI,
                audioUrl = audioUrl,
                onShowDeleteDialog = { showDeleteAudioDialog = true },
                isTranscribing = isTranscribing
            )

            // Recording playback UI
            AnimatedVisibility(
                visible = !isKeyboardOpen,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)),
                exit = fadeOut(animationSpec = tween(durationMillis = 150))
            ) {
                RecordingLayout(
                    audioUrl = audioUrl,
                    audioDuration = audioDuration,
                    isAudioPermanent = isAudioPermanent,
                    isTranscribing = isTranscribing,
                    onShowDeleteDialog = { showDeleteAudioDialog = true },
                    onShowFleetingDialog = { showFleetingDialog = true },
                    onAddEditDreamEvent = onAddEditDreamEvent,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier
            .consumeWindowInsets(WindowInsets.navigationBars)
            .imePadding())
    }
}
