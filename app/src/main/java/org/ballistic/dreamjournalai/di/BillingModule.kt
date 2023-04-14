package org.ballistic.dreamjournalai.di

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ballistic.dreamjournalai.store_billing.data.repository.BillingRepositoryImpl
import org.ballistic.dreamjournalai.store_billing.domain.repository.BillingRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Singleton
    @Provides
    fun provideBillingClient(
        @ApplicationContext context: Context,
        billingRepository: dagger.Lazy<BillingRepository>
    ): BillingClient {
        return BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        (billingRepository.get() as BillingRepositoryImpl).getPurchaseListener()?.invoke(purchase)
                    }
                }
            }
            .enablePendingPurchases()
            .build()
    }

    @Provides
    @Singleton
    fun provideBillingRepository(
        billingClient: BillingClient
    ): BillingRepository {
        return BillingRepositoryImpl(billingClient)
    }
}