package org.ballistic.dreamjournalai.feature_dream.domain.model

import com.google.gson.annotations.SerializedName
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat.Choice
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat.Usage

data class ChatCompletion(
    val id: String,
    @SerializedName("object")
    val objectValue: String,
    val created: Int,
    val choices: List<Choice>,
    val usage: Usage
)