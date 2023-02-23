package org.ballistic.dreamjournalai.feature_dream.data.repository

import android.util.Log
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.data.remote.OpenAITextApi
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.CompletionDTO
import org.ballistic.dreamjournalai.feature_dream.domain.model.Prompt
import org.ballistic.dreamjournalai.feature_dream.domain.repository.OpenAIRepository
import javax.inject.Inject

class OpenAIRepositoryImpl @Inject constructor(
    private val api: OpenAITextApi
): OpenAIRepository {

    override suspend fun getCompletion(
        prompt: Prompt
    ): Resource<CompletionDTO> {

        var result = api.getCompletion(prompt)
        if(result.isSuccessful){
            return Resource.Success(data = result.body()!!)
        }else{
            Log.d("GetOpenAITextResponse", "${result.code()} ${result.message()}, ${result.body()}, ${result.headers()}")
            return Resource.Error(message = result.message())
        }
    }
}

