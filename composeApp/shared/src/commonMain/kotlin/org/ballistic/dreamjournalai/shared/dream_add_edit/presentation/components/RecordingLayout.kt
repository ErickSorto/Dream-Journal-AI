package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent

@Composable
fun RecordingLayout(
    modifier: Modifier = Modifier,
    audioUrl: String,
    audioDuration: Long,
    isAudioPermanent: Boolean,
    isTranscribing: Boolean,
    onShowDeleteDialog: () -> Unit,
    onShowFleetingDialog: () -> Unit,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit,
) {
    AnimatedVisibility(
        visible = audioUrl.isNotBlank(),
        enter = fadeIn(animationSpec = tween(durationMillis = 150)),
        exit = fadeOut(animationSpec = tween(durationMillis = 150))
    ) {
        Column(modifier = modifier) {
            VoiceRecordingPlayback(
                audioUrl = audioUrl,
                duration = audioDuration,
                isPermanent = isAudioPermanent,
                onDelete = {
                    onAddEditDreamEvent(AddEditDreamEvent.TriggerVibration)
                    onShowDeleteDialog()
                },
                onEvent = onAddEditDreamEvent,
                onFleetingWarningClick = onShowFleetingDialog,
                onTranscriptionClick = {
                    onAddEditDreamEvent(AddEditDreamEvent.ToggleTranscriptionBottomSheet(true))
                },
                isTranscribing = isTranscribing
            )
        }
    }
}
