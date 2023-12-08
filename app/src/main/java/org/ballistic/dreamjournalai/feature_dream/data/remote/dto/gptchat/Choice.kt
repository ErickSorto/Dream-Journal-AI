package org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class Choice(
    val index : Int,
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String
)