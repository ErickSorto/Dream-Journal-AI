package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.data.remote.OpenAIApi
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.model.InvalidDreamException
import org.ballistic.dreamjournalai.feature_dream.domain.model.Prompt
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.AIResponse
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.GetOpenAITextResponse
import javax.inject.Inject

@HiltViewModel
class AddEditDreamViewModel @Inject constructor( //add ai state later on
    private val dreamUseCases: DreamUseCases,
    private val getOpenAITextResponse: GetOpenAITextResponse,
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

    private val _dreamAIResult = mutableStateOf(DreamTextFieldState(
        hint = "Dream AI Result..."
    ))
    val dreamAIResult: State<DreamTextFieldState> = _dreamAIResult

    private val _dreamTime = mutableStateOf(DreamTextFieldState(
        hint = "Enter Dream Time..."
    ))
    val dreamTime: State<DreamTextFieldState> = _dreamTime

    private val _dreamLucidity = mutableStateOf(DreamIntRating(
        rating = 0
    ))
    val dreamLucidity: State<DreamIntRating> = _dreamLucidity

    private val _dreamVividness = mutableStateOf(DreamIntRating(
        rating = 0
    ))
    val dreamVividness: State<DreamIntRating> = _dreamVividness

    private val _dreamRealism = mutableStateOf(DreamIntRating(
        rating = 0
    ))
    val dreamRealism: State<DreamIntRating> = _dreamRealism

    private val _dreamEmotion = mutableStateOf(DreamIntRating(
        rating = 0
    ))
    val dreamEmotion: State<DreamIntRating> = _dreamEmotion

    private val _dreamIsNightmare = mutableStateOf(DreamPropertyBoolean(
        value = false
    ))
    val dreamIsNightmare: State<DreamPropertyBoolean> = _dreamIsNightmare

    private val _dreamIsRecurring = mutableStateOf(DreamPropertyBoolean(
        value = false
    ))
    val dreamIsRecurring: State<DreamPropertyBoolean> = _dreamIsRecurring

    private val _dreamIsLucid = mutableStateOf(DreamPropertyBoolean(
        value = false
    ))
    val dreamIsLucid: State<DreamPropertyBoolean> = _dreamIsLucid

    private val _dreamIsFavourite = mutableStateOf(DreamPropertyBoolean(
        value = false
    ))
    val dreamIsFavourite: State<DreamPropertyBoolean> = _dreamIsFavourite

    private val _isFalseAwakening = mutableStateOf(DreamPropertyBoolean(
        value = false
    ))
    val isFalseAwakening: State<DreamPropertyBoolean> = _isFalseAwakening


    private val _dreamBackgroundColor = mutableStateOf<Int>(Dream.dreamBackgroundColors.random())
    val dreamBackgroundColor: State<Int> = _dreamBackgroundColor

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
                        _dreamBackgroundColor.value = dream.dreamImageBackground
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
                    isHintVisible = !event.focusState.isFocused &&
                            dreamTitle.value.text.isBlank()
                )
            }
            is AddEditDreamEvent.EnteredContent -> {
                _dreamContent.value = _dreamContent.value.copy(
                    text = event.value
                )
            }
            is AddEditDreamEvent.ChangeContentFocus -> {
                _dreamContent.value = _dreamContent.value.copy(
                    isHintVisible = !event.focusState.isFocused && _dreamContent.value.text.isBlank()
                )
            }
            is AddEditDreamEvent.ChangeColorBackground -> {
                _dreamBackgroundColor.value = event.colorBackGroundImage
            }
            is AddEditDreamEvent.ClickGenerateAIResponse -> {
                _dreamAIResult.value = _dreamAIResult.value.copy(
                 text = getOpenAITextResponse.invoke(prompt = Prompt(
                     "text-davinci-002",_dreamContent.value.text,250,1,0)).toString()
                )
            }
            is AddEditDreamEvent.ChangeRealism -> {
                _dreamRealism.value = _dreamRealism.value.copy(
                    rating = event.realism
                )
            }
            is AddEditDreamEvent.ChangeLucidity -> {
                _dreamLucidity.value = _dreamLucidity.value.copy(
                    rating = event.lucidity
                )
            }
            is AddEditDreamEvent.ChangeVividness -> {
                _dreamVividness.value = _dreamVividness.value.copy(
                    rating = event.vividness
                )
            }
            is AddEditDreamEvent.ChangeMood -> {
                _dreamEmotion.value = _dreamEmotion.value.copy(
                    rating = event.mood
                )
            }
            is AddEditDreamEvent.ChangeNightmare -> {
                _dreamIsNightmare.value = _dreamIsNightmare.value.copy(
                    value = event.boolean
                )
            }
            is AddEditDreamEvent.ChangeRecurrence -> {
                _dreamIsRecurring.value = _dreamIsRecurring.value.copy(
                    value = event.boolean
                )
            }
            is AddEditDreamEvent.ChangeIsLucid -> {
                _dreamIsLucid.value = _dreamIsLucid.value.copy(
                    value = event.boolean
                )
            }
            is AddEditDreamEvent.ChangeFavorite -> {
                _dreamIsFavourite.value = _dreamIsFavourite.value.copy(
                    value = event.boolean
                )
            }
            is AddEditDreamEvent.ChangeFalseAwakening -> {
                _isFalseAwakening.value = _isFalseAwakening.value.copy(
                    value = event.boolean
                )
            }
            is AddEditDreamEvent.ChangeTimeOfDay -> {
                _dreamTime.value = _dreamTime.value.copy(
                    text = event.timeOfDay
                )
            }


            is AddEditDreamEvent.SaveDream -> {
                viewModelScope.launch {
                    try {
                        dreamUseCases.addDream(
                            Dream(
                                title = dreamTitle.value.text,
                                content = dreamContent.value.text,
                                timestamp = System.currentTimeMillis(),
                                dreamImageBackground = dreamBackgroundColor.value,
                                id = currentDreamId,
                                isLucid = dreamIsLucid.value.value,
                                isNightmare = dreamIsNightmare.value.value,
                                isRecurring = dreamIsRecurring.value.value,
                                isFavorite = dreamIsFavourite.value.value,
                                lucidityRating = dreamLucidity.value.rating,
                                vividityRating = dreamVividness.value.rating,
                                moodRating = dreamEmotion.value.rating,
                                timeOfDay = dreamTime.value.text,
                                realismRating = dreamRealism.value.rating,
                                falseAwakening = isFalseAwakening.value.value,
                                AIResponse = dreamAIResult.value.text
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