package org.ballistic.dreamjournalai.feature_dream.data.repository

import kotlinx.coroutines.flow.Flow
import org.ballistic.dreamjournalai.feature_dream.data.data_source.DreamDao
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.repository.DreamRepository

class DreamRepositoryImplementation (private val dao: DreamDao) : DreamRepository {
    override fun getDreams(): Flow<List<Dream>> {
        return dao.getDreams()
    }

    override suspend fun getDreamById(id: Int): Dream? {
        return dao.getDreamById(id)
    }

    override suspend fun insertDream(dream: Dream) {
        dao.insertDream(dream)
    }

    override suspend fun deleteDream(dream: Dream) {
        dao.deleteDream(dream)
    }


}