package org.ballistic.dreamjournalai.shared.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.emotion_radar_anger
import dreamjournalai.composeapp.shared.generated.resources.emotion_radar_disgust
import dreamjournalai.composeapp.shared.generated.resources.emotion_radar_fear
import dreamjournalai.composeapp.shared.generated.resources.emotion_radar_interest
import dreamjournalai.composeapp.shared.generated.resources.emotion_radar_joy
import dreamjournalai.composeapp.shared.generated.resources.emotion_radar_love
import dreamjournalai.composeapp.shared.generated.resources.emotion_radar_sadness
import dreamjournalai.composeapp.shared.generated.resources.emotion_radar_surprise
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.DreamEmotionRadar
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.EmotionRadarAxis
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun EmotionRadarCard(
    title: String,
    radar: DreamEmotionRadar,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    editable: Boolean = false,
    onRadarChange: (DreamEmotionRadar) -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF171226).copy(alpha = 0.82f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.62f),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp,
                    )
                }
            }

            EmotionRadarChart(
                radar = radar,
                editable = editable,
                onRadarChange = onRadarChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = if (editable) 300.dp else 330.dp)
            )

            if (editable) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EmotionRadarAxis.entries.chunked(2).forEach { rowAxes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowAxes.forEach { axis ->
                                EmotionAxisStepper(
                                    axis = axis,
                                    value = radar.valueFor(axis),
                                    onValueChange = { value ->
                                        onRadarChange(radar.withValue(axis, value))
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowAxes.size == 1) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                val dominant = radar.dominantAxes()
                if (dominant.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dominant.forEach { axis ->
                            EmotionDominantChip(
                                axis = axis,
                                value = radar.valueFor(axis),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmotionRadarChart(
    radar: DreamEmotionRadar,
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    onRadarChange: (DreamEmotionRadar) -> Unit = {},
) {
    val latestRadar = rememberUpdatedState(radar)
    val latestOnRadarChange = rememberUpdatedState(onRadarChange)

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .padding(22.dp)
    ) {
        val labelRadius = maxWidth * 0.43f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(editable) {
                    if (!editable) return@pointerInput

                    fun updateFromPosition(position: Offset, selectedAxis: EmotionRadarAxis? = null): EmotionRadarAxis? {
                        val axes = EmotionRadarAxis.entries
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = min(size.width, size.height) * 0.34f
                        if (radius <= 0f) return selectedAxis

                        val axis = selectedAxis ?: nearestAxisFor(position, center, axes)
                        val value = radarValueForAxisDrag(
                            position = position,
                            axis = axis,
                            center = center,
                            axes = axes,
                            radius = radius
                        )

                        latestOnRadarChange.value(latestRadar.value.withValue(axis, value))
                        return axis
                    }

                    fun hitTestPoint(position: Offset): EmotionRadarAxis? {
                        val axes = EmotionRadarAxis.entries
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = min(size.width, size.height) * 0.34f
                        val baselineRadius = radius * 0.16f
                        val hitRadius = 28.dp.toPx()

                        return axes.minByOrNull { axis ->
                            val point = pointFor(
                                index = axes.indexOf(axis),
                                count = axes.size,
                                center = center,
                                radius = visualRadiusForValue(latestRadar.value.valueFor(axis), baselineRadius, radius)
                            )
                            hypot(position.x - point.x, position.y - point.y)
                        }?.takeIf { axis ->
                            val point = pointFor(
                                index = axes.indexOf(axis),
                                count = axes.size,
                                center = center,
                                radius = visualRadiusForValue(latestRadar.value.valueFor(axis), baselineRadius, radius)
                            )
                            hypot(position.x - point.x, position.y - point.y) <= hitRadius
                        }
                    }

                    awaitPointerEventScope {
                        while (true) {
                            val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                            val downChange = downEvent.changes.firstOrNull { it.pressed } ?: continue
                            var selectedAxis = hitTestPoint(downChange.position) ?: continue
                            updateFromPosition(downChange.position, selectedAxis)
                            downChange.consume()

                            while (true) {
                                val dragEvent = awaitPointerEvent(PointerEventPass.Initial)
                                val activeChange = dragEvent.changes.firstOrNull { it.pressed } ?: break
                                updateFromPosition(activeChange.position, selectedAxis)
                                dragEvent.changes.forEach { change ->
                                    if (change.pressed) change.consume()
                                }
                            }
                        }
                    }
                }
        ) {
            val axes = EmotionRadarAxis.entries
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) * 0.37f
            val baselineRadius = radius * 0.16f

            repeat(DreamEmotionRadar.MaxValue) { ringIndex ->
                val ringRadius = baselineRadius +
                        (radius - baselineRadius) * (ringIndex + 1) / DreamEmotionRadar.MaxValue
                val path = Path()
                axes.forEachIndexed { index, _ ->
                    val point = pointFor(index, axes.size, center, ringRadius)
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                path.close()
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.08f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            axes.forEachIndexed { index, axis ->
                val end = pointFor(index, axes.size, center, radius)
                drawLine(
                    color = axisColor(axis).copy(alpha = 0.45f),
                    start = center,
                    end = end,
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            val polygon = Path()
            axes.forEachIndexed { index, axis ->
                val valueRadius = visualRadiusForValue(radar.valueFor(axis), baselineRadius, radius)
                val point = pointFor(index, axes.size, center, valueRadius)
                if (index == 0) polygon.moveTo(point.x, point.y) else polygon.lineTo(point.x, point.y)
            }
            polygon.close()

            drawPath(
                path = polygon,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFC46A).copy(alpha = 0.46f),
                        Color(0xFFFF4DBC).copy(alpha = 0.30f),
                        Color(0xFF71E8FF).copy(alpha = 0.26f),
                    ),
                    center = center,
                    radius = radius
                )
            )
            drawPath(
                path = polygon,
                color = Color.White.copy(alpha = 0.70f),
                style = Stroke(width = 1.4.dp.toPx())
            )

            axes.forEachIndexed { index, axis ->
                val value = radar.valueFor(axis)
                val point = pointFor(index, axes.size, center, visualRadiusForValue(value, baselineRadius, radius))
                drawCircle(
                    color = axisColor(axis),
                    radius = (4 + value).dp.toPx().coerceAtMost(8.dp.toPx()),
                    center = point
                )
            }

            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = 34.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = 34.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        EmotionRadarAxis.entries.forEach { axis ->
            val label = axis.localizedLabel()
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = axis.radialLabelOffsetX(labelRadius),
                        y = axis.radialLabelOffsetY(labelRadius)
                    )
                    .background(
                        color = axisColor(axis).copy(alpha = 0.22f),
                        shape = CircleShape
                    )
                    .border(1.dp, axisColor(axis).copy(alpha = 0.34f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun EmotionAxisStepper(
    axis: EmotionRadarAxis,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = axis.localizedLabel()
    Surface(
        modifier = modifier,
        color = axisColor(axis).copy(alpha = 0.15f),
        border = BorderStroke(1.dp, axisColor(axis).copy(alpha = 0.30f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(axisColor(axis), CircleShape)
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.90f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { onValueChange(value - 1) },
                enabled = value > 0,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = if (value > 0) 0.82f else 0.28f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value.toString(),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(18.dp)
            )
            IconButton(
                onClick = { onValueChange(value + 1) },
                enabled = value < DreamEmotionRadar.MaxValue,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = if (value < DreamEmotionRadar.MaxValue) 0.82f else 0.28f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmotionDominantChip(
    axis: EmotionRadarAxis,
    value: Int,
    modifier: Modifier = Modifier,
) {
    val label = axis.localizedLabel()
    Surface(
        modifier = modifier,
        color = axisColor(axis).copy(alpha = 0.16f),
        border = BorderStroke(1.dp, axisColor(axis).copy(alpha = 0.34f)),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(axisColor(axis), CircleShape)
            )
            Text(
                text = "$label $value",
                color = Color.White.copy(alpha = 0.90f),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmotionRadarAxis.localizedLabel(): String {
    return when (this) {
        EmotionRadarAxis.Joy -> stringResource(Res.string.emotion_radar_joy)
        EmotionRadarAxis.Trust -> stringResource(Res.string.emotion_radar_love)
        EmotionRadarAxis.Fear -> stringResource(Res.string.emotion_radar_fear)
        EmotionRadarAxis.Surprise -> stringResource(Res.string.emotion_radar_surprise)
        EmotionRadarAxis.Sadness -> stringResource(Res.string.emotion_radar_sadness)
        EmotionRadarAxis.Disgust -> stringResource(Res.string.emotion_radar_disgust)
        EmotionRadarAxis.Anger -> stringResource(Res.string.emotion_radar_anger)
        EmotionRadarAxis.Anticipation -> stringResource(Res.string.emotion_radar_interest)
    }
}

private fun pointFor(index: Int, count: Int, center: Offset, radius: Float): Offset {
    val angle = (-90f + (360f / count) * index) * (PI.toFloat() / 180f)
    return Offset(
        x = center.x + cos(angle) * radius,
        y = center.y + sin(angle) * radius
    )
}

private fun visualRadiusForValue(value: Int, baselineRadius: Float, maxRadius: Float): Float {
    val clamped = value.coerceIn(0, DreamEmotionRadar.MaxValue)
    return baselineRadius + (maxRadius - baselineRadius) * (clamped.toFloat() / DreamEmotionRadar.MaxValue)
}

private fun radarValueForAxisDrag(
    position: Offset,
    axis: EmotionRadarAxis,
    center: Offset,
    axes: List<EmotionRadarAxis>,
    radius: Float
): Int {
    val index = axes.indexOf(axis).coerceAtLeast(0)
    val angle = (-90f + (360f / axes.size) * index) * (PI.toFloat() / 180f)
    val directionX = cos(angle)
    val directionY = sin(angle)
    val projectedDistance = ((position.x - center.x) * directionX + (position.y - center.y) * directionY)
        .coerceIn(0f, radius)

    return ((projectedDistance / radius) * DreamEmotionRadar.MaxValue)
        .roundToInt()
        .coerceIn(0, DreamEmotionRadar.MaxValue)
}

private fun nearestAxisFor(
    position: Offset,
    center: Offset,
    axes: List<EmotionRadarAxis>
): EmotionRadarAxis {
    val pointerAngle = normalizeDegrees(
        atan2(position.y - center.y, position.x - center.x) * 180f / PI.toFloat()
    )
    return axes.minBy { axis ->
        val axisAngle = normalizeDegrees(-90f + (360f / axes.size) * axes.indexOf(axis))
        angularDistance(pointerAngle, axisAngle)
    }
}

private fun normalizeDegrees(value: Float): Float {
    val normalized = value % 360f
    return if (normalized < 0f) normalized + 360f else normalized
}

private fun angularDistance(first: Float, second: Float): Float {
    val distance = abs(first - second) % 360f
    return min(distance, 360f - distance)
}

private fun EmotionRadarAxis.radialLabelOffsetX(radius: Dp): Dp {
    return when (this) {
        EmotionRadarAxis.Joy,
        EmotionRadarAxis.Sadness -> 0.dp
        EmotionRadarAxis.Trust,
        EmotionRadarAxis.Surprise -> radius.scaled(0.72f)
        EmotionRadarAxis.Fear -> radius
        EmotionRadarAxis.Disgust,
        EmotionRadarAxis.Anticipation -> radius.scaled(-0.72f)
        EmotionRadarAxis.Anger -> -radius
    }
}

private fun EmotionRadarAxis.radialLabelOffsetY(radius: Dp): Dp {
    return when (this) {
        EmotionRadarAxis.Joy -> -radius
        EmotionRadarAxis.Trust,
        EmotionRadarAxis.Anticipation -> radius.scaled(-0.72f)
        EmotionRadarAxis.Fear,
        EmotionRadarAxis.Anger -> 0.dp
        EmotionRadarAxis.Surprise,
        EmotionRadarAxis.Disgust -> radius.scaled(0.72f)
        EmotionRadarAxis.Sadness -> radius
    }
}

private fun Dp.scaled(multiplier: Float): Dp = (value * multiplier).dp

private fun axisColor(axis: EmotionRadarAxis): Color {
    return when (axis) {
        EmotionRadarAxis.Joy -> Color(0xFFFFC46A)
        EmotionRadarAxis.Trust -> Color(0xFFFF8A63)
        EmotionRadarAxis.Fear -> Color(0xFF71E8FF)
        EmotionRadarAxis.Surprise -> Color(0xFFFFE16A)
        EmotionRadarAxis.Sadness -> Color(0xFF68A9FF)
        EmotionRadarAxis.Disgust -> Color(0xFF9E86FF)
        EmotionRadarAxis.Anger -> Color(0xFFFF6C8F)
        EmotionRadarAxis.Anticipation -> Color(0xFF86F0B2)
    }
}
