package org.ballistic.dreamjournalai.feature_dream.domain.repository

import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.davinci.ImageGenerationDTO
import org.ballistic.dreamjournalai.feature_dream.domain.model.ImagePrompt

interface OpenAIImageRepository {
    suspend fun getImageGeneration(
      prompt: ImagePrompt
    ): Resource<ImageGenerationDTO>
}