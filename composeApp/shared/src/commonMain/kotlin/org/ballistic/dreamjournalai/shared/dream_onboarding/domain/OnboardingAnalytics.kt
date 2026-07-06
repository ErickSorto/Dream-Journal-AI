package org.ballistic.dreamjournalai.shared.dream_onboarding.domain

import co.touchlab.kermit.Logger
import org.ballistic.dreamjournalai.shared.core.analytics.AnalyticsParam
import org.ballistic.dreamjournalai.shared.core.analytics.AppAnalytics
import org.ballistic.dreamjournalai.shared.core.analytics.analyticsValue
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.OnboardingFlowStep

sealed interface OnboardingAnalyticsEvent {
    data class ScreenViewed(
        val step: OnboardingFlowStep,
    ) : OnboardingAnalyticsEvent

    data class ScreenDwellRecorded(
        val step: OnboardingFlowStep,
        val durationMs: Long,
    ) : OnboardingAnalyticsEvent

    data class SelectionMade(
        val step: OnboardingFlowStep,
        val field: String,
        val value: String,
    ) : OnboardingAnalyticsEvent

    data class CtaTapped(
        val step: OnboardingFlowStep,
        val ctaLabel: String,
    ) : OnboardingAnalyticsEvent

    data class ReviewPromptShown(
        val triggerStep: OnboardingFlowStep,
    ) : OnboardingAnalyticsEvent

    data class PaywallShown(
        val headlineVariant: String,
    ) : OnboardingAnalyticsEvent

    data class PaywallPrimaryTapped(
        val ctaLabel: String,
    ) : OnboardingAnalyticsEvent

    data class RescueShown(
        val entryPoint: String,
    ) : OnboardingAnalyticsEvent

    data class BasicModeStarted(
        val entryPoint: String,
    ) : OnboardingAnalyticsEvent

    data class OnboardingCompleted(
        val completionMode: String,
        val properties: Map<String, Any?> = emptyMap(),
    ) : OnboardingAnalyticsEvent
}

interface OnboardingAnalytics {
    fun track(event: OnboardingAnalyticsEvent)
}

class LoggingOnboardingAnalytics : OnboardingAnalytics {
    private val logger = Logger.withTag("OnboardingAnalytics")

    override fun track(event: OnboardingAnalyticsEvent) {
        logger.d { event.toString() }
    }
}

class FirebaseOnboardingAnalytics(
    private val appAnalytics: AppAnalytics,
) : OnboardingAnalytics {
    override fun track(event: OnboardingAnalyticsEvent) {
        when (event) {
            is OnboardingAnalyticsEvent.ScreenViewed -> {
                appAnalytics.track(
                    eventName = "onboarding_step_view",
                    params = event.step.baseParams()
                )
            }

            is OnboardingAnalyticsEvent.ScreenDwellRecorded -> {
                appAnalytics.track(
                    eventName = "onboarding_step_dwell",
                    params = event.step.baseParams() + mapOf(
                        AnalyticsParam.DurationMs to event.durationMs.coerceAtLeast(0)
                    )
                )
            }

            is OnboardingAnalyticsEvent.SelectionMade -> {
                appAnalytics.track(
                    eventName = "onboarding_select",
                    params = event.step.baseParams() + mapOf(
                        AnalyticsParam.Field to event.field,
                        AnalyticsParam.Value to event.value
                    )
                )
            }

            is OnboardingAnalyticsEvent.CtaTapped -> {
                appAnalytics.track(
                    eventName = "onboarding_cta_tap",
                    params = event.step.baseParams() + mapOf(
                        AnalyticsParam.Label to event.ctaLabel
                    )
                )
            }

            is OnboardingAnalyticsEvent.ReviewPromptShown -> {
                appAnalytics.track(
                    eventName = "review_prompt_show",
                    params = event.triggerStep.baseParams()
                )
            }

            is OnboardingAnalyticsEvent.PaywallShown -> {
                appAnalytics.track(
                    eventName = "onboarding_paywall_show",
                    params = mapOf(
                        AnalyticsParam.Variant to event.headlineVariant
                    )
                )
            }

            is OnboardingAnalyticsEvent.PaywallPrimaryTapped -> {
                appAnalytics.track(
                    eventName = "onboarding_paywall_cta_tap",
                    params = mapOf(
                        AnalyticsParam.Label to event.ctaLabel
                    )
                )
            }

            is OnboardingAnalyticsEvent.RescueShown -> {
                appAnalytics.track(
                    eventName = "onboarding_rescue_show",
                    params = mapOf(
                        AnalyticsParam.EntryPoint to event.entryPoint
                    )
                )
            }

            is OnboardingAnalyticsEvent.BasicModeStarted -> {
                appAnalytics.track(
                    eventName = "basic_mode_start",
                    params = mapOf(
                        AnalyticsParam.EntryPoint to event.entryPoint
                    )
                )
            }

            is OnboardingAnalyticsEvent.OnboardingCompleted -> {
                appAnalytics.track(
                    eventName = "onboarding_complete",
                    params = mapOf(
                        "completion_mode" to event.completionMode
                    ) + event.properties
                )
            }
        }
    }

    private fun OnboardingFlowStep.baseParams(): Map<String, Any> {
        return mapOf(
            AnalyticsParam.Step to analyticsValue(),
            AnalyticsParam.StepIndex to progressStep,
            "total_steps" to totalSteps,
            "is_premium_step" to (this in PremiumSteps)
        )
    }

    private companion object {
        val PremiumSteps = setOf(
            OnboardingFlowStep.PremiumTrialClarity,
            OnboardingFlowStep.PremiumPromise,
            OnboardingFlowStep.PremiumUnlocks,
            OnboardingFlowStep.PremiumOfferSheet,
            OnboardingFlowStep.PremiumGiftReveal,
            OnboardingFlowStep.PremiumOneTimeOffer,
        )
    }
}
