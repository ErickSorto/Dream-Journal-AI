package org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen

import android.content.Context
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.util.DreamOrder

sealed class DreamsEvent {
    data class Order(val dreamType: DreamOrder): DreamsEvent()
    data class DeleteDream(val dream: Dream, val context : Context): DreamsEvent()
    object ToggleOrderSection: DreamsEvent()
    object RestoreDream: DreamsEvent()

    data class SearchDreams(val searchQuery: String): DreamsEvent()
}
