package org.ballistic.dreamjournalai.shared.dream_add_edit.domain


import androidx.compose.runtime.Composable
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.ai
import dreamjournalai.composeapp.shared.generated.resources.ai_vector_icon
import dreamjournalai.composeapp.shared.generated.resources.baseline_book_24
import dreamjournalai.composeapp.shared.generated.resources.baseline_nightlight_24
import dreamjournalai.composeapp.shared.generated.resources.dream
import dreamjournalai.composeapp.shared.generated.resources.dream_details
import dreamjournalai.composeapp.shared.generated.resources.info
import dreamjournalai.composeapp.shared.generated.resources.symbols
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource


enum class AddEditPages(val icon: DrawableResource) {
    DREAM(Res.drawable.baseline_nightlight_24),
    AI(Res.drawable.ai_vector_icon),
    WORDS(Res.drawable.baseline_book_24),
    INFO(Res.drawable.dream_details);

    val title: String
        @Composable
        get() = when (this) {
            DREAM -> stringResource(Res.string.dream)
            AI -> stringResource(Res.string.ai)
            WORDS -> stringResource(Res.string.symbols)
            INFO -> stringResource(Res.string.info)
        }
}