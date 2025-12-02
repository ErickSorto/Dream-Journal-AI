package org.ballistic.dreamjournalai.shared.dream_add_edit.domain

import androidx.compose.runtime.Composable
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.dream_advice_title
import dreamjournalai.composeapp.shared.generated.resources.dream_interpretation
import dreamjournalai.composeapp.shared.generated.resources.dream_mood_analysis_title
import dreamjournalai.composeapp.shared.generated.resources.dream_painter_title
import dreamjournalai.composeapp.shared.generated.resources.dream_questions_title
import dreamjournalai.composeapp.shared.generated.resources.dream_story_title
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AIState
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AIType
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AddEditDreamState
import org.jetbrains.compose.resources.stringResource

enum class AIPageType(
    val getState: (AddEditDreamState) -> AIState,
    val buttonType: ButtonType,
) {
    PAINTER({ it.aiStates[AIType.IMAGE]!! },
        ButtonType.PAINT
    ),
    EXPLANATION({ it.aiStates[AIType.INTERPRETATION]!! },
        ButtonType.INTERPRET
    ),
    ADVICE({ it.aiStates[AIType.ADVICE]!! },
        ButtonType.ADVICE
    ),
    QUESTION({ it.aiStates[AIType.QUESTION_ANSWER]!! },
        ButtonType.QUESTION
    ),
    STORY({ it.aiStates[AIType.STORY]!! },
        ButtonType.STORY
    ),
    MOOD({ it.aiStates[AIType.MOOD]!! },
        ButtonType.MOOD
    );

    val title: String
        @Composable
        get() = when (this) {
            PAINTER -> stringResource(Res.string.dream_painter_title)
            EXPLANATION -> stringResource(Res.string.dream_interpretation)
            ADVICE -> stringResource(Res.string.dream_advice_title)
            QUESTION -> stringResource(Res.string.dream_questions_title)
            STORY -> stringResource(Res.string.dream_story_title)
            MOOD -> stringResource(Res.string.dream_mood_analysis_title)
        }
}