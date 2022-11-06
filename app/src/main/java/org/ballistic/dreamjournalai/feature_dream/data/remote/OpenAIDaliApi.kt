package org.ballistic.dreamjournalai.feature_dream.data.remote

import org.ballistic.dreamjournalai.BuildConfig
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.ImageGenerationDTO
import org.ballistic.dreamjournalai.feature_dream.domain.model.ImagePrompt
import retrofit2.Response
import retrofit2.http.*

interface OpenAIDaliApi {

    @Headers("Content-Type: application/json", "Authorization: Bearer " + BuildConfig.API_KEY)
    @POST("images/generations")
    suspend fun getImageGeneration(@Body prompt: ImagePrompt): Response<ImageGenerationDTO>
}
