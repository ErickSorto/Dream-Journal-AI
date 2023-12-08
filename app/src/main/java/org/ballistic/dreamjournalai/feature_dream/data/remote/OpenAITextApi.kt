package org.ballistic.dreamjournalai.feature_dream.data.remote

import androidx.annotation.Keep
import org.ballistic.dreamjournalai.BuildConfig
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat.CompletionChatDTO
import org.ballistic.dreamjournalai.feature_dream.domain.model.PromptChat
import retrofit2.Response
import retrofit2.http.*
@Keep
interface OpenAITextApi {

    @Headers("Content-Type: application/json", "Authorization: Bearer " + BuildConfig.API_KEY)
    @POST("completions")
    suspend fun getChatCompletion(@Body prompt: PromptChat): Response <CompletionChatDTO>
}