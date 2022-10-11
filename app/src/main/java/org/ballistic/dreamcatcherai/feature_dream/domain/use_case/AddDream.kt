package org.ballistic.dreamcatcherai.feature_dream.domain.use_case

import org.ballistic.dreamcatcherai.feature_dream.domain.model.Dream
import org.ballistic.dreamcatcherai.feature_dream.domain.model.InvalidDreamException
import org.ballistic.dreamcatcherai.feature_dream.domain.repository.DreamRepository

class AddDream(private val repository: DreamRepository) {
    @Throws(InvalidDreamException::class)
    suspend operator fun invoke(dream: Dream) {
        if(dream.title.isBlank()) {
            throw InvalidDreamException("The title of the dream can't be empty")
        }
        if (dream.content.isBlank()) {
            throw InvalidDreamException("The content of the dream can't be empty")
        }
        repository.insertDream(dream)
    }
}