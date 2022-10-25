package org.ballistic.dreamjournalai.feature_dream.data.remote

import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.CompletionDTO
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.PromptDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface OpenAIApi {


    @POST("v1/completions")
    fun getCompletion(@Body prompt: PromptDTO): Response<CompletionDTO>


}