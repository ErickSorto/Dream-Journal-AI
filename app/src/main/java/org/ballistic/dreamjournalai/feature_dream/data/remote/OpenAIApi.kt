package org.ballistic.dreamjournalai.feature_dream.data.remote

import org.ballistic.dreamjournalai.BuildConfig
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.CompletionDTO
import retrofit2.Response
import retrofit2.http.*


interface OpenAIApi {

    @Headers("Content-Type: application/json", "Authorization: Bearer " + BuildConfig.API_KEY)

    fun getCompletion(
        @Body model:String, @Body prompt: String,
        @Body maxTokens: Int, @Body temperature: Int): Response <CompletionDTO>
}