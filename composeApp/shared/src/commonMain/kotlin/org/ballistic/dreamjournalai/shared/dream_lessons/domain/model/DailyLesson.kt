package org.ballistic.dreamjournalai.shared.dream_lessons.domain.model

import kotlinx.serialization.Serializable

const val DAILY_LESSON_ACCESS_FREE = "free"
const val DAILY_LESSON_ACCESS_PREMIUM = "premium"
const val DAILY_LESSON_IMAGE_REGENERATION_QUEUED = "queued"
const val DAILY_LESSON_IMAGE_REGENERATION_RUNNING = "running"
const val DAILY_LESSON_IMAGE_REGENERATION_SUCCEEDED = "succeeded"
const val DAILY_LESSON_IMAGE_REGENERATION_FAILED = "failed"

@Serializable
data class DailyLesson(
    val id: String = "",
    val access: String = DAILY_LESSON_ACCESS_FREE,
    val isPremium: Boolean = false,
    val isDebug: Boolean = false,
    val ownerUid: String = "",
    val topic: String = "",
    val title: String = "",
    val quickDescription: String = "",
    val category: String = "",
    val summary: String = "",
    val contentMarkdown: String = "",
    val imagePrompt: String = "",
    val imageUrl: String = "",
    val minutesToRead: Int = 4,
    val dreamTokenAward: Int = 0,
    val whatYoullLearn: List<String> = emptyList(),
    val quote: String = "",
    val questions: List<DailyLessonQuestion> = emptyList(),
    val researchSources: List<DailyLessonResearchSource> = emptyList(),
    val createdDateIso: String = "",
    val weekKey: String = "",
    val weekStartsOn: String = "",
    val dayIndexInWeek: Int = 0,
    val completed: Boolean = false,
    val started: Boolean = false,
    val bookmarked: Boolean = false,
    val completedAtMillis: Long = 0,
    val updatedAtMillis: Long = 0,
    val selectedAnswers: Map<String, String> = emptyMap(),
    val quizScore: Int = 0,
    val adminImageRegenerationStatus: String = "",
    val adminImageRegenerationJobId: String = "",
    val adminImageRegenerationErrorMessage: String = "",
)

@Serializable
data class DailyLessonQuestion(
    val id: String = "",
    val prompt: String = "",
    val options: List<DailyLessonQuizOption> = emptyList(),
    val correctOptionId: String = "",
    val explanation: String = "",
)

@Serializable
data class DailyLessonQuizOption(
    val id: String = "",
    val text: String = "",
)

@Serializable
data class DailyLessonResearchSource(
    val title: String = "",
    val url: String = "",
    val publishedDate: String = "",
    val sourceType: String = "",
    val summary: String = "",
)

@Serializable
data class DailyLessonProgress(
    val lessonId: String = "",
    val started: Boolean = false,
    val completed: Boolean = false,
    val bookmarked: Boolean = false,
    val selectedAnswers: Map<String, String> = emptyMap(),
    val quizScore: Int = 0,
    val tokensAwarded: Int = 0,
    val completedAtMillis: Long = 0,
    val updatedAtMillis: Long = 0,
)

@Serializable
data class DailyLessonProgressPatch(
    val lessonId: String = "",
    val started: Boolean? = null,
    val bookmarked: Boolean? = null,
    val updatedAtMillis: Long? = null,
)

@Serializable
data class DailyLessonCompletion(
    val lessonId: String = "",
    val completed: Boolean = false,
    val tokensAwarded: Int = 0,
    val totalTokens: Int = 0,
    val quizScore: Int = 0,
)

enum class DailyLessonRegenerateSection(val wireValue: String) {
    Content("content"),
    Image("image"),
}

@Serializable
data class RegenerateDailyLessonSectionResponse(
    val lessonId: String = "",
    val section: String = "",
    val contentMarkdown: String = "",
    val imageUrl: String = "",
    val imagePrompt: String = "",
    val jobId: String = "",
    val queued: Boolean = false,
)

@Serializable
data class GenerateDebugDailyLessonResponse(
    val lessonId: String = "",
    val createdDateIso: String = "",
)

fun DailyLesson.withProgress(progress: DailyLessonProgress?): DailyLesson {
    if (progress == null) return this
    return copy(
        started = progress.started,
        completed = progress.completed,
        bookmarked = progress.bookmarked,
        completedAtMillis = progress.completedAtMillis,
        updatedAtMillis = progress.updatedAtMillis,
        selectedAnswers = progress.selectedAnswers,
        quizScore = progress.quizScore,
    )
}
