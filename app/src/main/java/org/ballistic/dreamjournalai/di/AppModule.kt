package org.ballistic.dreamjournalai.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ballistic.dreamjournalai.feature_dream.data.data_source.DreamDatabase
import org.ballistic.dreamjournalai.feature_dream.data.repository.DreamRepositoryImpl
import org.ballistic.dreamjournalai.feature_dream.domain.repository.DreamRepository
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.*
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
            DreamDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideDreamRepository(
        db: DreamDatabase
    ): DreamRepository {
        return DreamRepositoryImpl(db.dreamDao)
    }

    @Provides
    @Singleton
    fun provideDreamUseCases(
        repository: DreamRepository
    ): DreamUseCases {
        return DreamUseCases(
            getDreams = GetDreams(repository),
            deleteDream = DeleteDream(repository),
            addDream = AddDream(repository),
            getDream = GetDream(repository)

        )
    }
}