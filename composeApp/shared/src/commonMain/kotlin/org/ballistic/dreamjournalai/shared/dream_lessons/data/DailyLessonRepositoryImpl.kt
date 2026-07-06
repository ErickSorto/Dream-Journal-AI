package org.ballistic.dreamjournalai.shared.dream_lessons.data

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.functions.functions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ballistic.dreamjournalai.shared.core.Constants
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.platform.isDebugBuild
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DAILY_LESSON_ACCESS_PREMIUM
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLesson
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonCompletion
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonProgress
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonProgressPatch
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonRegenerateSection
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.GenerateDebugDailyLessonResponse
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.RegenerateDailyLessonSectionResponse
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.repository.DailyLessonRepository
import kotlin.time.Clock

private val lessonLogger = Logger.withTag("DailyLessonRepo")

class DailyLessonRepositoryImpl(
    private val db: FirebaseFirestore,
) : DailyLessonRepository {

    private val currentUser get() = Firebase.auth.currentUser

    override fun isLessonAdmin(): Boolean {
        return isAdminEmail(currentUser?.email)
    }

    override fun observeLessonAdmin(): Flow<Boolean> {
        val authAdmin = isLessonAdmin()
        val uid = currentUser?.uid ?: return flowOf(authAdmin)
        return db.collection(Constants.USERS)
            .document(uid)
            .snapshots()
            .map { snapshot ->
                authAdmin ||
                    isAdminEmail(snapshot.getOptionalString(Constants.EMAIL)) ||
                    snapshot.getOptionalBoolean("lessonAdmin") == true ||
                    snapshot.getOptionalBoolean("admin") == true
            }
            .catch { error ->
                lessonLogger.e(error) { "Failed to observe lesson admin status" }
                emit(authAdmin)
            }
    }

    override fun observeRecentLessons(): Flow<List<DailyLesson>> {
        val cutoffMillis = Clock.System.now().toEpochMilliseconds() - RECENT_WINDOW_MILLIS
        val cutoff = Instant.fromEpochMilliseconds(cutoffMillis)
            .toLocalDateTime(TimeZone.UTC)
            .date
            .toString()

        val publicLessons = db.collection(DAILY_LESSONS)
            .where { "createdDateIso" greaterThanOrEqualTo cutoff }
            .orderBy("createdDateIso", Direction.DESCENDING)
            .limit(90)
            .snapshots()
            .map { snapshot -> snapshot.documents.toDailyLessons() }
            .map { lessons -> lessons.filter { lesson -> !lesson.isDebug } }

        if (!isDebugBuild()) {
            return publicLessons
        }

        val uid = currentUser?.uid ?: return publicLessons
        val debugLessons = db.collection(Constants.USERS)
            .document(uid)
            .collection(DEBUG_DAILY_LESSONS)
            .where { "createdDateIso" greaterThanOrEqualTo cutoff }
            .orderBy("createdDateIso", Direction.DESCENDING)
            .limit(30)
            .snapshots()
            .map { snapshot -> snapshot.documents.toDailyLessons() }

        return combine(publicLessons, debugLessons) { public, debug ->
            (debug + public).sortedByDescending { lesson -> lesson.createdDateIso }
        }
    }

    private fun List<dev.gitlive.firebase.firestore.DocumentSnapshot>.toDailyLessons(): List<DailyLesson> {
        return mapNotNull { document ->
            try {
                val lesson = document.data<DailyLesson>()
                lesson.copy(
                    id = document.id,
                    isPremium = lesson.isPremium || lesson.access == DAILY_LESSON_ACCESS_PREMIUM,
                )
            } catch (error: Exception) {
                lessonLogger.e(error) { "Failed to parse daily lesson ${document.id}" }
                null
            }
        }
    }

    override fun observeProgress(): Flow<Map<String, DailyLessonProgress>> {
        val user = currentUser
        if (user == null || user.isAnonymous) return flowOf(emptyMap())
        val uid = user.uid
        return db.collection(Constants.USERS)
            .document(uid)
            .collection(LESSON_PROGRESS)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    try {
                        val progress = document.data<DailyLessonProgress>()
                        progress.copy(lessonId = document.id)
                    } catch (error: Exception) {
                        lessonLogger.e(error) { "Failed to parse lesson progress ${document.id}" }
                        null
                    }
                }.associateBy { it.lessonId }
            }
    }

    override suspend fun markStarted(lessonId: String): Resource<Unit> {
        if (lessonId.isBlank()) return Resource.Error("Lesson id is required.")
        return writeProgressPatch(
            lessonId = lessonId,
            patch = DailyLessonProgressPatch(
                lessonId = lessonId,
                started = true,
                updatedAtMillis = Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    override suspend fun setBookmarked(lessonId: String, bookmarked: Boolean): Resource<Unit> {
        if (lessonId.isBlank()) return Resource.Error("Lesson id is required.")
        return writeProgressPatch(
            lessonId = lessonId,
            patch = DailyLessonProgressPatch(
                lessonId = lessonId,
                bookmarked = bookmarked,
                updatedAtMillis = Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    override suspend fun completeLesson(
        lessonId: String,
        selectedAnswers: Map<String, String>,
    ): Resource<DailyLessonCompletion> {
        val user = currentUser
        if (user == null || user.isAnonymous) {
            return Resource.Error("Please sign in to complete lessons.")
        }
        return try {
            val result = Firebase.functions
                .httpsCallable("completeDailyLesson")
                .invoke(
                    mapOf(
                        "lessonId" to lessonId,
                        "selectedAnswers" to selectedAnswers,
                    )
                )
            Resource.Success(result.data<DailyLessonCompletion>())
        } catch (error: Exception) {
            lessonLogger.e(error) { "Failed to complete lesson $lessonId" }
            Resource.Error(cloudFunctionMessage(error, "Could not complete this lesson."))
        }
    }

    override suspend fun generateDebugLesson(): Resource<String> {
        if (!isDebugBuild()) {
            return Resource.Error("Debug lesson generation is only available in debug builds.")
        }
        return try {
            val result = Firebase.functions
                .httpsCallable("generateDebugDailyDreamLesson")
                .invoke(emptyMap<String, String>())
            val response = result.data<GenerateDebugDailyLessonResponse>()
            if (response.lessonId.isBlank()) {
                Resource.Error("Debug lesson was not created.")
            } else {
                Resource.Success(response.lessonId)
            }
        } catch (error: Exception) {
            lessonLogger.e(error) { "Failed to generate debug lesson" }
            Resource.Error(cloudFunctionMessage(error, "Could not generate a debug lesson."))
        }
    }

    override suspend fun regenerateLiveLessonSection(
        lessonId: String,
        section: DailyLessonRegenerateSection,
        instructions: String,
    ): Resource<RegenerateDailyLessonSectionResponse> {
        if (!isLessonAdmin()) {
            return Resource.Error("Lesson admin access is required.")
        }
        if (lessonId.isBlank()) {
            return Resource.Error("Lesson id is required.")
        }

        return try {
            val result = Firebase.functions
                .httpsCallable("regenerateDailyLessonSection")
                .invoke(
                    mapOf(
                        "lessonId" to lessonId,
                        "section" to section.wireValue,
                        "instructions" to instructions,
                    )
                )
            Resource.Success(result.data<RegenerateDailyLessonSectionResponse>())
        } catch (error: Exception) {
            lessonLogger.e(error) { "Failed to regenerate ${section.wireValue} for lesson $lessonId" }
            Resource.Error(cloudFunctionMessage(error, "Could not regenerate this lesson section."))
        }
    }

    private suspend fun writeProgressPatch(
        lessonId: String,
        patch: DailyLessonProgressPatch,
    ): Resource<Unit> {
        val user = currentUser
        if (user == null || user.isAnonymous) {
            return Resource.Error("Please sign in to save lesson progress.")
        }
        val uid = user.uid
        return try {
            db.collection(Constants.USERS)
                .document(uid)
                .collection(LESSON_PROGRESS)
                .document(lessonId)
                .set(patch, merge = true) {
                    encodeDefaults = false
                }
            Resource.Success(Unit)
        } catch (error: Exception) {
            lessonLogger.e(error) { "Failed to update lesson progress $lessonId" }
            Resource.Error(error.message ?: "Could not update lesson progress.")
        }
    }

    private fun cloudFunctionMessage(error: Exception, fallback: String): String {
        val message = error.message?.takeIf { it.isNotBlank() } ?: return fallback
        return when {
            message.contains("premium", ignoreCase = true) -> "Premium membership is required for this lesson."
            message.contains("not found", ignoreCase = true) -> "This lesson is still being prepared."
            message.contains("unauthenticated", ignoreCase = true) -> "Please sign in again before completing lessons."
            else -> message
        }
    }

    private fun isAdminEmail(email: String?): Boolean {
        return email?.trim()?.lowercase() == LESSON_ADMIN_EMAIL
    }

    private fun DocumentSnapshot.getOptionalString(field: String): String? {
        return try {
            get<String>(field)
        } catch (_: Exception) {
            null
        }
    }

    private fun DocumentSnapshot.getOptionalBoolean(field: String): Boolean? {
        return try {
            get<Boolean>(field)
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val DAILY_LESSONS = "daily_lessons"
        const val DEBUG_DAILY_LESSONS = "debug_daily_lessons"
        const val LESSON_PROGRESS = "lesson_progress"
        const val RECENT_WINDOW_MILLIS = 90L * 24L * 60L * 60L * 1000L
        const val LESSON_ADMIN_EMAIL = "ninjaballista3@gmail.com"
    }
}
