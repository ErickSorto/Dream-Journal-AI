package org.ballistic.dreamjournalai.feature_dream.data.remote

import org.ballistic.dreamjournalai.BuildConfig
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.CompletionDTO
import org.ballistic.dreamjournalai.feature_dream.domain.model.Prompt
import retrofit2.Response
import retrofit2.http.*


interface OpenAIApi {

    @Headers("Content-Type: application/json", "Authorization: Bearer " + "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")
    @POST("completions")
    suspend fun getCompletion(@Body prompt: Prompt): Response <CompletionDTO>


}