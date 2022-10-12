package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.model.InvalidDreamException
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.DreamUseCases
import javax.inject.Inject

@HiltViewModel
class AddEditDreamViewModel @Inject constructor( //add ai state later on
    private val dreamUseCases: DreamUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _dreamTitle = mutableStateOf(DreamTextFieldState(
        hint = "Enter Dream Title..."
    ))
    val dreamTitle: State<DreamTextFieldState> = _dreamTitle

    private val _dreamContent = mutableStateOf(DreamTextFieldState(
        hint = "Enter Dream Content..."
    ))
    val dreamContent: State<DreamTextFieldState> = _dreamContent

    private val _dreamColor = mutableStateOf<Int>(Dream.dreamColors.random().toArgb())
    val dreamColor: State<Int> = _dreamColor

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentDreamId: Int? = null

    init {
        savedStateHandle.get<Int>("dreamId")?.let { dreamId ->
            if(dreamId != -1) {
                viewModelScope.launch {
                    dreamUseCases.getDream(dreamId)?.also { dream ->
                        currentDreamId = dream.id
                        _dreamTitle.value = dreamTitle.value.copy(
                            text = dream.title,
                            isHintVisible = false
                        )
                        _dreamContent.value = _dreamContent.value.copy(
                            text = dream.content,
                            isHintVisible = false
                        )
                        _dreamColor.value = dream.color
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditDreamEvent){
        when(event){
            is AddEditDreamEvent.EnteredTitle -> {
                _dreamTitle.value = dreamTitle.value.copy(
                    text = event.value
                )
            }
            is AddEditDreamEvent.ChangeTitleFocus -> {
                _dreamTitle.value = dreamTitle.value.copy(
                    isHintVisible = !event.focusState.isFocused && dreamTitle.value.text.isBlank()
                )
            }
            is AddEditDreamEvent.EnteredContent -> {
                _dreamContent.value = dreamContent.value.copy(
                    text = event.value
                )
            }
            is AddEditDreamEvent.ChangeContentFocus -> {
                _dreamContent.value = dreamContent.value.copy(
                    isHintVisible = !event.focusState.isFocused && dreamContent.value.text.isBlank()
                )
            }
            is AddEditDreamEvent.ChangeColor -> {
                _dreamColor.value = event.color
            }
            is AddEditDreamEvent.SaveDream -> {
                viewModelScope.launch {
                    try {
                        dreamUseCases.addDream(
                            Dream(
                                title = dreamTitle.value.text,
                                content = dreamContent.value.text,
                                timestamp = System.currentTimeMillis(),
                                color = dreamColor.value,
                                id = currentDreamId
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveDream)
                    } catch (e: InvalidDreamException) {
                        _eventFlow.emit(UiEvent.ShowSnackBar(e.message ?: "Couldn't save dream"))
                    }
                }
            }



        }
    }

    sealed class UiEvent {
        data class ShowSnackBar(val message: String) : UiEvent()
        object SaveDream : UiEvent()
    }

}