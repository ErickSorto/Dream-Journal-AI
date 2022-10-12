package org.ballistic.dreamjournalai.feature_dream.domain.util

sealed class DreamOrder(val orderType: OrderType) {
    class Date(orderType: OrderType): DreamOrder(orderType)
}
