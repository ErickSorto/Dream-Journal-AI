package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.pages.AIPage.AISubPages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.core.components.TypewriterText
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AIPageType
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.ArcRotationAnimation
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.UniversalButton
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AIState
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AIType
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AddEditDreamState
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.components.singleClick
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.BrighterWhite
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.UniversalAIPage(
    contentType: AIPageType,
    addEditDreamState: AddEditDreamState,
    textFieldState: TextFieldState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAddEditEvent: (AddEditDreamEvent) -> Unit,
    snackBarState: () -> Unit,
    onImageClick: (String) -> Unit,
) {
    val aiContent = contentType.getState(addEditDreamState)
    val lastClickTime = remember { mutableLongStateOf(0L) }

    when (contentType) {
        AIPageType.QUESTION -> {
            AIQuestionPage(
                addEditDreamState = addEditDreamState,
                textFieldState = textFieldState,
                onAddEditEvent = onAddEditEvent,
                snackBarState = snackBarState,
            )
        }

        AIPageType.PAINTER -> {
            AIPainterPage(
                addEditDreamState = addEditDreamState,
                textFieldState = textFieldState,
                onAddEditEvent = onAddEditEvent,
                snackBarState = snackBarState,
                animatedVisibilityScope = animatedVisibilityScope,
                onImageClick = singleClick(
                    lastClickTimeState = lastClickTime,
                    onClick = {
                        onImageClick(addEditDreamState.aiStates[AIType.IMAGE]?.response ?: "")
                    }
                )
            )
        }

        else -> {
            StandardAIPageLayout(
                aiContent = aiContent,
                title = contentType.title,
                contentType = contentType,
                textFieldState = textFieldState,
                onAddEditEvent = onAddEditEvent,
                snackBarState = snackBarState
            )
        }
    }
}

@Composable
fun StandardAIPageLayout(
    aiContent: AIState,
    title: String,
    contentType: AIPageType,
    textFieldState: TextFieldState,
    onAddEditEvent: (AddEditDreamEvent) -> Unit,
    snackBarState: () -> Unit,
) {
    val (progress, showLoading, _) = rememberTimedProgress(
        isLoading = aiContent.isLoading,
        contentType = contentType,
    )

    if (showLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 0.dp, 16.dp, 16.dp)
                .clip(RoundedCornerShape(10.dp)),
        ) {
            // Arc at top with 1:1 ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) { ArcRotationAnimation() }
            // Spacer to push progress bar to bottom
            Spacer(modifier = Modifier.weight(1f))
            LoadingProgressBar(
                progress = progress,
                color = colorFor(contentType),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }
    } else if (aiContent.response.isNotEmpty()) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = title,
                color = BrighterWhite,
                style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 16.dp)
            )
            TypewriterText(
                text = aiContent.response.trim(),
                textAlign = TextAlign.Start,
                style = typography.bodyMedium,
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp),
                color = White,
                useMarkdown = true
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            // No content and not loading, show Universal Button
            UniversalButton(
                buttonType = contentType.buttonType,
                textFieldState = textFieldState,
                onAddEditEvent = onAddEditEvent,
                snackBarState = snackBarState,
                size = 160.dp,  // Adjusted size
                fontSize = 24.sp,  // Adjusted font size
                modifier = Modifier.fillMaxSize(),
                hasText = true
            )
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AIPainterPage(
    addEditDreamState: AddEditDreamState,
    textFieldState: TextFieldState,
    onAddEditEvent: (AddEditDreamEvent) -> Unit,
    snackBarState: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onImageClick: () -> Unit
) {
    val imageState = addEditDreamState.aiStates[AIType.IMAGE]!!
    val painter = rememberAsyncImagePainter(model = imageState.response)
    val painterState by painter.state.collectAsState()

    val (progress, showLoading, _) = rememberTimedProgress(
        isLoading = imageState.isLoading,
        contentType = AIPageType.PAINTER,
        onFullyFinished = {
            if (addEditDreamState.isNewImageGenerated) {
                onAddEditEvent(AddEditDreamEvent.SetStartAnimation(true))
            }
        }
    )

    val scale = remember { Animatable(if (addEditDreamState.isNewImageGenerated) 0.8f else 1f) }
    val alpha = remember { Animatable(if (addEditDreamState.isNewImageGenerated) 0f else 1f) }

    LaunchedEffect(addEditDreamState.startAnimation) {
        if (addEditDreamState.startAnimation) {
            alpha.snapTo(0f)
            scale.snapTo(0.9f)
            launch {
                scale.animateTo(1f, animationSpec = tween(600, easing = LinearOutSlowInEasing))
            }
            launch {
                alpha.animateTo(1f, animationSpec = tween(600, easing = LinearOutSlowInEasing))
            }
            delay(450)
            onAddEditEvent(AddEditDreamEvent.TriggerVibrationSuccess)
            onAddEditEvent(AddEditDreamEvent.ResetNewImageGeneratedFlag)
            onAddEditEvent(AddEditDreamEvent.SetStartAnimation(false))
        }
    }

    if (showLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 0.dp, 16.dp, 16.dp)
                .clip(RoundedCornerShape(10.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) { ArcRotationAnimation() }
            Spacer(modifier = Modifier.weight(1f))
            LoadingProgressBar(
                progress = progress,
                color = colorFor(AIPageType.PAINTER),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }
    } else if (imageState.response.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(imageState.response)
                    .placeholderMemoryCacheKey("image/${imageState.response}")
                    .memoryCacheKey("image/${imageState.response}")
                    .build(),
                contentDescription = "AI Generated Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onImageClick)
                    .sharedElement(
                        rememberSharedContentState(key = "image/${imageState.response}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> tween(500) }
                    ),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            UniversalButton(
                textFieldState = textFieldState,
                buttonType = AIPageType.PAINTER.buttonType,
                size = 160.dp,
                fontSize = 24.sp,
                onAddEditEvent = onAddEditEvent,
                snackBarState = snackBarState,
                modifier = Modifier.fillMaxSize(),
                hasText = true
            )
        }
    }
}

@Composable
fun AIQuestionPage(
    addEditDreamState: AddEditDreamState,
    textFieldState: TextFieldState,
    onAddEditEvent: (AddEditDreamEvent) -> Unit,
    snackBarState: () -> Unit,
) {
    val questionState = addEditDreamState.aiStates[AIType.QUESTION_ANSWER]!!

    val (progress, showLoading, _) = rememberTimedProgress(
        isLoading = questionState.isLoading,
        contentType = AIPageType.QUESTION
    )

    if (showLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 0.dp, 16.dp, 16.dp)
                .clip(RoundedCornerShape(10.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) { ArcRotationAnimation() }
            Spacer(modifier = Modifier.weight(1f))
            LoadingProgressBar(
                progress = progress,
                color = colorFor(AIPageType.QUESTION),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }
    } else if (questionState.response.isNotEmpty()) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Dream Answer",
                color = BrighterWhite,
                style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
            )
            Text(
                text = questionState.question + if (questionState.question.endsWith("?")) "" else "?",
                color = White,
                style = typography.titleSmall,
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp),
                textAlign = TextAlign.Center
            )
            TypewriterText(
                text = questionState.response.trim(),
                style = typography.bodyMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 16.dp),
                color = White,
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            UniversalButton(
                textFieldState = textFieldState,
                size = 160.dp,
                fontSize = 24.sp,
                onAddEditEvent = onAddEditEvent,
                snackBarState = snackBarState,
                modifier = Modifier.fillMaxSize(),
                buttonType = AIPageType.QUESTION.buttonType,
                hasText = true
            )
        }
    }
}

private fun colorFor(contentType: AIPageType): Color {
    return when (contentType) {
        AIPageType.PAINTER -> OriginalXmlColors.SkyBlue
        AIPageType.EXPLANATION -> OriginalXmlColors.Purple
        AIPageType.ADVICE -> OriginalXmlColors.Yellow
        AIPageType.QUESTION -> OriginalXmlColors.RedOrange
        AIPageType.STORY -> OriginalXmlColors.LighterYellow
        AIPageType.MOOD -> OriginalXmlColors.Green
    }
}

@Composable
private fun LoadingProgressBar(
    progress: Float,
    color: Color,
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
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                color.copy(alpha = 0.9f),
                                color.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
        }
        Text(
            text = "${pct.toInt()}%",
            color = Color.White,
            style = typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun rememberTimedProgress(
    isLoading: Boolean,
    contentType: AIPageType,
    onFullyFinished: () -> Unit = {}
): Triple<Float, Boolean, Boolean> {
    val progressAnim = remember { Animatable(0f) }
    var show by remember { mutableStateOf(false) }
    var isAnimationFinished by remember { mutableStateOf(false) }

    val (durationMs, bufferCap) = when (contentType) {
        AIPageType.PAINTER -> 30_000L to 0.95f
        else -> 10_000L to 0.97f
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            show = true
            isAnimationFinished = false
            progressAnim.snapTo(0f)
            progressAnim.animateTo(
                targetValue = bufferCap,
                animationSpec = tween(
                    durationMillis = durationMs.toInt(),
                    easing = LinearEasing
                )
            )
        } else {
            if (show) {
                progressAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(500)
                )
                delay(250)
                show = false
                isAnimationFinished = true
                onFullyFinished()
            }
        }
    }

    return Triple(progressAnim.value, show, isAnimationFinished)
}
