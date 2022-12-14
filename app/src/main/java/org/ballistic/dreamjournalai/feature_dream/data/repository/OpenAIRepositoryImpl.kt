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
        //AMINE
        //check the api response first, and see if result.isSuccessful is true,
        //handle the use case of failure too, this is where the  Resource class that you have in core comes in handy
        //i'll leave an example of usage here
        //You'll have to change the return type of the function to make it Result<Type>
        //in functions that use this (typically in your use case), u'll return the same object but either with your data
        //in case the fetch was successfull, or in the opposite case, you'll return a Result.Error(message="There was an error while fetching the data")
        //you can also be more specific and see what is the error code returned in api.getCompletion(prompt).code()
        //this way you can be more direct about the error message, for example 5xx is server side error, 4xx network error etc
        /*
            var data : Resource<Any>? = null
            var result = api.getCompletion(prompt)
            if(result.isSuccessful){
                data = Resource.Success(data = result.body()!!)
            }else{
                data = Resource.Error(message = result.message(), data = result.errorBody())
            }
            return data
        */

        var result = api.getCompletion(prompt)
        if(result.isSuccessful){
            return Resource.Success(data = result.body()!!)
        }else{
            Log.d("GetOpenAITextResponse", "${result.code()} ${result.message()}, ${result.body()}, ${result.headers()}")
            return Resource.Error(message = result.message())
        }

       //return api.getCompletion(prompt).body()!!
    }
}

