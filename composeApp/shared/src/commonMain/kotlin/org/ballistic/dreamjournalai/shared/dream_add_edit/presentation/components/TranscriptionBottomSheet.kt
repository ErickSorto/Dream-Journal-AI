package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ballistic.dreamjournalai.shared.core.util.formatDuration
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.BrighterWhite
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptionBottomSheet(
    transcription: String,
    date: String,
    duration: Long,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        onDismissRequest = onDismissRequest,
        containerColor = LightBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Audio Transcription",
                style = MaterialTheme.typography.headlineSmall,
                color = BrighterWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = White.copy(alpha = 0.7f)
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelMedium,
                    color = White.copy(alpha = 0.7f)
                )
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = White.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = transcription.ifBlank { "No transcription available." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = White
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
