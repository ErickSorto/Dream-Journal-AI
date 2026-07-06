package org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.*
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.viewmodel.StoreScreenViewModelState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime


@Composable
fun CustomButtonLayout(
    storeScreenViewModelState: StoreScreenViewModelState,
    buy500IsClicked: () -> Unit,
    buy100IsClicked: () -> Unit
) {
    val lastClickTime = remember { mutableLongStateOf(0L) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DreamToken500ButtonBuy(
            storeScreenViewModelState = storeScreenViewModelState,
            modifier = Modifier.fillMaxWidth(),
            buy500IsClicked = singleClick(lastClickTime) { buy500IsClicked() }
        )
        DreamToken100ButtonBuy(
            storeScreenViewModelState = storeScreenViewModelState,
            modifier = Modifier.fillMaxWidth(),
            buy100IsClicked = singleClick(lastClickTime) { buy100IsClicked() }
        )
    }
}

@Composable
fun DreamToken500ButtonBuy(
    storeScreenViewModelState: StoreScreenViewModelState,
    modifier: Modifier,
    buy500IsClicked: () -> Unit = {}
) {
    StoreTokenOfferCard(
        modifier = modifier
            .height(154.dp),
        highlighted = true,
        enabled = !storeScreenViewModelState.isBillingClientLoading
    ) { buy500IsClicked() }
}

@Composable
fun DreamToken100ButtonBuy(
    storeScreenViewModelState: StoreScreenViewModelState,
    modifier: Modifier,
    buy100IsClicked: () -> Unit
) {
    StoreTokenOfferCard(
        modifier = modifier
            .height(112.dp),
        highlighted = false,
        enabled = !storeScreenViewModelState.isBillingClientLoading
    ) { buy100IsClicked() }
}

@Composable
private fun StoreTokenOfferCard(
    modifier: Modifier,
    highlighted: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val containerBrush = if (highlighted) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFA91B78).copy(alpha = 0.94f),
                Color(0xFF4D1766).copy(alpha = 0.94f),
                Color(0xFFB95F34).copy(alpha = 0.88f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF21105F).copy(alpha = 0.92f),
                Color(0xFF100431).copy(alpha = 0.97f),
                Color(0xFF1E0B4F).copy(alpha = 0.92f)
            )
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerBrush)
            .border(
                BorderStroke(
                    if (highlighted) 2.dp else 1.dp,
                    if (highlighted) Color(0xFFFF9C55) else Color(0xFF8B68FF)
                ),
                shape
            )
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (highlighted) Color(0xFFFFB35E).copy(alpha = 0.50f) else Color(0xFF9C83FF).copy(alpha = 0.40f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(80f, 40f),
                        radius = 360f
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (highlighted) Color(0xFFFF6D5F).copy(alpha = 0.40f) else Color(0xFF5F64FF).copy(alpha = 0.34f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(260f, 105f),
                        radius = if (highlighted) 420f else 340f
                    )
                )
        )
        if (highlighted) {
            MostPopularBanner(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 18.dp, top = 13.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(if (highlighted) Alignment.BottomCenter else Alignment.Center)
                .padding(
                    start = if (highlighted) 8.dp else 10.dp,
                    end = 24.dp,
                    top = if (highlighted) 12.dp else 0.dp,
                    bottom = if (highlighted) 22.dp else 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (highlighted) Color(0xFFFFE18A).copy(alpha = 0.68f) else Color(0xFFD8D0FF).copy(alpha = 0.52f),
                                if (highlighted) Color(0xFFFF6E61).copy(alpha = 0.42f) else Color(0xFF7559FF).copy(alpha = 0.38f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    if (highlighted) Color(0xFFFFD070).copy(alpha = 0.95f) else Color(0xFFB9AAFF).copy(alpha = 0.86f),
                                    if (highlighted) Color(0xFFFF5C66).copy(alpha = 0.48f) else Color(0xFF6B47F5).copy(alpha = 0.58f),
                                    Color(0xFF2B1056).copy(alpha = 0.62f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            if (highlighted) 2.dp else 1.dp,
                            if (highlighted) Color(0xFFFFD072).copy(alpha = 0.90f) else Color(0xFFB9A8FF).copy(alpha = 0.76f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.dream_token),
                        contentDescription = stringResource(Res.string.dream_token_content_description_text),
                        modifier = Modifier.size(62.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (highlighted) "500" else "100",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = Bold,
                    lineHeight = 29.sp,
                    maxLines = 1
                )
                Text(
                    text = "Dream Tokens",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    lineHeight = 18.sp,
                    modifier = Modifier.offset(y = if (highlighted) (-10).dp else (-6).dp),
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (highlighted) {
                    Text(
                        text = stringResource(Res.string.price_14_99),
                        color = Color.White.copy(alpha = 0.58f),
                        fontSize = 19.sp,
                        fontWeight = Bold,
                        maxLines = 1,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
                Text(
                    text = stringResource(if (highlighted) Res.string.price_4_99 else Res.string.price_2_99),
                    style = TextStyle(
                        brush = Brush.verticalGradient(
                            if (highlighted) {
                                listOf(Color.White, Color(0xFFFFE08B), Color(0xFFFF8E58))
                            } else {
                                listOf(Color.White, Color(0xFFD8D0FF), Color(0xFFB18CFF))
                            }
                        ),
                        fontSize = 35.sp,
                        fontWeight = Bold
                    ),
                    fontSize = 35.sp,
                    fontWeight = Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MostPopularBanner(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // Slowing down the shimmer to make it less frequent
    val shinePosition by infiniteTransition.animateFloat(
        initialValue = 0f, // Adjusted to start from the beginning
        targetValue = 1f, // Adjusted to move fully across
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing), // Slower duration
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    // Defining a more vibrant red-orange gradient background
    val backgroundBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF755C).copy(alpha = 0.95f),
            Color(0xFFE21A7A).copy(alpha = 0.95f)
        )
    )

    // Adjusting the shimmer effect to be more focused and visible
    val shimmerBrush = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.3f), // Adjusted alpha for visibility
            Color.Transparent
        ),
        startX = lerp(fraction = shinePosition - 0.1f) * scale,
        endX = lerp(fraction = shinePosition) * scale
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(brush = backgroundBrush)
            .background(brush = shimmerBrush)
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 11.dp, vertical = 6.dp)
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            }
            Text(
                text = stringResource(Res.string.most_popular_save_66).replace("(", "• ").replace(")", ""),
                color = Color.White,
                style = typography.labelLarge,
                fontWeight = Bold,
                fontSize = 12.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// Helper function for linear interpolation
private fun lerp(fraction: Float): Float {
    return (1 - fraction) * -1000f + fraction * 1000f
}

@OptIn(ExperimentalTime::class)
@Composable
fun singleClick(
    lastClickTimeState: MutableState<Long>,
    onClick: () -> Unit
): () -> Unit {
    return {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        if (now - lastClickTimeState.value >= 300) {
            onClick()
            lastClickTimeState.value = now
        }
    }
}
