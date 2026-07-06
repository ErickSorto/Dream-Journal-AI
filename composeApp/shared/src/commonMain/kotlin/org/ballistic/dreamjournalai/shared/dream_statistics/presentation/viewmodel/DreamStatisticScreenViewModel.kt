package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
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
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.core.util.getDaysInMonth
import org.ballistic.dreamjournalai.shared.core.util.parseCustomDate
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.DreamEmotionRadar
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.util.OrderType
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.repository.DailyLessonRepository
import org.ballistic.dreamjournalai.shared.dream_statistics.StatisticEvent
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.viewmodel.DictionaryWord
import kotlin.time.ExperimentalTime

private val excludedStatisticWords = setOf("saw", "trying")

class DreamStatisticScreenViewModel(
    private val dreamUseCases: DreamUseCases,
    private val dictionaryRepository: DictionaryRepository,
    private val authRepository: AuthRepository,
    private val dailyLessonRepository: DailyLessonRepository,
    private val vibratorUtil: VibratorUtil
) : ViewModel() {

    private val _dreamStatisticScreen = MutableStateFlow(DreamStatisticScreenState())
    val dreamStatisticScreen: StateFlow<DreamStatisticScreenState> = _dreamStatisticScreen

    private var getDreamJob: Job? = null
    private var getDreamTokensJob: Job? = null
    private var lessonProgressJob: Job? = null

    init {
        observeCompletedLessons()
    }

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
                    val dreamsByServerDay = _dreamStatisticScreen.value.dreams
                        .mapNotNull { it.statisticsDay() }
                        .groupingBy { it }
                        .eachCount()
                    val streakStats = calculateStreakStats(dreamsByServerDay.keys)
                    val heatMapMonths = buildHeatMapMonths(dreamsByServerDay)

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
                        totalQuestions = _dreamStatisticScreen.value.dreams.count { it.dreamAIQuestionAnswer.isNotBlank() },
                        dreamWritingStreak = streakStats.currentStreak,
                        longestDreamWritingStreak = streakStats.longestStreak,
                        activeDreamWritingDays = dreamsByServerDay.keys.size,
                        averageEmotionRadar = DreamEmotionRadar.average(_dreamStatisticScreen.value.dreams.map { it.emotionalRadar }),
                        heatMapMonths = heatMapMonths
                    )
                }
            }
            is StatisticEvent.GetDreamTokens -> {
                getDreamTokensJob?.cancel()
                getDreamTokensJob = viewModelScope.launch {
                    collectDreamTokens()
                }
            }
            is StatisticEvent.TriggerVibration -> {
                vibratorUtil.triggerVibration()
            }
        }
    }

    private suspend fun collectDreamTokens() {
        authRepository.addDreamTokensFlowListener().collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    _dreamStatisticScreen.update {
                        it.copy(
                            dreamTokens = resource.data?.toInt() ?: 0,
                            dailyTokenCompletedWeeks = authRepository.dailyTokenCompletedWeeks.value
                        )
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

    private fun observeCompletedLessons() {
        lessonProgressJob?.cancel()
        lessonProgressJob = dailyLessonRepository.observeProgress()
            .onEach { progress ->
                _dreamStatisticScreen.update {
                    it.copy(totalCompletedLessons = progress.values.count { lesson -> lesson.completed })
                }
            }
            .catch {
                _dreamStatisticScreen.update { state -> state.copy(totalCompletedLessons = 0) }
            }
            .launchIn(viewModelScope)
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
                        .filter { it.isNotBlank() && it !in excludedStatisticWords }
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
                val normalizedWord = dictionaryWord.word.lowercase()
                if (normalizedWord in excludedStatisticWords) return@forEach

                val count = dreamWordFrequency[normalizedWord] ?: 0
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
    val totalQuestions: Int = 0,
    val dreamWritingStreak: Int = 0,
    val longestDreamWritingStreak: Int = 0,
    val activeDreamWritingDays: Int = 0,
    val dailyTokenCompletedWeeks: Int = 0,
    val totalCompletedLessons: Int = 0,
    val averageEmotionRadar: DreamEmotionRadar = DreamEmotionRadar(),
    val heatMapMonths: List<DreamHeatMapMonth> = emptyList()
)

@Stable
data class DreamHeatMapMonth(
    val label: String,
    val monthNumber: Int = 1,
    val yearShort: Int = 0,
    val days: List<DreamHeatMapDay>,
)

@Stable
data class DreamHeatMapDay(
    val dayOfMonth: Int,
    val count: Int,
    val leadingBlankSlots: Int = 0,
)

private data class StreakStats(
    val currentStreak: Int,
    val longestStreak: Int,
)

@OptIn(ExperimentalTime::class)
private fun Dream.statisticsDay(): LocalDate? {
    serverDreamDay.takeIf { it.isNotBlank() }?.let { day ->
        runCatching { LocalDate.parse(day) }.getOrNull()?.let { return it }
    }

    if (timestamp > 0L) {
        return kotlin.time.Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.UTC)
            .date
    }

    return runCatching { parseCustomDate(date) }.getOrNull()
}

@OptIn(ExperimentalTime::class)
private fun calculateStreakStats(days: Set<LocalDate>): StreakStats {
    if (days.isEmpty()) return StreakStats(currentStreak = 0, longestStreak = 0)

    val today = kotlin.time.Clock.System.todayIn(TimeZone.UTC)
    var currentStreak = 0
    var cursor = today
    while (days.contains(cursor)) {
        currentStreak += 1
        cursor = cursor.minus(DatePeriod(days = 1))
    }

    val sortedDays = days.sorted()
    var longestStreak = 0
    var runningStreak = 0
    var previousDay: LocalDate? = null
    sortedDays.forEach { day ->
        runningStreak = if (previousDay?.plus(DatePeriod(days = 1)) == day) {
            runningStreak + 1
        } else {
            1
        }
        longestStreak = maxOf(longestStreak, runningStreak)
        previousDay = day
    }

    return StreakStats(
        currentStreak = currentStreak,
        longestStreak = longestStreak
    )
}

@OptIn(ExperimentalTime::class)
private fun buildHeatMapMonths(dreamsByDay: Map<LocalDate, Int>): List<DreamHeatMapMonth> {
    val today = kotlin.time.Clock.System.todayIn(TimeZone.UTC)
    return listOf(
        monthShift(today, -2),
        monthShift(today, -1),
        LocalDate(today.year, today.month, 1)
    ).map { monthStart ->
        val isCurrentMonth = monthStart.year == today.year && monthStart.month == today.month
        val visibleDays = if (isCurrentMonth) {
            today.day
        } else {
            getDaysInMonth(monthStart.year, monthStart.month.number)
        }

        DreamHeatMapMonth(
            label = "${monthStart.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${monthStart.year % 100}",
            monthNumber = monthStart.month.number,
            yearShort = monthStart.year % 100,
            days = (1..visibleDays).map { day ->
                val date = LocalDate(monthStart.year, monthStart.month, day)
                DreamHeatMapDay(
                    dayOfMonth = day,
                    count = dreamsByDay[date] ?: 0,
                    leadingBlankSlots = if (day == 1) monthStart.dayOfWeek.ordinal else 0
                )
            }
        )
    }
}

private fun monthShift(date: LocalDate, offset: Int): LocalDate {
    var year = date.year
    var month = date.month.number + offset
    while (month < 1) {
        month += 12
        year -= 1
    }
    while (month > 12) {
        month -= 12
        year += 1
    }
    return LocalDate(year, month, 1)
}
