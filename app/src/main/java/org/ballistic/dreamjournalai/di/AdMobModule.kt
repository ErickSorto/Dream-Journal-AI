package org.ballistic.dreamjournalai.di

import android.app.Activity
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ballistic.dreamjournalai.ad_feature.data.AdManagerRepositoryImpl
import org.ballistic.dreamjournalai.ad_feature.domain.AdManagerRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdMobModule {

    @Singleton
    @Provides
    fun provideAdManagerRepository(): AdManagerRepository {
        return AdManagerRepositoryImpl("ca-app-pub-8710979310678386/8178296701")
    }
}