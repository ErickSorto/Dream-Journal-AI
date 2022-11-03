package org.ballistic.dreamjournalai.feature_dream.domain.repository

import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.CompletionDTO
import org.ballistic.dreamjournalai.feature_dream.domain.model.Prompt

interface OpenAIRepository {
    suspend fun getCompletion(
      prompt: Prompt
    ): Resource<CompletionDTO>
}