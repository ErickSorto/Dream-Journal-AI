package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
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
            columnModel {
                series(data.map { it.toFloat() })
            }
        }
    }

    val listOfColor = remember {
        listOf(
            OriginalXmlColors.SkyBlue.copy(alpha = 0.96f),
            OriginalXmlColors.Purple.copy(alpha = 0.94f),
            OriginalXmlColors.LighterYellow.copy(alpha = 0.96f),
            OriginalXmlColors.RedOrange.copy(alpha = 0.92f),
            OriginalXmlColors.Green.copy(alpha = 0.9f)
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
                extraStore: ExtraStore,
            ): LineComponent {
                return baseComponent.copy(
                    fill = Fill(listOfColor[entry.x.toInt() % listOfColor.size])
                )
            }

            override fun getWidestSeriesColumn(
                seriesKey: Any,
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
            .background(OriginalXmlColors.LightBlack.copy(alpha = 0.86f))
            .border(1.dp, OriginalXmlColors.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.dream_types),
                modifier = Modifier.padding(18.dp, 16.dp, 18.dp, 4.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = OriginalXmlColors.BrighterWhite,
                    fontWeight = FontWeight.SemiBold
                ),
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
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .height(360.dp)
                    .fillMaxWidth()
                    .background(
                        Color.Transparent
                    )
            )
        }
    }
}
