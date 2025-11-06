package org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.components


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.background_during_day
import dreamjournalai.composeapp.shared.generated.resources.baseline_cached_24
import dreamjournalai.composeapp.shared.generated.resources.baseline_star_24
import dreamjournalai.composeapp.shared.generated.resources.false_awakening_icon
import dreamjournalai.composeapp.shared.generated.resources.lighthouse_vector
import dreamjournalai.composeapp.shared.generated.resources.nightmare_ghost_closed
import dreamjournalai.composeapp.shared.generated.resources.nightmare_ghost_open
import kotlinx.coroutines.delay
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream.Companion.dreamBackgroundImages
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.BrighterWhite
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.Green
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.Purple
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.RedOrange
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.SkyBlue
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.Yellow
import org.jetbrains.compose.resources.painterResource
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.launch
import androidx.compose.runtime.key

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DreamItem(
    modifier: Modifier = Modifier,
    dream: Dream,
    hasBorder: Boolean = false,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val imageResId =
        if (dream.backgroundImage >= 0 && dream.backgroundImage < dreamBackgroundImages.size) {
            dreamBackgroundImages[dream.backgroundImage]
        } else {
            Res.drawable.background_during_day
        }

    // Only run border/shimmer animations if hasBorder is true to avoid unnecessary redraws
    val borderThickness = if (hasBorder) {
        val t = rememberInfiniteTransition(label = "border")
        t.animateFloat(
            initialValue = 4f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "borderThickness"
        )
    } else null

    val shimmerBrush = if (hasBorder) shimmerBrush() else null
    val glowColor = Color.White

    val chosenModifier = if (hasBorder) {
        modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                clip = false,
                ambientColor = glowColor
            )
            .border(
            width = (borderThickness?.value ?: 4f).dp,
            brush = shimmerBrush!!,
            shape = RoundedCornerShape(8.dp)
        )
    } else {
        modifier
    }
    var isLongPressTriggered by remember { mutableStateOf(false) }
    Box(
        modifier = chosenModifier
            .clip(RoundedCornerShape(8.dp))
            .background(LightBlack.copy(alpha = 0.8f))
            .combinedClickable(
                onClick = {
                    if (!isLongPressTriggered) {
                        onClick()
                    }
                },
                onLongClick = {
                    isLongPressTriggered = true
                    if (isLongPressTriggered) onDeleteClick()
                    isLongPressTriggered = false // Reset after handling
                }
            )
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .padding(12.dp, 12.dp, 4.dp, 12.dp)
                    .size(116.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Transparent)
                    .shadow(4.dp, RoundedCornerShape(8.dp), true)

            ) {
                val generatedImage = if (dream.generatedImage != "") {
                    dream.generatedImage
                } else {
                    null
                }

                val chosenBackground = imageResId

                if (generatedImage != null) {
                    // Force a fresh subcomposition when the image string changes
                    key(generatedImage) {
                        SubcomposeAsyncImage(
                            model = generatedImage,
                            contentDescription = "Dream Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                // Fallback + shimmer while loading/empty
                                Image(
                                    painter = painterResource(chosenBackground),
                                    contentDescription = "Dream Image Placeholder",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shimmerEffect()
                                )
                            },
                            error = {
                                // Fallback on error
                                Image(
                                    painter = painterResource(chosenBackground),
                                    contentDescription = "Dream Image Fallback",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            },
                            success = {
                                SubcomposeAsyncImageContent()
                            }
                        )
                    }
                } else {
                     Image(
                         painter = painterResource(chosenBackground),
                         modifier = Modifier.fillMaxSize(),
                         contentScale = ContentScale.Crop,
                         contentDescription = "Dream Image"
                     )
                 }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp, 12.dp, 0.dp, 12.dp)
            ) {
                Text(
                    text = dream.title,
                    style = typography.titleSmall,
                    fontSize = 16.sp,
                    color = BrighterWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = dream.content,
                    style = typography.bodySmall,
                    fontSize = 13.sp,
                    color = White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                modifier = Modifier
                    .padding(0.dp, 8.dp, 8.dp, 8.dp)
                    .fillMaxHeight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (dream.isFavorite) {
                    Icon(
                        painter = painterResource(Res.drawable.baseline_star_24),
                        tint = Yellow,
                        contentDescription = "Favorite",
                        modifier = Modifier
                            .size(26.dp)
                            .padding(bottom = 4.dp),
                    )
                }

                if (dream.isNightmare) {
                    NightmareGhostAnimatedIcon(
                        modifier = Modifier
                            .size(26.dp)
                            .padding(bottom = 4.dp),
                    )
                }

                if (dream.isRecurring) {
                    Icon(
                        painter = painterResource(Res.drawable.baseline_cached_24),
                        tint = Green,
                        contentDescription = "Recurring",
                        modifier = Modifier
                            .size(26.dp)
                            .padding(bottom = 4.dp),
                    )
                }

                if (dream.isLucid) {
                    Icon(
                        painter = painterResource(Res.drawable.lighthouse_vector),
                        tint = SkyBlue,
                        contentDescription = "Lucid",
                        modifier = Modifier
                            .size(26.dp)
                            .padding(bottom = 4.dp),
                    )
                }

                if (dream.falseAwakening) {
                    Icon(
                        painter = painterResource(Res.drawable.false_awakening_icon),
                        tint = Purple,
                        contentDescription = "Day Dream",
                        modifier = Modifier
                            .size(26.dp)
                            .padding(bottom = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "borderBrush")
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    return Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.6f),
            Color.White.copy(alpha = 0.9f),
            Color.White,
            Color.White.copy(alpha = 0.9f),
            Color.White.copy(alpha = 0.6f)
        ),
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim + 300f, translateAnim + 300f)
    )
}


@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "")

    // Animate the shimmer's offset
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(), // Start from the left beyond the component
        targetValue = 2 * size.width.toFloat(),   // End to the right beyond the component
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFBEBEBE), // Light gray for the edges
                Color(0xFFF8F8F8), // Very light gray, nearly white for the center
                Color(0xFFBEBEBE), // Light gray for the edges
            ),
            start = Offset(startOffsetX, 0f),        // Start position of the gradient
            end = Offset(startOffsetX + size.width, 0f) // End position of the gradient
        )
    ).onGloballyPositioned {
        size = it.size  // Update the size when the layout changes
    }
}

@Composable
private fun NightmareGhostAnimatedIcon(
    modifier: Modifier = Modifier,
    tintOpen: Color = RedOrange,
    tintClosed: Color = White,         // closed is white
    holdOpenMillis: Int = 5000,        // hold at full open ~5s
    closedHoldMillis: Int = 900,       // brief closed pause
    crossfadeMillis: Int = 260,        // snappy swap
    moveDurationMillis: Int = 800,     // slower move/grow/shrink (was 420)
    moveDistanceDp: Float = 2f         // further ascend/descend (was 3f)
) {
    var isOpen by remember { mutableStateOf(false) } // start closed

    // Controlled scale and vertical position (dp)
    val scale = remember { Animatable(0.94f) }
    val offsetY = remember { Animatable(0f) } // dp units; 0 when open, -moveDistance when closed

    // Timeline: closed (hold) -> open (descend+grow) -> hold -> closed (ascend+shrink)
    LaunchedEffect(Unit) {
        while (true) {
            // Closed hold at current position
            delay(closedHoldMillis.toLong())

            // Open: crossfade to red and animate down to baseline while growing
            isOpen = true
            kotlinx.coroutines.coroutineScope {
                launch { scale.animateTo(1.06f, animationSpec = tween(durationMillis = moveDurationMillis, easing = LinearOutSlowInEasing)) }
                launch { offsetY.animateTo(0f, animationSpec = tween(durationMillis = moveDurationMillis, easing = LinearOutSlowInEasing)) }
            }

            // Hold fully open
            delay(holdOpenMillis.toLong())

            // Close: crossfade to white and animate up a bit while shrinking
            isOpen = false
            kotlinx.coroutines.coroutineScope {
                launch { scale.animateTo(0.90f, animationSpec = tween(durationMillis = moveDurationMillis, easing = LinearOutSlowInEasing)) }
                launch { offsetY.animateTo(-moveDistanceDp, animationSpec = tween(durationMillis = moveDurationMillis, easing = LinearOutSlowInEasing)) }
            }
            // loop repeats (will hold closed again)
        }
    }

    Crossfade(targetState = isOpen, animationSpec = tween(crossfadeMillis, easing = LinearOutSlowInEasing), label = "") { open ->
        val painter = painterResource(if (open) Res.drawable.nightmare_ghost_open else Res.drawable.nightmare_ghost_closed)
        val tint = if (open) tintOpen else tintClosed
        Icon(
            painter = painter,
            contentDescription = if (open) "Nightmare (open)" else "Nightmare (closed)",
            tint = tint,
            modifier = modifier
                .offset(y = offsetY.value.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .alpha(if (open) 1f else 0.77f)
        )
    }
}
