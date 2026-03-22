package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingProgressTracker(
    currentStep: Int,
    totalSteps: Int,
    title: String,
    modifier: Modifier = Modifier,
) {
    val progressTarget = (currentStep.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "onboarding_progress"
    )
    val shimmerAlpha by rememberInfiniteTransition(label = "onboarding_progress_shimmer").animateFloat(
        initialValue = 0.60f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding_progress_shimmer_alpha"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f))
                        .padding(5.dp)
                )
                Text(
                    text = title,
                    style = TextStyle(
                        color = Color(0xFFFFE7D3),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Text(
                text = "$currentStep / $totalSteps",
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.84f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.14f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFFB07C),
                                Color(0xFFFF8FB8),
                                Color(0xFFB7A1FF)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0f),
                                Color.White.copy(alpha = 0.16f * shimmerAlpha),
                                Color.White.copy(alpha = 0f)
                            )
                        )
                    )
            )
        }
    }
}
