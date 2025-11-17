package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.chaintech.cmpcharts.ui.piechart.models.PieChartData

@Composable
fun CustomLegends(
    pieChartData: PieChartData,
    textColor: Color = Color.White
) {
    val legends = pieChartData.slices
    // Group legends into vertical pairs for each column
    val columnsData = legends.chunked(2)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Use 3 columns of equal weight to ensure proper alignment
        for (i in 0 until 3) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (i < columnsData.size) {
                    val columnItems = columnsData[i]
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        columnItems.forEach { legend ->
                            LegendItem(legend, textColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    legend: PieChartData.Slice,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(legend.color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = legend.value.toInt().toString(),
                color = textColor,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = legend.label.substringBefore(":"), // Extract just the word
            color = textColor,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}