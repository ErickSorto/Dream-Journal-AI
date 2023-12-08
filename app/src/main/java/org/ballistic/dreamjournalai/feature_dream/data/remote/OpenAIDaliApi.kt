package org.ballistic.dreamjournalai.feature_dream.data.remote

import androidx.annotation.Keep
import org.ballistic.dreamjournalai.BuildConfig
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.davinci.ImageGenerationDTO
import org.ballistic.dreamjournalai.feature_dream.domain.model.ImagePrompt
import retrofit2.Response
import retrofit2.http.*
@Keep
interface OpenAIDaliApi {

    @Headers("Content-Type: application/json", "Authorization: Bearer " + BuildConfig.API_KEY)
    @POST("images/generations")
    suspend fun getImageGeneration(@Body prompt: ImagePrompt): Response<ImageGenerationDTO>
}
