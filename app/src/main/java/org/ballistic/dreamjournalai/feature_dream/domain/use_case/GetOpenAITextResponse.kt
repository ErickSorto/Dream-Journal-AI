package org.ballistic.dreamjournalai.feature_dream.domain.use_case

import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.domain.model.ChatCompletion
import org.ballistic.dreamjournalai.feature_dream.domain.model.PromptChat
import org.ballistic.dreamjournalai.feature_dream.domain.repository.OpenAIRepository
import retrofit2.HttpException
import javax.inject.Inject

//UseCase
@Keep
class GetOpenAITextResponse @Inject constructor(
    private val repository: OpenAIRepository,
) {

    operator fun invoke(prompt: PromptChat): Flow<Resource<ChatCompletion>> = flow {
        emit(Resource.Loading())
        val response = repository.getChatCompletion(prompt)
        emit(response)
    }.onStart {
        emit(Resource.Loading())
    }.catch { e ->
        when (e) {
            is HttpException -> {
                emit(Resource.Error("Network Error"))
                Log.e("GetOpenAITextResponse", "Network Error: ${e.message()}")
            }
            else -> {
                emit(Resource.Error("Conversion Error"))
                Log.e("GetOpenAITextResponse", "Conversion Error: ${e.message}")
            }
        }
    }
}