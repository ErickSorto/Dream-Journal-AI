package org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.feature_dream.domain.util.OrderType
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.DreamsEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.state.DreamsState
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DreamsViewModel @Inject constructor(
    private val dreamUseCases: DreamUseCases
) : ViewModel() {

    private val imageCache = mutableMapOf<String, ByteArray>()

    private val _state = MutableStateFlow(DreamsState())
    val state: StateFlow<DreamsState> = _state.asStateFlow()

    private val _searchedText = MutableStateFlow("")
    val searchedText: StateFlow<String> = _searchedText.asStateFlow()



    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var recentlyDeletedDream: Dream? = null

    private var getDreamJob: Job? = null

    init {
        getDreams(OrderType.Date)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: DreamsEvent) {
        when (event) {
            is DreamsEvent.DeleteDream -> {
                viewModelScope.launch {
                    dreamUseCases.deleteDream(event.dream)
                    recentlyDeletedDream = event.dream
                }
            }
            is DreamsEvent.ToggleOrderSection -> {
                _state.value = _state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible)
            }
            is DreamsEvent.RestoreDream -> {
                viewModelScope.launch {
                    val dreamToRestore = recentlyDeletedDream?.copy(generatedImage = null) ?: return@launch
                    dreamUseCases.addDream(dreamToRestore)
                    recentlyDeletedDream = null
                }
            }
            is DreamsEvent.Order -> {
                if (state.value.orderType == event.orderType) {
                    return
                }
                getDreams(event.orderType)
            }
            is DreamsEvent.SearchDreams -> {
                _searchedText.value = event.searchQuery
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDreams(orderType: OrderType) {
        getDreamJob?.cancel()
        getDreamJob = dreamUseCases.getDreams(orderType)
            .combine(_searchedText) { dreams, searchText ->
                if (searchText.isBlank()) {
                    dreams
                } else {
                    dreams.filter { it.doesMatchSearchQuery(searchText) }
                }
            }
            .onEach { filteredDreams ->
                _state.value = state.value.copy(
                    dreams = filteredDreams,
                    orderType = orderType,
                )
            }
            .launchIn(viewModelScope)
    }
}


