package org.ballistic.dreamjournalai.feature_dream.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream

@Database(entities = [Dream::class], version = 1)
abstract class DreamDatabase: RoomDatabase() {

    abstract val dreamDao: DreamDao
}

