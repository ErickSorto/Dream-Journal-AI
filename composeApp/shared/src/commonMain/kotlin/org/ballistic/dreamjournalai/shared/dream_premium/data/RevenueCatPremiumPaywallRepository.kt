package org.ballistic.dreamjournalai.shared.dream_premium.data

import co.touchlab.kermit.Logger
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumEntitlementId
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPurchaseResult
import org.ballistic.dreamjournalai.shared.dream_premium.domain.repository.PremiumPaywallRepository

class RevenueCatPremiumPaywallRepository : PremiumPaywallRepository {
    private val logger = Logger.withTag("PremiumPaywallRepo")

    override suspend fun syncAppUser(userId: String?) {
        runCatching {
            val purchases = Purchases.sharedInstance
            if (userId.isNullOrBlank()) {
                purchases.awaitLogOut()
            } else if (purchases.appUserID != userId) {
                purchases.awaitLogIn(userId)
            }
        }.onFailure { error ->
            logger.e(error) { "Failed to sync RevenueCat app user." }
        }
    }

    override suspend fun getCurrentCustomerInfo(): CustomerInfo {
        return Purchases.sharedInstance.awaitCustomerInfo()
    }

    override suspend fun getPlacementOffering(placementId: String): Offering? {
        return runCatching {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            offerings.getCurrentOfferingForPlacement(placementId)
                ?: offerings.current
                ?: offerings.all.values.firstOrNull()
        }.onFailure { error ->
            logger.e(error) { "Failed to fetch RevenueCat offering for placement: $placementId" }
        }.getOrNull()
    }

    override suspend fun purchasePackage(packageToPurchase: Package): PremiumPurchaseResult {
        return runCatching {
            val purchase = Purchases.sharedInstance.awaitPurchase(packageToPurchase)
            when {
                hasPremiumEntitlement(purchase.customerInfo) ->
                    PremiumPurchaseResult.Success(packageToPurchase.identifier)
                else -> PremiumPurchaseResult.Pending
            }
        }.getOrElse { throwable ->
            val message = throwable.message.orEmpty().lowercase()
            when {
                "cancel" in message -> PremiumPurchaseResult.UserCancelled
                "already" in message && "purchase" in message -> PremiumPurchaseResult.AlreadySubscribed
                "deferred" in message || "pending" in message -> PremiumPurchaseResult.Pending
                else -> PremiumPurchaseResult.Error(throwable.message ?: "Unable to complete purchase.")
            }
        }
    }

    override suspend fun restorePurchases(): CustomerInfo {
        return Purchases.sharedInstance.awaitRestore()
    }

    override fun hasPremiumEntitlement(customerInfo: CustomerInfo): Boolean {
        return customerInfo.entitlements.active.containsKey(PremiumEntitlementId)
    }

    override fun managementUrl(customerInfo: CustomerInfo): String? {
        return customerInfo.managementUrlString
    }
}
