package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.audio_transcription
import dreamjournalai.composeapp.shared.generated.resources.date_duration_separator
import dreamjournalai.composeapp.shared.generated.resources.no_transcription_available
import dreamjournalai.composeapp.shared.generated.resources.pause
import dreamjournalai.composeapp.shared.generated.resources.play
import org.ballistic.dreamjournalai.shared.core.platform.rememberAudioPlayer
import org.ballistic.dreamjournalai.shared.core.util.darkModalBottomSheetProperties
import org.ballistic.dreamjournalai.shared.core.util.formatDuration
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptionBottomSheet(
    audioUrl: String,
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
        containerColor = OriginalXmlColors.LightBlack,
        contentColor = OriginalXmlColors.White,
        scrimColor = Color.Transparent,
        contentWindowInsets = { WindowInsets(0.dp) },
        properties = darkModalBottomSheetProperties()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(Res.string.audio_transcription),
                style = MaterialTheme.typography.headlineSmall,
                color = OriginalXmlColors.BrighterWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = OriginalXmlColors.White.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(Res.string.date_duration_separator),
                    style = MaterialTheme.typography.labelMedium,
                    color = OriginalXmlColors.White.copy(alpha = 0.7f)
                )
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = OriginalXmlColors.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (audioUrl.isNotBlank()) {
                AudioTranscriptionPlaybackRow(
                    audioUrl = audioUrl,
                    duration = duration
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = transcription.ifBlank { stringResource(Res.string.no_transcription_available) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = OriginalXmlColors.White
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun AudioTranscriptionPlaybackRow(
    audioUrl: String,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val player = rememberAudioPlayer()
    val isPlaying by player.isPlaying.collectAsState()
    val progress by player.progress.collectAsState()
    val barHeights = remember(audioUrl) {
        val random = kotlin.random.Random(audioUrl.hashCode())
        List(32) { random.nextFloat().coerceIn(0.3f, 1f) }
    }
    val filledBrush = remember {
        Brush.verticalGradient(
            colors = listOf(OriginalXmlColors.RedOrange, OriginalXmlColors.SkyBlue)
        )
    }
    val emptyBrush = remember {
        SolidColor(OriginalXmlColors.White.copy(alpha = 0.28f))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(40.dp)
                .background(OriginalXmlColors.White.copy(alpha = 0.18f))
                .clickable {
                    if (isPlaying) {
                        player.pause()
                    } else {
                        player.play(audioUrl)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) {
                    stringResource(Res.string.pause)
                } else {
                    stringResource(Res.string.play)
                },
                tint = OriginalXmlColors.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .height(26.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            barHeights.forEachIndexed { index, height ->
                val barThreshold = (index + 1).toFloat() / barHeights.size
                val isFilled = progress >= barThreshold

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(height)
                        .padding(horizontal = 1.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isFilled) filledBrush else emptyBrush)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.labelMedium,
            color = OriginalXmlColors.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold
        )
    }
}
