package org.ballistic.dreamjournalai.store_billing.presentation.store_screen

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenEvent
import org.ballistic.dreamjournalai.store_billing.domain.repository.BillingRepository
import javax.inject.Inject

@HiltViewModel
class StoreScreenViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {



    private val productIds = listOf("dream_token_100", "dream_tokens_500")

    fun onEvent (event: StoreEvent) = viewModelScope.launch {
        when (event) {
            is StoreEvent.Buy100DreamTokens -> {
                purchaseDreamTokens(event.activity, "dream_token_100")
            }
            is StoreEvent.Buy500DreamTokens -> {
                purchaseDreamTokens(event.activity, "dream_tokens_500")
            }
        }
    }

    private suspend fun queryProductDetails(): List<ProductDetails> {
        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.SkuType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        return withContext(Dispatchers.IO) {
            billingRepository.queryProductDetails(params)
        }
    }

    private suspend fun purchaseDreamTokens(activity: Activity, productId: String) {
        val productDetailsList = queryProductDetails()
        val productDetails = productDetailsList.find { it.productId == productId }

        productDetails?.let {
            billingRepository.initiatePurchaseFlow(activity, it)
        }
    }
}