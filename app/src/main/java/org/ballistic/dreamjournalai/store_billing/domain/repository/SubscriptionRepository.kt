package org.ballistic.dreamjournalai.store_billing.domain.repository

import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.store_billing.domain.model.Subscription

interface SubscriptionRepository {
    suspend fun getSubscription(userId: String): Resource<Subscription>
    suspend fun purchaseMonthlySubscription(userId: String, price: Double, tier: Int): Resource<Boolean>
    suspend fun purchaseYearlySubscription(userId: String, price: Double, tier: Int): Resource<Boolean>
    suspend fun upgradeSubscription(userId: String, newTier: Int): Resource<Boolean>
    suspend fun addDreamTokens(userId: String, amount: Int): Resource<Boolean>
    suspend fun deductDreamTokens(userId: String, amount: Int): Resource<Boolean>
}