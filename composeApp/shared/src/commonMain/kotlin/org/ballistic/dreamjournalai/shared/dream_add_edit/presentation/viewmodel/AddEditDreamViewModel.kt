package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dreamjournalai.composeapp.shared.generated.resources.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.domain.DictionaryRepository
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.core.util.formatLocalDate
import org.ballistic.dreamjournalai.shared.core.util.formatLocalTime
import org.ballistic.dreamjournalai.shared.dream_add_edit.data.AIResult
import org.ballistic.dreamjournalai.shared.dream_add_edit.data.AITextType
import org.ballistic.dreamjournalai.shared.dream_add_edit.data.DreamAIService
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.ImageStyle
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.viewmodel.DictionaryWord
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

enum class AIPage {
    IMAGE,
    INTERPRETATION,
    ADVICE,
    QUESTION,
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
        message: StringValue,
        actionLabel: StringValue = StringValue.Resource(Res.string.dismiss)
    ) {
        SnackbarController.sendEvent(
            SnackbarEvent(
                message = message,
                action = SnackbarAction(
                    name = actionLabel,
                    action = {}
                )
            )
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
                dreamEmotion = dream.moodRating,
                dreamAudioUrl = dream.audioUrl,
                dreamAudioDuration = dream.audioDuration,
                dreamAudioTimestamp = dream.audioTimestamp,
                dreamIsAudioPermanent = dream.isAudioPermanent,
                dreamAudioTranscription = dream.audioTranscription
            ),
            aiStates = aiStates.toImmutableMap()
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
                            onEvent(AddEditDreamEvent.GetDreamTokens)
                            onEvent(AddEditDreamEvent.FilterDreamWordInDictionary)
                        }
                        is Resource.Error<*> -> {
                            logger.e { "init: load dream failed: ${resource.message}" }
                            viewModelScope.launch { showSnack(StringValue.Resource(Res.string.get_dream_error)) }
                        }
                        is Resource.Loading<*> -> { /* no-op */ }
                    }
                }
            } else {
                onEvent(AddEditDreamEvent.GetUnlockedWords)
                onEvent(AddEditDreamEvent.GetDreamTokens)
            }
        }

        viewModelScope.launch {
            authRepository.isUserAnonymous.collect { isAnon ->
                _addEditDreamState.update { it.copy(isUserAnonymous = isAnon) }
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
            state.copy(aiStates = newStates.toImmutableMap())
        }
    }

    // Consolidated save logic. Returns true if successful, false otherwise.
    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    private suspend fun performSave(onSaveSuccess: () -> Unit = {}): Boolean {
        val saveStart = kotlin.time.Clock.System.now()
        logger.d { "performSave: STARTED at ${saveStart.toEpochMilliseconds()}" }

        // Auto title if blank
        val contentText = contentTextFieldState.value.text.toString()
        val transcription = _addEditDreamState.value.dreamInfo.dreamAudioTranscription

        if (titleTextFieldState.value.text.isBlank()) {
            // Use content text if available, otherwise try transcription
            val textToGenerateFrom = if (contentText.isNotBlank()) contentText else transcription

            if (textToGenerateFrom.isNotBlank()) {
                val titleStart = kotlin.time.Clock.System.now()
                logger.d { "performSave: Title generation STARTED" }
                when (val titleRes = aiService.generateText(AITextType.TITLE, textToGenerateFrom, cost = 0)) {
                    is AIResult.Success -> {
                        val titleEnd = kotlin.time.Clock.System.now()
                        logger.d { "performSave: Title generation SUCCESS in ${(titleEnd - titleStart).inWholeMilliseconds}ms. Title: ${titleRes.data}" }
                        _titleTextFieldState.value = TextFieldState(initialText = titleRes.data)
                    }
                    is AIResult.Error -> {
                        logger.w { "performSave: Title gen failed: ${titleRes.message}" }
                    }
                }
            }
        }

        if (_addEditDreamState.value.dreamInfo.dreamId.isNullOrEmpty()) {
            updateDreamInfo { copy(dreamId = Uuid.random().toString()) }
        }

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
            dreamAIMood = addEditDreamState.value.aiStates[AIType.MOOD]?.response ?: "",
            audioUrl = addEditDreamState.value.dreamInfo.dreamAudioUrl,
            audioDuration = addEditDreamState.value.dreamInfo.dreamAudioDuration,
            isAudioPermanent = addEditDreamState.value.dreamInfo.dreamIsAudioPermanent,
            audioTranscription = addEditDreamState.value.dreamInfo.dreamAudioTranscription,
            audioTimestamp = if(addEditDreamState.value.dreamInfo.dreamAudioUrl.isNotBlank()) kotlin.time.Clock.System.now().toEpochMilliseconds() else 0
        )

        logger.d { "performSave: calling addDream..." }
        val dbStart = kotlin.time.Clock.System.now()
        val saveResult = dreamUseCases.addDream(dreamToSave)
        val dbEnd = kotlin.time.Clock.System.now()
        logger.d { "performSave: addDream FINISHED in ${(dbEnd - dbStart).inWholeMilliseconds}ms" }

        if (saveResult is Resource.Error) {
            logger.e { "performSave: FAILED with error: ${saveResult.message}" }
            _addEditDreamState.update { it.copy(dreamIsSavingLoading = false) }
            showSnack(StringValue.DynamicString(saveResult.message ?: "Couldn't save dream :("))
            return false
        }

        // Polling optimization:
        // Only poll if we are waiting for a remote URL (image or audio)
        // Check if current generatedImage is remote
        val hasRemoteImage = dreamToSave.generatedImage.contains("firebasestorage.googleapis.com")
        // Check if current audioUrl is remote
        val hasRemoteAudio = dreamToSave.audioUrl.contains("firebasestorage.googleapis.com")

        // If we expect an image (e.g. user requested one but it's blank or local), we poll.
        // But here, if generatedImage is blank, we don't expect one.
        // If audioUrl is blank, we don't expect one.

        // We want to poll ONLY if we sent a local path and expect a remote path back.
        // Local paths usually don't contain "http".
        val sentLocalImage = dreamToSave.generatedImage.isNotBlank() && !dreamToSave.generatedImage.startsWith("http")
        val sentLocalAudio = dreamToSave.audioUrl.isNotBlank() && !dreamToSave.audioUrl.startsWith("http")

        val needsPolling = sentLocalImage || sentLocalAudio

        if (needsPolling) {
            val savedId = addEditDreamState.value.dreamInfo.dreamId
            if (!savedId.isNullOrBlank()) {
                repeat(12) { _ ->
                    when (val refreshed = dreamUseCases.getDream(savedId)) {
                        is Resource.Success<*> -> {
                            val latest = refreshed.data as Dream
                            var satisfied = true

                            if (sentLocalImage) {
                                val newImage = latest.generatedImage
                                if (newImage.contains("firebasestorage.googleapis.com")) {
                                    updateAIState(AIType.IMAGE) { copy(response = newImage) }
                                    updateAIState(AIType.DETAILS) { copy(response = latest.generatedDetails) }
                                } else {
                                    satisfied = false
                                }
                            }

                            if (sentLocalAudio) {
                                val newAudio = latest.audioUrl
                                if (newAudio.contains("firebasestorage.googleapis.com")) {
                                    updateDreamInfo { copy(dreamAudioUrl = newAudio) }
                                } else {
                                    satisfied = false
                                }
                            }

                            if (satisfied) return@repeat
                        }
                        else -> Unit
                    }
                    kotlinx.coroutines.delay(250)
                }
            }
        }

        _addEditDreamState.update { it.copy(dreamIsSavingLoading = false) }
        logger.d { "performSave: SUCCESS calling onSaveSuccess" }
        onSaveSuccess()
        val saveTotalEnd = kotlin.time.Clock.System.now()
        logger.d { "performSave: TOTAL TIME ${(saveTotalEnd - saveStart).inWholeMilliseconds}ms" }
        return true
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

    private suspend fun loadWordsIfNeeded(): ImmutableList<DictionaryWord> {
        if (_addEditDreamState.value.dictionaryWords.isNotEmpty()) {
            return _addEditDreamState.value.dictionaryWords
        }

        val words = withContext(Dispatchers.Default) {
            dictionaryRepository.loadDictionaryWordsFromCsv("dream_dictionary.csv")
        }.toImmutableList()

        _addEditDreamState.update { it.copy(dictionaryWords = words) }
        return words
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
            is AddEditDreamEvent.ClickGenerateAIImage -> { markChanged(); requestImage(event.style, event.cost) }
            is AddEditDreamEvent.AdAIImageToggle -> _addEditDreamState.update { it.copy(isAdImage = event.value) }
            is AddEditDreamEvent.OnImageStyleChanged -> _addEditDreamState.update { it.copy(imageStyle = event.style) }
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
                viewModelScope.launch {
                    val dictionaryList = loadWordsIfNeeded()
                    val content = contentTextFieldState.value.text.toString()

                    val filteredWords = withContext(Dispatchers.Default) {
                        dictionaryRepository.dictionaryWordsInDreamFilterList(
                            dreamContent = content,
                            dictionaryWordList = dictionaryList
                        )
                    }
                    _addEditDreamState.update {
                        it.copy(
                            dreamFilteredDictionaryWords = filteredWords.toImmutableList(),
                            dreamContentChanged = false
                        )
                    }
                }
            }
            is AddEditDreamEvent.ContentHasChanged -> {
                _addEditDreamState.update { it.copy(dreamContentChanged = true, dreamHasChanged = true) }
                onEvent(AddEditDreamEvent.FilterDreamWordInDictionary)
            }
            is AddEditDreamEvent.GetUnlockedWords -> {
                viewModelScope.launch {
                    authRepository.getUnlockedWords().collect { result ->
                        when (result) {
                            is Resource.Success -> _addEditDreamState.update { it.copy(unlockedWords = result.data?.toImmutableList() ?: persistentListOf()) }
                            is Resource.Error -> {
                                Logger.e("AddEditDreamViewModel") { result.message.toString() }
                                showSnack(StringValue.Resource(Res.string.get_unlocked_words_error))
                            }
                            else -> Unit
                        }
                    }
                }
            }
            is AddEditDreamEvent.ClickBuyWord -> { _addEditDreamState.update { it.copy(isDreamExitOff = true) }; handleUnlockWord(event) }
            is AddEditDreamEvent.SetAIPage -> _addEditDreamState.update { it.copy(aiPage = event.page) }
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
                        is Resource.Success<*> -> showSnack(StringValue.Resource(Res.string.dream_flagged_success))
                        is Resource.Error<*> -> showSnack(StringValue.Resource(Res.string.dream_flagged_error))
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
            is AddEditDreamEvent.TriggerVibrationSuccess -> viewModelScope.launch { vibratorUtil.triggerVibrationSuccess() }
            is AddEditDreamEvent.ResetNewImageGeneratedFlag -> _addEditDreamState.update { it.copy(isNewImageGenerated = false) }
            is AddEditDreamEvent.SetStartAnimation -> _addEditDreamState.update { it.copy(startAnimation = event.value) }

            is AddEditDreamEvent.OnVoiceRecordingSaved -> {
                markChanged()
                _addEditDreamState.update { it.copy(isTranscribing = true) }
                updateDreamInfo {
                    copy(
                        dreamAudioUrl = event.path,
                        dreamAudioDuration = event.duration,
                        dreamAudioTimestamp = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                        dreamIsAudioPermanent = false
                    )
                }

                viewModelScope.launch {
                    logger.d { "OnVoiceRecordingSaved: Starting initial save..." }
                    val success = performSave(onSaveSuccess = {
                        // Upload done. Now transcribe.
                        viewModelScope.launch {
                            val uid = Firebase.auth.currentUser?.uid
                            val dreamId = addEditDreamState.value.dreamInfo.dreamId

                            if (uid != null && dreamId != null) {
                                val storagePath = "$uid/dream_recordings/$dreamId.m4a"
                                logger.d { "OnVoiceRecordingSaved: Starting transcription for $storagePath" }
                                val transStart = kotlin.time.Clock.System.now()
                                when (val result = aiService.transcribeAudio(storagePath)) {
                                    is AIResult.Success -> {
                                        val transEnd = kotlin.time.Clock.System.now()
                                        logger.d { "OnVoiceRecordingSaved: Transcription SUCCESS in ${(transEnd - transStart).inWholeMilliseconds}ms" }

                                        updateDreamInfo { copy(dreamAudioTranscription = result.data) }

                                        // Trigger Title generation implicitly via performSave check
                                        logger.d { "OnVoiceRecordingSaved: Calling final save (includes title gen)" }
                                        performSave(onSaveSuccess = {})
                                    }
                                    is AIResult.Error -> {
                                        logger.e { "OnVoiceRecordingSaved: Transcription FAILED: ${result.message}" }
                                        showSnack(StringValue.Resource(Res.string.transcription_failed, result.message ?: ""))
                                    }
                                }
                            }
                            logger.d { "OnVoiceRecordingSaved: Process complete, setting isTranscribing=false" }
                            _addEditDreamState.update { it.copy(isTranscribing = false) }
                        }
                    })

                    // If save failed (e.g. empty content), close the popup so it doesn't get stuck
                    if (!success) {
                        logger.e { "OnVoiceRecordingSaved: Initial save failed" }
                        _addEditDreamState.update { it.copy(isTranscribing = false) }
                    }
                }
            }
            is AddEditDreamEvent.DeleteVoiceRecording -> {
                markChanged()
                updateDreamInfo {
                    copy(
                        dreamAudioUrl = "",
                        dreamAudioDuration = 0,
                        dreamAudioTimestamp = 0,
                        dreamIsAudioPermanent = false,
                        dreamAudioTranscription = ""
                    )
                }
            }
            is AddEditDreamEvent.MakeAudioPermanent -> {
                viewModelScope.launch {
                    val cost = event.cost
                    if (_addEditDreamState.value.dreamTokens >= cost) {
                        authRepository.consumeDreamTokens(cost)
                        markChanged()
                        updateDreamInfo { copy(dreamIsAudioPermanent = true) }
                        showSnack(StringValue.Resource(Res.string.audio_permanent_success))
                        performSave(onSaveSuccess = {})
                    } else {
                        showSnack(StringValue.Resource(Res.string.not_enough_dream_tokens))
                    }
                }
            }
            is AddEditDreamEvent.ToggleTranscriptionBottomSheet -> {
                _addEditDreamState.update { it.copy(transcriptionBottomSheetState = event.value) }
            }
        }
    }

    private suspend fun processUnlockWordResult(
        result: Resource<Boolean>, dictionaryWord: DictionaryWord
    ) {
        when (result) {
            is Resource.Error -> {
                _addEditDreamState.update { it.copy(bottomSheetState = false) }
                showSnack(message = StringValue.DynamicString(result.message ?: "Unknown error"))
            }
            is Resource.Success -> updateScreenStateForUnlockedWord(dictionaryWord)
            is Resource.Loading -> Unit
        }
    }

    private fun updateScreenStateForUnlockedWord(dictionaryWord: DictionaryWord) {
        _addEditDreamState.update { state ->
            val newUnlockedWords = state.unlockedWords.toMutableList()
            newUnlockedWords.add(dictionaryWord.word)
            state.copy(
                isClickedWordUnlocked = true,
                clickedWord = dictionaryWord,
                unlockedWords = newUnlockedWords.toImmutableList()
            )
        }
    }

    private fun requestAIText(aiType: AIType, content: String, cost: Int) {
        logger.d { "requestAIText: AIType=${aiType}, content length=${content.length}, cost=${cost}" }
        val isBlocking = aiType == AIType.INTERPRETATION
        if (isBlocking) {
            _addEditDreamState.update { it.copy(isDreamExitOff = true) }
        }

        viewModelScope.launch {
            val transcription = addEditDreamState.value.dreamInfo.dreamAudioTranscription
            val fullContent = if (transcription.isNotBlank()) {
                if (content.isNotBlank()) "$content\n\nAudio Transcription:\n$transcription" else "Audio Transcription:\n$transcription"
            } else {
                content
            }

            if (isBlocking && fullContent.length < 20) {
                _addEditDreamState.update { it.copy(isDreamExitOff = false) }
                logger.d { "Snackbar shown: Dream content is too short. Content: '$fullContent'" }
                showSnack(StringValue.DynamicString(if (fullContent.isEmpty()) "Dream content is empty" else "Dream content is too short"))
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

            when (val res = aiService.generateText(aiTextType, fullContent, cost, extra)) {
                is AIResult.Success -> updateAIState(aiType) { copy(response = res.data) }
                is AIResult.Error -> showSnack(StringValue.Resource(Res.string.ai_response_error))
            }

            updateAIState(aiType) { copy(isLoading = false) }
            if (cost > 0) authRepository.consumeDreamTokens(cost)
            if (isBlocking) {
                _addEditDreamState.update { it.copy(isDreamExitOff = false) }
            }
        }
    }

    private fun requestImage(style: String, cost: Int) = viewModelScope.launch {
        _addEditDreamState.update { it.copy(isDreamExitOff = true) }
        updateAIState(AIType.IMAGE) { copy(isLoading = true) }
        updateAIState(AIType.DETAILS) { copy(isLoading = true) }
        val dreamContent = _contentTextFieldState.value.text.toString()
        val transcription = addEditDreamState.value.dreamInfo.dreamAudioTranscription
        val fullContent = if (transcription.isNotBlank()) {
            if (dreamContent.isNotBlank()) "$dreamContent\n\nAudio Transcription:\n$transcription" else "Audio Transcription:\n$transcription"
        } else {
            dreamContent
        }

        val details = when (val d = aiService.generateDetails(fullContent, cost)) {
            is AIResult.Success -> d.data
            is AIResult.Error -> {
                showSnack(StringValue.Resource(Res.string.ai_response_error))
                updateAIState(AIType.DETAILS) { copy(isLoading = false) }
                _addEditDreamState.update { it.copy(isDreamExitOff = false) }
                return@launch
            }
        }
        updateAIState(AIType.DETAILS) { copy(response = details, isLoading = false) }

        when (val img = aiService.generateImageFromDetails(details, cost, style)) {
            is AIResult.Success -> {
                updateAIState(AIType.IMAGE) { copy(response = img.data) }
                _addEditDreamState.update { it.copy(isNewImageGenerated = true) }
            }
            is AIResult.Error -> showSnack(StringValue.Resource(Res.string.ai_image_error, img.message ?: ""))
        }
        updateAIState(AIType.IMAGE) { copy(isLoading = false) }
        if (cost > 0) authRepository.consumeDreamTokens(cost)
        _addEditDreamState.update { it.copy(isDreamExitOff = false) }
    }
}

@Stable
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
    val aiStates: ImmutableMap<AIType, AIState> = AIType.entries.associateWith { AIState() }.toImmutableMap(),
    val dreamContentChanged: Boolean = true,
    val dreamIsSavingLoading: Boolean = false,
    val dialogState: Boolean = false,
    val calendarDialogState: Boolean = false,
    val sleepTimePickerDialogState: Boolean = false,
    val wakeTimePickerDialogState: Boolean = false,
    val aiPage: AIPage? = null,
    val isDreamExitOff: Boolean = false,
    val dictionaryWords: ImmutableList<DictionaryWord> = persistentListOf(),
    val dreamFilteredDictionaryWords: ImmutableList<DictionaryWord> = persistentListOf(),
    val unlockedWords: ImmutableList<String> = persistentListOf(),
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
    val isAdImage: Boolean = false,
    val imageStyle: ImageStyle = ImageStyle.VIBRANT,
    val isNewImageGenerated: Boolean = false,
    val startAnimation: Boolean = false,
    val isTranscribing: Boolean = false,
    val transcriptionBottomSheetState: Boolean = false,
    val isUserAnonymous: Boolean = false
)

@Stable
data class AIState(
    val response: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    //for question
    val question: String = ""
)

@Stable
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
    val dreamAudioUrl: String = "",
    val dreamAudioDuration: Long = 0,
    val dreamAudioTimestamp: Long = 0,
    val dreamIsAudioPermanent: Boolean = false,
    val dreamAudioTranscription: String = ""
)
