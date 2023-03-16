package org.ballistic.dreamjournalai.feature_dream.domain.model

import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import org.ballistic.dreamjournalai.R
import java.time.Clock
import java.time.LocalDate
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Parcelize
data class Dream @RequiresApi(Build.VERSION_CODES.O) constructor(
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = LocalDate.now(Clock.systemUTC()).toString(),
    val sleepTime: String = String.format("%02d:%02d", 23, 0),
    val wakeTime: String = String.format("%02d:%02d", 7, 0),
    @PropertyName("airesponse")
    val AIResponse: String,
    val isFavorite: Boolean,
    val isLucid: Boolean,
    val isNightmare: Boolean,
    val isRecurring: Boolean,
    val falseAwakening: Boolean,
    val lucidityRating: Int,
    val moodRating: Int,
    val vividnessRating: Int,
    val timeOfDay: String,
    val backgroundImage: Int,
    val generatedImage: String?,
    val generatedDetails: String,
    val id: String?
) : Parcelable {

    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombination = listOf(
            title,
            content,
            "$title $content",
            "$content $title"
        )
        return matchingCombination.any { it.contains(query, true) }
    }

    companion object { //backgroundssss
        val dreamBackgroundImages = listOf(
            R.drawable.purple_lighthouse_background,
            R.drawable.red_lighthouse_background,
            R.drawable.dark_blue_lighthouse_background,
            R.drawable.blue_lighthouse,
            R.drawable.garden_lighthouse,
            R.drawable.white_lighthouse_background,
            R.drawable.gray_lighthouse_background,
            R.drawable.yellow_lighthouse_background,
            R.drawable.trippy_dreambackground,
            R.drawable.ocean_lighthouse
        )
    }

    constructor() : this(
        "",
        "",
        0,
        "",
        "",
        "",
        "",
        false,
        false,
        false,
        false,
        false,
        0,
        0,
        0,
        "",
        0,
        null,
        "",
        null
    )
}

class InvalidDreamException(message: String) : Exception(message)