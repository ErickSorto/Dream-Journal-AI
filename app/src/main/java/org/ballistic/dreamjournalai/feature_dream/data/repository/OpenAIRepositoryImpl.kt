package org.ballistic.dreamjournalai.feature_dream.data.repository

import org.ballistic.dreamjournalai.feature_dream.data.remote.OpenAIApi
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.CompletionDTO
import org.ballistic.dreamjournalai.feature_dream.domain.repository.OpenAIRepository
import javax.inject.Inject

class OpenAIRepositoryImpl @Inject constructor(
    private val api: OpenAIApi

): OpenAIRepository {


    override suspend fun getCompletion(
        apiKey: String,
        model: String,
        prompt: String,
        maxTokens: Int,
        temperature: Int,
        frequencyPenalty: Int
    ): CompletionDTO {
        return api.getCompletion(apiKey, model, prompt, maxTokens, temperature).body()!!
    }
}

