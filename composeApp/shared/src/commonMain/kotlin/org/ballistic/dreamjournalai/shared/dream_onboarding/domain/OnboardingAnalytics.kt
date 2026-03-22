package org.ballistic.dreamjournalai.shared.dream_onboarding.domain

import co.touchlab.kermit.Logger
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
