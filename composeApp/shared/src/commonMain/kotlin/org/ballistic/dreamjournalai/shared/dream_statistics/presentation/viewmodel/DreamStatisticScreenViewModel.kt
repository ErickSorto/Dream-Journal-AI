package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.domain.DictionaryRepository
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.util.OrderType
import org.ballistic.dreamjournalai.shared.dream_statistics.StatisticEvent
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.viewmodel.DictionaryWord


class DreamStatisticScreenViewModel(
    private val dreamUseCases: DreamUseCases,
    private val dictionaryRepository: DictionaryRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _dreamStatisticScreen = MutableStateFlow(DreamStatisticScreenState())
    val dreamStatisticScreen: StateFlow<DreamStatisticScreenState> = _dreamStatisticScreen

    private var getDreamJob: Job? = null

    fun onEvent(event: StatisticEvent) {
        when (event) {
            is StatisticEvent.LoadDreams -> {
                getDreams()
            }

            is StatisticEvent.LoadDictionary -> {
                viewModelScope.launch {
                    loadWords()
                }
            }

            is StatisticEvent.LoadStatistics -> {
                viewModelScope.launch {
                    _dreamStatisticScreen.value = _dreamStatisticScreen.value.copy(
                        totalLucidDreams = _dreamStatisticScreen.value.dreams.count { it.isLucid },
                        totalNormalDreams = _dreamStatisticScreen.value.dreams.count {
                            !it.isLucid && !it.isNightmare && !it.isRecurring
                        },
                        totalNightmares = _dreamStatisticScreen.value.dreams.count { it.isNightmare },
                        totalDreams = _dreamStatisticScreen.value.dreams.size,
                        totalFavoriteDreams = _dreamStatisticScreen.value.dreams.count { it.isFavorite },
                        totalRecurringDreams = _dreamStatisticScreen.value.dreams.count { it.isRecurring },
                        totalFalseAwakenings = _dreamStatisticScreen.value.dreams.count { it.falseAwakening },
                        totalInterpretations = _dreamStatisticScreen.value.dreams.count { it.AIResponse.isNotBlank() },
                        totalStories = _dreamStatisticScreen.value.dreams.count { it.dreamAIStory.isNotBlank() },
                        totalMoods = _dreamStatisticScreen.value.dreams.count { it.dreamAIMood.isNotBlank() },
                        totalImages = _dreamStatisticScreen.value.dreams.count { it.generatedImage.isNotBlank() },
                        totalAdvice = _dreamStatisticScreen.value.dreams.count { it.dreamAIAdvice.isNotBlank() },
                        totalQuestions = _dreamStatisticScreen.value.dreams.count { it.dreamAIQuestionAnswer.isNotBlank() }
                    )
                }
            }
            is StatisticEvent.GetDreamTokens -> {

                viewModelScope.launch {
                    collectDreamTokens()
                }
            }
        }
    }

    private suspend fun collectDreamTokens() {
        authRepository.addDreamTokensFlowListener().collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    _dreamStatisticScreen.update {
                        it.copy(dreamTokens = resource.data?.toInt() ?: 0)
                    }
                }
                is Resource.Error -> {
                    // Optionally reset to 0 if logged out
                    _dreamStatisticScreen.update { state ->
                        state.copy(dreamTokens = 0)
                    }
                }
                is Resource.Loading -> {
                }
            }
        }
    }

    private fun loadWords() {
        viewModelScope.launch(Dispatchers.IO) {
            _dreamStatisticScreen.update { it.copy(isDreamWordFilterLoading = true) }

            // Step 1: Calculate frequency of each word across all dreams
            val dreamWordFrequency = mutableMapOf<String, Int>()
            withContext(Dispatchers.Default) {
                _dreamStatisticScreen.value.dreams.forEach { dream ->
                    val uniqueWordsInDream = dream.content.lowercase()
                        .split(Regex("[^a-z]+")) // Simple word tokenization
                        .filter { it.isNotBlank() }
                        .toSet()

                    uniqueWordsInDream.forEach { word ->
                        dreamWordFrequency[word] = (dreamWordFrequency[word] ?: 0) + 1
                    }
                }
            }

            // Step 2: Load dictionary and map frequencies to dictionary words
            val dictionaryWords = dictionaryRepository.loadDictionaryWordsFromCsv("dream_dictionary.csv")
            _dreamStatisticScreen.update { it.copy(dictionaryWordMutableList = dictionaryWords) }

            val wordDreamCount = mutableMapOf<DictionaryWord, Int>()
            dictionaryWords.forEach { dictionaryWord ->
                val count = dreamWordFrequency[dictionaryWord.word.lowercase()] ?: 0
                if (count > 0) {
                    wordDreamCount[dictionaryWord] = count
                }
            }

            // Step 3: Get top six words
            val topSixWordsInDreams = wordDreamCount.entries
                .sortedByDescending { it.value }
                .take(6)
                .associate { it.toPair() }

            // Step 4: Update the state
            _dreamStatisticScreen.update {
                it.copy(
                    topSixWordsInDreams = topSixWordsInDreams,
                    isDreamWordFilterLoading = false
                )
            }
        }
    }


    private fun getDreams() {
        getDreamJob?.cancel()
        getDreamJob = dreamUseCases.getDreams(OrderType.Date)
            .onEach { dreams ->

                _dreamStatisticScreen.value = dreamStatisticScreen.value.copy(
                    dreams = dreams
                )
                onEvent(StatisticEvent.LoadStatistics)
                onEvent(StatisticEvent.LoadDictionary)
            }
            .catch { exception ->
            }
            .launchIn(viewModelScope)
    }

}

@Stable
data class DreamStatisticScreenState(
    val dreams: List<Dream> = emptyList(),
    val topSixWordsInDreams: Map<DictionaryWord, Int> = mapOf(),
    val dictionaryWordMutableList: List<DictionaryWord> = emptyList(),
    val totalLucidDreams: Int = 0,
    val totalNormalDreams: Int = 0,
    val totalNightmares: Int = 0,
    val totalDreams: Int = 0,
    val totalFavoriteDreams: Int = 0,
    val totalRecurringDreams: Int = 0,
    val totalFalseAwakenings: Int = 0,
    val isDreamWordFilterLoading: Boolean = true,
    val dreamTokens: Int = 0,
    val totalInterpretations: Int = 0,
    val totalStories: Int = 0,
    val totalMoods: Int = 0,
    val totalImages: Int = 0,
    val totalAdvice: Int = 0,
    val totalQuestions: Int = 0
)
