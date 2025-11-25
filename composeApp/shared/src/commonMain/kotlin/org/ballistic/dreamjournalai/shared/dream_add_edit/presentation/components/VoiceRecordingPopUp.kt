package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.core.platform.AudioRecorder
import org.ballistic.dreamjournalai.shared.core.platform.rememberAudioRecorder
import org.ballistic.dreamjournalai.shared.core.platform.rememberRecordAudioPermissionState
import org.ballistic.dreamjournalai.shared.core.util.formatDuration
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.RedOrange
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.SkyBlue
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import kotlin.time.ExperimentalTime

enum class RecorderState {
    IDLE, RECORDING, PAUSED, MAX_REACHED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun VoiceRecordingPopUp(
    isTranscribing: Boolean,
    onDismissRequest: () -> Unit,
    onRecordingSaved: (String, Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val recorder = rememberAudioRecorder()
    val permissionState = rememberRecordAudioPermissionState()
    var recorderState by remember { mutableStateOf(RecorderState.IDLE) }
    var durationSeconds by remember { mutableLongStateOf(0L) }
    val maxDuration = 1200L // 20 minutes
    val warningThreshold = 300L // 5 minutes remaining

    // Progress Logic
    val progressAnim = remember { Animatable(0f) }
    var isFinishing by remember { mutableStateOf(false) }
    var hasEnteredProcessingMode by remember { mutableStateOf(false) }
    var currentMessage by remember { mutableStateOf("Saving Recording...") }

    // Once we enter processing mode, we latch it
    if (isTranscribing) {
        hasEnteredProcessingMode = true
    }

    LaunchedEffect(isTranscribing) {
        if (isTranscribing) {
            progressAnim.snapTo(0f)
            launch {
                // Slower animation as requested (5 seconds)
                progressAnim.animateTo(
                    targetValue = 0.95f,
                    animationSpec = tween(durationMillis = 8000, easing = LinearEasing)
                )
            }
            launch {
                while(progressAnim.value < 1f && isActive) {
                    val p = progressAnim.value
                    currentMessage = when {
                        p < 0.33f -> "Saving Recording..."
                        p < 0.66f -> "Encrypting..."
                        else -> "Transcribing..."
                    }
                    delay(50)
                }
            }
        } else {
            // Check if we need to finish (if we were processing)
            if (hasEnteredProcessingMode) {
                isFinishing = true
                // Slower finish animation to ensure user sees all steps
                progressAnim.animateTo(1f, tween(800))
                delay(200)
                onDismissRequest()
            }
        }
    }
    
    // We only show progress if we have entered processing mode at least once
    val showProgress = hasEnteredProcessingMode

    // Timer Logic
    LaunchedEffect(recorderState) {
        if (recorderState == RecorderState.RECORDING) {
            val startTime = kotlin.time.Clock.System.now().toEpochMilliseconds() - (durationSeconds * 1000)
            while (isActive) {
                val elapsed = (kotlin.time.Clock.System.now().toEpochMilliseconds() - startTime) / 1000
                durationSeconds = elapsed
                
                if (durationSeconds >= maxDuration) {
                    recorder.pause()
                    recorderState = RecorderState.MAX_REACHED
                }
                
                delay(100)
            }
        }
    }

    // Force stop on dismiss/transcribe if needed
    LaunchedEffect(isTranscribing) {
        if (isTranscribing) {
            // ensure recorder is stopped if we entered transcribing state
            if (recorderState == RecorderState.RECORDING || recorderState == RecorderState.PAUSED) {
                recorder.stop()
            }
        }
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        onDismissRequest = {
            if (!hasEnteredProcessingMode) {
                if (recorderState != RecorderState.IDLE && recorderState != RecorderState.MAX_REACHED) {
                    recorder.stop()
                }
                if (recorderState == RecorderState.MAX_REACHED) {
                    recorder.stop()
                }
                onDismissRequest()
            }
        },
        containerColor = LightBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showProgress) {
                AnimatedContent(
                    targetState = currentMessage,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(600)) togetherWith 
                                fadeOut(animationSpec = tween(200))
                    },
                    label = "ProcessTextAnimation"
                ) { targetText ->
                    SmoothTypewriterText(
                        text = targetText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = White
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                LoadingProgressBar(
                    progress = progressAnim.value,
                    brush = Brush.horizontalGradient(
                        colors = listOf(RedOrange, SkyBlue)
                    ),
                    modifier = Modifier.height(48.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Text(
                    text = "Voice Recorder",
                    style = MaterialTheme.typography.headlineSmall,
                    color = White
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Visualizer and Timer
                RecordingVisualizer(
                    recorder = recorder,
                    durationSeconds = durationSeconds,
                    isRecording = recorderState == RecorderState.RECORDING,
                    maxDuration = maxDuration,
                    warningThreshold = warningThreshold
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                when (recorderState) {
                    RecorderState.IDLE -> {
                        Button(
                            onClick = {
                                if (permissionState.isGranted) {
                                    recorder.start()
                                    recorderState = RecorderState.RECORDING
                                } else {
                                    permissionState.launchRequest()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (permissionState.isGranted) "Start Recording" else "Grant Permission",
                                style = MaterialTheme.typography.titleMedium,
                                color = White
                            )
                        }
                    }
                    RecorderState.RECORDING -> {
                        Button(
                            onClick = {
                                recorder.pause()
                                recorderState = RecorderState.PAUSED
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedOrange.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Pause",
                                style = MaterialTheme.typography.titleMedium,
                                color = White
                            )
                        }
                    }
                    RecorderState.PAUSED -> {
                        Column {
                            if (durationSeconds < maxDuration) {
                                Button(
                                    onClick = {
                                        recorder.resume()
                                        recorderState = RecorderState.RECORDING
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RedOrange),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Resume",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = White
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            Button(
                                onClick = {
                                    val path = recorder.stop()
                                    onRecordingSaved(path, durationSeconds)
                                    // Do NOT dismiss here, wait for isTranscribing
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = White
                                )
                            }
                        }
                    }
                    RecorderState.MAX_REACHED -> {
                        Button(
                            onClick = {
                                val path = recorder.stop()
                                onRecordingSaved(path, durationSeconds)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.titleMedium,
                                color = White
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RecordingVisualizer(
    recorder: AudioRecorder,
    durationSeconds: Long,
    isRecording: Boolean,
    maxDuration: Long,
    warningThreshold: Long,
    modifier: Modifier = Modifier
) {
    val amplitude by recorder.amplitude.collectAsState()
    val amplitudes = remember { mutableStateListOf<Float>() }
    val maxBars = 40

    LaunchedEffect(amplitude) {
        if (isRecording) {
            amplitudes.add(amplitude)
            if (amplitudes.size > maxBars) {
                amplitudes.removeAt(0)
            }
        }
    }
    
    // Pulsing effect when recording
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val remaining = maxDuration - durationSeconds
    val showWarning = remaining <= warningThreshold && isRecording

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = formatDuration(durationSeconds),
            style = MaterialTheme.typography.displayMedium,
            color = if (isRecording) White.copy(alpha = pulseAlpha) else White
        )
        
        if (showWarning) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Max almost reached: ${formatDuration(remaining)}",
                style = MaterialTheme.typography.bodyMedium,
                color = RedOrange
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val barWidth = size.width / maxBars
                val gap = barWidth * 0.3f
                val effectiveBarWidth = barWidth - gap
                val startX = (size.width - (amplitudes.size * barWidth)) / 2f

                drawLine(
                    color = White.copy(alpha = 0.1f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx()
                )

                amplitudes.forEachIndexed { index, amp ->
                    val height = (amp * size.height * 0.8f).coerceAtLeast(4.dp.toPx())
                    
                    val x = startX + index * barWidth
                    val y = (size.height - height) / 2

                    val brush = Brush.verticalGradient(
                        colors = listOf(RedOrange, SkyBlue)
                    )

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(x, y),
                        size = Size(effectiveBarWidth, height),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingProgressBar(
    progress: Float,
    brush: Brush,
    modifier: Modifier = Modifier,
) {
    val pct = (progress * 100).coerceIn(0f, 100f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush = brush)
            )
        }
        Text(
            text = "${pct.toInt()}%",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun SmoothTypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = White
) {
    val progress = remember { Animatable(0f) }
    
    // Reset when text changes
    LaunchedEffect(text) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = text.length.toFloat(),
            animationSpec = tween(
                durationMillis = text.length * 100, // Speed control
                easing = LinearEasing
            )
        )
    }
    
    val currentProgress = progress.value
    
    Text(
        text = buildAnnotatedString {
            text.forEachIndexed { index, char ->
                // Smooth fade-in without cursor
                val alpha = (currentProgress - index + 1).coerceIn(0f, 1f)
                withStyle(SpanStyle(color = color.copy(alpha = alpha))) {
                    append(char)
                }
            }
        },
        modifier = modifier,
        style = style
    )
}
