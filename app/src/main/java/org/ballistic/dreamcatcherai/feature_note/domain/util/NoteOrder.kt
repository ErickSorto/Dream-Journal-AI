package org.ballistic.dreamcatcherai.feature_note.domain.util

sealed class NoteOrder(val orderType: OrderType) {
    class Date(orderType: OrderType): NoteOrder(orderType)
}
