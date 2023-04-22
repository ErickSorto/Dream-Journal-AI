package org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat

import com.google.gson.annotations.SerializedName

data class Choice(
    val index : Int,
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String
)