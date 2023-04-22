package org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat



import com.google.gson.annotations.SerializedName
import org.ballistic.dreamjournalai.feature_dream.domain.model.ChatCompletion

data class CompletionChatDTO(
    val id: String,
    @SerializedName("object")
    val objectValue: String,
    val created: Int,
    val choices: List<Choice>,
    val usage: Usage
)

fun CompletionChatDTO.toChatCompletion() = ChatCompletion(
    id = id,
    objectValue = objectValue,
    created = created,
    choices = choices.map {
        Choice(
            index = it.index,
            message = it.message,
            finishReason = it.finishReason
        )
    },
    usage = Usage(
        promptTokens = usage.promptTokens,
        completionTokens = usage.completionTokens,
        totalTokens = usage.totalTokens
    )
)