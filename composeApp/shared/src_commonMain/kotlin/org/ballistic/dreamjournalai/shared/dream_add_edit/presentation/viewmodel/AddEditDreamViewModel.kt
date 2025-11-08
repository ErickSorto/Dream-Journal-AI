package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.domain.DictionaryRepository
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.core.util.formatLocalDate
import org.ballistic.dreamjournalai.shared.core.util.formatLocalTime
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.InvalidDreamException
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.viewmodel.DictionaryWord
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.ballistic.dreamjournalai.shared.dream_add_edit.data.AIResult
import org.ballistic.dreamjournalai.shared.dream_add_edit.data.AITextType
import org.ballistic.dreamjournalai.shared.dream_add_edit.data.DreamAIService

// Get current date
@OptIn(ExperimentalTime::class)
private val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
private val currentDate = now.date

// Static sleep and wake times
private val sleepTime = LocalTime(23, 0) // 11 PM
private val wakeTime = LocalTime(7, 0)   // 7 A
private val logger = Logger.withTag("AddEditViewModel")

enum class AIType {
    INTERPRETATION,
    IMAGE,
    ADVICE,
    DETAILS,
    QUESTION_ANSWER,
    STORY,
    MOOD
}

class AddEditDreamViewModel(
    savedStateHandle: SavedStateHandle,
    private val dreamUseCases: DreamUseCases,
    private val authRepository: AuthRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val vibratorUtil: VibratorUtil,
    private val aiService: DreamAIService, // injected
) : ViewModel() {

    private val _addEditDreamState = MutableStateFlow(AddEditDreamState())
    val addEditDreamState: StateFlow<AddEditDreamState> = _addEditDreamState.asStateFlow()

    private val _titleTextFieldState = MutableStateFlow(TextFieldState())
    val titleTextFieldState: StateFlow<TextFieldState> = _titleTextFieldState.asStateFlow()

    private val _contentTextFieldState = MutableStateFlow(TextFieldState())
    val contentTextFieldState: StateFlow<TextFieldState> = _contentTextFieldState.asStateFlow()

    // Small helper to show snackbars succinctly
    private suspend fun showSnack(
        message: String,
        actionLabel: String = "Dismiss",
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        _addEditDreamState.value.snackBarHostState.value.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration
        )
    }

    // Map a saved Dream into our screen state
    private fun stateFromDream(dream: Dream): AddEditDreamState {
        val aiStates = mutableMapOf<AIType, AIState>()
        aiStates[AIType.INTERPRETATION] = AIState(response = dream.AIResponse)
        aiStates[AIType.IMAGE] = AIState(response = dream.generatedImage)
        aiStates[AIType.QUESTION_ANSWER] = AIState(response = dream.dreamAIQuestionAnswer, question = dream.dreamQuestion)
        aiStates[AIType.ADVICE] = AIState(response = dream.dreamAIAdvice)
        aiStates[AIType.MOOD] = AIState(response = dream.dreamAIMood)
        aiStates[AIType.STORY] = AIState(response = dream.dreamAIStory)
        aiStates[AIType.DETAILS] = AIState(response = dream.generatedDetails)

        return AddEditDreamState(
            dreamInfo = DreamInfo(
                dreamId = dream.id,
                dreamUID = dream.uid,
                dreamBackgroundImage = dream.backgroundImage,
                dreamIsLucid = dream.isLucid,
                dreamIsFavorite = dream.isFavorite,
                dreamIsNightmare = dream.isNightmare,
                dreamIsRecurring = dream.isRecurring,
                dreamIsFalseAwakening = dream.falseAwakening,
                dreamSleepTime = dream.sleepTime,
                dreamWakeTime = dream.wakeTime,
                dreamDate = dream.date,
                dreamTimeOfDay = dream.timeOfDay,
                dreamLucidity = dream.lucidityRating,
                dreamVividness = dream.vividnessRating,
                dreamEmotion = dream.moodRating
            ),
            aiStates = aiStates
        )
    }


    override fun onCleared() {
        super.onCleared()
        onEvent(AddEditDreamEvent.OnCleared)
    }

    init {
        savedStateHandle.get<String>("dreamID")?.let { dreamId ->
            if (dreamId.isNotEmpty()) {
                viewModelScope.launch {
                    when (val resource = dreamUseCases.getDream(dreamId)) {
                        is Resource.Success<*> -> {
                            resource.data?.let { dream: Dream ->
                                _titleTextFieldState.value = TextFieldState(initialText = dream.title)
                                _contentTextFieldState.value = TextFieldState(initialText = dream.content)
                                _addEditDreamState.value = stateFromDream(dream)
                            }
                            onEvent(AddEditDreamEvent.ToggleDreamHasChanged(false))
                            onEvent(AddEditDreamEvent.GetUnlockedWords)
                            onEvent(AddEditDreamEvent.LoadWords)
                            onEvent(AddEditDreamEvent.GetDreamTokens)
                        }
                        is Resource.Error<*> -> {
                            logger.e { "init: load dream failed: ${resource.message}" }
                            viewModelScope.launch { showSnack("Couldn't get dream :(") }
                        }
                        is Resource.Loading<*> -> { /* no-op */ }
                    }
                }
            } else {
                onEvent(AddEditDreamEvent.GetUnlockedWords)
                onEvent(AddEditDreamEvent.LoadWords)
                onEvent(AddEditDreamEvent.GetDreamTokens)
            }
        }
    }

    // Helper to mark dream as changed without dispatching another event
    private fun markChanged() {
        _addEditDreamState.update { it.copy(dreamHasChanged = true) }
    }

    // Helper to update DreamInfo fields succinctly
    private fun updateDreamInfo(transform: DreamInfo.() -> DreamInfo) {
        _addEditDreamState.update { it.copy(dreamInfo = it.dreamInfo.transform()) }
    }

    // Generic helper to update an AI data section
    private fun updateAIState(type: AIType, transform: AIState.() -> AIState) {
        _addEditDreamState.update { state ->
            val newStates = state.aiStates.toMutableMap()
            newStates[type] = newStates[type]!!.transform()
            state.copy(aiStates = newStates)
        }
    }

    // Consolidated save logic
    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    private suspend fun performSave(onSaveSuccess: () -> Unit) {
        // Auto title if blank
        val contentText = contentTextFieldState.value.text.toString()
        if (titleTextFieldState.value.text.isBlank() && contentText.isNotBlank()) {
            when (val titleRes = aiService.generateText(AITextType.TITLE, contentText, cost = 0)) {
                is AIResult.Success -> _titleTextFieldState.value = TextFieldState(initialText = titleRes.data)
                is AIResult.Error -> Logger.w { "Title gen failed: ${titleRes.message}" }
            }
        }
        if (_addEditDreamState.value.dreamInfo.dreamId.isNullOrEmpty()) {
            updateDreamInfo { copy(dreamId = Uuid.random().toString()) }
        }

        try {
            val dreamToSave = Dream(
                id = addEditDreamState.value.dreamInfo.dreamId,
                uid = addEditDreamState.value.dreamInfo.dreamUID,
                title = titleTextFieldState.value.text.toString(),
                content = contentTextFieldState.value.text.toString(),
                timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                date = addEditDreamState.value.dreamInfo.dreamDate,
                sleepTime = addEditDreamState.value.dreamInfo.dreamSleepTime,
                wakeTime = addEditDreamState.value.dreamInfo.dreamWakeTime,
                AIResponse = addEditDreamState.value.aiStates[AIType.INTERPRETATION]?.response ?: "",
                isFavorite = addEditDreamState.value.dreamInfo.dreamIsFavorite,
                isLucid = addEditDreamState.value.dreamInfo.dreamIsLucid,
                isNightmare = addEditDreamState.value.dreamInfo.dreamIsNightmare,
                isRecurring = addEditDreamState.value.dreamInfo.dreamIsRecurring,
                falseAwakening = addEditDreamState.value.dreamInfo.dreamIsFalseAwakening,
                lucidityRating = addEditDreamState.value.dreamInfo.dreamLucidity,
                moodRating = addEditDreamState.value.dreamInfo.dreamEmotion,
                vividnessRating = addEditDreamState.value.dreamInfo.dreamVividness,
                timeOfDay = addEditDreamState.value.dreamInfo.dreamTimeOfDay,
                backgroundImage = addEditDreamState.value.dreamInfo.dreamBackgroundImage,
                generatedImage = addEditDreamState.value.aiStates[AIType.IMAGE]?.response ?: "",
                generatedDetails = addEditDreamState.value.aiStates[AIType.DETAILS]?.response ?: "",
                dreamQuestion = addEditDreamState.value.aiStates[AIType.QUESTION_ANSWER]?.question ?: "",
                dreamAIQuestionAnswer = addEditDreamState.value.aiStates[AIType.QUESTION_ANSWER]?.response ?: "",
                dreamAIStory = addEditDreamState.value.aiStates[AIType.STORY]?.response ?: "",
                dreamAIAdvice = addEditDreamState.value.aiStates[AIType.ADVICE]?.response ?: "",
                dreamAIMood = addEditDreamState.value.aiStates[AIType.MOOD]?.response ?: ""
            )
            dreamUseCases.addDream(dreamToSave)

            // Refresh for canonical storage URL (condensed)
            val savedId = addEditDreamState.value.dreamInfo.dreamId
            if (!savedId.isNullOrBlank()) {
                repeat(12) { _ ->
                    when (val refreshed = dreamUseCases.getDream(savedId)) {
                        is Resource.Success<*> -> {
                            val latest = refreshed.data as Dream
                            val url = latest.generatedImage
                            val isFinalFirebase = url.contains("firebasestorage.googleapis.com")
                            if (isFinalFirebase) {
                                updateAIState(AIType.IMAGE) { copy(response = url) }
                                updateAIState(AIType.DETAILS) { copy(response = latest.generatedDetails) }
                                return@repeat
                            }
                        }
                        else -> Unit
                    }
                    kotlinx.coroutines.delay(250)
                }
            }

            _addEditDreamState.update { it.copy(dreamIsSavingLoading = false) }
            onSaveSuccess()
        } catch (e: InvalidDreamException) {
            _addEditDreamState.update { it.copy(dreamIsSavingLoading = false) }
            addEditDreamState.value.snackBarHostState.value.showSnackbar(
                e.message ?: "Couldn't save dream :(",
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Long
            )
        }
    }

    // Consolidated unlock word handling
    private fun handleUnlockWord(event: AddEditDreamEvent.ClickBuyWord) {
        viewModelScope.launch {
            val cost = if (event.isAd) 0 else event.dictionaryWord.cost
            val result = authRepository.unlockWord(event.dictionaryWord.word, cost)
            processUnlockWordResult(result, event.dictionaryWord)
            _addEditDreamState.update { it.copy(isDreamExitOff = false) }
        }
    }

    // Load dictionary words from CSV
    private fun loadWords() {
        viewModelScope.launch(Dispatchers.Default) {
            val words = dictionaryRepository.loadDictionaryWordsFromCsv("dream_dictionary.csv")
            _addEditDreamState.update { state -> state.copy(dictionaryWordMutableList = words.toMutableList()) }
        }
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    fun onEvent(event: AddEditDreamEvent) {
        when (event) {
            is AddEditDreamEvent.ChangeDreamBackgroundImage -> {
                markChanged(); updateDreamInfo { copy(dreamBackgroundImage = event.dreamBackGroundImage) }
            }
            is AddEditDreamEvent.ClickGenerateAIResponse -> { markChanged(); requestAIText(AIType.INTERPRETATION, event.content, event.cost) }
            is AddEditDreamEvent.AdAIResponseToggle -> _addEditDreamState.update { it.copy(isAdResponse = event.value) }
            is AddEditDreamEvent.ClickGenerateAIAdvice -> { markChanged(); requestAIText(AIType.ADVICE, event.content, event.cost) }
            is AddEditDreamEvent.AdAIAdviceToggle -> _addEditDreamState.update { it.copy(isAdAdvice = event.value) }
            is AddEditDreamEvent.ClickGenerateMood -> { markChanged(); requestAIText(AIType.MOOD, event.content, event.cost) }
            is AddEditDreamEvent.AdMoodToggle -> _addEditDreamState.update { it.copy(isAdMood = event.value) }
            is AddEditDreamEvent.ClickGenerateStory -> { markChanged(); requestAIText(AIType.STORY, event.content, event.cost) }
            is AddEditDreamEvent.AdStoryToggle -> _addEditDreamState.update { it.copy(isAdStory = event.value) }
            is AddEditDreamEvent.ClickGenerateFromQuestion -> { markChanged(); requestAIText(AIType.QUESTION_ANSWER, event.content, event.cost) }
            is AddEditDreamEvent.AdQuestionToggle -> _addEditDreamState.update { it.copy(isAdQuestion = event.value) }
            is AddEditDreamEvent.ClickGenerateAIImage -> { markChanged(); requestImage(event.content, event.cost) }
            is AddEditDreamEvent.AdAIImageToggle -> _addEditDreamState.update { it.copy(isAdImage = event.value) }
            is AddEditDreamEvent.ChangeLucidity -> { markChanged(); updateDreamInfo { copy(dreamLucidity = event.lucidity) } }
            is AddEditDreamEvent.ChangeVividness -> { markChanged(); updateDreamInfo { copy(dreamVividness = event.vividness) } }
            is AddEditDreamEvent.ChangeMood -> { markChanged(); updateDreamInfo { copy(dreamEmotion = event.mood) } }
            is AddEditDreamEvent.ChangeNightmare -> { markChanged(); updateDreamInfo { copy(dreamIsNightmare = event.boolean) } }
            is AddEditDreamEvent.ChangeRecurrence -> { markChanged(); updateDreamInfo { copy(dreamIsRecurring = event.boolean) } }
            is AddEditDreamEvent.ChangeIsLucid -> { markChanged(); updateDreamInfo { copy(dreamIsLucid = event.boolean) } }
            is AddEditDreamEvent.ChangeFavorite -> { markChanged(); updateDreamInfo { copy(dreamIsFavorite = event.boolean) } }
            is AddEditDreamEvent.ChangeFalseAwakening -> { markChanged(); updateDreamInfo { copy(dreamIsFalseAwakening = event.boolean) } }
            is AddEditDreamEvent.ChangeTimeOfDay -> { markChanged(); updateDreamInfo { copy(dreamTimeOfDay = event.timeOfDay) } }
            is AddEditDreamEvent.ClickGenerateFromDescription -> {
                updateAIState(AIType.IMAGE) { copy(isLoading = true) }
                updateAIState(AIType.INTERPRETATION) { copy(isLoading = true) }
            }
            is AddEditDreamEvent.ChangeDetailsOfDream -> { markChanged(); updateAIState(AIType.DETAILS) { copy(response = event.value) } }
            is AddEditDreamEvent.DeleteDream -> viewModelScope.launch { dreamUseCases.deleteDream(SavedStateHandle()["dreamId"]!!) }
            is AddEditDreamEvent.ChangeDreamDate -> { markChanged(); updateDreamInfo { copy(dreamDate = formatLocalDate(event.value)) } }
            is AddEditDreamEvent.ChangeDreamWakeTime -> { markChanged(); updateDreamInfo { copy(dreamWakeTime = formatLocalTime(event.value)) } }
            is AddEditDreamEvent.ChangeDreamSleepTime -> { markChanged(); updateDreamInfo { copy(dreamSleepTime = formatLocalTime(event.value)) } }
            is AddEditDreamEvent.ChangeQuestionOfDream -> { markChanged(); updateAIState(AIType.QUESTION_ANSWER) { copy(question = event.value) } }
            is AddEditDreamEvent.ClickWord -> _addEditDreamState.update { it.copy(bottomSheetState = true, isClickedWordUnlocked = event.word.cost == 0, clickedWord = event.word) }
            is AddEditDreamEvent.FilterDreamWordInDictionary -> {
                viewModelScope.launch(Dispatchers.Default) {
                    val content = contentTextFieldState.value.text.toString()
                    val dictionaryList = addEditDreamState.value.dictionaryWordMutableList
                    val filteredWords = dictionaryRepository.dictionaryWordsInDreamFilterList(
                        dreamContent = content,
                        dictionaryWordList = dictionaryList
                    )
                    _addEditDreamState.update { it.copy(dreamFilteredDictionaryWords = filteredWords, dreamContentChanged = false) }
                }
            }
            is AddEditDreamEvent.LoadWords -> viewModelScope.launch { loadWords() }
            is AddEditDreamEvent.ContentHasChanged -> _addEditDreamState.update { it.copy(dreamContentChanged = true, dreamHasChanged = true) }
            is AddEditDreamEvent.GetUnlockedWords -> {
                viewModelScope.launch {
                    authRepository.getUnlockedWords().collect { result ->
                        when (result) {
                            is Resource.Success -> _addEditDreamState.update { it.copy(unlockedWords = result.data?.toMutableList() ?: mutableListOf()) }
                            is Resource.Error -> {
                                Logger.e("AddEditDreamViewModel") { result.message.toString() }
                                _addEditDreamState.value.snackBarHostState.value.showSnackbar("Couldn't get unlocked words :(", actionLabel = "Dismiss")
                            }
                            else -> Unit
                        }
                    }
                }
            }
            is AddEditDreamEvent.ClickBuyWord -> { _addEditDreamState.update { it.copy(isDreamExitOff = true) }; handleUnlockWord(event) }
            is AddEditDreamEvent.ToggleDreamImageGenerationPopUpState -> _addEditDreamState.update { it.copy(dreamImageGenerationPopUpState = event.value) }
            is AddEditDreamEvent.ToggleDreamInterpretationPopUpState -> _addEditDreamState.update { it.copy(dreamInterpretationPopUpState = event.value) }
            is AddEditDreamEvent.ToggleDreamAdvicePopUpState -> _addEditDreamState.update { it.copy(dreamAdvicePopUpState = event.value) }
            is AddEditDreamEvent.ToggleDreamQuestionPopUpState -> _addEditDreamState.update { it.copy(dreamQuestionPopUpState = event.value) }
            is AddEditDreamEvent.ToggleDreamStoryPopUpState -> _addEditDreamState.update { it.copy(dreamStoryPopupState = event.value) }
            is AddEditDreamEvent.ToggleDreamMoodPopUpState -> _addEditDreamState.update { it.copy(dreamMoodPopupState = event.value) }
            is AddEditDreamEvent.ToggleDialogState -> _addEditDreamState.update { it.copy(dialogState = event.value) }
            is AddEditDreamEvent.ToggleBottomSheetState -> _addEditDreamState.update { it.copy(bottomSheetState = event.value) }
            is AddEditDreamEvent.SaveDream -> {
                _addEditDreamState.update { it.copy(dreamIsSavingLoading = true) }
                viewModelScope.launch { performSave(event.onSaveSuccess) }
            }
            is AddEditDreamEvent.ToggleDreamHasChanged -> _addEditDreamState.update { it.copy(dreamHasChanged = event.value) }
            is AddEditDreamEvent.FlagDreamContent -> {
                viewModelScope.launch {
                    when (dreamUseCases.flagDream(
                        addEditDreamState.value.dreamInfo.dreamId,
                        addEditDreamState.value.aiStates[AIType.IMAGE]?.response
                    )) {
                        is Resource.Success<*> -> showSnack("Dream flagged successfully")
                        is Resource.Error<*> -> showSnack("Couldn't flag dream :(")
                        else -> Unit
                    }
                }
            }
            is AddEditDreamEvent.GetDreamTokens -> {
                viewModelScope.launch {
                    authRepository.addDreamTokensFlowListener().collect { resource ->
                        when (resource) {
                            is Resource.Success -> _addEditDreamState.update { it.copy(dreamTokens = resource.data?.toInt() ?: 0) }
                            is Resource.Error -> Logger.e("AddEditDreamViewModel") { resource.message.toString() }
                            else -> Unit
                        }
                    }
                }
            }
            is AddEditDreamEvent.OnCleared -> Unit
            is AddEditDreamEvent.ToggleSleepTimePickerDialog -> _addEditDreamState.update { it.copy(sleepTimePickerDialogState = event.show) }
            is AddEditDreamEvent.ToggleWakeTimePickerDialog -> _addEditDreamState.update { it.copy(wakeTimePickerDialogState = event.show) }
            is AddEditDreamEvent.ToggleCalendarDialog -> _addEditDreamState.update { it.copy(calendarDialogState = event.show) }
            is AddEditDreamEvent.TriggerVibration -> viewModelScope.launch { vibratorUtil.triggerVibration() }
        }
    }

    private suspend fun processUnlockWordResult(
        result: Resource<Boolean>, dictionaryWord: DictionaryWord
    ) {
        when (result) {
            is Resource.Error -> {
                _addEditDreamState.update { it.copy(bottomSheetState = false) }
                showSnack(message = "${result.message}")
            }
            is Resource.Success -> updateScreenStateForUnlockedWord(dictionaryWord)
            is Resource.Loading -> Unit
        }
    }

    private fun updateScreenStateForUnlockedWord(dictionaryWord: DictionaryWord) {
        _addEditDreamState.update { state ->
            state.copy(
                isClickedWordUnlocked = true,
                clickedWord = dictionaryWord,
                unlockedWords = state.unlockedWords.apply {
                    add(dictionaryWord.word)
                })
        }
    }

    private fun requestAIText(aiType: AIType, content: String, cost: Int) {
        logger.d { "requestAIText: AIType=${aiType}, content length=${content.length}, cost=${cost}" }
        val isBlocking = aiType == AIType.INTERPRETATION
        if (isBlocking) {
            _addEditDreamState.update { it.copy(isDreamExitOff = true) }
        }

        viewModelScope.launch {
            if (isBlocking && content.length <= 20) {
                _addEditDreamState.update { it.copy(isDreamExitOff = false) }
                logger.d { "Snackbar shown: Dream content is too short. Content: '$content'" }
                showSnack(if (content.isEmpty()) "Dream content is empty" else "Dream content is too short")
                return@launch
            }

            updateAIState(aiType) { copy(isLoading = true) }

            val aiTextType = when (aiType) {
                AIType.INTERPRETATION -> AITextType.INTERPRETATION
                AIType.ADVICE -> AITextType.ADVICE
                AIType.MOOD -> AITextType.MOOD
                AIType.STORY -> AITextType.STORY
                AIType.QUESTION_ANSWER -> AITextType.QUESTION_ANSWER
                else -> throw IllegalArgumentException("Unsupported AIType for text generation: $aiType")
            }
            val extra = if (aiType == AIType.QUESTION_ANSWER) addEditDreamState.value.aiStates[AIType.QUESTION_ANSWER]?.question ?: "" else null


            when (val res = aiService.generateText(aiTextType, content, cost, extra)) {
                is AIResult.Success -> updateAIState(aiType) { copy(response = res.data) }
                is AIResult.Error -> showSnack("Error getting AI response")
            }

            updateAIState(aiType) { copy(isLoading = false) }
            if (cost > 0) authRepository.consumeDreamTokens(cost)
            if (isBlocking) {
                _addEditDreamState.update { it.copy(isDreamExitOff = false) }
            }
        }
    }

    private fun requestImage(content: String, cost: Int) = viewModelScope.launch {
        _addEditDreamState.update { it.copy(isDreamExitOff = true) }
        // First generate details
        updateAIState(AIType.DETAILS) { copy(isLoading = true) }
        val dreamContent = _contentTextFieldState.value.text.toString()
        val details = when (val d = aiService.generateDetails(dreamContent, cost)) {
            is AIResult.Success -> d.data
            is AIResult.Error -> {
                showSnack("Error getting AI response")
                updateAIState(AIType.DETAILS) { copy(isLoading = false) }
                _addEditDreamState.update { it.copy(isDreamExitOff = false) }
                return@launch
            }
        }
        updateAIState(AIType.DETAILS) { copy(response = details, isLoading = false) }
        // Then generate image
        updateAIState(AIType.IMAGE) { copy(isLoading = true) }
        when (val img = aiService.generateImageFromDetails(details, cost)) {
            is AIResult.Success -> updateAIState(AIType.IMAGE) { copy(response = img.data) }
            is AIResult.Error -> showSnack("Error getting AI image: ${'$'}{img.message}")
        }
        updateAIState(AIType.IMAGE) { copy(isLoading = false) }
        if (cost > 0) authRepository.consumeDreamTokens(cost)
        _addEditDreamState.update { it.copy(isDreamExitOff = false) }
    }
}

data class AddEditDreamState(
    val dreamInfo: DreamInfo = DreamInfo(
        dreamId = "",
        dreamUID = "",
        dreamBackgroundImage = Dream.dreamBackgroundImages.indices.random(),
        dreamIsLucid = false,
        dreamIsFavorite = false,
        dreamIsNightmare = false,
        dreamIsRecurring = false,
        dreamIsFalseAwakening = false,
        dreamSleepTime = formatLocalTime(sleepTime),    // "11:00 PM"
        dreamWakeTime = formatLocalTime(wakeTime),      // "7:00 AM"
        dreamDate = formatLocalDate(currentDate),
        dreamTimeOfDay = "",
        dreamLucidity = 0,
        dreamVividness = 0,
        dreamEmotion = 0
    ),
    val aiStates: Map<AIType, AIState> = AIType.entries.associateWith { AIState() },
    val dreamContentChanged: Boolean = true,
    val dreamIsSavingLoading: Boolean = false,
    val dialogState: Boolean = false,
    val calendarDialogState: Boolean = false,
    val sleepTimePickerDialogState: Boolean = false,
    val wakeTimePickerDialogState: Boolean = false,
    val dreamImageGenerationPopUpState: Boolean = false,
    val dreamInterpretationPopUpState: Boolean = false,
    val dreamAdvicePopUpState: Boolean = false,
    val dreamQuestionPopUpState: Boolean = false,
    val dreamStoryPopupState: Boolean = false,
    val dreamMoodPopupState: Boolean = false,
    val isDreamExitOff: Boolean = false,
    val snackBarHostState: MutableState<SnackbarHostState> = mutableStateOf(SnackbarHostState()),
    val dictionaryWordMutableList: MutableList<DictionaryWord> = mutableListOf(),
    val dreamFilteredDictionaryWords: List<DictionaryWord> = mutableListOf(),
    val unlockedWords: MutableList<String> = mutableListOf(),
    val bottomSheetState: Boolean = false,
    val clickedWord: DictionaryWord = DictionaryWord("", "", false, 0),
    val isClickedWordUnlocked: Boolean = false,
    val isDreamFilterLoading: Boolean = false,
    val dreamTokens: Int = 0,
    val dreamHasChanged: Boolean = false,
    val isAdResponse: Boolean = false,
    val isAdAdvice: Boolean = false,
    val isAdQuestion: Boolean = false,
    val isAdStory: Boolean = false,
    val isAdMood: Boolean = false,
    val isAdImage: Boolean = false
)

data class AIState(
    val response: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    //for question
    val question: String = ""
)

data class DreamInfo(
    val dreamId: String?,
    val dreamUID: String?,
    var dreamBackgroundImage: Int,
    val dreamIsLucid: Boolean,
    val dreamIsFavorite: Boolean,
    val dreamIsNightmare: Boolean,
    val dreamIsRecurring: Boolean,
    val dreamIsFalseAwakening: Boolean,
    val dreamSleepTime: String,
    val dreamWakeTime: String,
    val dreamDate: String,
    val dreamTimeOfDay: String,
    val dreamLucidity: Int,
    val dreamVividness: Int,
    val dreamEmotion: Int,
)
