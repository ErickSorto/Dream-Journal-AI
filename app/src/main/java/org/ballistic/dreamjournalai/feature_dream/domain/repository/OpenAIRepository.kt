package org.ballistic.dreamjournalai.feature_dream.domain.repository

import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.domain.model.ChatCompletion
import org.ballistic.dreamjournalai.feature_dream.domain.model.PromptChat

interface OpenAIRepository {

    suspend fun getChatCompletion(prompt: PromptChat): Resource<ChatCompletion>

}