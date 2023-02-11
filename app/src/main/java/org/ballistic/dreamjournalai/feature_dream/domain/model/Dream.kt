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
    constructor():this("","","", "",false,false,false,false,false,0,0,0,"",0,null,"",null)
}
class InvalidDreamException(message: String) : Exception(message)