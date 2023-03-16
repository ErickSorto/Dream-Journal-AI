package org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.feature_dream.domain.util.DreamOrder
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

    private val _state = mutableStateOf(DreamsState())
    val state: State<DreamsState> = _state

    private val _searchDreams = MutableStateFlow("")
    val searchDreams: StateFlow<String> = _searchDreams.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var recentlyDeletedDream: Dream? = null

    private var getDreamJob: Job? = null

    init {
        getDreams(DreamOrder.Date(OrderType.Descending))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: DreamsEvent) {
        when(event) {
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
                if (state.value.dreamOrder::class == event.dreamType::class &&
                    state.value.dreamOrder.orderType == event.dreamType.orderType) {
                    return
                }
                getDreams(event.dreamType)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSearchChange(text: String) {
        _searchDreams.value = text
        _isSearching.value = true
        getDreams(state.value.dreamOrder)
        _isSearching.value = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDreams(dreamOrder: DreamOrder) {
        getDreamJob?.cancel()
        getDreamJob = dreamUseCases.getDreams(dreamOrder).onEach { dreams ->
            val filteredDreams = if (searchDreams.value.isBlank()) {
                dreams
            } else {
                dreams.filter { it.doesMatchSearchQuery(searchDreams.value) }
            }
            _state.value = state.value.copy(
                dreams = filteredDreams,
                dreamOrder = dreamOrder,
                searchText = searchDreams.value
            )
        }.launchIn(viewModelScope)
    }
}


