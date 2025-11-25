package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import org.ballistic.dreamjournalai.shared.core.platform.rememberAudioPlayer
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.RedOrange
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.SkyBlue
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.Yellow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VoiceRecordingPlayback(
    audioUrl: String,
    duration: Long,
    isPermanent: Boolean,
    onDelete: () -> Unit,
    onEvent: (AddEditDreamEvent) -> Unit,
    onFleetingWarningClick: () -> Unit,
    onTranscriptionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = rememberAudioPlayer()
    val isPlaying by player.isPlaying.collectAsState()
    val progress by player.progress.collectAsState()

    // Generate pseudo-random bar heights based on URL to look like a waveform
    val barHeights = remember(audioUrl) {
        val random = kotlin.random.Random(audioUrl.hashCode())
        List(35) { random.nextFloat().coerceIn(0.3f, 1f) }
    }

    val filledBrush = remember {
        Brush.verticalGradient(
            colors = listOf(RedOrange, SkyBlue)
        )
    }

    val emptyBrush = remember {
        SolidColor(White.copy(alpha = 0.3f))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "HourglassRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000 // Total cycle time: 1s rotate + 1s pause
                0f at 0 using LinearEasing // Start at 0
                180f at 1000 using LinearEasing // Rotate to 180 in 1s
                180f at 2000 // Hold at 180 for another 1s
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(55.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .combinedClickable(
                onClick = { },
                onLongClick = { onDelete() }
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Fleeting Warning Icon (if not permanent)
                if (!isPermanent) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(32.dp) // Slightly bigger
                            .clip(CircleShape)
                            .clickable {
                                onEvent(AddEditDreamEvent.TriggerVibration)
                                onFleetingWarningClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.HourglassEmpty, // Rounded Icon
                            contentDescription = "Fleeting Audio Warning",
                            tint = Yellow,
                            modifier = Modifier
                                .size(24.dp) // Slightly bigger
                                .rotate(rotation)
                        )
                    }
                }

                // Transcription Icon
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            onEvent(AddEditDreamEvent.TriggerVibration)
                            onTranscriptionClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "View Transcription",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Visualizer
            Column(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                // Dynamic Visualizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
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
                                .background(
                                    if (isFilled) filledBrush else emptyBrush
                                )
                        )
                    }
                }
            }

            // Play/Pause Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .background(White.copy(alpha = 0.2f))
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
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
