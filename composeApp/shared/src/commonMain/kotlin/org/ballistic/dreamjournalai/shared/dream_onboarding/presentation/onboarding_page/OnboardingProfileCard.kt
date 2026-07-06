package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.onboarding_profile_welcome_generated
import dreamjournalai.composeapp.shared.generated.resources.onboarding_overlay_moon_vector
import dreamjournalai.composeapp.shared.generated.resources.onboarding_overlay_sparkle_trails_vector
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.jetbrains.compose.resources.painterResource

@Composable
fun OnboardingProfileCard(
    name: String,
    selectedAgeRange: String?,
    ageRanges: List<String>,
    onNameChange: (String) -> Unit,
    onAgeRangeSelect: (String) -> Unit,
    onContinue: () -> Unit,
    startAnimation: Boolean,
    onHeroCenterChanged: (Offset) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val cardScale = remember { Animatable(0.88f) }
    val cardAlpha = remember { Animatable(0f) }
    val headlineVisible = remember { mutableStateOf(false) }
    val nameFieldVisible = remember { mutableStateOf(false) }
    val ageSectionVisible = remember { mutableStateOf(false) }
    val buttonVisible = remember { mutableStateOf(false) }

    LaunchedEffect(startAnimation) {
        if (!startAnimation) return@LaunchedEffect
        coroutineScope {
            this.launch {
                cardAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                )
            }
            this.launch {
                cardScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessVeryLow
                    )
                )
            }
            delay(760)
            headlineVisible.value = true
            delay(300)
            nameFieldVisible.value = true
            delay(240)
            ageSectionVisible.value = true
            delay(260)
            buttonVisible.value = true
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing))
            .graphicsLayer(
                alpha = cardAlpha.value,
                scaleX = cardScale.value,
                scaleY = cardScale.value,
                transformOrigin = TransformOrigin(0.5f, 0.08f)
            ),
        color = LightBlack.copy(alpha = 0.62f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingProgressTracker(
                currentStep = 1,
                totalSteps = 4,
                title = "Your profile"
            )

            var heroBounds by remember { mutableStateOf<Rect?>(null) }

            val revealProgress by animateFloatAsState(
                targetValue = if (startAnimation) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = 0.64f,
                    stiffness = 360f
                ),
                label = "hero_reveal"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(228.dp)
                    .onGloballyPositioned { coords ->
                        val bounds = coords.boundsInRoot()
                        heroBounds = bounds
                        onHeroCenterChanged(bounds.center)
                    }
                    .graphicsLayer {
                        val bounds = heroBounds
                        val finalCenter = bounds?.center
                        val origin = finalCenter

                        if (bounds != null && finalCenter != null && origin != null) {
                            translationX = (origin.x - finalCenter.x) * (1f - revealProgress)
                            translationY = (origin.y - finalCenter.y) * (1f - revealProgress)
                        }

                        val scale = 0.06f + 0.94f * revealProgress
                        scaleX = scale
                        scaleY = scale
                        alpha = revealProgress.coerceIn(0f, 1f)
                    }
            ) {
                WelcomeHeroArtwork()
            }

            AnimatedVisibility(
                visible = headlineVisible.value,
                enter = fadeIn(animationSpec = tween(1000, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.96f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "A softer way to begin",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 23.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = "Set your profile, then let DreamNorth shape the rest.",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = nameFieldVisible.value,
                enter = fadeIn(animationSpec = tween(1000, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.97f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    ) +
                    slideInVertically(
                        initialOffsetY = { it / 6 },
                        animationSpec = tween(1000, easing = FastOutSlowInEasing)
                    )
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "First name",
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
                        focusedBorderColor = Color(0xFFFFC48F),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                        focusedContainerColor = Color.White.copy(alpha = 0.06f),
                        cursorColor = Color(0xFFFFC48F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFFFD8B7),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.62f)
                    )
                )
            }

            AnimatedVisibility(
                visible = ageSectionVisible.value,
                enter = fadeIn(animationSpec = tween(1000, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.97f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    ) +
                    slideInVertically(
                        initialOffsetY = { it / 7 },
                        animationSpec = tween(1000, easing = FastOutSlowInEasing)
                    )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Age range",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.86f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    ageRanges.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { ageRange ->
                                AgeRangeChip(
                                    text = ageRange,
                                    selected = selectedAgeRange == ageRange,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onAgeRangeSelect(ageRange) }
                                )
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = buttonVisible.value,
                enter = fadeIn(animationSpec = tween(1000, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.94f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    ) +
                    slideInVertically(
                        initialOffsetY = { it / 8 },
                        animationSpec = tween(1000, easing = FastOutSlowInEasing)
                    )
            ) {
                OnboardingPrimaryButton(
                    text = "Next",
                    onClick = onContinue
                )
            }
        }
    }
}

@Composable
private fun WelcomeHeroArtwork() {
    val transition = rememberInfiniteTransition(label = "profile_welcome_art")
    val floatOffset = transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "profile_welcome_float"
    )
    val moonPulse = transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "profile_moon_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(228.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color(0xFF33255F).copy(alpha = 0.56f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 18.dp)
                .size(8.dp)
                .background(Color(0xFFFFD8B7).copy(alpha = 0.72f), CircleShape)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0x44FFC18D),
                            Color(0x22B9AAFF),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        )

        Image(
            painter = painterResource(Res.drawable.onboarding_overlay_sparkle_trails_vector),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(alpha = 0.34f),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(Res.drawable.onboarding_overlay_moon_vector),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(116.dp)
                .padding(end = 18.dp)
                .graphicsLayer(
                    translationY = -14f,
                    alpha = 0.62f,
                    scaleX = moonPulse.value,
                    scaleY = moonPulse.value
                ),
            contentScale = ContentScale.Fit
        )

        Image(
            painter = painterResource(Res.drawable.onboarding_profile_welcome_generated),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    translationY = 34f + (floatOffset.value * 0.9f)
                ),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent,
                            Color(0xFF130D2D).copy(alpha = 0.24f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun AgeRangeChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) Color(0x22FFB07C) else Color.Transparent,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            if (selected) Color(0x66FFD3AE) else Color.White.copy(alpha = 0.10f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
    }
}