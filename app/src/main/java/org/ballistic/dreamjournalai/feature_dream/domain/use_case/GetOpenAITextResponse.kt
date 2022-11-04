package org.ballistic.dreamjournalai.feature_dream.domain.use_case

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.toCompletion
import org.ballistic.dreamjournalai.feature_dream.domain.model.Completion
import org.ballistic.dreamjournalai.feature_dream.domain.model.Prompt
import org.ballistic.dreamjournalai.feature_dream.domain.repository.OpenAIRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

//UseCase
class GetOpenAITextResponse @Inject constructor(
    private val repository: OpenAIRepository,
    ) {

    operator fun invoke(prompt: Prompt) = flow {
            emit(Resource.Loading<String>())
            val response = repository.getCompletion(prompt)
            when(response){
                is Resource.Success -> {
                    emit(Resource.Success(response.data!!.toCompletion().choices[0].text))
                }
                is Resource.Error -> {
                    Log.d("GetOpenAITextResponse", response.message.toString())
                    emit(Resource.Error(response.message!!))
                }
                else -> {

                }
            }
    }.onStart {
        emit(Resource.Loading())
    }.catch {
        when(it){
            is HttpException -> {
                Log.d("GetOpenAITextResponse", "HttpException")
                emit(Resource.Error("http exception.. ${it.message()}"))
            }
            else -> {
                Log.d("GetOpenAITextResponse", "Exception")
                emit(Resource.Error("some exception happened.."))
            }
        }
    }
}

