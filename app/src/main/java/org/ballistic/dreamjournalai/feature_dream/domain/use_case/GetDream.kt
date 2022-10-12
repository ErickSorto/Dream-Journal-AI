package org.ballistic.dreamjournalai.feature_dream.domain.use_case

import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.repository.DreamRepository

class GetDream (
    private val repository: DreamRepository
        ){
    suspend operator fun invoke(id: Int): Dream? {
        return repository.getDreamById(id)
    }
}