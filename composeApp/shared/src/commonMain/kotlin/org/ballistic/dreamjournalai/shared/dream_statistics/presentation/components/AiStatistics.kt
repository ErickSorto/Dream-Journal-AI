package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AITool
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel.DreamStatisticScreenState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun AiStatistics(
    dreamStatisticScreenState: DreamStatisticScreenState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(OriginalXmlColors.LightBlack.copy(alpha = 0.8f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI Generated Content",
            style = MaterialTheme.typography.titleMedium,
            color = OriginalXmlColors.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val animationDelayStep = 200L
            AiStatisticItem(
                title = "Paintings",
                value = dreamStatisticScreenState.totalImages,
                icon = painterResource(AITool.PAINT_DREAM.icon),
                color = AITool.PAINT_DREAM.color,
                animationDelay = animationDelayStep * 0
            )
            AiStatisticItem(
                title = "Interpretations",
                value = dreamStatisticScreenState.totalInterpretations,
                icon = painterResource(AITool.INTERPRET_DREAM.icon),
                color = AITool.INTERPRET_DREAM.color,
                animationDelay = animationDelayStep
            )
            AiStatisticItem(
                title = "Stories",
                value = dreamStatisticScreenState.totalStories,
                icon = painterResource(AITool.DREAM_STORY.icon),
                color = AITool.DREAM_STORY.color,
                animationDelay = animationDelayStep * 2
            )
            AiStatisticItem(
                title = "Moods",
                value = dreamStatisticScreenState.totalMoods,
                icon = painterResource(AITool.DREAM_MOOD.icon),
                color = AITool.DREAM_MOOD.color,
                animationDelay = animationDelayStep * 3
            )
            AiStatisticItem(
                title = "Advice",
                value = dreamStatisticScreenState.totalAdvice,
                icon = painterResource(AITool.DREAM_ADVICE.icon),
                color = AITool.DREAM_ADVICE.color,
                animationDelay = animationDelayStep * 4
            )
            AiStatisticItem(
                title = "Questions",
                value = dreamStatisticScreenState.totalQuestions,
                icon = painterResource(AITool.DREAM_QUESTION.icon),
                color = AITool.DREAM_QUESTION.color,
                animationDelay = animationDelayStep * 5
            )
        }
    }
}