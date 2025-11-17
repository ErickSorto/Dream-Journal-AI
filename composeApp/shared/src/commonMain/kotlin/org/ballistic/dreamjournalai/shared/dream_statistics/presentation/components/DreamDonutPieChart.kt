package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import network.chaintech.cmpcharts.common.model.PlotType
import network.chaintech.cmpcharts.common.utils.getMinOfTwo
import network.chaintech.cmpcharts.ui.piechart.PieChartConstants.NO_SELECTED_SLICE
import network.chaintech.cmpcharts.ui.piechart.charts.drawPie
import network.chaintech.cmpcharts.ui.piechart.models.PieChartConfig
import network.chaintech.cmpcharts.ui.piechart.models.PieChartData
import network.chaintech.cmpcharts.ui.piechart.utils.convertTouchEventPointToAngle
import network.chaintech.cmpcharts.ui.piechart.utils.proportion
import network.chaintech.cmpcharts.ui.piechart.utils.sweepAngles

@Composable
fun DreamDonutPieChart(
    modifier: Modifier,
    pieChartData: PieChartData,
    pieChartConfig: PieChartConfig,
    onSliceClick: (PieChartData.Slice) -> Unit = {}
) {
    val sumOfValues = pieChartData.totalLength
    val textMeasurer = rememberTextMeasurer()
    val proportions = pieChartData.slices.proportion(sumOfValues)
    val sweepAngles = proportions.sweepAngles()

    val progressSize = mutableListOf<Float>()
    progressSize.add(sweepAngles.first())

    for (x in 1 until sweepAngles.size) {
        progressSize.add(sweepAngles[x] + progressSize[x - 1])
    }

    var activePie by rememberSaveable {
        mutableStateOf(NO_SELECTED_SLICE)
    }
    Surface(
        modifier = modifier,
        color = Color.Transparent
    ) {
        val boxModifier = if (pieChartConfig.isClickOnSliceEnabled) {
            Modifier
                .aspectRatio(ratio = 1f)
                .background(pieChartConfig.backgroundColor)
        } else {
            Modifier
                .aspectRatio(1f)
        }
        BoxWithConstraints(
            modifier = boxModifier
        ) {

            val sideSize = getMinOfTwo(constraints.maxWidth, constraints.maxHeight)
            val padding = (sideSize * pieChartConfig.chartPadding) / 100f
            val size = Size(sideSize.toFloat() - padding, sideSize.toFloat() - padding)

            val pathPortion = remember {
                Animatable(initialValue = 0f)
            }

            if (pieChartConfig.isAnimationEnable) {
                LaunchedEffect(key1 = Unit) {
                    pathPortion.animateTo(
                        1f, animationSpec = tween(pieChartConfig.animationDuration)
                    )
                }
            }

            val canvasModifier = if (pieChartConfig.isClickOnSliceEnabled) {
                Modifier
                    .size(sideSize.dp)
                    .pointerInput(true) {
                        detectTapGestures {
                            val clickedAngle = convertTouchEventPointToAngle(
                                sideSize.toFloat(),
                                sideSize.toFloat(),
                                it.x,
                                it.y
                            )
                            progressSize.forEachIndexed { index, item ->
                                if (clickedAngle <= item) {
                                    activePie = if (activePie != index)
                                        index
                                    else
                                        NO_SELECTED_SLICE
                                    onSliceClick(pieChartData.slices[index])
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
            } else {
                Modifier
                    .width(sideSize.dp)
                    .height(sideSize.dp)
            }
            Canvas(
                modifier = canvasModifier

            ) {
                var sAngle = pieChartConfig.startAngle

                sweepAngles.forEachIndexed { index, arcProgress ->
                    drawPie(
                        color = pieChartData.slices[index].color,
                        startAngle = sAngle,
                        arcProgress = if (pieChartConfig.isAnimationEnable)
                            arcProgress * pathPortion.value else arcProgress,
                        size = size,
                        padding = padding,
                        isDonut = pieChartData.plotType == PlotType.Donut,
                        strokeWidth = pieChartConfig.strokeWidth,
                        isActive = activePie == index,
                        pieChartConfig = pieChartConfig
                    )
                    sAngle += arcProgress
                }
                when {
                    activePie != -1 && pieChartConfig.labelVisible -> {
                        val selectedSlice = pieChartData.slices[activePie]
                            val fontSize = pieChartConfig.labelFontSize.toPx()
                            var isValue = false
                            val textToDraw = when (pieChartConfig.labelType) {
                                PieChartConfig.LabelType.PERCENTAGE -> "${
                                    proportions[activePie].roundToInt()
                                }%"
                                PieChartConfig.LabelType.VALUE -> {
                                    isValue = true
                                    selectedSlice.value.toString()
                                }
                            }
                            val labelColor = when (pieChartConfig.labelColorType) {
                                PieChartConfig.LabelColorType.SPECIFIED_COLOR -> pieChartConfig.labelColor
                                PieChartConfig.LabelColorType.SLICE_COLOR -> selectedSlice.color
                            }
                            val shouldShowUnit = isValue && pieChartConfig.sumUnit.isNotEmpty()
                            drawLabel(
                                pieChartConfig = pieChartConfig,
                                labelColor = labelColor,
                                shouldShowUnit = shouldShowUnit,
                                fontSize = fontSize,
                                textToDraw = textToDraw,
                                sideSize = sideSize,
                                textMeasurer = textMeasurer
                            )
                        }
                    activePie == -1 && pieChartConfig.isSumVisible -> {
                            val fontSize = pieChartConfig.labelFontSize.toPx()
                            val textToDraw = "$sumOfValues"
                            drawLabel(
                                pieChartConfig = pieChartConfig,
                                labelColor = pieChartConfig.labelColor,
                                shouldShowUnit = pieChartConfig.sumUnit.isNotEmpty(),
                                fontSize = fontSize,
                                textToDraw = textToDraw,
                                sideSize = sideSize,
                                textMeasurer = textMeasurer
                            )
                        }
//                    }

                }
            }
        }
    }
}

private fun DrawScope.drawLabel(
    pieChartConfig: PieChartConfig,
    labelColor: Color,
    shouldShowUnit: Boolean,
    fontSize: Float,
    textToDraw: String,
    sideSize: Int,
    textMeasurer: TextMeasurer
) {
    val x = (sideSize / 2).toFloat() - (fontSize * textToDraw.length / 2)
    var y: Float = (sideSize / 2).toFloat() - fontSize / 2
    if (shouldShowUnit)
        y -= fontSize / 2f

    drawText(
        textMeasurer = textMeasurer,
        text = textToDraw,
        style = TextStyle(
            fontSize = (fontSize).toSp(),
            color = labelColor,
            fontFamily = pieChartConfig.fontFamily,
            textAlign = TextAlign.Center,
            fontWeight = pieChartConfig.labelFontWeight
        ),
        topLeft = Offset(x = x, y = y),
        maxLines = 1,
        size = Size(
            fontSize * textToDraw.length, fontSize + 2f
        )
    )
    if (shouldShowUnit) {
        y += fontSize / 1.25f
        drawText(
            textMeasurer = textMeasurer,
            text = pieChartConfig.sumUnit,
            style = TextStyle(
                fontSize = (fontSize).toSp(),
                color = labelColor,
                textAlign = TextAlign.Center,
                fontFamily = pieChartConfig.fontFamily,
                fontWeight = pieChartConfig.labelFontWeight
            ),
            topLeft = Offset(x = x, y = y),
            maxLines = 1,
            size = Size(
                fontSize * textToDraw.length, fontSize + 2f
            )
        )
    }
}
