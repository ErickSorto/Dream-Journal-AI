package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack

@Composable
fun OnboardingLoadingBanner(
    text: String,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "onboarding_loading_banner")
    val shimmer by transition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "onboarding_loading_banner_progress"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding_loading_banner_pulse"
    )
    val floatY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding_loading_banner_float"
    )

    Surface(
        modifier = modifier,
        color = LightBlack.copy(alpha = 0.68f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = floatY.dp)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0x66FFD1A8),
                                    Color(0x55C5B2FF),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                        .padding(10.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer(
                                scaleX = pulse,
                                scaleY = pulse
                            )
                            .width(12.dp)
                            .height(12.dp)
                            .background(Color.White.copy(alpha = 0.92f), CircleShape)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = text,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Opening dream doors...",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.76f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val travel = maxWidth * 0.72f
                val density = LocalDensity.current
                val travelPx = with(density) { travel.toPx() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                )

                Box(
                    modifier = Modifier
                        .width(maxWidth * 0.28f)
                        .height(8.dp)
                        .graphicsLayer {
                            translationX = travelPx * shimmer
                        }
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFFB07C),
                                    Color(0xFFFFD0A8),
                                    Color(0xFFB7A1FF)
                                )
                            ),
                            shape = RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    }
}
