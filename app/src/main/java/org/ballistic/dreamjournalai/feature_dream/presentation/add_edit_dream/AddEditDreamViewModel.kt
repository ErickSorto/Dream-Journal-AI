package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.model.InvalidDreamException
import org.ballistic.dreamjournalai.feature_dream.domain.model.Prompt
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.GetOpenAITextResponse
import javax.inject.Inject

@HiltViewModel
class AddEditDreamViewModel @Inject constructor( //add ai state later on
    private val dreamUseCases: DreamUseCases,
    private val getOpenAITextResponse: GetOpenAITextResponse,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var dreamUiState = mutableStateOf(DreamUiState())
        private set

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        savedStateHandle.get<Int>("dreamId")?.let { dreamId ->
            if(dreamId != -1) {
                viewModelScope.launch {
                    dreamUseCases.getDream(dreamId)?.also { dream ->

                        dreamUiState.value = dream.id?.let {
                            DreamInfo(
                                dreamId = it,
                                dreamBackgroundImage= dream.backgroundImage,
                                dreamTimeOfDay = dream.timeOfDay,
                                dreamLucidity = dream.lucidityRating,
                                dreamVividness = dream.vividnessRating,
                                dreamEmotion = dream.moodRating,
                                dreamIsNightmare = dream.isNightmare,
                                dreamIsRecurring = dream.isRecurring,
                                dreamIsLucid = dream.isLucid,
                                dreamIsFavorite = dream.isFavorite,
                                dreamIsFalseAwakening = dream.falseAwakening
                            )
                        }?.let {
                            dreamUiState.value.copy(
                                dreamTitle = dream.title,
                                dreamContent = dream.content,
                                dreamInfo = it,
                            )
                        }!!
                    }
                }
            }
        }
    }



    fun onEvent(event: AddEditDreamEvent){
        when(event){
            is AddEditDreamEvent.EnteredTitle -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamTitle = event.value
                )
            }

            is AddEditDreamEvent.EnteredContent -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamContent = event.value
                )
            }

            is AddEditDreamEvent.ChangeDreamBackgroundImage -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamInfo = dreamUiState.value.dreamInfo.copy(
                        dreamBackgroundImage = event.dreamBackGroundImage
                    )
                )
            }
            is AddEditDreamEvent.ClickGenerateAIResponse -> {
                getAIResponse()
            }

            is AddEditDreamEvent.ChangeLucidity -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamInfo = dreamUiState.value.dreamInfo.copy(
                        dreamLucidity = event.lucidity
                    )
                )
            }
            is AddEditDreamEvent.ChangeVividness -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamInfo = dreamUiState.value.dreamInfo.copy(
                        dreamVividness = event.vividness
                    )
                )
            }
            is AddEditDreamEvent.ChangeMood -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamInfo = dreamUiState.value.dreamInfo.copy(
                        dreamEmotion = event.mood
                    )
                )
            }
            is AddEditDreamEvent.ChangeNightmare -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamInfo = dreamUiState.value.dreamInfo.copy(
                        dreamIsNightmare = event.boolean
                    )
                )
            }
            is AddEditDreamEvent.ChangeRecurrence -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamInfo = dreamUiState.value.dreamInfo.copy(
                        dreamIsRecurring = event.boolean
                    )
                )
            }
            is AddEditDreamEvent.ChangeIsLucid -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamInfo = dreamUiState.value.dreamInfo.copy(
                        dreamIsLucid = event.boolean
                    )
                )
            }
            is AddEditDreamEvent.ChangeFavorite -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamInfo = dreamUiState.value.dreamInfo.copy(
                        dreamIsFavorite = event.boolean
                    )
                )
            }
            is AddEditDreamEvent.ChangeFalseAwakening -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamInfo = dreamUiState.value.dreamInfo.copy(
                        dreamIsFalseAwakening = event.boolean
                    )
                )
            }
            is AddEditDreamEvent.ChangeTimeOfDay -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamInfo = dreamUiState.value.dreamInfo.copy(
                        dreamTimeOfDay = event.timeOfDay
                    )
                )
            }

            is AddEditDreamEvent.SaveDream -> {
                viewModelScope.launch {
                    try {
                        dreamUseCases.addDream(
                            Dream(
                                title = dreamUiState.value.dreamTitle,
                                content = dreamUiState.value.dreamContent,
                                backgroundImage = dreamUiState.value.dreamInfo.dreamBackgroundImage,
                                id = dreamUiState.value.dreamInfo.dreamId,
                                isLucid = dreamUiState.value.dreamInfo.dreamIsLucid,
                                isNightmare = dreamUiState.value.dreamInfo.dreamIsNightmare,
                                isRecurring = dreamUiState.value.dreamInfo.dreamIsRecurring,
                                isFavorite = dreamUiState.value.dreamInfo.dreamIsFavorite,
                                lucidityRating = dreamUiState.value.dreamInfo.dreamLucidity,
                                vividnessRating = dreamUiState.value.dreamInfo.dreamVividness,
                                moodRating = dreamUiState.value.dreamInfo.dreamEmotion,
                                timeOfDay = dreamUiState.value.dreamInfo.dreamTimeOfDay,
                                falseAwakening = dreamUiState.value.dreamInfo.dreamIsFalseAwakening,
                                AIResponse = dreamUiState.value.dreamAIExplanation
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

//    private fun getAIResponse() {
//        viewModelScope.launch {
//
//            val response = getOpenAITextResponse.invoke(Prompt("text-davinci-002", "Analyze the following dream and try to find meaning in it: " + dreamUiState.value.dreamContent, 250,1,0))
//            dreamUiState.value = dreamUiState.value.copy(
//                dreamAIExplanation = response
//            )
//        }
//    }

    private fun getAIResponse(){
            val result = getOpenAITextResponse(Prompt("text-davinci-002",
                "Analyze the following dream and try to find meaning in it: "
                        + dreamUiState.value.dreamContent, 250,1,0))
            when(result){
                is Resource.Success -> {
                    dreamUiState.value = dreamUiState.value.copy(
                        dreamAIExplanation = result.data as String
                    )
                }
                is Resource.Error -> {
                    Log.d("AddEditDreamViewModel", "Error: ${result.message}")
                }
                is Resource.Loading -> {
                    Log.d("AddEditDreamViewModel", "Loading")
                }
            }
    }

}

data class DreamUiState(
    val dreamTitle: String = "",
    val dreamContent: String ="",
    val dreamInfo: DreamInfo = DreamInfo(
        dreamId = 0,
        dreamBackgroundImage =  Dream.dreamBackgroundImages.random(),
        dreamIsLucid = false, dreamIsFavorite = false, dreamIsNightmare = false, dreamIsRecurring = false, dreamIsFalseAwakening = false,
        dreamTimeOfDay = "", dreamLucidity = 0, dreamVividness = 0, dreamEmotion = 0
    ),
    val dreamAIExplanation: String = ""
)

data class DreamInfo(
    val dreamId: Int,
    var dreamBackgroundImage: Int,
    val dreamIsLucid: Boolean,
    val dreamIsFavorite: Boolean,
    val dreamIsNightmare: Boolean,
    val dreamIsRecurring: Boolean,
    val dreamIsFalseAwakening: Boolean,
    val dreamTimeOfDay: String,
    val dreamLucidity: Int,
    val dreamVividness: Int,
    val dreamEmotion: Int,
)
