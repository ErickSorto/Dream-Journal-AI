package org.ballistic.dreamjournalai.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import network.chaintech.cmpcharts.common.model.LegendLabel
import network.chaintech.cmpcharts.common.model.LegendsConfig
import network.chaintech.cmpcharts.ui.piechart.models.PieChartData

@Composable
fun getLegendsConfigFromPieChartData(
    pieChartData: PieChartData,
    gridColumnCount: Int,
    textColor: Color = Color.Black // Default to black if not specified
): LegendsConfig {
    val legendsList = mutableListOf<LegendLabel>()
    pieChartData.slices.forEach { slice ->
        legendsList.add(LegendLabel(slice.color, slice.label))
    }
    return LegendsConfig(
        legendLabelList = legendsList,
        gridColumnCount = gridColumnCount,
        legendsArrangement = Arrangement.Start,
        textStyle = TextStyle(
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    )
}
