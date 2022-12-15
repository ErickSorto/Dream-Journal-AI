package org.ballistic.dreamjournalai.feature_dream.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ballistic.dreamjournalai.R

@Entity
data class Dream(
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
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
    @PrimaryKey val id: Int? = null
) {
    companion object { //backgrounds
        val dreamBackgroundImages = listOf(

            R.drawable.pink_river,
            R.drawable.red_planet,
            R.drawable.dark_blue_moon,
            R.drawable.blue_lighthouse,
            R.drawable.green_mushroom,
            R.drawable.white_snow,
            )
    }
}

class InvalidDreamException(message: String) : Exception(message)
