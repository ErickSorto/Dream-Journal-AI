package org.ballistic.dreamjournalai.shared.dream_tools.domain

import kotlinx.coroutines.flow.Flow
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.dream_tools.domain.model.DreamWorldPainting

interface DreamWorldPaintingRepository {
    fun getPaintings(): Flow<List<DreamWorldPainting>>
    suspend fun savePainting(painting: DreamWorldPainting): Resource<Unit>
    suspend fun deletePainting(id: String): Resource<Unit>
}
