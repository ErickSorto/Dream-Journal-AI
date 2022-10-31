package org.ballistic.dreamjournalai.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ballistic.dreamjournalai.feature_dream.data.remote.OpenAIApi
import org.ballistic.dreamjournalai.feature_dream.data.repository.OpenAIRepositoryImpl
import org.ballistic.dreamjournalai.feature_dream.domain.repository.OpenAIRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OpenAIModule {
    @Provides
    @Singleton
    fun provideOpenAIApi(
    ): OpenAIApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenAIRepository(
        api: OpenAIApi
    ): OpenAIRepository {
        return OpenAIRepositoryImpl(api)
    }
}