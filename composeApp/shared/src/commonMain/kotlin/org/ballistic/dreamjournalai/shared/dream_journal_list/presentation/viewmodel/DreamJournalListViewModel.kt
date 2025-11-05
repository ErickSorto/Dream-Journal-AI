package org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.DreamListEvent
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.util.OrderType


class DreamJournalListViewModel(
    private val dreamUseCases: DreamUseCases,
    private val vibratorUtil: VibratorUtil,
 //   private val reviewComponent: ReviewComponent
) : ViewModel() {

    companion object {
        private const val UNDO_TIMEOUT_MS = 10_000L // align with SnackbarDuration.Long
    }

    private val _dreamJournalListState = MutableStateFlow(DreamJournalListState())
    val dreamJournalListState: StateFlow<DreamJournalListState> = _dreamJournalListState.asStateFlow()

    private val _searchTextFieldState = MutableStateFlow(TextFieldState())

    val searchTextFieldState: StateFlow<TextFieldState> = _searchTextFieldState.asStateFlow()

    private var recentlyDeletedDream: Dream? = null

    // ids hidden locally until user confirms or undo timeout elapses
    private val filteredOutIds = MutableStateFlow<Set<String>>(emptySet())
    // job per pending delete to cancel on undo
    private val pendingDeletes = mutableMapOf<String, Job>()

    private var getDreamJob: Job? = null

    init {
        getDreams(OrderType.Date)
    }

    fun onEvent(event: DreamListEvent) {
        when (event) {
            is DreamListEvent.DeleteDream -> {
                viewModelScope.launch {
                    val id = event.dream.id
                    if (id == null) {
                        // Fallback: if no id, perform immediate delete (cannot defer reliably)
                        dreamUseCases.deleteDream(event.dream)
                        return@launch
                    }
                    // Soft-delete locally: hide from list and schedule finalization
                    recentlyDeletedDream = event.dream
                    filteredOutIds.value = filteredOutIds.value + id
                    // schedule finalize
                    pendingDeletes[id]?.cancel()
                    pendingDeletes[id] = launch {
                        delay(UNDO_TIMEOUT_MS)
                        // If still pending (no undo), perform remote delete
                        dreamUseCases.deleteDream(event.dream)
                        filteredOutIds.value = filteredOutIds.value - id
                        pendingDeletes.remove(id)
                        if (recentlyDeletedDream?.id == id) recentlyDeletedDream = null
                    }
                    // Reintroduce undo snackbar via centralized controller
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = "Dream deleted",
                            action = SnackbarAction("Undo") { viewModelScope.launch { onEvent(DreamListEvent.RestoreDream) } }
                        )
                    )
                }
            }
            is DreamListEvent.TriggerReview -> {
                // For example, check conditions before prompting
                val dreamCount = dreamJournalListState.value.dreams.size
                // or load from DB, or pass in from the composable, etc.

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
                    val dreamToRestore = recentlyDeletedDream ?: return@launch
                    val id = dreamToRestore.id
                    if (id != null) {
                        // Cancel pending remote delete if any and unfilter locally
                        pendingDeletes[id]?.cancel()
                        pendingDeletes.remove(id)
                        filteredOutIds.value = filteredOutIds.value - id
                        // If remote delete already happened (race), re-add dream
                        // We can't trivially detect it without an extra call; adding is idempotent enough
                        dreamUseCases.addDream(dreamToRestore)
                    } else {
                        // No id: just re-add to repository
                        dreamUseCases.addDream(dreamToRestore)
                    }
                    recentlyDeletedDream = null
                }
            }
            is DreamListEvent.Order -> {
                if (dreamJournalListState.value.orderType == event.orderType) {
                    return
                }
                getDreams(event.orderType)
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
                getDreams(dreamJournalListState.value.orderType)
            }
        }
    }

    private fun getDreams(orderType: OrderType) {
        getDreamJob?.cancel()
        getDreamJob = dreamUseCases.getDreams(orderType)
            .combine(
                snapshotFlow { searchTextFieldState.value.text }
            ) { dreams, searchText ->
                if (searchText.isBlank()) dreams else dreams.filter { it.doesMatchSearchQuery(searchText.toString()) }
            }
            .combine(filteredOutIds) { dreams, filteredIds ->
                dreams.filter { it.id == null || it.id !in filteredIds }
            }
            .onEach { visibleDreams ->
                _dreamJournalListState.value = dreamJournalListState.value.copy(
                    dreams = visibleDreams,
                    orderType = orderType,
                )
            }
            .launchIn(viewModelScope)
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