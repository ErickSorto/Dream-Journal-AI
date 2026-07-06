package org.ballistic.dreamjournalai.shared.dream_store.domain

import co.touchlab.kermit.Logger
import org.ballistic.dreamjournalai.shared.core.analytics.AnalyticsParam
import org.ballistic.dreamjournalai.shared.core.analytics.AppAnalytics
import org.ballistic.dreamjournalai.shared.core.analytics.analyticsValue
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPlanOption

sealed interface StoreAnalyticsEvent {
    data class StoreViewed(
        val initialPage: String,
        val isAnonymous: Boolean,
        val tokenBalance: Int,
        val dreamCount: Int,
    ) : StoreAnalyticsEvent

    data class PageViewed(
        val page: String,
    ) : StoreAnalyticsEvent

    data class PageChanged(
        val fromPage: String,
        val toPage: String,
        val action: String,
    ) : StoreAnalyticsEvent

    data class StoreClosed(
        val page: String,
    ) : StoreAnalyticsEvent

    data class PremiumOfferLoaded(
        val offeringId: String,
        val packageCount: Int,
        val hasPremium: Boolean,
        val defaultPlan: PremiumPlanOption,
    ) : StoreAnalyticsEvent

    data class PremiumOfferUnavailable(
        val reason: String,
    ) : StoreAnalyticsEvent

    data class PremiumPlanSelected(
        val plan: PremiumPlanOption,
        val packageId: String?,
    ) : StoreAnalyticsEvent

    data class PremiumPurchaseStarted(
        val plan: PremiumPlanOption,
        val packageId: String,
    ) : StoreAnalyticsEvent

    data class PremiumPurchaseCompleted(
        val result: String,
        val plan: PremiumPlanOption,
        val packageId: String?,
        val message: String? = null,
        val properties: Map<String, Any?> = emptyMap(),
    ) : StoreAnalyticsEvent

    data class TokenProductTapped(
        val productId: String,
        val tokenCount: Int,
    ) : StoreAnalyticsEvent

    data class TokenProductsFetched(
        val result: String,
        val productCount: Int,
        val requestedCount: Int,
        val message: String? = null,
    ) : StoreAnalyticsEvent

    data class TokenPurchaseStarted(
        val productId: String,
        val tokenCount: Int,
    ) : StoreAnalyticsEvent

    data class TokenPurchaseCompleted(
        val result: String,
        val productId: String,
        val tokenCount: Int,
        val message: String? = null,
    ) : StoreAnalyticsEvent

    data class TokenVerificationCompleted(
        val result: String,
        val productId: String,
        val tokenCount: Int,
    ) : StoreAnalyticsEvent
}

interface StoreAnalytics {
    fun track(event: StoreAnalyticsEvent)
}

class LoggingStoreAnalytics : StoreAnalytics {
    private val logger = Logger.withTag("StoreAnalytics")

    override fun track(event: StoreAnalyticsEvent) {
        logger.d { event.toString() }
    }
}

class FirebaseStoreAnalytics(
    private val appAnalytics: AppAnalytics,
) : StoreAnalytics {
    override fun track(event: StoreAnalyticsEvent) {
        when (event) {
            is StoreAnalyticsEvent.StoreViewed -> {
                appAnalytics.track(
                    eventName = "store_view",
                    params = mapOf(
                        AnalyticsParam.InitialPage to event.initialPage,
                        AnalyticsParam.IsAnonymous to event.isAnonymous,
                        AnalyticsParam.TokenBalance to event.tokenBalance,
                        "dream_count" to event.dreamCount
                    )
                )
            }

            is StoreAnalyticsEvent.PageViewed -> {
                appAnalytics.track(
                    eventName = "store_page_view",
                    params = event.page.basePageParams()
                )
            }

            is StoreAnalyticsEvent.PageChanged -> {
                appAnalytics.track(
                    eventName = "store_page_change",
                    params = mapOf(
                        "from_page" to event.fromPage,
                        "to_page" to event.toPage,
                        AnalyticsParam.Action to event.action
                    )
                )
            }

            is StoreAnalyticsEvent.StoreClosed -> {
                appAnalytics.track(
                    eventName = "store_close",
                    params = event.page.basePageParams()
                )
            }

            is StoreAnalyticsEvent.PremiumOfferLoaded -> {
                appAnalytics.track(
                    eventName = "store_premium_offer_loaded",
                    params = mapOf(
                        AnalyticsParam.OfferingId to event.offeringId,
                        "package_count" to event.packageCount,
                        AnalyticsParam.HasPremium to event.hasPremium,
                        AnalyticsParam.Plan to event.defaultPlan.analyticsValue()
                    )
                )
            }

            is StoreAnalyticsEvent.PremiumOfferUnavailable -> {
                appAnalytics.track(
                    eventName = "store_premium_offer_missing",
                    params = mapOf(
                        AnalyticsParam.Result to event.reason
                    )
                )
            }

            is StoreAnalyticsEvent.PremiumPlanSelected -> {
                appAnalytics.track(
                    eventName = "store_premium_plan_select",
                    params = premiumParams(event.plan, event.packageId)
                )
            }

            is StoreAnalyticsEvent.PremiumPurchaseStarted -> {
                appAnalytics.track(
                    eventName = "store_premium_purchase_start",
                    params = premiumParams(event.plan, event.packageId)
                )
            }

            is StoreAnalyticsEvent.PremiumPurchaseCompleted -> {
                appAnalytics.track(
                    eventName = "store_premium_purchase_result",
                    params = premiumParams(event.plan, event.packageId) + mapOf(
                        AnalyticsParam.Result to event.result,
                        AnalyticsParam.Error to event.message
                    ) + event.properties
                )
            }

            is StoreAnalyticsEvent.TokenProductTapped -> {
                appAnalytics.track(
                    eventName = "token_product_tap",
                    params = tokenParams(event.productId, event.tokenCount)
                )
            }

            is StoreAnalyticsEvent.TokenProductsFetched -> {
                appAnalytics.track(
                    eventName = "token_products_fetch_result",
                    params = mapOf(
                        AnalyticsParam.Result to event.result,
                        "product_count" to event.productCount,
                        "requested_count" to event.requestedCount,
                        AnalyticsParam.Error to event.message
                    )
                )
            }

            is StoreAnalyticsEvent.TokenPurchaseStarted -> {
                appAnalytics.track(
                    eventName = "token_purchase_start",
                    params = tokenParams(event.productId, event.tokenCount)
                )
            }

            is StoreAnalyticsEvent.TokenPurchaseCompleted -> {
                appAnalytics.track(
                    eventName = "token_purchase_result",
                    params = tokenParams(event.productId, event.tokenCount) + mapOf(
                        AnalyticsParam.Result to event.result,
                        AnalyticsParam.Error to event.message
                    )
                )
            }

            is StoreAnalyticsEvent.TokenVerificationCompleted -> {
                appAnalytics.track(
                    eventName = "token_verification_result",
                    params = tokenParams(event.productId, event.tokenCount) + mapOf(
                        AnalyticsParam.Result to event.result
                    )
                )
            }
        }
    }

    private fun String.basePageParams(): Map<String, Any> {
        return mapOf(
            AnalyticsParam.Area to "store",
            AnalyticsParam.Page to this
        )
    }

    private fun premiumParams(
        plan: PremiumPlanOption,
        packageId: String?,
    ): Map<String, Any?> {
        return mapOf(
            AnalyticsParam.Plan to plan.analyticsValue(),
            AnalyticsParam.PackageId to packageId
        )
    }

    private fun tokenParams(
        productId: String,
        tokenCount: Int,
    ): Map<String, Any> {
        return mapOf(
            AnalyticsParam.ProductId to productId,
            AnalyticsParam.TokenCount to tokenCount
        )
    }
}
