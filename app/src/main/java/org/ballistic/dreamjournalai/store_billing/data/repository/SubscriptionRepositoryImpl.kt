package org.ballistic.dreamjournalai.store_billing.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.store_billing.domain.model.Subscription
import org.ballistic.dreamjournalai.store_billing.domain.repository.SubscriptionRepository

class SubscriptionRepositoryImpl(private val fireStore: FirebaseFirestore) :
    SubscriptionRepository {
    override suspend fun getSubscription(userId: String): Resource<Subscription> {
        return try {
            val document = fireStore.collection("subscriptions").document(userId).get().await()
            if (document.exists()) {
                val subscription = document.toObject(Subscription::class.java)
                Resource.Success(subscription)
            } else {
                Resource.Error("Subscription not found", null)
            }
        } catch (e: Exception) {
            Resource.Error("Failed to retrieve subscription", null)
        }
    }

    override suspend fun purchaseMonthlySubscription(
        userId: String,
        price: Double,
        tier: Int,
    ): Resource<Boolean> {
        return try {
            val subscription = Subscription(tier, 60, true, true)
            fireStore.collection("subscriptions").document(userId).set(subscription).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to purchase monthly subscription", false)
        }
    }

    override suspend fun purchaseYearlySubscription(
        userId: String,
        price: Double,
        tier: Int,
    ): Resource<Boolean> {
        return try {
            val subscription = Subscription(tier, 720, true, true )
            fireStore.collection("subscriptions").document(userId).set(subscription).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to purchase yearly subscription", false)
        }
    }

    override suspend fun upgradeSubscription(userId: String, newTier: Int): Resource<Boolean> {
        return try {
            val document = fireStore.collection("subscriptions").document(userId).get().await()
            if (document.exists()) {
                val subscription = document.toObject(Subscription::class.java)
                val updatedSubscription = subscription?.copy(tier = newTier)
                if (updatedSubscription != null) {
                    fireStore.collection("subscriptions").document(userId).set(updatedSubscription)
                        .await()
                }
                Resource.Success(true)
            } else {
                Resource.Error("Subscription not found", false)
            }
        } catch (e: Exception) {
            Resource.Error("Failed to upgrade subscription", false)
        }
    }

    override suspend fun addDreamTokens(userId: String, amount: Int): Resource<Boolean> {
        return try {
            val document = fireStore.collection("subscriptions").document(userId).get().await()
            if (document.exists()) {
                val subscription = document.toObject(Subscription::class.java)
                val updatedSubscription = subscription?.copy(dreamTokens = subscription.dreamTokens + amount)
                if (updatedSubscription != null) {
                    fireStore.collection("subscriptions").document(userId).set(updatedSubscription)
                        .await()
                }
                Resource.Success(true)
            } else {
                Resource.Error("Subscription not found", false)
            }
        } catch (e: Exception) {
            Resource.Error("Failed to add dream tokens", false)
        }
    }

    override suspend fun deductDreamTokens(userId: String, amount: Int): Resource<Boolean> {
        return try {
            val document = fireStore.collection("subscriptions").document(userId).get().await()
            if (document.exists()) {
                val subscription = document.toObject(Subscription::class.java)
                val updatedSubscription = subscription?.copy(dreamTokens = subscription.dreamTokens - amount)
                if (updatedSubscription != null) {
                    fireStore.collection("subscriptions").document(userId).set(updatedSubscription)
                        .await()
                }
                Resource.Success(true)
            } else {
                Resource.Error("Subscription not found", false)
            }
        } catch (e: Exception) {
            Resource.Error("Failed to deduct dream tokens", false)
        }
    }
}