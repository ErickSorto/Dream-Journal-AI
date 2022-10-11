package org.ballistic.dreamcatcherai.feature_dream.domain.util

sealed class OrderType{
    object Ascending: OrderType()
    object Descending: OrderType()
}
