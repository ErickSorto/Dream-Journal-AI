package org.ballistic.dreamjournalai.shared.dream_tools.domain

import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.baseline_history_24
import dreamjournalai.composeapp.shared.generated.resources.dream_word_vector_icon
import dreamjournalai.composeapp.shared.generated.resources.interpret_vector
import dreamjournalai.composeapp.shared.generated.resources.mass_interpretation_tab_dreams
import dreamjournalai.composeapp.shared.generated.resources.mass_interpretation_tab_history
import dreamjournalai.composeapp.shared.generated.resources.mass_interpretation_tab_result
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class MassInterpretationTabs(
    val title: StringResource,
    val icon: DrawableResource
) {
    DREAMS(Res.string.mass_interpretation_tab_dreams, Res.drawable.dream_word_vector_icon),
    INTERPRETATION_RESULTS(Res.string.mass_interpretation_tab_result, Res.drawable.interpret_vector),
    INTERPRETATION_LIST(Res.string.mass_interpretation_tab_history, Res.drawable.baseline_history_24),
}