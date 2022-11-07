package org.ballistic.dreamjournalai.feature_dream.domain.use_case

import android.util.Log
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.domain.model.ImagePrompt
import org.ballistic.dreamjournalai.feature_dream.domain.repository.OpenAIImageRepository
import retrofit2.HttpException
import javax.inject.Inject

class GetOpenAIImageGeneration @Inject constructor(
    private val repository: OpenAIImageRepository
) {
     operator fun invoke(prompt: ImagePrompt) = flow{
         emit (Resource.Loading<String>())
            val response = repository.getImageGeneration(prompt)
            when(response){
                is Resource.Success -> {
                    emit(Resource.Success(response.data!!))
                }
                is Resource.Error -> {
                    Log.d("GetOpenAIImageGeneration", response.message.toString())
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
                 Log.d("GetOpenAIImageGeneration", "HttpException")
                 emit(Resource.Error("http exception.. ${it.message()}"))
             }
             else -> {
                 Log.d("GetOpenAIImageGeneration", "Exception")
                 emit(Resource.Error("some exception happened.."))
             }
         }
     }
}