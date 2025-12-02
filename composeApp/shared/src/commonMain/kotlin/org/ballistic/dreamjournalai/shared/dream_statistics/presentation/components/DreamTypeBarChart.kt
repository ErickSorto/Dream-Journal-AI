package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.columnSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent
import com.patrykandpatrick.vico.multiplatform.common.component.rememberTextComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.day_dream
import dreamjournalai.composeapp.shared.generated.resources.dream_types
import dreamjournalai.composeapp.shared.generated.resources.favorite
import dreamjournalai.composeapp.shared.generated.resources.lucid
import dreamjournalai.composeapp.shared.generated.resources.nightmare
import dreamjournalai.composeapp.shared.generated.resources.recurring
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel.DreamStatisticScreenState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun DreamChartBarChart(
    dreamStatisticScreenState: DreamStatisticScreenState
) {
    if (dreamStatisticScreenState.dreams.isEmpty()) {
        return
    }

    val dreamTypeLabels =
        listOf(
            stringResource(Res.string.lucid),
            stringResource(Res.string.nightmare),
            stringResource(Res.string.favorite),
            stringResource(Res.string.recurring),
            stringResource(Res.string.day_dream)
        )

    // Map data to the dream type labels
    val data = listOf(
        dreamStatisticScreenState.totalLucidDreams,
        dreamStatisticScreenState.totalNightmares,
        dreamStatisticScreenState.totalFavoriteDreams,
        dreamStatisticScreenState.totalRecurringDreams,
        dreamStatisticScreenState.totalFalseAwakenings
    )

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                series(data.map { it.toFloat() })
            }
        }
    }

    val listOfColor = remember {
        listOf(
            Color(0xFFEF5350), // Red
            Color(0xFFAB47BC), // Purple
            Color(0xFF42A5F5), // Blue
            Color(0xFFFFA726), // Orange
            Color(0xFF26A69A), // Teal
            Color(0xFF66BB6A)  // Light Green
        )
    }

    val columnProvider = remember(listOfColor) {
        object : ColumnCartesianLayer.ColumnProvider {
            private val baseComponent = LineComponent(
                thickness = 15.dp,
                shape = RoundedCornerShape(16.dp),
                fill = Fill(listOfColor.first())
            )

            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineComponent {
                return baseComponent.copy(
                    fill = Fill(listOfColor[entry.x.toInt() % listOfColor.size])
                )
            }

            override fun getWidestSeriesColumn(
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineComponent = baseComponent
        }
    }

    val bottomAxis = HorizontalAxis.rememberBottom(
        label = rememberTextComponent(
            style = TextStyle(color = Color.White),
        ),
        valueFormatter = CartesianValueFormatter { _, value, _ ->
            dreamTypeLabels.getOrNull(value.roundToInt()) ?: ""
        },
        labelRotationDegrees = 90f
    )

    val startAxis = VerticalAxis.rememberStart(
        label = rememberTextComponent(
            style = TextStyle(color = Color.White),
        ),
        itemPlacer = VerticalAxis.ItemPlacer.count(count = { 5 })
    )

    Box(
        modifier = Modifier
            .imePadding()
            .clip(RoundedCornerShape(16.dp))
            .background(
                OriginalXmlColors.LightBlack.copy(alpha = 0.8f)
            )
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.dream_types),
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = OriginalXmlColors.White
                ).copy(fontWeight = FontWeight.Normal),
            )
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider = columnProvider
                    ),
                    startAxis = startAxis,
                    bottomAxis = bottomAxis,
                    marker = rememberDefaultCartesianMarker(
                        label = rememberTextComponent(
                            style = TextStyle(color = Color.White),
                        ),
                        indicatorSize = 6.dp,
                    ),
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .padding(16.dp)
                    .height(400.dp)
                    .fillMaxWidth()
                    .background(
                        Color.Transparent
                    )
            )
        }
    }
}
