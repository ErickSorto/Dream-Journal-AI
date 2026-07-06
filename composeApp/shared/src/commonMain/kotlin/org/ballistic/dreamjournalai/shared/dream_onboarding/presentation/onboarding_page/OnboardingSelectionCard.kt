package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class OnboardingInterestOption(
    val id: String,
    val title: String,
    val description: String,
    val stat: String,
    val icon: DrawableResource,
)

@Composable
fun OnboardingSelectionCard(
    userName: String,
    selectedOptionId: String?,
    options: List<OnboardingInterestOption>,
    onSelect: (OnboardingInterestOption) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val continueEnabled = selectedOptionId != null

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
                currentStep = 2,
                totalSteps = 4,
                title = "Pick one"
            )

            Text(
                text = if (userName.isBlank()) {
                    "What are you excited about most in this app?"
                } else {
                    "$userName, what are you excited about most in this app?"
                },
                style = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "Choose one to personalize your start.",
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            )

            options.forEachIndexed { index, option ->
                SelectionOptionRow(
                    index = index + 1,
                    option = option,
                    selected = selectedOptionId == option.id,
                    onClick = { onSelect(option) }
                )
            }

            OnboardingPrimaryButton(
                text = if (continueEnabled) "Continue" else "Choose a section",
                onClick = onContinue,
                enabled = continueEnabled
            )
        }
    }
}

@Composable
fun SelectedInterestPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color(0x33FFC18D),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = TextStyle(
                color = Color(0xFFFFE0C7),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun SelectionOptionRow(
    index: Int,
    option: OnboardingInterestOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) Color(0x26FFBA93) else Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            if (selected) Color(0x88FFD3AE) else Color.White.copy(alpha = 0.16f)
        )
    ) {
        Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PremiumOptionIcon(
                option = option,
                index = index,
                selected = selected
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (selected) Color(0x33FFD0A7) else Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = index.toString().padStart(2, '0'),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = TextStyle(
                                color = Color(0xFFFFE2C6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Text(
                        text = option.title,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = option.description,
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.74f),
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = option.stat,
                    style = TextStyle(
                        color = Color(0xFFFFD8B7),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun PremiumOptionIcon(
    option: OnboardingInterestOption,
    index: Int,
    selected: Boolean,
) {
    val tilt = if (index % 2 == 0) -5f else 5f

    Box(
        modifier = Modifier.size(92.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(116.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            if (selected) Color(0x55FFC08F) else Color(0x36FFC08F),
                            Color(0x32D1BEFF),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
        )

        Surface(
            modifier = Modifier
                .size(108.dp)
                .offset(y = (-4).dp)
                .graphicsLayer(rotationZ = tilt),
            color = Color.White.copy(alpha = if (selected) 0.14f else 0.10f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                1.dp,
                if (selected) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.18f)
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.14f),
                                Color.Transparent,
                                Color(0x22B8A5FF)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0x40FFD6AE),
                                    Color(0x24C4B2FF),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                )
            }
        }

        Image(
            painter = painterResource(option.icon),
            contentDescription = option.title,
            modifier = Modifier
                .align(Alignment.Center)
                .size(148.dp)
                .offset(y = (-4).dp),
            contentScale = ContentScale.Fit
        )

        PremiumSparkle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-2).dp, y = 6.dp),
            size = 10.dp,
            alpha = if (selected) 0.92f else 0.72f
        )
        PremiumSparkle(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 6.dp, y = (-2).dp),
            size = 7.dp,
            alpha = if (selected) 0.76f else 0.56f
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 10.dp, y = 14.dp)
                .size(6.dp)
                .background(Color(0xFFFFD8B7).copy(alpha = if (selected) 0.88f else 0.58f), CircleShape)
        )
    }
}

@Composable
private fun PremiumSparkle(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp,
    alpha: Float,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(Color(0xFFFFE5C8).copy(alpha = alpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(width = size * 0.28f, height = size * 1.25f)
                .background(Color.White.copy(alpha = alpha), RoundedCornerShape(999.dp))
        )
        Box(
            modifier = Modifier
                .size(width = size * 1.25f, height = size * 0.28f)
                .background(Color.White.copy(alpha = alpha), RoundedCornerShape(999.dp))
        )
    }
}
