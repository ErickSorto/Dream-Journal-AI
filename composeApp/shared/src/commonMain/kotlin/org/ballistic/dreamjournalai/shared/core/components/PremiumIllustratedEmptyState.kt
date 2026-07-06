package org.ballistic.dreamjournalai.shared.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun PremiumIllustratedEmptyState(
    image: DrawableResource,
    eyebrow: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF120F2A).copy(alpha = 0.58f),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(248.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x44FFD2A2),
                                Color(0x2E8AB2FF),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(image),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .height(220.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0x26FFD1A8),
                                Color(0x18FFFFFF)
                            )
                        ),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = eyebrow,
                    style = TextStyle(
                        color = Color(0xFFFFDFC2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Text(
                text = title,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
            )

            Text(
                text = body,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
