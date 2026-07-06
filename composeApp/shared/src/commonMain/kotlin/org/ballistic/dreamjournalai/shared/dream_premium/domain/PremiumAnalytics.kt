package org.ballistic.dreamjournalai.shared.dream_premium.domain

import co.touchlab.kermit.Logger
import org.ballistic.dreamjournalai.shared.core.analytics.AnalyticsParam
import org.ballistic.dreamjournalai.shared.core.analytics.AppAnalytics
import org.ballistic.dreamjournalai.shared.core.analytics.analyticsValue

sealed interface PremiumAnalyticsEvent {
    data class PlacementShown(
        val placement: PremiumPlacement,
        val offeringId: String,
        val entrySource: PremiumEntrySource? = null,
    ) : PremiumAnalyticsEvent
    data class PageViewed(val page: PremiumPageKind) : PremiumAnalyticsEvent
    data class DwellRecorded(val page: PremiumPageKind, val durationMs: Long) : PremiumAnalyticsEvent
    data class TrialToggleChanged(val enabled: Boolean) : PremiumAnalyticsEvent
    data class PackageSelected(val page: PremiumPageKind, val packageId: String) : PremiumAnalyticsEvent
    data class CtaTapped(val page: PremiumPageKind, val label: String) : PremiumAnalyticsEvent
    data class PurchaseStarted(val page: PremiumPageKind, val packageId: String) : PremiumAnalyticsEvent
    data class PurchaseCancelled(val page: PremiumPageKind, val packageId: String?) : PremiumAnalyticsEvent
    data class PurchasePending(val page: PremiumPageKind, val packageId: String) : PremiumAnalyticsEvent
    data class PurchaseSucceeded(
        val page: PremiumPageKind,
        val packageId: String,
        val properties: Map<String, Any?> = emptyMap(),
    ) : PremiumAnalyticsEvent
    data class PurchaseAlreadyActive(val page: PremiumPageKind, val packageId: String?) : PremiumAnalyticsEvent
    data class PurchaseFailed(val page: PremiumPageKind, val packageId: String?, val message: String) : PremiumAnalyticsEvent
    data class PaywallDismissed(val page: PremiumPageKind) : PremiumAnalyticsEvent
    data class RescueShown(val source: PremiumRescueSource, val variant: PremiumRescueVariant) : PremiumAnalyticsEvent
    data class RescueAccepted(val source: PremiumRescueSource, val variant: PremiumRescueVariant) : PremiumAnalyticsEvent
    data class OfferingMissing(val placement: PremiumPlacement) : PremiumAnalyticsEvent
    data object RestoreTapped : PremiumAnalyticsEvent
    data class RestoreCompleted(val premiumActive: Boolean) : PremiumAnalyticsEvent
    data class RestoreFailed(val message: String) : PremiumAnalyticsEvent
    data object CustomerCenterOpened : PremiumAnalyticsEvent
}

interface PremiumAnalytics {
    fun track(event: PremiumAnalyticsEvent)
}

class LoggingPremiumAnalytics : PremiumAnalytics {
    private val logger = Logger.withTag("PremiumAnalytics")

    override fun track(event: PremiumAnalyticsEvent) {
        logger.d { event.toString() }
    }
}

class FirebasePremiumAnalytics(
    private val appAnalytics: AppAnalytics,
) : PremiumAnalytics {
    override fun track(event: PremiumAnalyticsEvent) {
        when (event) {
            is PremiumAnalyticsEvent.PlacementShown -> {
                appAnalytics.track(
                    eventName = "premium_placement_show",
                    params = mapOf(
                        AnalyticsParam.Placement to event.placement.placementId,
                        AnalyticsParam.OfferingId to event.offeringId,
                        AnalyticsParam.Source to event.entrySource?.analyticsValue()
                    )
                )
            }

            is PremiumAnalyticsEvent.PageViewed -> {
                appAnalytics.track(
                    eventName = "premium_page_view",
                    params = event.page.baseParams()
                )
            }

            is PremiumAnalyticsEvent.DwellRecorded -> {
                appAnalytics.track(
                    eventName = "premium_page_dwell",
                    params = event.page.baseParams() + mapOf(
                        AnalyticsParam.DurationMs to event.durationMs.coerceAtLeast(0)
                    )
                )
            }

            is PremiumAnalyticsEvent.TrialToggleChanged -> {
                appAnalytics.track(
                    eventName = "premium_trial_toggle",
                    params = mapOf(
                        "enabled" to event.enabled
                    )
                )
            }

            is PremiumAnalyticsEvent.PackageSelected -> {
                appAnalytics.track(
                    eventName = "premium_package_select",
                    params = event.page.baseParams() + packageParams(event.packageId)
                )
            }

            is PremiumAnalyticsEvent.CtaTapped -> {
                appAnalytics.track(
                    eventName = "premium_cta_tap",
                    params = event.page.baseParams() + mapOf(
                        AnalyticsParam.Label to event.label
                    )
                )
            }

            is PremiumAnalyticsEvent.PurchaseStarted -> {
                appAnalytics.track(
                    eventName = "premium_purchase_start",
                    params = event.page.baseParams() + packageParams(event.packageId)
                )
            }

            is PremiumAnalyticsEvent.PurchaseCancelled -> {
                appAnalytics.track(
                    eventName = "premium_purchase_cancel",
                    params = event.page.baseParams() + nullablePackageParams(event.packageId)
                )
            }

            is PremiumAnalyticsEvent.PurchasePending -> {
                appAnalytics.track(
                    eventName = "premium_purchase_pending",
                    params = event.page.baseParams() + packageParams(event.packageId)
                )
            }

            is PremiumAnalyticsEvent.PurchaseSucceeded -> {
                appAnalytics.track(
                    eventName = "premium_purchase_success",
                    params = event.page.baseParams() + packageParams(event.packageId) + event.properties
                )
            }

            is PremiumAnalyticsEvent.PurchaseAlreadyActive -> {
                appAnalytics.track(
                    eventName = "premium_already_active",
                    params = event.page.baseParams() + nullablePackageParams(event.packageId)
                )
            }

            is PremiumAnalyticsEvent.PurchaseFailed -> {
                appAnalytics.track(
                    eventName = "premium_purchase_error",
                    params = event.page.baseParams() + nullablePackageParams(event.packageId) + mapOf(
                        AnalyticsParam.Error to event.message
                    )
                )
            }

            is PremiumAnalyticsEvent.PaywallDismissed -> {
                appAnalytics.track(
                    eventName = "premium_paywall_dismiss",
                    params = event.page.baseParams()
                )
            }

            is PremiumAnalyticsEvent.RescueShown -> {
                appAnalytics.track(
                    eventName = "premium_rescue_show",
                    params = rescueParams(event.source, event.variant)
                )
            }

            is PremiumAnalyticsEvent.RescueAccepted -> {
                appAnalytics.track(
                    eventName = "premium_rescue_accept",
                    params = rescueParams(event.source, event.variant)
                )
            }

            is PremiumAnalyticsEvent.OfferingMissing -> {
                appAnalytics.track(
                    eventName = "premium_offering_missing",
                    params = mapOf(
                        AnalyticsParam.Placement to event.placement.placementId
                    )
                )
            }

            PremiumAnalyticsEvent.RestoreTapped -> {
                appAnalytics.track(eventName = "premium_restore_tap")
            }

            is PremiumAnalyticsEvent.RestoreCompleted -> {
                appAnalytics.track(
                    eventName = "premium_restore_complete",
                    params = mapOf(
                        AnalyticsParam.HasPremium to event.premiumActive,
                        AnalyticsParam.Result to if (event.premiumActive) "premium_active" else "none_found"
                    )
                )
            }

            is PremiumAnalyticsEvent.RestoreFailed -> {
                appAnalytics.track(
                    eventName = "premium_restore_error",
                    params = mapOf(
                        AnalyticsParam.Error to event.message
                    )
                )
            }

            PremiumAnalyticsEvent.CustomerCenterOpened -> {
                appAnalytics.track(eventName = "premium_customer_center_open")
            }
        }
    }

    private fun PremiumPageKind.baseParams(): Map<String, Any> {
        return mapOf(
            AnalyticsParam.Area to "premium",
            AnalyticsParam.Page to analyticsValue()
        )
    }

    private fun packageParams(packageId: String): Map<String, Any> {
        return mapOf(
            AnalyticsParam.PackageId to packageId,
            AnalyticsParam.Plan to packageId.toPlanValue()
        )
    }

    private fun nullablePackageParams(packageId: String?): Map<String, Any> {
        return if (packageId == null) {
            emptyMap()
        } else {
            packageParams(packageId)
        }
    }

    private fun rescueParams(
        source: PremiumRescueSource,
        variant: PremiumRescueVariant,
    ): Map<String, Any> {
        return mapOf(
            AnalyticsParam.Source to source.analyticsValue,
            AnalyticsParam.Variant to variant.analyticsValue
        )
    }

    private fun String.toPlanValue(): String {
        val lower = lowercase()
        return when {
            "annual" in lower || "year" in lower -> "annual"
            "month" in lower -> "monthly"
            "week" in lower -> "weekly"
            else -> "unknown"
        }
    }
}
