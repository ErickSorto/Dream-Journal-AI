package org.ballistic.dreamjournalai.feature_dream.domain.repository

import androidx.annotation.Keep
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.domain.model.ChatCompletion
import org.ballistic.dreamjournalai.feature_dream.domain.model.PromptChat
@Keep
interface OpenAIRepository {

    suspend fun getChatCompletion(prompt: PromptChat): Resource<ChatCompletion>

}