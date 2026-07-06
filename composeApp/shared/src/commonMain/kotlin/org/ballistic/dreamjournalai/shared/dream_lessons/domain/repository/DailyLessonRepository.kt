package org.ballistic.dreamjournalai.shared.dream_lessons.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLesson
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonCompletion
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonProgress
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonRegenerateSection
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.RegenerateDailyLessonSectionResponse

interface DailyLessonRepository {
    fun observeRecentLessons(): Flow<List<DailyLesson>>
    fun observeProgress(): Flow<Map<String, DailyLessonProgress>>
    fun observeLessonAdmin(): Flow<Boolean>
    fun isLessonAdmin(): Boolean
    suspend fun markStarted(lessonId: String): Resource<Unit>
    suspend fun setBookmarked(lessonId: String, bookmarked: Boolean): Resource<Unit>
    suspend fun completeLesson(
        lessonId: String,
        selectedAnswers: Map<String, String>,
    ): Resource<DailyLessonCompletion>
    suspend fun generateDebugLesson(): Resource<String>
    suspend fun regenerateLiveLessonSection(
        lessonId: String,
        section: DailyLessonRegenerateSection,
        instructions: String,
    ): Resource<RegenerateDailyLessonSectionResponse>
}
