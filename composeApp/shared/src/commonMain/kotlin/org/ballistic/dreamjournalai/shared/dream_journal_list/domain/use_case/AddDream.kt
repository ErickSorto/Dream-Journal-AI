package org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case

import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.InvalidDreamException
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.repository.DreamRepository
import kotlin.coroutines.cancellation.CancellationException

class AddDream(private val repository: DreamRepository) {
    @Throws(InvalidDreamException::class, CancellationException::class)
    suspend operator fun invoke(dream: Dream): Resource<Unit> {
        if (dream.content.isBlank() && dream.audioUrl.isBlank()) {
            return Resource.Error("Please enter a dream")
        }
        return repository.insertDream(dream)
    }
}
