package org.ballistic.dreamjournalai.shared.dream_add_edit.domain

import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AIState
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AIType
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AddEditDreamState

enum class AIPageType(
    val title: String,
    val getState: (AddEditDreamState) -> AIState,
    val buttonType: ButtonType,
) {
    PAINTER("Dream Painter", { it.aiStates[AIType.IMAGE]!! },
        ButtonType.PAINT
    ),
    EXPLANATION("Dream Interpretation", { it.aiStates[AIType.INTERPRETATION]!! },
        ButtonType.INTERPRET
    ),
    ADVICE("Dream Advice", { it.aiStates[AIType.ADVICE]!! },
        ButtonType.ADVICE
    ),
    QUESTION("Dream Questions", { it.aiStates[AIType.QUESTION_ANSWER]!! },
        ButtonType.QUESTION
    ),
    STORY("Dream Story", { it.aiStates[AIType.STORY]!! },
        ButtonType.STORY
    ),
    MOOD("Dream Mood Analysis", { it.aiStates[AIType.MOOD]!! },
        ButtonType.MOOD
    ),
}