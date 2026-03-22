package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnboardingFlowModelsTest {

    @Test
    fun `derive plan uses recall range to choose recall-first track`() {
        val plan = deriveOnboardingPlan(
            OnboardingAnswers(
                nearGoals = listOf(NearGoal.RememberDreams),
                farGoal = FarGoal.SelfUnderstanding,
                recallDaysPerWeek = 1,
                mainBlocker = RecallBlocker.ForgetWithinMinutes,
                startedAtTimeOfDay = OnboardingTimeOfDay.MorningDay,
            )
        )

        assertEquals(PlanTrack.RecallFirst, plan.planTrack)
    }

    @Test
    fun `derive plan uses near goal to build seven night roadmap`() {
        val plan = deriveOnboardingPlan(
            OnboardingAnswers(
                nearGoals = listOf(NearGoal.ExploreLucidDreaming),
                farGoal = FarGoal.LucidMastery,
                recallDaysPerWeek = 4,
                mainBlocker = RecallBlocker.Inconsistent,
                startedAtTimeOfDay = OnboardingTimeOfDay.MorningDay,
            )
        )

        assertEquals("Strengthen recall first", plan.roadmapMilestones.first().title)
        assertEquals("Recognize repeating dream signs", plan.roadmapMilestones.last().title)
    }

    @Test
    fun `derive plan uses far goal for adaptive snapshot card`() {
        val plan = deriveOnboardingPlan(
            OnboardingAnswers(
                nearGoals = listOf(NearGoal.SparkCreativity),
                farGoal = FarGoal.CreativeBreakthroughs,
                recallDaysPerWeek = 5,
                mainBlocker = RecallBlocker.WantMeaning,
                startedAtTimeOfDay = OnboardingTimeOfDay.MorningDay,
            )
        )

        val adaptiveCard = plan.snapshotCards.last()
        assertEquals("Creative Insight Potential", adaptiveCard.title)
        assertTrue(
            adaptiveCard.body.contains("ideas", ignoreCase = true) ||
                adaptiveCard.body.contains("breakthrough", ignoreCase = true)
        )
    }

    @Test
    fun `derive plan uses time of day for paywall cta`() {
        val plan = deriveOnboardingPlan(
            OnboardingAnswers(
                nearGoals = listOf(NearGoal.SleepWithIntention),
                farGoal = FarGoal.CalmerSleep,
                recallDaysPerWeek = 2,
                mainBlocker = RecallBlocker.WakeGroggy,
                startedAtTimeOfDay = OnboardingTimeOfDay.EveningNight,
            )
        )

        assertEquals("Start tonight's plan", plan.primaryCtaVariant)
        assertEquals("Start tonight's plan", plan.commitmentSummary.primaryCta)
    }
}
