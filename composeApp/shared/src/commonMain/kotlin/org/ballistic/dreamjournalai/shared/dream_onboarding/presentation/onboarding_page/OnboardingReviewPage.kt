package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack

private data class ReviewHighlight(
    val title: String,
    val body: String,
)

@Composable
fun OnboardingReviewPage(
    userName: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val introName = userName.takeIf { it.isNotBlank() }?.let { "$it, " }.orEmpty()
    val highlights = listOf(
        ReviewHighlight(
            title = "Beautiful from the start",
            body = "A calm space that makes dream journaling feel special."
        ),
        ReviewHighlight(
            title = "Actually useful",
            body = "Quick capture, interpretation, and insights in one place."
        ),
        ReviewHighlight(
            title = "Review-ready",
            body = "Set expectations for a five-star moment after first use."
        )
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = LightBlack.copy(alpha = 0.60f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OnboardingProgressTracker(
                currentStep = 3,
                totalSteps = 4,
                title = "Loved already"
            )

            Text(
                text = "${introName}people love apps that feel this polished.",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "When DreamNorth earns it, we want your five-star review too.",
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.84f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) {
                    Text(
                        text = "★",
                        style = TextStyle(
                            color = Color(0xFFFFD089),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = "Your future rating",
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            highlights.forEach { review ->
                ReviewHighlightCard(review = review)
            }

            OnboardingPrimaryButton(
                text = "I can see the 5 stars",
                onClick = onContinue
            )
        }
    }
}

@Composable
private fun ReviewHighlightCard(
    review: ReviewHighlight,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.06f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = review.title,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Text(
                text = review.body,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            )
        }
    }
}
