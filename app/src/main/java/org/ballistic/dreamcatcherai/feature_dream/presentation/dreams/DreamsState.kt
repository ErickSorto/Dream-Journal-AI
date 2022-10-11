package org.ballistic.dreamcatcherai.feature_dream.presentation.dreams

import org.ballistic.dreamcatcherai.feature_dream.domain.model.Dream
import org.ballistic.dreamcatcherai.feature_dream.domain.util.DreamOrder
import org.ballistic.dreamcatcherai.feature_dream.domain.util.OrderType

object DreamsState {
    val dreams = mutableListOf<Dream>()
    val dreamOrder: DreamOrder = DreamOrder.Date(OrderType.Descending)
    val isOrderSectionVisible: Boolean = false
}