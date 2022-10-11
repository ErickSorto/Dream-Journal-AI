package org.ballistic.dreamcatcherai.feature_dream.data.data_source

import androidx.room.*
import org.ballistic.dreamcatcherai.feature_dream.domain.model.Dream

import kotlinx.coroutines.flow.Flow

@Dao
interface DreamDao {
    @Query("SELECT * FROM dream")
    fun getDreams(): Flow<List<Dream>>

    @Query("SELECT * FROM dream WHERE id = :id")
    suspend fun getDreamById(id: Int): Dream?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDream(dream: Dream)

    @Delete
    suspend fun deleteDream(dream: Dream)
}