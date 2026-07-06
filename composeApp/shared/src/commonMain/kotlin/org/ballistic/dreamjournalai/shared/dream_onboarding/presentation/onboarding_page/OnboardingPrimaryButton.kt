package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDisabledClick: (() -> Unit)? = null,
) {
    val transition = rememberInfiniteTransition(label = "onboarding_primary_button")
    val glowAlpha = transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding_primary_glow"
    )
    val shimmerOffset = transition.animateFloat(
        initialValue = -220f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "onboarding_primary_shimmer"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(alpha = if (enabled) glowAlpha.value else 0.12f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x66FFC493),
                            Color(0x44FF8FC0),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = if (enabled) {
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFFB07C),
                                    Color(0xFFFF8FB8),
                                    Color(0xFFB7A1FF)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.10f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (enabled) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (enabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer(alpha = 0.44f)
                            .offset(x = shimmerOffset.value.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.16f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Text(
                    text = text,
                    modifier = Modifier.padding(horizontal = 18.dp),
                    style = TextStyle(
                        color = if (enabled) Color.White else Color.White.copy(alpha = 0.56f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        if (!enabled && onDisabledClick != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onDisabledClick)
            )
        }
    }
}
