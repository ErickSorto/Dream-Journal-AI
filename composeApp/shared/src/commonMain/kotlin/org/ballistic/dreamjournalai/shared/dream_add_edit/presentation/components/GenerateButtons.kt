package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.*
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.ButtonType
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.components.singleClick
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GenerateButtonsLayout(
    onAddEditEvent: (AddEditDreamEvent) -> Unit,
    animateToPage: suspend (Int) -> Unit,
    snackBarState: () -> Unit,
    isUserAnonymous: Boolean = false,
    canGenerateAI: Boolean,
    audioUrl: String,
    onShowDeleteDialog: () -> Unit,
    isTranscribing: Boolean,
    isImageGenerationPending: Boolean = false,
    showPrimaryRecordingButton: Boolean = false
) {
    var showVoicePopup by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun openVoiceRecorder() {
        onAddEditEvent(AddEditDreamEvent.TriggerVibration)
        if (isUserAnonymous) {
            scope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.Resource(Res.string.sign_in_to_use_feature),
                        action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {})
                    )
                )
            }
        } else if (audioUrl.isNotBlank()) {
            scope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.Resource(Res.string.delete_recording_to_continue),
                        action = SnackbarAction(StringValue.Resource(Res.string.delete), {
                            onShowDeleteDialog()
                        })
                    )
                )
            }
        } else {
            showVoicePopup = true
        }
    }

    if (showVoicePopup) {
        VoiceRecordingPopUp(
            isTranscribing = isTranscribing,
            onDismissRequest = { showVoicePopup = false },
            onRecordingSaved = { path, duration ->
                onAddEditEvent(AddEditDreamEvent.OnVoiceRecordingSaved(path, duration))
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = showPrimaryRecordingButton,
            enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                expandVertically(animationSpec = tween(durationMillis = 260)),
            exit = fadeOut(animationSpec = tween(durationMillis = 150)) +
                shrinkVertically(animationSpec = tween(durationMillis = 220))
        ) {
            PrimaryVoiceRecordingButton(
                onClick = ::openVoiceRecorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Text(
            text = stringResource(Res.string.ai_tools_selection),
            style = MaterialTheme.typography.labelMedium,
            color = OriginalXmlColors.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Row(
            modifier = Modifier
                .padding(bottom = 0.dp, start = 16.dp, end = 16.dp, top = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = OriginalXmlColors.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ButtonType.entries.forEach { item ->
                        val isPendingPaintButton = isImageGenerationPending && item == ButtonType.PAINT
                        UniversalButton(
                            buttonType = item,
                            canGenerateAI = canGenerateAI && !isPendingPaintButton,
                            animateToPage = { index ->
                                animateToPage(index)
                            },
                            onAddEditEvent = onAddEditEvent,
                            snackBarState = {
                                if (isPendingPaintButton) {
                                    scope.launch {
                                        SnackbarController.sendEvent(
                                            SnackbarEvent(
                                                message = StringValue.DynamicString("Painting in background"),
                                                action = SnackbarAction(
                                                    name = StringValue.Resource(Res.string.dismiss),
                                                    action = {}
                                                )
                                            )
                                        )
                                    }
                                } else {
                                    snackBarState()
                                }
                            },
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .size(44.dp),
                            hasText = false
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = !showPrimaryRecordingButton,
                enter = fadeIn(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 120))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CompactVoiceRecordingButton(onClick = ::openVoiceRecorder)
                }
            }
        }
    }
}

@Composable
private fun PrimaryVoiceRecordingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(104.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF3B195C).copy(alpha = 0.92f),
                        Color(0xFF9A2B87).copy(alpha = 0.86f),
                        Color(0xFF2B1A59).copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFF7FD6),
                                Color(0xFFBC4AF4),
                                Color(0xFF42206E)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.26f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.start_recording),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.voice_recorder),
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CompactVoiceRecordingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = OriginalXmlColors.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = stringResource(Res.string.voice_recording),
            tint = OriginalXmlColors.White,
            modifier = Modifier.size(24.dp)
        )
    }
}


@Composable
fun UniversalButton(
    modifier: Modifier = Modifier,
    buttonType: ButtonType,
    canGenerateAI: Boolean,
    animateToPage: suspend (Int) -> Unit = {},
    onAddEditEvent: (AddEditDreamEvent) -> Unit,
    snackBarState: () -> Unit = {},
    size: Dp = 32.dp,
    fontSize: TextUnit = 14.sp,
    hasText: Boolean = true
) {
    val keyBoardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val textColor = if (canGenerateAI) {
        buttonType.longTextColorId
    } else {
        buttonType.baseColorId
    }

    val alpha = if (canGenerateAI) {
        1f
    } else {
        0.7f
    }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
            ) {
                onAddEditEvent(AddEditDreamEvent.TriggerVibration)
                if (canGenerateAI) {
                    keyBoardController?.hide()
                    focusManager.clearFocus()
                    scope.launch {
                        animateToPage(buttonType.pageIndex)
                        onAddEditEvent(AddEditDreamEvent.SetAIPage(buttonType.aiPage))
                    }
                } else {
                    snackBarState()
                }

            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasText) {
            Text(
                text = stringResource(Res.string.tap),
                fontSize = fontSize,
                color = Color.Transparent,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Icon(
            painter = painterResource(buttonType.drawableId),
            contentDescription = stringResource(buttonType.title),
            modifier = Modifier
                .padding(8.dp)
                .size(size),
            tint = textColor.copy(alpha = alpha)
        )
        if (hasText) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.tap_to_generate, stringResource(buttonType.title)),
                fontSize = fontSize,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun AdTokenLayout(
    onAdClick: () -> Unit = {},
    onDreamTokenClick: (amount: Int) -> Unit = {},
    isAdButtonVisible: Boolean = true,
    amount: Int,
    customText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DreamTokenGenerateButton(onClick = { onDreamTokenClick(amount) }, amount = amount, customText = customText)
        if (isAdButtonVisible) {
            Spacer(modifier = Modifier.height(12.dp))
            WatchAdButton(onClick = { onAdClick() })
        }
    }
}

@Composable
fun WatchAdButton(
    onClick: () -> Unit,
) {
    val lastClickTime = remember { mutableLongStateOf(0L) }
    Button(
        onClick = singleClick(lastClickTime) { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = OriginalXmlColors.RedOrange.copy(alpha = 0.8f)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.baseline_smart_display_24),
                contentDescription = stringResource(Res.string.watch_ad),
                modifier = Modifier.size(24.dp),
                tint = OriginalXmlColors.White,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(Res.string.watch_ad),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DreamTokenGenerateButton(
    onClick: () -> Unit,
    amount: Int,
    customText: String? = null
) {
    val amountText = if (amount == 0) stringResource(Res.string.free) else "$amount"
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = OriginalXmlColors.SkyBlue.copy(alpha = 0.8f)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (customText != null) {
                Text(
                    text = customText,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Image(
                    painter = painterResource(Res.drawable.dream_token),
                    contentDescription = stringResource(Res.string.dream_token),
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(Res.string.use_dream_tokens, amountText),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
