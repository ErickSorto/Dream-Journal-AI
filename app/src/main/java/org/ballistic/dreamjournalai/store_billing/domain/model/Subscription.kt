package org.ballistic.dreamjournalai.store_billing.domain.model

data class Subscription(
    val tier: Int,
    val dreamTokens: Int,
    val isMonthly: Boolean,
    val isActive: Boolean
)