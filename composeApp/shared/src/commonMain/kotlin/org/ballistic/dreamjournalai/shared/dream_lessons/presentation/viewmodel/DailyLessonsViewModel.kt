package org.ballistic.dreamjournalai.shared.dream_lessons.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DAILY_LESSON_IMAGE_REGENERATION_QUEUED
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DAILY_LESSON_IMAGE_REGENERATION_RUNNING
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLesson
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonRegenerateSection
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.withProgress
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.repository.DailyLessonRepository
import kotlin.time.Clock

private val lessonsLogger = Logger.withTag("DailyLessonsVM")

enum class LessonFlowStage {
    Preview,
    Reading,
    Quiz,
    Completed,
}

class DailyLessonsViewModel(
    private val repository: DailyLessonRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DailyLessonsState())
    val state: StateFlow<DailyLessonsState> = _state.asStateFlow()

    init {
        _state.update { it.copy(isLessonAdmin = repository.isLessonAdmin()) }
        observeLessons()
    }

    fun onEvent(event: DailyLessonEvent) {
        when (event) {
            DailyLessonEvent.ClearMessage -> {
                _state.update { it.copy(message = null, error = null) }
            }
            is DailyLessonEvent.StartLesson -> startLesson(event.lessonId)
            is DailyLessonEvent.StartQuiz -> startQuiz(event.lessonId)
            is DailyLessonEvent.NextQuizQuestion -> nextQuizQuestion(event.lessonId, event.questionCount)
            is DailyLessonEvent.RetryQuizQuestion -> retryQuizQuestion(event.lessonId, event.questionId)
            is DailyLessonEvent.MarkStarted -> markStarted(event.lessonId)
            is DailyLessonEvent.SelectAnswer -> selectAnswer(event.lessonId, event.questionId, event.optionId)
            is DailyLessonEvent.ToggleBookmark -> toggleBookmark(event.lesson)
            is DailyLessonEvent.CompleteLesson -> completeLesson(event.lesson)
            is DailyLessonEvent.RegenerateLessonSection -> {
                regenerateLessonSection(event.lessonId, event.section, event.instructions)
            }
            DailyLessonEvent.GenerateDebugLesson -> generateDebugLesson()
        }
    }

    private fun observeLessons() {
        combine(
            repository.observeRecentLessons(),
            repository.observeProgress(),
            repository.observeLessonAdmin(),
        ) { lessons, progress, isLessonAdmin ->
            val mergedLessons = lessons
                .map { lesson -> lesson.withProgress(progress[lesson.id]) }
                .sortedByDescending { lesson -> lesson.createdDateIso }
            mergedLessons to isLessonAdmin
        }
            .onEach { (lessons, isLessonAdmin) ->
                _state.update { current ->
                    val lessonsById = lessons.associateBy { lesson -> lesson.id }
                    val pendingImageLessonIds = current.pendingImageRegenerationLessonIds
                        .filter { lessonId ->
                            val status = lessonsById[lessonId]?.adminImageRegenerationStatus.orEmpty()
                            status.isBlank() || status.isActiveImageRegenerationStatus()
                        }
                        .toSet()
                    current.copy(
                        lessons = lessons,
                        isLoading = false,
                        isLessonAdmin = isLessonAdmin,
                        pendingImageRegenerationLessonIds = pendingImageLessonIds,
                    )
                }
            }
            .catch { error ->
                lessonsLogger.e(error) { "Failed to observe lessons" }
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Could not load daily lessons.",
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun markStarted(lessonId: String) {
        if (lessonId.isBlank()) return
        viewModelScope.launch {
            repository.markStarted(lessonId)
        }
    }

    private fun startLesson(lessonId: String) {
        if (lessonId.isBlank()) return
        _state.update { current ->
            current.copy(
                lessonStages = current.lessonStages + (lessonId to LessonFlowStage.Reading),
                error = null,
                message = null,
            )
        }
        markStarted(lessonId)
    }

    private fun startQuiz(lessonId: String) {
        if (lessonId.isBlank()) return
        _state.update { current ->
            current.copy(
                lessonStages = current.lessonStages + (lessonId to LessonFlowStage.Quiz),
                quizQuestionIndexes = current.quizQuestionIndexes + (lessonId to 0),
                error = null,
                message = null,
            )
        }
    }

    private fun nextQuizQuestion(lessonId: String, questionCount: Int) {
        if (lessonId.isBlank() || questionCount <= 0) return
        _state.update { current ->
            val currentIndex = current.currentQuizIndex(lessonId)
            current.copy(
                quizQuestionIndexes = current.quizQuestionIndexes +
                    (lessonId to (currentIndex + 1).coerceAtMost(questionCount - 1)),
                error = null,
                message = null,
            )
        }
    }

    private fun retryQuizQuestion(lessonId: String, questionId: String) {
        if (lessonId.isBlank() || questionId.isBlank()) return
        _state.update { current ->
            val updatedLessonAnswers = current.answerSelections[lessonId]
                .orEmpty()
                .minus(questionId)
            current.copy(
                answerSelections = current.answerSelections + (lessonId to updatedLessonAnswers),
                error = null,
                message = null,
            )
        }
    }

    private fun selectAnswer(lessonId: String, questionId: String, optionId: String) {
        _state.update { current ->
            val lessonAnswers = current.answerSelections[lessonId].orEmpty() + (questionId to optionId)
            current.copy(
                answerSelections = current.answerSelections + (lessonId to lessonAnswers),
                error = null,
                message = null,
            )
        }
    }

    private fun toggleBookmark(lesson: DailyLesson) {
        viewModelScope.launch {
            val next = !lesson.bookmarked
            when (val result = repository.setBookmarked(lesson.id, next)) {
                is Resource.Success -> Unit
                is Resource.Error -> _state.update { it.copy(error = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun completeLesson(lesson: DailyLesson) {
        if (lesson.completed || _state.value.completingLessonId == lesson.id) return
        val answers = _state.value.answersFor(lesson)
        val unanswered = lesson.questions.any { question -> answers[question.id].isNullOrBlank() }
        if (unanswered) {
            _state.update { it.copy(error = "Answer all questions before completing this lesson.") }
            return
        }
        val notAllCorrect = lesson.questions.any { question ->
            answers[question.id] != question.correctOptionId
        }
        if (lesson.questions.isEmpty() || notAllCorrect) {
            _state.update { it.copy(error = "Answer each quiz question correctly to complete this lesson.") }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    completingLessonId = lesson.id,
                    error = null,
                    message = null,
                )
            }
            when (val result = repository.completeLesson(lesson.id, answers)) {
                is Resource.Success -> {
                    val awarded = result.data?.tokensAwarded ?: 0
                    val nowMillis = Clock.System.now().toEpochMilliseconds()
                    _state.update {
                        it.copy(
                            lessons = it.lessons.map { currentLesson ->
                                if (currentLesson.id == lesson.id) {
                                    currentLesson.copy(
                                        started = true,
                                        completed = true,
                                        selectedAnswers = answers,
                                        quizScore = lesson.questions.size,
                                        completedAtMillis = nowMillis,
                                        updatedAtMillis = nowMillis,
                                    )
                                } else {
                                    currentLesson
                                }
                            },
                            completingLessonId = null,
                            lessonStages = it.lessonStages + (lesson.id to LessonFlowStage.Completed),
                            message = if (awarded > 0) {
                                "Lesson complete. +$awarded DreamToken"
                            } else {
                                "Lesson complete."
                            },
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            completingLessonId = null,
                            error = result.message ?: "Could not complete this lesson.",
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun generateDebugLesson() {
        if (_state.value.isGeneratingDebugLesson) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isGeneratingDebugLesson = true,
                    error = null,
                    message = "Generating debug lesson...",
                )
            }
            when (val result = repository.generateDebugLesson()) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isGeneratingDebugLesson = false,
                            message = "Debug lesson is generating. It will appear here shortly.",
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isGeneratingDebugLesson = false,
                            error = result.message ?: "Could not generate debug lesson.",
                            message = null,
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun regenerateLessonSection(
        lessonId: String,
        section: DailyLessonRegenerateSection,
        instructions: String,
    ) {
        val current = _state.value
        if (!current.isLessonAdmin || current.regeneratingLessonId != null || lessonId.isBlank()) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    regeneratingLessonId = lessonId,
                    regeneratingSection = section,
                    error = null,
                    message = "Regenerating ${section.wireValue}...",
                )
            }

            when (val result = repository.regenerateLiveLessonSection(lessonId, section, instructions)) {
                is Resource.Success -> {
                    val queuedImageLessonIds = if (
                        section == DailyLessonRegenerateSection.Image &&
                        result.data?.queued == true
                    ) {
                        _state.value.pendingImageRegenerationLessonIds + lessonId
                    } else {
                        _state.value.pendingImageRegenerationLessonIds
                    }
                    _state.update {
                        it.copy(
                            regeneratingLessonId = null,
                            regeneratingSection = null,
                            pendingImageRegenerationLessonIds = queuedImageLessonIds,
                            message = when (section) {
                                DailyLessonRegenerateSection.Content -> "Lesson content regenerated."
                                DailyLessonRegenerateSection.Image -> "Lesson image queued. It will update here when ready."
                            },
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            regeneratingLessonId = null,
                            regeneratingSection = null,
                            error = result.message ?: "Could not regenerate this lesson section.",
                            message = null,
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}

private fun String.isActiveImageRegenerationStatus(): Boolean {
    return equals(DAILY_LESSON_IMAGE_REGENERATION_QUEUED, ignoreCase = true) ||
        equals(DAILY_LESSON_IMAGE_REGENERATION_RUNNING, ignoreCase = true)
}

data class DailyLessonsState(
    val lessons: List<DailyLesson> = emptyList(),
    val answerSelections: Map<String, Map<String, String>> = emptyMap(),
    val lessonStages: Map<String, LessonFlowStage> = emptyMap(),
    val quizQuestionIndexes: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val isLessonAdmin: Boolean = false,
    val isGeneratingDebugLesson: Boolean = false,
    val regeneratingLessonId: String? = null,
    val regeneratingSection: DailyLessonRegenerateSection? = null,
    val pendingImageRegenerationLessonIds: Set<String> = emptySet(),
    val completingLessonId: String? = null,
    val error: String? = null,
    val message: String? = null,
) {
    fun answersFor(lesson: DailyLesson): Map<String, String> {
        return answerSelections[lesson.id] ?: lesson.selectedAnswers
    }

    fun stageFor(lesson: DailyLesson): LessonFlowStage {
        return when {
            lesson.completed -> LessonFlowStage.Completed
            lessonStages[lesson.id] != null -> lessonStages.getValue(lesson.id)
            lesson.started -> LessonFlowStage.Reading
            else -> LessonFlowStage.Preview
        }
    }

    fun currentQuizIndex(lessonId: String): Int {
        return quizQuestionIndexes[lessonId] ?: 0
    }
}

sealed interface DailyLessonEvent {
    data object ClearMessage : DailyLessonEvent
    data class StartLesson(val lessonId: String) : DailyLessonEvent
    data class StartQuiz(val lessonId: String) : DailyLessonEvent
    data class NextQuizQuestion(val lessonId: String, val questionCount: Int) : DailyLessonEvent
    data class RetryQuizQuestion(val lessonId: String, val questionId: String) : DailyLessonEvent
    data class MarkStarted(val lessonId: String) : DailyLessonEvent
    data class ToggleBookmark(val lesson: DailyLesson) : DailyLessonEvent
    data class SelectAnswer(
        val lessonId: String,
        val questionId: String,
        val optionId: String,
    ) : DailyLessonEvent
    data class CompleteLesson(val lesson: DailyLesson) : DailyLessonEvent
    data class RegenerateLessonSection(
        val lessonId: String,
        val section: DailyLessonRegenerateSection,
        val instructions: String,
    ) : DailyLessonEvent
    data object GenerateDebugLesson : DailyLessonEvent
}
