package org.ballistic.dreamcatcherai.feature_dream.presentation.dreams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.ballistic.dreamcatcherai.feature_dream.domain.use_case.DreamUseCases
import javax.inject.Inject

@HiltViewModel
class DreamsViewModel @Inject constructor(
    private val dreamUseCases: DreamUseCases
) : ViewModel() {

    val dreams = dreamUseCases.getDreams().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


