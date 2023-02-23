package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.ImageGenerationDTO
import org.ballistic.dreamjournalai.feature_dream.domain.model.*
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.GetOpenAIImageGeneration
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.GetOpenAITextResponse
import javax.inject.Inject

@HiltViewModel
class AddEditDreamViewModel @Inject constructor( //add ai state later on
    private val dreamUseCases: DreamUseCases,
    private val getOpenAITextResponse: GetOpenAITextResponse,
    private val getOpenAIImageGeneration: GetOpenAIImageGeneration,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var dreamUiState = mutableStateOf(DreamUiState())
        private set

    val saveSuccess = mutableStateOf(false)

    var dialogState =  mutableStateOf(false)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    init {
        savedStateHandle.get<String>("dreamId")?.let { dreamId ->
            if (dreamId != "") {
                viewModelScope.launch {
                    dreamUiState.value = dreamUiState.value.copy(isLoading = true)
                    val resource = dreamUseCases.getDream(dreamId)
                    when (resource) {
                        is Resource.Success -> {
                            val dream = resource.data
                            dream?.let {
                                dreamUiState.value = DreamUiState(
                                    dreamTitle = dream.title,
                                    dreamContent = dream.content,
                                    dreamInfo = DreamInfo(
                                        dreamId = dream.id,
                                        dreamBackgroundImage = dream.backgroundImage,
                                        dreamIsLucid = dream.isLucid,
                                        dreamIsFavorite = dream.isFavorite,
                                        dreamIsNightmare = dream.isNightmare,
                                        dreamIsRecurring = dream.isRecurring,
                                        dreamIsFalseAwakening = dream.falseAwakening,
                                        dreamTimeOfDay = dream.timeOfDay,
                                        dreamLucidity = dream.lucidityRating,
                                        dreamVividness = dream.vividnessRating,
                                        dreamEmotion = dream.moodRating
                                    ),
                                    dreamAIExplanation = DreamAIExplanation(
                                        response = dream.AIResponse,
                                    ),
                                    dreamAIImage = DreamAIImage(
                                        image = dream.generatedImage,
                                    ),
                                    dreamGeneratedDetails = DreamAIGeneratedDetails(
                                        response = dream.generatedDetails,
                                    ),
                                    isLoading = false
                                )
                            }
                        }
                        is Resource.Error -> {
                            // handle error
                        }
                        is Resource.Loading -> {
                            // handle loading
                        }
                    }
                }
            }
        }
    }


    fun onEvent(event: AddEditDreamEvent) {
        when (event) {
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

            is AddEditDreamEvent.CLickGenerateAIImage -> {
                getOpenAIImageResponse()
            }
            is AddEditDreamEvent.ClickGenerateDetails -> {
                if (dreamUiState.value.dreamContent.length >= 1000) {
                    getAIDetailsResponse()
                } else {
                    dreamUiState.value = dreamUiState.value.copy(
                        dreamGeneratedDetails = DreamAIGeneratedDetails(
                            response = dreamUiState.value.dreamContent
                        )
                    )
                }
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
            is AddEditDreamEvent.ClickGenerateFromDescription -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamAIImage = DreamAIImage(
                        isLoading = true
                    )
                )
                dreamUiState.value = dreamUiState.value.copy(
                    dreamAIExplanation = DreamAIExplanation(
                        isLoading = true
                    )
                )
            }

            is AddEditDreamEvent.ChangeDetailsOfDream -> {
                dreamUiState.value = dreamUiState.value.copy(
                    dreamGeneratedDetails = DreamAIGeneratedDetails(
                        response = event.value
                    )
                )
            }

            is AddEditDreamEvent.DeleteDream -> {
                viewModelScope.launch {
                    dreamUseCases.deleteDream(SavedStateHandle()["dreamId"]!!)
                }
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
                                AIResponse = dreamUiState.value.dreamAIExplanation.response,
                                generatedDetails = dreamUiState.value.dreamGeneratedDetails.response,
                                generatedImage = dreamUiState.value.dreamAIImage.image,
                            )
                        )
                        saveSuccess.value = true
                        _eventFlow.emit(UiEvent.SaveDream)
                    } catch (e: InvalidDreamException) {
                        saveSuccess.value = false
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

    private fun getAIResponse() {
        viewModelScope.launch {
            val result = getOpenAITextResponse(
                Prompt(
                    "text-davinci-003",
                    "Analyze the following dream and try to find meaning in it: "
                            + dreamUiState.value.dreamContent, 250, 1, 0
                )
            )

            result.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data as Completion

                        dreamUiState.value = dreamUiState.value.copy(
                            dreamAIExplanation = dreamUiState.value.dreamAIExplanation.copy(
                                response = result.data.choices[0].text,
                                isLoading = false
                            )
                        )
                    }
                    is Resource.Error -> {
                        Log.d("AddEditDreamViewModel", "Error: ${result.message}")
                    }
                    is Resource.Loading -> {
                        dreamUiState.value = dreamUiState.value.copy(
                            dreamAIExplanation = dreamUiState.value.dreamAIExplanation.copy(
                                isLoading = true
                            )
                        )
                        Log.d("AddEditDreamViewModel", "Loading")
                    }
                }
            }
        }
    }

    private fun getAIDetailsResponse() {
        viewModelScope.launch {
            val result = getOpenAITextResponse(
                Prompt(
                    "text-davinci-002",
                    "From the following dream build a scene prompt so that it may be painted by an AI: : \n"
                            + dreamUiState.value.dreamContent, 60, 1, 0
                )
            )

            result.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data as Completion

                        dreamUiState.value = dreamUiState.value.copy(
                            dreamGeneratedDetails = dreamUiState.value.dreamGeneratedDetails.copy(
                                response = result.data.choices[0].text,
                                isSuccessful = true,
                                isLoading = false
                            )
                        )
                    }
                    is Resource.Error -> {
                        Log.d("AddEditDreamViewModel", "Error: ${result.message}")
                    }
                    is Resource.Loading -> {
                        dreamUiState.value = dreamUiState.value.copy(
                            dreamGeneratedDetails = dreamUiState.value.dreamGeneratedDetails.copy(
                                isLoading = true
                            )
                        )
                        Log.d("AddEditDreamViewModel", "Loading")
                    }
                }
            }
        }
    }

    private fun getOpenAIImageResponse() {
        viewModelScope.launch {
            val result = getOpenAIImageGeneration(
                ImagePrompt(
                    dreamUiState.value.dreamGeneratedDetails.response,
                    1,
                    "512x512"
                )
            )
            result.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data as ImageGenerationDTO

                        dreamUiState.value = dreamUiState.value.copy(
                            dreamAIImage = dreamUiState.value.dreamAIImage.copy(
                                image = result.data.dataList[0].url,
                                isLoading = false
                            )
                        )
                    }
                    is Resource.Error -> {
                        Log.d("AddEditDreamViewModel", "Error: ${result.message}")
                    }
                    is Resource.Loading -> {
                        dreamUiState.value = dreamUiState.value.copy(
                            dreamAIImage = dreamUiState.value.dreamAIImage.copy(
                                isLoading = true
                            )
                        )
                        Log.d("AddEditDreamViewModel", "Loading")
                    }
                }
            }
        }
    }

}

data class DreamUiState(
    val dreamTitle: String = "",
    val dreamContent: String = "",
    val dreamInfo: DreamInfo = DreamInfo(
        dreamId = "",
        dreamBackgroundImage = Dream.dreamBackgroundImages.random(),
        dreamIsLucid = false,
        dreamIsFavorite = false,
        dreamIsNightmare = false,
        dreamIsRecurring = false,
        dreamIsFalseAwakening = false,
        dreamTimeOfDay = "",
        dreamLucidity = 0,
        dreamVividness = 0,
        dreamEmotion = 0
    ),
    val dreamAIExplanation: DreamAIExplanation = DreamAIExplanation(
        response = "",
        isLoading = false,
        error = "",
    ),
    val dreamAIImage: DreamAIImage = DreamAIImage(
        image = null,
        isLoading = false,
        error = ""
    ),
    val dreamGeneratedDetails: DreamAIGeneratedDetails = DreamAIGeneratedDetails(
        response = "",
        isLoading = false,
        isSuccessful = false,
        error = ""
    ),
    val isLoading: Boolean = false,
)

data class DreamAIExplanation(
    val response: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class DreamAIGeneratedDetails(
    val response: String = "",
    val isLoading: Boolean = false,
    val isSuccessful: Boolean = false,
    val error: String? = null,
)

data class DreamAIImage(
    val image: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DreamInfo(
    val dreamId: String?,
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