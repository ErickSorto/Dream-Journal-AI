package org.ballistic.dreamjournalai.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object OpenAIImageModule {


    @Provides
    @Singleton
    fun provideOpenAIImageApi(
        okHttpClient: OkHttpClient
    ): OpenAIDaliApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(OpenAIDaliApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenAIImageRepository(
        api: OpenAIDaliApi
    ): OpenAIImageRepository {
        return OpenAIImageRepositoryImpl(api)
    }

}