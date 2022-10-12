package org.ballistic.dreamjournalai.feature_dream.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Dream(
    val title:String,
    val content:String,
    val timestamp:Long,
    val color:Int,
    @PrimaryKey val id:Int? = null
){
    companion object{
        val dreamColors = listOf(
            Red,
            Gray,
            Color.Magenta,
            Color.Blue,
            Yellow
        )
    }
}

class InvalidDreamException(message: String): Exception(message)
