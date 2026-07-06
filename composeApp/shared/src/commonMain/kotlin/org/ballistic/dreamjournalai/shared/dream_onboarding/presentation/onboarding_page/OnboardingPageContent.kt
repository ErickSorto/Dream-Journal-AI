package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.app_name
import dreamjournalai.composeapp.shared.generated.resources.dream_onboarding_design
import dreamjournalai.composeapp.shared.generated.resources.dream_journal_empty_background
import dreamjournalai.composeapp.shared.generated.resources.onboarding_overlay_cloud_frame_vector
import dreamjournalai.composeapp.shared.generated.resources.onboarding_overlay_moon_vector
import dreamjournalai.composeapp.shared.generated.resources.onboarding_overlay_sparkle_trails_vector
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.ShootingStarLayer
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.TwinklesLayer
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BoxScope.OnboardingPageBackground(
    cameraScale: Float,
    cameraBiasY: Float,
    panProgress: Float,
    shootingStarTrigger: Int,
    overlayAlpha: Float = 1f,
    modifier: Modifier = Modifier,
) {
    val ambientFloat by rememberInfiniteTransition(label = "onboarding_ambient_float").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding_ambient_float_value"
    )
    val haloPulse by rememberInfiniteTransition(label = "onboarding_halo_pulse").animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding_halo_pulse_value"
    )

    Image(
        painter = painterResource(Res.drawable.dream_journal_empty_background),
        modifier = modifier
            .graphicsLayer(
                scaleX = cameraScale,
                scaleY = cameraScale,
                transformOrigin = TransformOrigin(0.5f, 1f)
            )
            .zIndex(0f),
        contentScale = ContentScale.Crop,
        alignment = VerticalBiasAlignment(cameraBiasY),
        contentDescription = stringResource(Res.string.app_name),
    )

    Image(
        painter = painterResource(Res.drawable.onboarding_overlay_cloud_frame_vector),
        contentDescription = null,
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer(
                alpha = 0.28f * overlayAlpha,
                translationY = (-22f + 44f * ambientFloat),
                scaleX = 1.02f,
                scaleY = 1.02f
            )
            .zIndex(0.02f),
        contentScale = ContentScale.Crop
    )

    TwinklesLayer(
        twinkleCount = 34,
        panProgress = panProgress,
        driftUpFraction = 1.3f,
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer(alpha = overlayAlpha)
            .zIndex(0.05f)
    )

    Image(
        painter = painterResource(Res.drawable.onboarding_overlay_sparkle_trails_vector),
        contentDescription = null,
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer(
                alpha = 0.20f * overlayAlpha,
                translationY = 16f - 36f * ambientFloat
            )
            .zIndex(0.07f),
        contentScale = ContentScale.Crop
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(0.08f)
    ) {
        Image(
            painter = painterResource(Res.drawable.onboarding_overlay_moon_vector),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(230.dp)
                .offset(x = 22.dp, y = 48.dp)
                .graphicsLayer(
                    alpha = 0.42f * overlayAlpha,
                    scaleX = haloPulse,
                    scaleY = haloPulse,
                    translationY = -18f + 28f * ambientFloat
                ),
            contentScale = ContentScale.Fit
        )
    }

    ShootingStarLayer(
        trigger = shootingStarTrigger,
        starColor = androidx.compose.ui.graphics.Color(0xFFB49CFF),
        trailMaxAlpha = 0.75f,
        trailCount = 28,
        trailStep = 0.028f,
        durationMs = 1250,
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer(alpha = overlayAlpha)
            .zIndex(0.1f)
    )
}

@Composable
fun OnboardingPageHeroImage(
    visible: Boolean,
    drawableRes: DrawableResource = Res.drawable.dream_onboarding_design,
    modifier: Modifier = Modifier,
    onEnteredChanged: (Boolean) -> Unit,
) {
    val onboardingScale = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            onboardingScale.snapTo(0f)
            kotlinx.coroutines.delay(180)
            onboardingScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessVeryLow
                )
            )
            onEnteredChanged(true)
        } else {
            onboardingScale.snapTo(0f)
            onEnteredChanged(false)
        }
    }

    val floatTransition = rememberInfiniteTransition(label = "onboarding_float")
    val floatOffset by floatTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(214.dp)
            .scale(onboardingScale.value)
            .offset(y = floatOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = stringResource(Res.string.app_name),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

private data class VerticalBiasAlignment(
    val verticalBias: Float
) : Alignment {
    override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset {
        val x = (space.width - size.width) / 2
        val biasFraction = (verticalBias + 1f) / 2f
        val y = ((space.height - size.height) * biasFraction).toInt()
        return IntOffset(x, y)
    }
}
