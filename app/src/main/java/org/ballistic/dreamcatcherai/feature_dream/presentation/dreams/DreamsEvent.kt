package org.ballistic.dreamcatcherai.feature_dream.presentation.dreams

import org.ballistic.dreamcatcherai.feature_dream.domain.model.Dream
import org.ballistic.dreamcatcherai.feature_dream.domain.util.DreamOrder

sealed class DreamsEvent {
    data class Order(val dreamType: DreamOrder): DreamsEvent()
    data class DeleteDream(val dream: Dream): DreamsEvent()
    object ToggleOrderSection: DreamsEvent()
    object RestoreDream: DreamsEvent()
}
