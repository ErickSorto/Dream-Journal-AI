package org.ballistic.dreamjournalai.shared.dream_tools.presentation.paint_dreams_screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.baseline_brush_24
import dreamjournalai.composeapp.shared.generated.resources.dream_token
import dreamjournalai.composeapp.shared.generated.resources.onboarding_long
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.BottomNavigationController
import org.ballistic.dreamjournalai.shared.BottomNavigationEvent
import org.ballistic.dreamjournalai.shared.core.components.ActionBottomSheet
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.ImageGenerationPopUp
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.ShootingStarLayer
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.TwinklesLayer
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.PaintDreamWorldEvent
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.DreamToolButton
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.DreamToolScreenWithNavigateUpTopBar
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.paint_dreams_screen.viewmodel.PaintDreamWorldScreenState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.RedOrange
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.SkyBlue
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import org.jetbrains.compose.resources.painterResource
import kotlin.math.absoluteValue

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PaintDreamWorldScreen(
    paintDreamWorldScreenState: PaintDreamWorldScreenState,
    bottomPaddingValue: Dp,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (PaintDreamWorldEvent) -> Unit,
    onMainEvent: (MainScreenEvent) -> Unit,
    onImageClick: (String) -> Unit,
    navigateUp: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isAnimationFinished = remember { mutableStateOf(paintDreamWorldScreenState.isAnimationPlayed) }
    
    val cost = if (paintDreamWorldScreenState.hasGeneratedDreamWorld) 5 else 0

    // Loading Animation State
    val progressAnim = remember { Animatable(0f) }
    var currentMessage by remember { mutableStateOf("Analyzing your recent dreams...") }

    // Start loading animation when isLoading becomes true
    LaunchedEffect(paintDreamWorldScreenState.isLoading) {
        if (paintDreamWorldScreenState.isLoading) {
            progressAnim.snapTo(0f)
            launch {
                // Animate progress to 95% over 60 seconds
                progressAnim.animateTo(
                    targetValue = 0.95f,
                    animationSpec = tween(durationMillis = 60000, easing = LinearEasing)
                )
            }
            launch {
                while (isActive && progressAnim.value < 1f) {
                    val p = progressAnim.value
                    currentMessage = when {
                        p < 0.25f -> "Analyzing your recent dreams..."
                        p < 0.50f -> "Extracting themes and emotions..."
                        p < 0.85f -> "Painting your dream world..."
                        else -> "Finalizing masterpiece..."
                    }
                    delay(100)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        BottomNavigationController.sendEvent(BottomNavigationEvent.SetVisibility(false))
    }

    LaunchedEffect(paintDreamWorldScreenState.error) {
        paintDreamWorldScreenState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    
    if (paintDreamWorldScreenState.isImageGenerationPopUpVisible) {
        ImageGenerationPopUp(
            dreamTokens = paintDreamWorldScreenState.dreamTokens,
            imageStyle = paintDreamWorldScreenState.imageStyle,
            onDreamTokenClick = { amount, style ->
                onEvent(PaintDreamWorldEvent.GeneratePainting(amount, style))
            },
            onAdClick = {
            },
            onClickOutside = {
                onEvent(PaintDreamWorldEvent.ToggleImageGenerationPopUp(false))
            },
            onImageStyleChange = {
                onEvent(PaintDreamWorldEvent.OnImageStyleChanged(it))
            },
            isWorldPainting = true,
            fixedCost = cost
        )
    }

    if (paintDreamWorldScreenState.isDeleteDialogVisible) {
        ActionBottomSheet(
            title = "Delete Painting",
            message = "Are you sure you want to delete this painting?",
            buttonText = "Delete",
            onClick = {
                paintDreamWorldScreenState.paintingToDelete?.let {
                    onEvent(PaintDreamWorldEvent.DeletePainting(it))
                }
                onEvent(PaintDreamWorldEvent.ToggleDeleteConfirmation(false))
            },
            onClickOutside = { onEvent(PaintDreamWorldEvent.ToggleDeleteConfirmation(false)) }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundAnimation(
            shouldAnimate = !paintDreamWorldScreenState.isAnimationPlayed,
            onAnimationFinished = { 
                isAnimationFinished.value = true
                onEvent(PaintDreamWorldEvent.AnimationFinished)
            }
        )

        Scaffold(
            topBar = {
                AnimatedVisibility(
                    visible = isAnimationFinished.value,
                    enter = fadeIn(animationSpec = tween(1000))
                ) {
                    DreamToolScreenWithNavigateUpTopBar(
                        titleComposable = {
                            if (paintDreamWorldScreenState.isLoading) {
                                AnimatedContent(
                                    targetState = currentMessage,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(600)) togetherWith 
                                                fadeOut(animationSpec = tween(200))
                                    },
                                    label = "LoadingText"
                                ) { targetText ->
                                    SmoothTypewriterText(
                                        text = targetText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Text(
                                    text = "Paint Dream World",
                                    color = White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        navigateUp = navigateUp,
                        onEvent = { onEvent(PaintDreamWorldEvent.TriggerVibration) },
                        enabledBack = !paintDreamWorldScreenState.isLoading,
                    )
                }
            },
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                    .dynamicBottomNavigationPadding()
                    .fillMaxSize()
            ) {
                AnimatedVisibility(
                    visible = isAnimationFinished.value,
                    enter = fadeIn(animationSpec = tween(1000))
                ) {
                    MainContent(
                        paintDreamWorldScreenState = paintDreamWorldScreenState,
                        onEvent = onEvent,
                        onMainEvent = onMainEvent,
                        onImageClick = onImageClick,
                        animatedVisibilityScope = animatedVisibilityScope,
                        cost = cost,
                        loadingProgress = if (paintDreamWorldScreenState.isLoading) progressAnim.value else 0f
                    )
                }
            }
        }
    }
}

private data class VerticalBiasAlignment(
    val verticalBias: Float
) : Alignment {
    override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset {
        val x = (space.width - size.width) / 2
        val biasFraction = (verticalBias + 1f) / 2f // -1..1 -> 0..1
        val y = ((space.height - size.height) * biasFraction).toInt()
        return IntOffset(x, y)
    }
}

@Composable
fun BackgroundAnimation(
    shouldAnimate: Boolean,
    onAnimationFinished: () -> Unit
) {
    // If shouldAnimate is false, we start at final values immediately.
    val startBias = if (shouldAnimate) 1.19f else -1f
    val startScale = if (shouldAnimate) 1.22f else 1f
    val startBlur = if (shouldAnimate) 20f else 0f

    val cameraBiasY = remember { Animatable(startBias) }
    val cameraScale = remember { Animatable(startScale) }
    val blurAmount = remember { Animatable(startBlur) }

    val shootingStarTrigger = remember { mutableStateOf(0) }

    LaunchedEffect(shouldAnimate) {
        if (shouldAnimate) {
            delay(500)
            blurAmount.animateTo(0f, animationSpec = tween(1000, easing = LinearEasing))
            launch {
                cameraBiasY.animateTo(
                    targetValue = -1f,
                    animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
                )
            }
            launch {
                cameraScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
                )
            }
            delay(2500)
            delay(500)
            shootingStarTrigger.value += 1
            onAnimationFinished()
        } else {
            // Ensure final state if skipping animation
            cameraBiasY.snapTo(-1f)
            cameraScale.snapTo(1f)
            blurAmount.snapTo(0f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.onboarding_long),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = VerticalBiasAlignment(cameraBiasY.value),
            modifier = Modifier
                .fillMaxSize()
                .blur(blurAmount.value.dp)
                .graphicsLayer {
                    scaleX = cameraScale.value
                    scaleY = cameraScale.value
                }
        )

        // Twinkles
        val panProgress = ((cameraBiasY.value + 1f) / 2f).coerceIn(0f, 1f)
        TwinklesLayer(
            twinkleCount = 34,
            panProgress = panProgress,
            driftUpFraction = 1.3f,
            modifier = Modifier.fillMaxSize()
        )

        ShootingStarLayer(
            trigger = shootingStarTrigger.value,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MainContent(
    paintDreamWorldScreenState: PaintDreamWorldScreenState,
    onEvent: (PaintDreamWorldEvent) -> Unit,
    onMainEvent: (MainScreenEvent) -> Unit,
    onImageClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    cost: Int,
    loadingProgress: Float
) {
    if (paintDreamWorldScreenState.paintings.isEmpty() && !paintDreamWorldScreenState.isLoading) {
        EmptyStateContent(onEvent, cost)
    } else {
        ContentState(
            state = paintDreamWorldScreenState,
            onEvent = onEvent,
            onImageClick = onImageClick,
            animatedVisibilityScope = animatedVisibilityScope,
            cost = cost,
            loadingProgress = loadingProgress
        )
    }
}

@Composable
fun EmptyStateContent(onEvent: (PaintDreamWorldEvent) -> Unit, cost: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Visualize your dream world",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Combine themes from your dreams into a unique digital painting.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        DreamToolButton(
            text = if (cost > 0) "Paint New World" else "Paint My First Dream \uD83C\uDF19",
            icon = Res.drawable.baseline_brush_24,
            onClick = {
                onEvent(PaintDreamWorldEvent.TriggerVibration)
                onEvent(PaintDreamWorldEvent.ToggleImageGenerationPopUp(true))
            },
            modifier = Modifier.fillMaxWidth(),
            buttonModifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ContentState(
    state: PaintDreamWorldScreenState,
    onEvent: (PaintDreamWorldEvent) -> Unit,
    onImageClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    cost: Int,
    loadingProgress: Float
) {
    // Key the Pager on the IDs of all paintings.
    // This forces a complete recreation of the Pager whenever the list of paintings changes.
    val pagerKey = state.paintings.joinToString { it.id }

    key(pagerKey) {
        val pagerState = rememberPagerState(
            pageCount = { state.paintings.size }
        )
        val scope = rememberCoroutineScope()

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                if (state.paintings.isNotEmpty() && page in state.paintings.indices) {
                    val painting = state.paintings[page]
                    if (state.selectedPainting?.id != painting.id) {
                        onEvent(PaintDreamWorldEvent.SelectPainting(painting))
                    }
                }
            }
        }

        LaunchedEffect(state.selectedPainting) {
            state.selectedPainting?.let { selected ->
                val index = state.paintings.indexOfFirst { it.id == selected.id }
                if (index >= 0) {
                     if (pagerState.currentPage != index) {
                         // Only animate if reasonable distance, else snap? 
                         // For now always animate for smoothness, unless key change happened which handles 0 case.
                         pagerState.animateScrollToPage(index)
                     }
                }
            }
        }

        Column {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                pageSpacing = 16.dp,
                // Removed key={...} from here as we are keying the whole pager
            ) { page ->
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 10000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                if (page < state.paintings.size) {
                    val painting = state.paintings[page]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val pageOffset =
                                    ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

                                val horizontalScale = lerp(
                                    start = 0.85f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleX = horizontalScale
                                scaleY = horizontalScale
                                
                                alpha = lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightBlack.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 60.dp), // Extra padding for arrows
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalPlatformContext.current)
                                        .data(painting.imageUrl)
                                        .placeholderMemoryCacheKey("image/${painting.imageUrl}")
                                        .memoryCacheKey("image/${painting.imageUrl}")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Dream World Painting",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onImageClick(painting.imageUrl) }
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                        .sharedElement(
                                            rememberSharedContentState(key = "image/${painting.imageUrl}"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            boundsTransform = { _, _ -> tween(500) }
                                        ),
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = {
                                        onEvent(PaintDreamWorldEvent.TriggerVibration)
                                        onEvent(PaintDreamWorldEvent.SetPaintingToDelete(painting))
                                        onEvent(PaintDreamWorldEvent.ToggleDeleteConfirmation(true))
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = RedOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Text(
                                text = painting.date,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f),
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 0.dp)
                            )

                            Text(
                                text = painting.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        if (page > 0) {
                            IconButton(
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(page - 1) }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous",
                                    tint = Color.White
                                )
                            }
                        }

                        if (page < state.paintings.size - 1) {
                            IconButton(
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(page + 1) }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowRight,
                                    contentDescription = "Next",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isLoading) {
                    LoadingProgressBar(
                        progress = loadingProgress,
                        brush = Brush.horizontalGradient(
                            colors = listOf(RedOrange, SkyBlue)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp)
                    )
                } else {
                    DreamToolButton(
                        text = if (cost > 0) "Paint New World" else "Paint My First Dream \uD83C\uDF19",
                        icon = Res.drawable.baseline_brush_24,
                        onClick = {
                            onEvent(PaintDreamWorldEvent.TriggerVibration)
                            onEvent(PaintDreamWorldEvent.ToggleImageGenerationPopUp(true))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        buttonModifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingProgressBar(
    progress: Float,
    brush: Brush,
    modifier: Modifier = Modifier,
) {
    val pct = (progress * 100).coerceIn(0f, 100f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(16.dp))
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
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 4.dp, vertical = 4.dp), // Thinner padding
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(12.dp))
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
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = White,
    textAlign: TextAlign = TextAlign.Start
) {
    val progress = remember { Animatable(0f) }
    
    // Reset when text changes
    LaunchedEffect(text) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = text.length.toFloat(),
            animationSpec = tween(
                durationMillis = text.length * 50, // Speed control
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
        style = style,
        fontWeight = fontWeight,
        textAlign = textAlign
    )
}

fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}
