package org.ballistic.dreamjournalai.feature_dream.data.repository

import androidx.annotation.Keep
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.data.remote.OpenAITextApi
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.gptchat.toChatCompletion

import org.ballistic.dreamjournalai.feature_dream.domain.model.ChatCompletion


import org.ballistic.dreamjournalai.feature_dream.domain.model.PromptChat
import org.ballistic.dreamjournalai.feature_dream.domain.repository.OpenAIRepository
import javax.inject.Inject
@Keep
class OpenAIRepositoryImpl @Inject constructor(
    private val api: OpenAITextApi
): OpenAIRepository {

    override suspend fun getChatCompletion(prompt: PromptChat): Resource<ChatCompletion> {
        return try {
            val response = api.getChatCompletion(prompt)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it.toChatCompletion())
                } ?: Resource.Error("Response body is null")
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}