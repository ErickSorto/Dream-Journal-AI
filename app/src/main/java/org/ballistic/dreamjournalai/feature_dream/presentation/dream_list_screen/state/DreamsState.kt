package org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.util.OrderType

data class DreamsState (
    val dreams: List<Dream> = emptyList(),
    val orderType: OrderType = OrderType.Date,
    val isOrderSectionVisible: Boolean = false,
    val searchedText: StateFlow<String> = MutableStateFlow(""),
    val isLoading: Boolean = false,
    val error: String? = null
)
