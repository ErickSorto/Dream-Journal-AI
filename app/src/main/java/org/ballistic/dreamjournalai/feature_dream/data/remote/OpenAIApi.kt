package org.ballistic.dreamjournalai.feature_dream.data.remote

import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.CompletionDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface OpenAIApi {
    @POST("v1/completions")
    fun getCompletion(
        @Query ("api_key") apiKey: String,
        @Body model:String, @Body prompt: String,
        @Body maxTokens: Int, @Body temperature: Int): Response <CompletionDTO>
}