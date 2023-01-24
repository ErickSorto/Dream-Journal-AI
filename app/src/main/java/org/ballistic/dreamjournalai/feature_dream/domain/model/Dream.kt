package org.ballistic.dreamjournalai.feature_dream.domain.model

import com.google.firebase.firestore.PropertyName
import org.ballistic.dreamjournalai.R
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Dream(
    val title: String,
    val content: String,
    val timestamp: String = System.currentTimeMillis().toString(),
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
): Parcelable {
    companion object { //backgroundssss
        val dreamBackgroundImages = listOf(

            R.drawable.pink_river,
            R.drawable.red_planet,
            R.drawable.dark_blue_moon,
            R.drawable.blue_lighthouse,
            R.drawable.green_mushroom,
            R.drawable.white_snow,
            )
    }

    constructor():this("","","", "",false,false,false,false,false,0,0,0,"",0,null,"",null)
}

class InvalidDreamException(message: String) : Exception(message)
