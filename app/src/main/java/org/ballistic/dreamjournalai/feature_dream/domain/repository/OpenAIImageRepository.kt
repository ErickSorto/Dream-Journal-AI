package org.ballistic.dreamjournalai.feature_dream.domain.repository

import androidx.annotation.Keep
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.davinci.ImageGenerationDTO
import org.ballistic.dreamjournalai.feature_dream.domain.model.ImagePrompt
@Keep
interface OpenAIImageRepository {
    suspend fun getImageGeneration(
      prompt: ImagePrompt
    ): Resource<ImageGenerationDTO>
}