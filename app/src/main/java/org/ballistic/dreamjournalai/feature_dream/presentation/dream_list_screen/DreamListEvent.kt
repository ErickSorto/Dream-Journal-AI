package org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen

import android.content.Context
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.util.OrderType

sealed class DreamListEvent {
    data class Order(val orderType: OrderType): DreamListEvent()
    data class DeleteDream(val dream: Dream, val context : Context): DreamListEvent()
    object ToggleOrderSection: DreamListEvent()
    object RestoreDream: DreamListEvent()

    data class SearchDreams(val searchQuery: String): DreamListEvent()
}
