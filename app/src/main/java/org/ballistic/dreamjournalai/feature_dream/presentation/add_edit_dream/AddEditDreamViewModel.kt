package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.DreamUseCases
import javax.inject.Inject

@HiltViewModel
class AddEditDreamViewModel @Inject constructor( //add ai state later on
    private val dreamUseCases: DreamUseCases,

) : ViewModel() {

    private val _dreamTitle = mutableStateOf(DreamTextFieldState())
    val dreamTitle: State<DreamTextFieldState> = _dreamTitle

    private val _dreamTitle = mutableStateOf(DreamTextFieldState())
    val dreamTitle: State<DreamTextFieldState> = _dreamTitle
}