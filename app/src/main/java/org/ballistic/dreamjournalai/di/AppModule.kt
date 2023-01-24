package org.ballistic.dreamjournalai.di

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ballistic.dreamjournalai.feature_dream.data.repository.DreamRepositoryImpl
import org.ballistic.dreamjournalai.feature_dream.domain.repository.DreamRepository
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.*
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideDreamRepository(
        storage: FirebaseStorage,
        db: FirebaseFirestore
    ): DreamRepository {
        return DreamRepositoryImpl(
            storage = storage,
            db = db
        )
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
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