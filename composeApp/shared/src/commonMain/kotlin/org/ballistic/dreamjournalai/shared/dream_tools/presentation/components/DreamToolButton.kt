package org.ballistic.dreamjournalai.shared.dream_tools.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun DreamToolButton(
    text: String,
    icon: DrawableResource? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    isGlowVisible: Boolean = true,
) {
    // Glow Animation
    val infiniteTransition = rememberInfiniteTransition(label = "Glow Animation")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow Alpha"
    )

    val visibilityAlpha by animateFloatAsState(
        targetValue = if (isGlowVisible) 1f else 0f,
        animationSpec = tween(1000)
    )

    // The root box enforces the layout height of 56.dp
    Box(
        modifier = modifier.height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glow Effect Layer
        BoxWithConstraints(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.Center
        ) {
            val boxWidth = maxWidth

            if (visibilityAlpha > 0f) {
                // Outer Glow Box (Blurred)
                Box(
                    modifier = Modifier
                        .requiredWidth(boxWidth + 150.dp)
                        .requiredHeight(160.dp)
                        .graphicsLayer { alpha = visibilityAlpha }
                        .blur(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner Gradient Box
                    Box(
                        modifier = Modifier
                            .requiredWidth(boxWidth - 24.dp)
                            .requiredHeight(60.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = glowAlpha),
                                        Color(0xFFE040FB).copy(alpha = glowAlpha), // Purple
                                        Color.White.copy(alpha = glowAlpha)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                }
            }
        }

        // The Actual Button
        Button(
            onClick = onClick,
            modifier = buttonModifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF261341),
                                Color(0xFF391D61),
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Image(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            colorFilter = ColorFilter.tint(Color.White),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = text,
                        modifier = Modifier
                            .weight(1f),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    if (icon != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Image(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            colorFilter = ColorFilter.tint(Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
