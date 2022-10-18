package org.ballistic.dreamjournalai.feature_dream.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ballistic.dreamjournalai.R

@Entity
data class Dream(
    val title: String,
    val content: String,
    val AIResponse: String,
    val timestamp: Long,
    val isFavorite: Boolean,
    val isLucid: Boolean,
    val isNightmare: Boolean,
    val isRecurring: Boolean,
    val falseAwakening: Boolean,
    val lucidityRating: Int,
    val moodRating: Int,
    val vividityRating: Int,
    val realismRating: Int,
    val timeOfDay: String,

    val dreamImageBackground: Int,
    @PrimaryKey val id: Int? = null
) {
    companion object {
        val dreamBackgroundColors = listOf(

            R.drawable.pink_background,
            R.drawable.red_background,
            R.drawable.black_backgrownd,
            R.drawable.blue_background,
            R.drawable.green_background,
            R.drawable.white_background,
            )
    }
}

class InvalidDreamException(message: String) : Exception(message)
