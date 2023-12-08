package org.ballistic.dreamjournalai.feature_dream.domain.model

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat.Choice
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat.Usage
@Keep
data class ChatCompletion(
    val id: String,
    @SerializedName("object")
    val objectValue: String,
    val created: Int,
    val choices: List<Choice>,
    val usage: Usage
)