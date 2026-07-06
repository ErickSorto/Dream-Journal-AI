package org.ballistic.dreamjournalai.shared.dream_premium.domain.repository

import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPurchaseResult

interface PremiumPaywallRepository {
    suspend fun syncAppUser(userId: String?)
    suspend fun getCurrentCustomerInfo(): CustomerInfo
    suspend fun getPlacementOffering(placementId: String): Offering?
    suspend fun purchasePackage(packageToPurchase: Package): PremiumPurchaseResult
    suspend fun restorePurchases(): CustomerInfo
    fun hasPremiumEntitlement(customerInfo: CustomerInfo): Boolean
    fun managementUrl(customerInfo: CustomerInfo): String?
}
