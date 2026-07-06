package org.ballistic.dreamjournalai.shared.dream_statistics.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.*
import dreamjournalai.composeapp.shared.generated.resources.dream_token
import dreamjournalai.composeapp.shared.generated.resources.statistics_empty_hero
import dreamjournalai.composeapp.shared.generated.resources.total_dreams
import dreamjournalai.composeapp.shared.generated.resources.dream_tokens
import org.ballistic.dreamjournalai.shared.core.components.PremiumIllustratedEmptyState
import org.ballistic.dreamjournalai.shared.core.components.EmotionRadarCard
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.ArcRotationAnimation
import org.ballistic.dreamjournalai.shared.dream_statistics.StatisticEvent
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components.AiStatistics
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components.DreamChartBarChart
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components.DreamStatisticScreenTopBar
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components.DreamWritingConsistency
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components.StatisticInfo
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components.TopSixWordPieChart
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel.DreamStatisticScreenState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DreamStatisticScreen(
    dreamStatisticScreenState: DreamStatisticScreenState,
    bottomPaddingValue: Dp,
    onEvent: (StatisticEvent) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        onEvent(StatisticEvent.LoadDreams)
        onEvent(StatisticEvent.GetDreamTokens)
    }

    Scaffold(
        topBar = {
            DreamStatisticScreenTopBar(
                onEvent = onEvent
            )
        },
        containerColor = Color.Transparent,
    ) {
        if (dreamStatisticScreenState.dreams.isEmpty() && !dreamStatisticScreenState.isDreamWordFilterLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = it.calculateTopPadding(),
                        bottom = bottomPaddingValue,
                        start = 16.dp,
                        end = 16.dp
                    )
                    .dynamicBottomNavigationPadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PremiumIllustratedEmptyState(
                        image = Res.drawable.statistics_empty_hero,
                        eyebrow = stringResource(Res.string.statistics),
                        title = stringResource(Res.string.statistics_empty_title),
                        body = stringResource(Res.string.statistics_empty_body),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (dreamStatisticScreenState.dailyTokenCompletedWeeks > 0) {
                        StatisticInfo(
                            title = stringResource(Res.string.daily_token_completed_weeks_stat),
                            value = dreamStatisticScreenState.dailyTokenCompletedWeeks,
                            modifier = Modifier.fillMaxWidth(),
                            icon = painterResource(Res.drawable.dream_token)
                        )
                    }
                    if (dreamStatisticScreenState.totalCompletedLessons > 0) {
                        StatisticInfo(
                            title = stringResource(Res.string.completed_lessons_stat),
                            value = dreamStatisticScreenState.totalCompletedLessons,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Filled.School
                        )
                    }
                }
            }
        } else if (dreamStatisticScreenState.isDreamWordFilterLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ArcRotationAnimation()
            }
        } else {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = it.calculateTopPadding(),
                        bottom = bottomPaddingValue,
                        start = 16.dp,
                        end = 16.dp
                    )
                    .dynamicBottomNavigationPadding()
            ) {

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatisticInfo(
                            title = stringResource(Res.string.total_dreams),
                            value = dreamStatisticScreenState.totalDreams,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Book
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        StatisticInfo(
                            title = stringResource(Res.string.dream_tokens),
                            value = dreamStatisticScreenState.dreamTokens,
                            modifier = Modifier.weight(1f),
                            icon = painterResource(Res.drawable.dream_token)
                        )
                    }
                }
                item {
                    DreamWritingConsistency(
                        dreamStatisticScreenState = dreamStatisticScreenState
                    )
                }
                item {
                    EmotionRadarCard(
                        title = stringResource(Res.string.emotional_radar_title),
                        radar = dreamStatisticScreenState.averageEmotionRadar,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    StatisticInfo(
                        title = stringResource(Res.string.completed_lessons_stat),
                        value = dreamStatisticScreenState.totalCompletedLessons,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.School
                    )
                }
                item {
                    DreamChartBarChart(dreamStatisticScreenState = dreamStatisticScreenState)
                }
                item {
                    TopSixWordPieChart(
                        dreamStatisticScreenState = dreamStatisticScreenState,
                    )
                }
                if (dreamStatisticScreenState.dailyTokenCompletedWeeks > 0) {
                    item {
                        StatisticInfo(
                            title = stringResource(Res.string.daily_token_completed_weeks_stat),
                            value = dreamStatisticScreenState.dailyTokenCompletedWeeks,
                            modifier = Modifier.fillMaxWidth(),
                            icon = painterResource(Res.drawable.dream_token)
                        )
                    }
                }
                item {
                    AiStatistics(dreamStatisticScreenState = dreamStatisticScreenState)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
