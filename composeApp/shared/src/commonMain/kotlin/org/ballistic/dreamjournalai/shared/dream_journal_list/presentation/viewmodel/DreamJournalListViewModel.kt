package org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.DreamListEvent
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.util.OrderType


@OptIn(ExperimentalCoroutinesApi::class)
class DreamJournalListViewModel(
    private val dreamUseCases: DreamUseCases,
    private val vibratorUtil: VibratorUtil,
 //   private val reviewComponent: ReviewComponent
) : ViewModel() {

    private val _dreamJournalListState = MutableStateFlow(DreamJournalListState())
    val dreamJournalListState: StateFlow<DreamJournalListState> = _dreamJournalListState.asStateFlow()
    private val _searchTextFieldState = MutableStateFlow(TextFieldState())
    val searchTextFieldState: StateFlow<TextFieldState> = _searchTextFieldState.asStateFlow()

    private var recentlyDeletedDream: Dream? = null

    // Track order as a flow to drive a single hot subscription
    private val orderTypeFlow: MutableStateFlow<OrderType> = MutableStateFlow(OrderType.Date)

    init {
        onEvent(DreamListEvent.FetchDreams)
    }

    fun onEvent(event: DreamListEvent) {
        when (event) {
            is DreamListEvent.DeleteDream -> {
                viewModelScope.launch {
                    dreamUseCases.deleteDream(event.dream)
                    recentlyDeletedDream = event.dream
                }
            }
            is DreamListEvent.TriggerReview -> {
                // For example, check conditions before prompting
                val dreamCount = dreamJournalListState.value.dreams.size
                if (dreamCount >= 2) {
//                    viewModelScope.launch {
//                        reviewComponent.requestInAppReview().collect { resultCode ->
//                           //TODO: Handle result code
//                        }
//                    }
                }
            }
            is DreamListEvent.TriggerVibration -> {
                vibratorUtil.triggerVibration()
            }
            is DreamListEvent.ToggleOrderSection -> {
                _dreamJournalListState.value = _dreamJournalListState.value.copy(
                    isOrderSectionVisible = !dreamJournalListState.value.isOrderSectionVisible)
            }
            is DreamListEvent.RestoreDream -> {
                viewModelScope.launch {
                    val dreamToRestore = recentlyDeletedDream?.copy(generatedImage = "") ?: return@launch
                    dreamUseCases.addDream(dreamToRestore)
                    recentlyDeletedDream = null
                }
            }
            is DreamListEvent.Order -> {
                // Update order type; the single subscription will react via flatMapLatest
                if (dreamJournalListState.value.orderType != event.orderType) {
                    _dreamJournalListState.value = _dreamJournalListState.value.copy(orderType = event.orderType)
                    orderTypeFlow.value = event.orderType
                }
            }
            is DreamListEvent.SetSearchingState -> {
                _dreamJournalListState.value = _dreamJournalListState.value.copy(
                    isSearching = event.state
                )
            }

            is DreamListEvent.ToggleBottomDeleteCancelSheetState -> {
                _dreamJournalListState.value = dreamJournalListState.value.copy(
                    bottomDeleteCancelSheetState = event.bottomDeleteCancelSheetState
                )
            }

            is DreamListEvent.DreamToDelete -> {
                _dreamJournalListState.value = dreamJournalListState.value.copy(
                    chosenDreamToDelete = event.dream
                )
            }
            is DreamListEvent.FetchDreams -> {
                // Build a single subscription that reacts to order changes and search text
                orderTypeFlow
                    .flatMapLatest { orderType ->
                        dreamUseCases.getDreams(orderType)
                    }
                    .combine(
                        snapshotFlow { searchTextFieldState.value.text }
                    ) { dreams, searchText ->
                        if (searchText.isBlank()) dreams
                        else dreams.filter { it.doesMatchSearchQuery(searchText.toString()) }
                    }
                    .onEach { filteredDreams ->
                        _dreamJournalListState.value = _dreamJournalListState.value.copy(
                            dreams = filteredDreams,
                            orderType = orderTypeFlow.value,
                        )
                    }
                    .launchIn(viewModelScope)
            }
        }
    }
}


data class DreamJournalListState(
    val dreams: List<Dream> = emptyList(),
    val bottomDeleteCancelSheetState: Boolean = false,
    val chosenDreamToDelete: Dream? = null,
    val orderType: OrderType = OrderType.Date,
    val isOrderSectionVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSearching: Boolean = false,
)