package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import network.chaintech.cmpcharts.common.model.PlotType
import network.chaintech.cmpcharts.ui.piechart.models.PieChartConfig
import network.chaintech.cmpcharts.ui.piechart.models.PieChartData
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel.DreamStatisticScreenState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors

@Composable
fun TopSixWordPieChart(
    dreamStatisticScreenState: DreamStatisticScreenState
) {
    if (dreamStatisticScreenState.topSixWordsInDreams.isEmpty()) {
        return
    }

    val topSixWords = dreamStatisticScreenState.topSixWordsInDreams.entries.take(6)
    val numberOfRows = 2 // We'll have 2 rows of legends in each column
    val legendHeight = numberOfRows * 48.dp // Assuming each row of legends is roughly 24.dp tall

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(OriginalXmlColors.LightBlack.copy(alpha = 0.8f))
            .padding(8.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Top 6 Words in Dreams",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                color = OriginalXmlColors.White
            ).copy(fontWeight = FontWeight.Normal),
        )

        val listOfColor = listOf(
            Color(0xFFEF5350), // Red
            Color(0xFFAB47BC), // Purple
            Color(0xFF42A5F5), // Blue
            Color(0xFFFFA726), // Orange
            Color(0xFF26A69A), // Teal
            Color(0xFF66BB6A), // Light Green
        )

        val donutChartData = PieChartData(
            slices = topSixWords.mapIndexed { index, entry ->
                PieChartData.Slice(
                    label = "${entry.key.word}: ${entry.value}",
                    value = entry.value.toFloat(),
                    color = listOfColor[index % listOfColor.size],
                )
            },
            plotType = PlotType.Donut
        )

        val pieChartConfig = PieChartConfig(
            labelVisible = true,
            strokeWidth = 120f,
            labelColor = OriginalXmlColors.White,
            activeSliceAlpha = .9f,
            isEllipsizeEnabled = true,
            labelFontWeight = FontWeight.Bold,
            isAnimationEnable = true,
            chartPadding = 25,
            labelFontSize = 42.sp,
            backgroundColor = Color.Transparent
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            DreamDonutPieChart(
                modifier = Modifier.fillMaxSize(),
                pieChartData = donutChartData,
                pieChartConfig = pieChartConfig
            )
        }
        Box(modifier = Modifier.height(legendHeight)) {
            CustomLegends(
                pieChartData = donutChartData,
                textColor = Color.White
            )
        }
    }
}