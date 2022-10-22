package org.ballistic.dreamjournalai.feature_dream.data.remote

import retrofit2.Response
import retrofit2.http.GET


interface OpenAIApi {

    @GET("v1/models/text-davinci-002")
    fun getModel(text: String): Response <String>


}