package org.ballistic.dreamcatcherai.feature_dream.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ballistic.dreamcatcherai.feature_dream.domain.model.Dream

interface DreamRepository {

    fun getDreams(): Flow<List<Dream>>

    suspend fun getDreamById(id: Int): Dream?

    suspend fun insertDream(dream: Dream)

    suspend fun deleteDream(dream: Dream)
}