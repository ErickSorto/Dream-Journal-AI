package org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model


import androidx.compose.runtime.Stable
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.background_during_day
import dreamjournalai.composeapp.shared.generated.resources.beautiful_lighthouse
import dreamjournalai.composeapp.shared.generated.resources.blue_lighthouse
import dreamjournalai.composeapp.shared.generated.resources.dark_night_lighthouse
import dreamjournalai.composeapp.shared.generated.resources.full_life_lighthouse
import dreamjournalai.composeapp.shared.generated.resources.green_lighthouse_background
import dreamjournalai.composeapp.shared.generated.resources.heaven_lighthouse
import dreamjournalai.composeapp.shared.generated.resources.purple_skies_lighthouse
import dreamjournalai.composeapp.shared.generated.resources.red_lighthouse_background
import dreamjournalai.composeapp.shared.generated.resources.sunrise_lighthouse
import dreamjournalai.composeapp.shared.generated.resources.yellow_lighthouse_background
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Stable
@Serializable
data class Dream @OptIn(ExperimentalTime::class) constructor(
    val title: String = "",
    val content: String = "",
    val timestamp: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
    val date: String = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .toString(),  // e.g. "2023-07-07"
    val sleepTime: String = "23:00",  // "23:00"
    val wakeTime: String = "07:00",   // "07:00"
    @SerialName("airesponse") // If you want JSON key "airesponse"
    val AIResponse: String = "",
    val isFavorite: Boolean = false,
    val isLucid: Boolean = false,
    val isNightmare: Boolean = false,
    val isRecurring: Boolean = false,
    val falseAwakening: Boolean = false,

    val lucidityRating: Int = 0,
    val moodRating: Int = 0,
    val vividnessRating: Int = 0,
    val emotionalRadar: DreamEmotionRadar = DreamEmotionRadar(),
    val timeOfDay: String = "",

    // For a KMM-friendly approach, we store just an Int or a String reference to an image.
    // If you need actual images, you might store a URL or a resource ID differently on each platform.
    val backgroundImage: Int = 0,
    val generatedImage: String = "",
    val generatedDetails: String = "",
    val imageGenerationStatus: String = "",
    val imageGenerationJobId: String = "",
    val imageGenerationStartedAt: Long = 0,
    val imageGenerationUpdatedAt: Long = 0,
    val imageGenerationCompletedAt: Long = 0,
    val imageGenerationErrorCode: String = "",
    val imageGenerationErrorMessage: String = "",

    val dreamAIAdvice: String = "",
    val dreamQuestion: String = "",
    val dreamAIQuestionAnswer: String = "",
    val dreamAIStory: String = "",
    val dreamAIMood: String = "",
    
    val audioUrl: String = "",
    val audioTimestamp: Long = 0,
    val audioDuration: Long = 0,
    val isAudioPermanent: Boolean = false,
    val audioTranscription: String = "",
    val serverDreamDay: String = "",

    val id: String? = null,
    val uid: String? = null
) {
    @OptIn(ExperimentalTime::class)
    fun hasPendingImageGeneration(
        nowMillis: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
    ): Boolean {
        if (imageGenerationStatus != IMAGE_GENERATION_STATUS_QUEUED &&
            imageGenerationStatus != IMAGE_GENERATION_STATUS_RUNNING
        ) {
            return false
        }

        val lastActivityAt = maxOf(imageGenerationUpdatedAt, imageGenerationStartedAt)
        if (lastActivityAt <= 0L) {
            return false
        }

        return nowMillis - lastActivityAt <= IMAGE_GENERATION_STALE_AFTER_MILLIS
    }

    @OptIn(ExperimentalTime::class)
    fun hasStalePendingImageGeneration(
        nowMillis: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
    ): Boolean {
        val pendingStatus = imageGenerationStatus == IMAGE_GENERATION_STATUS_QUEUED ||
                imageGenerationStatus == IMAGE_GENERATION_STATUS_RUNNING
        return pendingStatus && !hasPendingImageGeneration(nowMillis)
    }

    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombination = listOf(
            title,
            content,
            "$title $content",
            "$content $title"
        )
        return matchingCombination.any { it.contains(query, ignoreCase = true) }
    }

    companion object {
        const val IMAGE_GENERATION_STATUS_QUEUED = "queued"
        const val IMAGE_GENERATION_STATUS_RUNNING = "running"
        const val IMAGE_GENERATION_STATUS_FAILED = "failed"
        const val IMAGE_GENERATION_STALE_AFTER_MILLIS = 10 * 60 * 1000L

        val dreamBackgroundImages = listOf(
            Res.drawable.purple_skies_lighthouse,
            Res.drawable.red_lighthouse_background,
            Res.drawable.green_lighthouse_background,
            Res.drawable.blue_lighthouse,
            Res.drawable.full_life_lighthouse,
            Res.drawable.heaven_lighthouse,
            Res.drawable.dark_night_lighthouse,
            Res.drawable.yellow_lighthouse_background,
            Res.drawable.beautiful_lighthouse,
            Res.drawable.background_during_day,
            Res.drawable.sunrise_lighthouse
        )
    }

    constructor() : this(
        title = "",
        content = "",
        timestamp = 0,
        date = "",
        sleepTime = "",
        wakeTime = "",
        AIResponse = "",
        isFavorite = false,
        isLucid = false,
        isNightmare = false,
        isRecurring = false,
        falseAwakening = false,
        lucidityRating = 0,
        moodRating = 0,
        vividnessRating = 0,
        emotionalRadar = DreamEmotionRadar(),
        timeOfDay = "",
        backgroundImage = 0,
        generatedImage = "",
        generatedDetails = "",
        imageGenerationStatus = "",
        imageGenerationJobId = "",
        imageGenerationStartedAt = 0,
        imageGenerationUpdatedAt = 0,
        imageGenerationCompletedAt = 0,
        imageGenerationErrorCode = "",
        imageGenerationErrorMessage = "",
        dreamAIAdvice = "",
        dreamQuestion = "",
        dreamAIQuestionAnswer = "",
        dreamAIStory = "",
        dreamAIMood = "",
        audioUrl = "",
        audioTimestamp = 0,
        audioDuration = 0,
        isAudioPermanent = false,
        audioTranscription = "",
        serverDreamDay = "",
        id = null,
        uid = null
    )
}

class InvalidDreamException(message: String) : Exception(message)
