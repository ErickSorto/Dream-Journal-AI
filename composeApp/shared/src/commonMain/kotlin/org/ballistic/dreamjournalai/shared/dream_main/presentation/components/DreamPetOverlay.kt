package org.ballistic.dreamjournalai.shared.dream_main.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.dream_pet_star_front
import dreamjournalai.composeapp.shared.generated.resources.dream_pet_star_jump
import dreamjournalai.composeapp.shared.generated.resources.dream_pet_star_left
import dreamjournalai.composeapp.shared.generated.resources.dream_pet_star_right
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DreamPetOverlay(
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var position by remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var pose by remember { mutableStateOf(DreamPetPose.Front) }
    var dragPoseDistance by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(modifier = modifier) {
        val spriteSize = 88.dp
        val edgePaddingPx = with(density) { 14.dp.toPx() }
        val topPaddingPx = with(density) { 18.dp.toPx() }
        val bottomPaddingPx = with(density) { 118.dp.toPx() }
        val spritePx = with(density) { spriteSize.toPx() }
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val minX = edgePaddingPx
        val maxX = (containerWidthPx - spritePx - edgePaddingPx).coerceAtLeast(minX)
        val minY = topPaddingPx
        val maxY = (containerHeightPx - spritePx - bottomPaddingPx).coerceAtLeast(minY)
        val poseSwitchThresholdPx = with(density) { 10.dp.toPx() }
        val currentPosition = position ?: Offset(minX, maxY)

        LaunchedEffect(containerWidthPx, containerHeightPx, bottomPaddingPx, edgePaddingPx) {
            if (position == null) {
                position = Offset(minX, maxY)
            } else {
                position = currentPosition.copy(
                    x = currentPosition.x.coerceIn(minX, maxX),
                    y = currentPosition.y.coerceIn(minY, maxY),
                )
            }
        }

        val animatedX by animateFloatAsState(
            targetValue = currentPosition.x,
            animationSpec = if (isDragging) {
                tween(durationMillis = 0)
            } else {
                spring(dampingRatio = 0.72f, stiffness = 320f)
            },
            label = "dream-pet-x",
        )
        val animatedY by animateFloatAsState(
            targetValue = currentPosition.y,
            animationSpec = if (isDragging) {
                tween(durationMillis = 0)
            } else {
                spring(dampingRatio = 0.72f, stiffness = 320f)
            },
            label = "dream-pet-y",
        )

        DreamPetSprite(
            pose = pose,
            isDragging = isDragging,
            modifier = Modifier
                .offset { IntOffset(animatedX.roundToInt(), animatedY.roundToInt()) }
                .pointerInput(containerWidthPx, containerHeightPx) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragPoseDistance = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            val dragPosition = position ?: currentPosition
                            val leftDistance = abs(dragPosition.x - minX)
                            val rightDistance = abs(maxX - dragPosition.x)
                            val snapX = if (leftDistance < rightDistance) minX else maxX
                            pose = if (snapX < dragPosition.x) DreamPetPose.Left else DreamPetPose.Right
                            position = dragPosition.copy(x = snapX)
                        },
                        onDragCancel = {
                            isDragging = false
                            pose = DreamPetPose.Front
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragPoseDistance += abs(dragAmount.x) + abs(dragAmount.y)
                            if (dragPoseDistance >= poseSwitchThresholdPx) {
                                pose = when {
                                    dragAmount.y < -abs(dragAmount.x) -> DreamPetPose.Jump
                                    dragAmount.x < -1f -> DreamPetPose.Left
                                    dragAmount.x > 1f -> DreamPetPose.Right
                                    else -> pose
                                }
                                dragPoseDistance = 0f
                            }
                            val dragPosition = position ?: currentPosition
                            position = Offset(
                                x = (dragPosition.x + dragAmount.x).coerceIn(minX, maxX),
                                y = (dragPosition.y + dragAmount.y).coerceIn(minY, maxY),
                            )
                        },
                    )
                },
            size = spriteSize,
        )

        LaunchedEffect(isDragging, currentPosition.x, currentPosition.y) {
            if (!isDragging) {
                delay(520)
                pose = DreamPetPose.Front
            }
        }
    }
}

@Composable
private fun DreamPetSprite(
    pose: DreamPetPose,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 88.dp,
) {
    val transition = rememberInfiniteTransition(label = "dream-pet-float")
    val bob by transition.animateFloat(
        initialValue = -2f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 960, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dream-pet-bob",
    )
    val lift = if (pose == DreamPetPose.Jump) -7f else 0f

    Image(
        painter = painterResource(resourceFor(pose)),
        contentDescription = "Dream pet ${pose.name.lowercase()} sprite",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                translationY = if (isDragging) lift else bob + lift
            },
    )
}

private enum class DreamPetPose {
    Front,
    Jump,
    Left,
    Right,
}

private fun resourceFor(
    pose: DreamPetPose,
): DrawableResource = when (pose) {
    DreamPetPose.Front -> Res.drawable.dream_pet_star_front
    DreamPetPose.Jump -> Res.drawable.dream_pet_star_jump
    DreamPetPose.Left -> Res.drawable.dream_pet_star_left
    DreamPetPose.Right -> Res.drawable.dream_pet_star_right
}
