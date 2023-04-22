package org.ballistic.dreamjournalai.feature_dream.data.repository

import android.util.Log
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.data.remote.OpenAIDaliApi
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.davinci.ImageGenerationDTO
import org.ballistic.dreamjournalai.feature_dream.domain.model.ImagePrompt
import org.ballistic.dreamjournalai.feature_dream.domain.repository.OpenAIImageRepository
import javax.inject.Inject

class OpenAIImageRepositoryImpl @Inject constructor(
    private val api: OpenAIDaliApi
): OpenAIImageRepository {
    override suspend fun getImageGeneration(prompt: ImagePrompt): Resource<ImageGenerationDTO> {
        var result = api.getImageGeneration(prompt)

        if (result.isSuccessful) {
            return Resource.Success(result.body()!!)
        } else {
            Log.d("OpenAIImageRepository", "${result.code()} ${result.message()}, ${result.body()}, ${result.headers()}")
            return Resource.Error(result.message())
        }
    }

}