package org.ballistic.dreamjournalai.shared.dream_tools.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.back_button_content_description
import dreamjournalai.composeapp.shared.generated.resources.baseline_report_24
import dreamjournalai.composeapp.shared.generated.resources.dismiss
import dreamjournalai.composeapp.shared.generated.resources.flag
import dreamjournalai.composeapp.shared.generated.resources.flag_content
import dreamjournalai.composeapp.shared.generated.resources.flag_content_message
import dreamjournalai.composeapp.shared.generated.resources.full_screen_image
import dreamjournalai.composeapp.shared.generated.resources.ic_baseline_arrow_left_alt_24
import dreamjournalai.composeapp.shared.generated.resources.image_flagged_successfully
import dreamjournalai.composeapp.shared.generated.resources.onboarding_long
import dreamjournalai.composeapp.shared.generated.resources.report
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.ballistic.dreamjournalai.shared.BottomNavigationController
import org.ballistic.dreamjournalai.shared.BottomNavigationEvent
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.components.ActionBottomSheet
import org.ballistic.dreamjournalai.shared.core.util.BackHandler
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.dream_fullscreen.FullScreenEvent
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.TwinklesLayer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.ToolFullScreenImageScreen(
    imageID: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onFullScreenEvent: (FullScreenEvent) -> Unit = {},
    onBackPress: () -> Unit
) {
    BackHandler(true) { onBackPress() }

    val backButtonEnabled = remember { mutableStateOf(true) }
    val flagContentBottomSheetState = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        BottomNavigationController.sendEvent(BottomNavigationEvent.SetVisibility(false))
    }

    // Compute top bar height exactly like AddEditDreamScreen
    val rawStatusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val minTopBar = 56.dp
    val maxTopBar = 80.dp
    val initialTopBar = (rawStatusBarTop + 24.dp).coerceIn(minTopBar, maxTopBar)
    var topBarHeight by remember { mutableStateOf(initialTopBar) }
    var layoutReady by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        val observedInset = withTimeoutOrNull(500L) {
            snapshotFlow { rawStatusBarTop }
                .filter { it >= 0.dp }
                .first()
        } ?: rawStatusBarTop
        val recomputed = (observedInset + 32.dp).coerceIn(minTopBar, maxTopBar)
        topBarHeight = recomputed
        layoutReady = true
    }

    // Static background state (Final state of PaintDreamWorldScreen animation)
    val cameraBiasY = -1f
    val cameraScale = 1f
    
    // Alpha for the black overlay (starts transparent, animates to opaque)
    val overlayAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // Animate to full black over 500ms to match the shared element transition time
        overlayAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(500)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Galaxy Background (matches PaintDreamWorldScreen)
        Image(
            painter = painterResource(Res.drawable.onboarding_long),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = VerticalBiasAlignment(cameraBiasY),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = cameraScale
                    scaleY = cameraScale
                }
        )

        // Twinkles (matches PaintDreamWorldScreen)
        TwinklesLayer(
            twinkleCount = 34,
            panProgress = ((cameraBiasY + 1f) / 2f).coerceIn(0f, 1f),
            driftUpFraction = 1.3f,
            modifier = Modifier.fillMaxSize()
        )

        // Black Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .graphicsLayer {
                    alpha = overlayAlpha.value
                }
        )

        Scaffold(
            snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) },
            containerColor = Color.Transparent,
            topBar = {
                if (layoutReady) {
                    CenterAlignedTopAppBar(
                        modifier = Modifier
                            .height(topBarHeight)
                            .graphicsLayer { alpha = 1f },
                        title = {
                            Box(modifier = Modifier.height(topBarHeight)) {
                                Text(
                                    text = "",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    onBackPress()
                                    backButtonEnabled.value = false
                                    // Do NOT set bottom bar to true here, let destination screen handle it if needed
                                },
                                enabled = backButtonEnabled.value,
                            ) {
                                Icon(
                                    painterResource(Res.drawable.ic_baseline_arrow_left_alt_24),
                                    contentDescription = stringResource(Res.string.back_button_content_description),
                                    tint = Color.White,
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { flagContentBottomSheetState.value = true }
                            ) {
                                Icon(
                                    painterResource(Res.drawable.baseline_report_24),
                                    contentDescription = stringResource(Res.string.report),
                                    tint = Color.White,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = Color.White,
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                    )
                }
            }
        ) { innerPadding ->
            // Background behind the app bar and content
            if (flagContentBottomSheetState.value) {
                ActionBottomSheet(
                    title = stringResource(Res.string.flag_content),
                    message = stringResource(Res.string.flag_content_message),
                    buttonText = stringResource(Res.string.flag),
                    onClick = {
                        onFullScreenEvent(
                            FullScreenEvent.Flag(
                                imageID,
                                onSuccessEvent = {
                                    scope.launch {
                                        SnackbarController.sendEvent(
                                            SnackbarEvent(
                                                message = StringValue.Resource(Res.string.image_flagged_successfully),
                                                action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {})
                                            )
                                        )
                                    }

                                }
                            )
                        )
                        flagContentBottomSheetState.value = false
                    },
                    onClickOutside = { flagContentBottomSheetState.value = false }
                )
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Fullscreen image (shared element).
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(imageID)
                        .crossfade(true)
                        .placeholderMemoryCacheKey("image/$imageID")
                        .memoryCacheKey("image/$imageID")
                        .build(),
                    contentDescription = stringResource(Res.string.full_screen_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .sharedElement(
                            rememberSharedContentState(key = "image/$imageID"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> tween(500) }
                        ),
                    contentScale = ContentScale.Crop
                )
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
