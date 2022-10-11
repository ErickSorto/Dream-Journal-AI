package org.ballistic.dreamcatcherai.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ballistic.dreamcatcherai.feature_dream.data.data_source.DreamDatabase
import org.ballistic.dreamcatcherai.feature_dream.data.repository.DreamRepositoryImplementation
import org.ballistic.dreamcatcherai.feature_dream.domain.repository.DreamRepository
import org.ballistic.dreamcatcherai.feature_dream.domain.use_case.DeleteDream
import org.ballistic.dreamcatcherai.feature_dream.domain.use_case.DreamUseCases
import org.ballistic.dreamcatcherai.feature_dream.domain.use_case.GetDreams
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDreamDatabase(
        app: Application
    ): DreamDatabase {
        return Room.databaseBuilder(
            app,
            DreamDatabase::class.java,
            "DreamDatabase"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDreamRepository(
        db: DreamDatabase
    ): DreamRepository {
        return DreamRepositoryImplementation(db.dreamDao)
    }

    @Provides
    @Singleton
    fun provideDreamUseCases(
        repository: DreamRepository
    ): DreamUseCases {
        return DreamUseCases(
            getDreams = GetDreams(repository),
            deleteDream = DeleteDream(repository),

        )
    }

}