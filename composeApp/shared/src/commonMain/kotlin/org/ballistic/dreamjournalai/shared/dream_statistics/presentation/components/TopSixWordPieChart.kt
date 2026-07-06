package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.top_6_words_in_dreams
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel.DreamStatisticScreenState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopSixWordPieChart(
    dreamStatisticScreenState: DreamStatisticScreenState
) {
    if (dreamStatisticScreenState.topSixWordsInDreams.isEmpty()) {
        return
    }

    val topSixWords = dreamStatisticScreenState.topSixWordsInDreams.entries.take(6)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OriginalXmlColors.LightBlack.copy(alpha = 0.86f))
            .border(1.dp, OriginalXmlColors.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.top_6_words_in_dreams),
            modifier = Modifier.padding(bottom = 6.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                color = OriginalXmlColors.BrighterWhite,
                fontWeight = FontWeight.SemiBold
            ),
        )

        val listOfColor = listOf(
            OriginalXmlColors.SkyBlue.copy(alpha = 0.96f),
            OriginalXmlColors.Purple.copy(alpha = 0.94f),
            OriginalXmlColors.LighterYellow.copy(alpha = 0.96f),
            OriginalXmlColors.RedOrange.copy(alpha = 0.92f),
            OriginalXmlColors.Green.copy(alpha = 0.9f),
            OriginalXmlColors.DarkPurple.copy(alpha = 0.98f),
        )

        val slices = topSixWords.mapIndexed { index, entry ->
            DreamWordSlice(
                word = entry.key.word,
                count = entry.value,
                color = listOfColor[index % listOfColor.size]
            )
        }
        val modelProducer = remember { PieChartModelProducer() }
        LaunchedEffect(slices) {
            modelProducer.runTransaction {
                pieSeries { series(slices.map { it.count.toFloat() }) }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.08f)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            PieChartHost(
                chart = rememberPieChart(
                    sliceProvider = PieChart.SliceProvider.series(
                        slices.map { slice ->
                            PieChart.Slice(fill = Fill(slice.color))
                        }
                    ),
                    spacing = 2.dp,
                    innerSize = PieSize.Inner.fixed(112.dp),
                    valueFormatter = PieValueFormatter { _, value, _ -> value.toInt().toString() }
                ),
                modelProducer = modelProducer,
                modifier = Modifier.fillMaxSize(),
            )
        }
        DreamWordLegends(slices = slices, textColor = Color.White)
    }
}

private data class DreamWordSlice(
    val word: String,
    val count: Int,
    val color: Color
)

@Composable
private fun DreamWordLegends(
    slices: List<DreamWordSlice>,
    textColor: Color
) {
    val columnsData = slices.chunked(2)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        for (i in 0 until 3) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (i < columnsData.size) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        columnsData[i].forEach { legend ->
                            DreamWordLegendItem(legend = legend, textColor = textColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DreamWordLegendItem(
    legend: DreamWordSlice,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(legend.color.copy(alpha = 0.92f))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = legend.count.toString(),
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = legend.word,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
