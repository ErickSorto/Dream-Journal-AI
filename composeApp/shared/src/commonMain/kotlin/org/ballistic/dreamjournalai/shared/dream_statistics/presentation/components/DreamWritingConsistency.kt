package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.*
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel.DreamHeatMapDay
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel.DreamHeatMapMonth
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel.DreamStatisticScreenState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DreamWritingConsistency(
    dreamStatisticScreenState: DreamStatisticScreenState,
    modifier: Modifier = Modifier,
) {
    val maxDayCount = remember(dreamStatisticScreenState.heatMapMonths) {
        dreamStatisticScreenState.heatMapMonths
            .flatMap { it.days }
            .maxOfOrNull { it.count }
            ?.coerceAtLeast(1) ?: 1
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF190D3B).copy(alpha = 0.94f),
                        OriginalXmlColors.LightBlack.copy(alpha = 0.88f)
                    )
                )
            )
            .border(1.dp, Color(0xFF9B6BFF).copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6D8E).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF8BA5),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = stringResource(Res.string.dream_streak_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = OriginalXmlColors.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(Res.string.dream_streak_server_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCFC2FF)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StreakMetric(
                label = stringResource(Res.string.streak_current),
                value = dreamStatisticScreenState.dreamWritingStreak,
                icon = Icons.Filled.Whatshot,
                modifier = Modifier.weight(1f)
            )
            StreakMetric(
                label = stringResource(Res.string.streak_best),
                value = dreamStatisticScreenState.longestDreamWritingStreak,
                icon = Icons.Filled.NightsStay,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = stringResource(Res.string.active_writing_days, dreamStatisticScreenState.activeDreamWritingDays),
            color = Color(0xFFFFD6A4),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        DreamHeatMap(
            months = dreamStatisticScreenState.heatMapMonths,
            maxDayCount = maxDayCount
        )
    }
}

@Composable
private fun StreakMetric(
    label: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF2A155A).copy(alpha = 0.72f))
            .border(1.dp, Color(0xFF8C6BFF).copy(alpha = 0.24f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFFC269),
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = value.toString(),
                color = OriginalXmlColors.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$label ${stringResource(if (value == 1) Res.string.streak_day else Res.string.streak_days)}",
                color = Color(0xFFCFC2FF),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DreamHeatMap(
    months: List<DreamHeatMapMonth>,
    maxDayCount: Int,
) {
    val weeksByMonth = remember(months) {
        months.map { month -> month to buildHeatMapWeeks(month) }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val weekGap = 1.dp
        val allWeeks = weeksByMonth.flatMap { (_, weeks) -> weeks }
        val totalWeekColumns = allWeeks.size.coerceAtLeast(1)
        val totalGaps = weekGap * (totalWeekColumns - 1).coerceAtLeast(0)
        val cellSize = ((maxWidth - totalGaps).coerceAtLeast(0.dp) / totalWeekColumns)
            .coerceIn(11.dp, 26.dp)

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(weekGap)
            ) {
                weeksByMonth.forEach { (month, weeks) ->
                    Text(
                        text = "${stringResource(monthShortResource(month.monthNumber))} ${month.yearShort.toString().padStart(2, '0')}",
                        color = Color(0xFFE6DDFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.width((cellSize * weeks.size) + (weekGap * (weeks.size - 1).coerceAtLeast(0)))
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(weekGap),
                verticalAlignment = Alignment.Top
            ) {
                allWeeks.forEach { week ->
                    HeatMapWeekColumn(
                        week = week,
                        maxDayCount = maxDayCount,
                        cellSize = cellSize,
                        cellGap = weekGap
                    )
                }
            }
        }
    }
}

@Composable
private fun HeatMapWeekColumn(
    week: List<DreamHeatMapDay?>,
    maxDayCount: Int,
    cellSize: androidx.compose.ui.unit.Dp,
    cellGap: androidx.compose.ui.unit.Dp,
) {
    Column(verticalArrangement = Arrangement.spacedBy(cellGap)) {
        week.forEach { day ->
            if (day == null) {
                Box(
                    modifier = Modifier
                        .size(cellSize)
                        .clip(RoundedCornerShape(3.dp))
                        .background(heatMapColor(0f))
                )
            } else {
                val intensity = (day.count.toFloat() / maxDayCount).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .size(cellSize)
                        .clip(RoundedCornerShape(3.dp))
                        .background(heatMapColor(intensity))
                ) {
                }
            }
        }
    }
}

private fun buildHeatMapWeeks(month: DreamHeatMapMonth): List<List<DreamHeatMapDay?>> {
    val leadingBlankSlots = month.days.firstOrNull()?.leadingBlankSlots ?: 0
    val cells = List<DreamHeatMapDay?>(leadingBlankSlots) { null } + month.days
    return cells
        .chunked(7)
        .map { week -> week + List(7 - week.size) { null } }
}

private fun heatMapColor(intensity: Float): Color {
    if (intensity <= 0f) return Color(0xFF3A2864).copy(alpha = 0.92f)
    val low = Color(0xFF6E5BC0).copy(alpha = 0.94f)
    val high = Color(0xFFFF7BAE)
    return lerp(low, high, intensity.coerceAtMost(0.92f))
}

private fun monthShortResource(monthNumber: Int): StringResource {
    return when (monthNumber) {
        1 -> Res.string.month_jan_short
        2 -> Res.string.month_feb_short
        3 -> Res.string.month_mar_short
        4 -> Res.string.month_apr_short
        5 -> Res.string.month_may_short
        6 -> Res.string.month_jun_short
        7 -> Res.string.month_jul_short
        8 -> Res.string.month_aug_short
        9 -> Res.string.month_sep_short
        10 -> Res.string.month_oct_short
        11 -> Res.string.month_nov_short
        else -> Res.string.month_dec_short
    }
}
