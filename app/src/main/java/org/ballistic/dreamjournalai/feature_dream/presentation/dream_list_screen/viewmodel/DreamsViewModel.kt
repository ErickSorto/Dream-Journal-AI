package org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.feature_dream.domain.util.DreamOrder
import org.ballistic.dreamjournalai.feature_dream.domain.util.OrderType
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.DreamsEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.state.DreamsState
import javax.inject.Inject

@HiltViewModel
class DreamsViewModel @Inject constructor(
    private val dreamUseCases: DreamUseCases
) : ViewModel() {

    private val _state = mutableStateOf(DreamsState())
    val state: State<DreamsState> = _state

    private var recentlyDeletedDream: Dream? = null

    private var getDreamJob: Job? = null

    init {
        getDreams(DreamOrder.Date(OrderType.Descending))
    }

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
                    dreamUseCases.addDream(recentlyDeletedDream ?: return@launch)
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

    private fun getDreams(dreamOrder: DreamOrder){
        getDreamJob?.cancel()
        getDreamJob = dreamUseCases.getDreams(dreamOrder).onEach { dreams ->
            _state.value = state.value.copy(
                dreams = dreams,
                dreamOrder = dreamOrder,
            )
        }.launchIn(viewModelScope)
    }
}

