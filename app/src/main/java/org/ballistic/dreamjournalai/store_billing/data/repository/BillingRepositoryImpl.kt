package org.ballistic.dreamjournalai.store_billing.data.repository

import android.app.Activity
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.ballistic.dreamjournalai.store_billing.data.api.PurchaseVerificationApi
import org.ballistic.dreamjournalai.store_billing.domain.repository.BillingRepository
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BillingRepositoryImpl(
    val billingClient: BillingClient
) : BillingRepository {
    private var purchaseListener: ((Purchase) -> Unit)? = null


    private val retrofit = Retrofit.Builder()
        .baseUrl("https://us-central1-dream-journal-ai.cloudfunctions.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val purchaseVerificationApi: PurchaseVerificationApi =
        retrofit.create(PurchaseVerificationApi::class.java)


    init {
        connect()
    }

    override fun getPurchaseListener(): ((Purchase) -> Unit)? {
        return purchaseListener
    }


    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing client is ready to use.
                } else {
                    // Handle the error case.
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry to connect to the billing service.
                connect()
            }
        })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun queryProductDetails(params: QueryProductDetailsParams): List<ProductDetails> =
        suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(productDetailsList, onCancellation = { })
                } else {
                    continuation.resumeWith(Result.failure(Exception(billingResult.debugMessage)))
                }
            }
        }

    override suspend fun initiatePurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        withContext(Dispatchers.Main) {
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun handlePurchase(purchase: Purchase): Boolean =
        suspendCancellableCoroutine { continuation ->

            suspend fun verifyPurchaseOnServer(purchase: Purchase): Boolean {
                val dreamTokens = when (purchase.skus.firstOrNull()) {
                    "dream_token_100" -> 100
                    "dream_tokens_500" -> 500
                    else -> 0
                }

                if (dreamTokens == 0) return false

                val uniqueOrderId = "USER_ID_${System.currentTimeMillis()}" // Create a unique order ID with timestamp

                val response = purchaseVerificationApi.verifyPurchase(
                    purchaseToken = purchase.purchaseToken,
                    purchaseTime = purchase.purchaseTime,
                    orderId = uniqueOrderId,
                    userId = "USER_ID",
                    dreamTokens = dreamTokens
                )

                return if (response.isSuccessful) {
                    response.body()?.isValid ?: false
                } else {
                    false
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                val isPurchaseValid = verifyPurchaseOnServer(purchase)
                if (isPurchaseValid) {
                    val isConsumed = consumePurchase(purchase)
                    continuation.resume(isConsumed, onCancellation = { })
                } else {
                    continuation.resume(false, onCancellation = { })
                }
            }
        }


    override suspend fun consumePurchase(purchase: Purchase): Boolean =
        suspendCancellableCoroutine { continuation ->
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.consumeAsync(consumeParams) { billingResult, purchaseToken ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(true, onCancellation = { })
                } else {
                    continuation.resume(false, onCancellation = { })
                }
            }
        }
}

