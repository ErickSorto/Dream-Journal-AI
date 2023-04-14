package org.ballistic.dreamjournalai.store_billing.data.api

import org.ballistic.dreamjournalai.store_billing.domain.entities.PurchaseVerificationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PurchaseVerificationApi {
    @GET("verifyPurchases")
    suspend fun verifyPurchase(
        @Query("purchaseToken") purchaseToken: String,
        @Query("purchaseTime") purchaseTime: Long,
        @Query("orderId") orderId: String,
        @Query("userId") userId: String,
        @Query("dreamTokens") dreamTokens: Int
    ): Response<PurchaseVerificationResponse>
}