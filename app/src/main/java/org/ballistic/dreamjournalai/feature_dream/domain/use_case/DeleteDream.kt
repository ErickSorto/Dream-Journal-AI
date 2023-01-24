package org.ballistic.dreamjournalai.feature_dream.domain.use_case

import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.repository.DreamRepository

class DeleteDream(private val repository: DreamRepository) {
    suspend operator fun invoke(dream: Dream) {
        dream.id?.let { repository.deleteDream(it) }
    }
}
