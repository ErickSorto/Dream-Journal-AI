package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation

import androidx.compose.runtime.Immutable
import dreamjournalai.composeapp.shared.generated.resources.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource

enum class OnboardingFlowStep(
    val progressStep: Int,
    val totalSteps: Int = 9,
) {
    DreamOutcome(progressStep = 1),
    Name(progressStep = 2),
    NearOutcome(progressStep = 3),
    FarOutcome(progressStep = 4),
    Recall(progressStep = 5),
    Snapshot(progressStep = 6),
    FirstSevenNights(progressStep = 7),
    Paywall(progressStep = 8),
    Review(progressStep = 9),
    Rescue(progressStep = 9),
    Auth(progressStep = 9),
}

enum class OnboardingTimeOfDay {
    MorningDay,
    EveningNight,
}

enum class PlanTrack {
    RecallFirst,
    PatternBuilder,
    DeepInsight,
}

enum class HeroArtVariant {
    Recall,
    Pattern,
    Insight,
}

enum class NearGoal(
    val title: String,
    val icon: DrawableResource,
    val shortOutcome: String,
    val cardDescription: String,
) {
    RememberDreams(
        title = "Remember dreams more often",
        icon = Res.drawable.onboarding_icon_moon_glass,
        shortOutcome = "stronger morning recall",
        cardDescription = "Capture more before it slips away."
    ),
    UnderstandSymbols(
        title = "Understand recurring symbols",
        icon = Res.drawable.onboarding_icon_eye_glass,
        shortOutcome = "clearer symbol meaning",
        cardDescription = "See repeated images and themes with clarity."
    ),
    FeelConnected(
        title = "Feel more connected to myself",
        icon = Res.drawable.onboarding_icon_heart_glass,
        shortOutcome = "deeper self-connection",
        cardDescription = "Turn dream reflection into self-understanding."
    ),
    SparkCreativity(
        title = "Spark creativity and ideas",
        icon = Res.drawable.onboarding_icon_lightbulb_glass,
        shortOutcome = "more creative inspiration",
        cardDescription = "Use dream fragments as creative fuel."
    ),
    SleepWithIntention(
        title = "Sleep with more intention",
        icon = Res.drawable.onboarding_icon_feather_glass,
        shortOutcome = "a steadier night ritual",
        cardDescription = "Build a calmer rhythm around sleep and reflection."
    ),
    ExploreLucidDreaming(
        title = "Explore lucid dreaming",
        icon = Res.drawable.onboarding_icon_portal_glass,
        shortOutcome = "lucid dreaming readiness",
        cardDescription = "Strengthen recall and notice dream cues sooner."
    ),
}

enum class FarGoal(
    val title: String,
    val icon: DrawableResource,
    val snapshotTitle: String,
    val snapshotBody: String,
) {
    SelfUnderstanding(
        title = "Clearer self-understanding",
        icon = Res.drawable.onboarding_icon_compass_glass,
        snapshotTitle = "Self-Insight Arc",
        snapshotBody = "Your path should turn dream reflection into a clearer read on what you feel, want, and repeat."
    ),
    EmotionalBalance(
        title = "Better emotional balance",
        icon = Res.drawable.onboarding_icon_heart_glass,
        snapshotTitle = "Reflection Depth",
        snapshotBody = "Your plan should slow dreams down enough to notice emotional patterns without making the process feel heavy."
    ),
    CreativeBreakthroughs(
        title = "More creative breakthroughs",
        icon = Res.drawable.onboarding_icon_spark_glass,
        snapshotTitle = "Creative Insight Potential",
        snapshotBody = "Your dream path should keep vivid fragments close so they can turn into ideas, scenes, and breakthroughs."
    ),
    StrongerIntuition(
        title = "Stronger intuition",
        icon = Res.drawable.onboarding_icon_star_cluster_glass,
        snapshotTitle = "Pattern Awareness",
        snapshotBody = "Your plan should help subtle patterns feel visible sooner so your intuition has something concrete to build on."
    ),
    LucidMastery(
        title = "Lucid dreaming mastery",
        icon = Res.drawable.onboarding_icon_portal_glass,
        snapshotTitle = "Lucid Readiness",
        snapshotBody = "Your plan should start with recall and recurring cues so lucidity has a stronger foundation."
    ),
    CalmerSleep(
        title = "A calmer relationship with sleep",
        icon = Res.drawable.onboarding_icon_feather_glass,
        snapshotTitle = "Night Ritual Stability",
        snapshotBody = "Your dream path should feel steady, light, and calming enough to become part of how you end and begin each day."
    ),
}

enum class RecallBlocker(
    val title: String,
    val icon: DrawableResource,
    val helperLabel: String,
) {
    ForgetWithinMinutes(
        title = "I forget within minutes",
        icon = Res.drawable.onboarding_icon_cloud_glass,
        helperLabel = "dreams fade before you can hold them"
    ),
    WakeGroggy(
        title = "I wake up too groggy",
        icon = Res.drawable.onboarding_icon_hourglass_glass,
        helperLabel = "low-energy mornings make capture harder"
    ),
    UnsureWhatMatters(
        title = "I don't know what details matter",
        icon = Res.drawable.onboarding_icon_mirror_glass,
        helperLabel = "unclear details make recall feel slippery"
    ),
    Inconsistent(
        title = "I'm inconsistent",
        icon = Res.drawable.onboarding_icon_thread_glass,
        helperLabel = "consistency is the friction point"
    ),
    WantMeaning(
        title = "I want meaning, not just logging",
        icon = Res.drawable.onboarding_icon_mirror_glass,
        helperLabel = "you want reflection, not just notes"
    ),
}

@Immutable
data class OnboardingAnswers(
    val firstName: String = "",
    val nearGoals: List<NearGoal> = emptyList(),
    val farGoal: FarGoal? = null,
    val recallDaysPerWeek: Int? = null,
    val mainBlocker: RecallBlocker? = null,
    val startedAtTimeOfDay: OnboardingTimeOfDay = detectOnboardingTimeOfDay(),
) {
    val primaryNearGoal: NearGoal?
        get() = nearGoals.firstOrNull()

    val displayName: String
        get() = firstName.trim()
}

@Immutable
data class SnapshotCardModel(
    val title: String,
    val body: String,
    val progress: Float,
    val icon: DrawableResource,
)

@Immutable
data class RoadmapMilestoneModel(
    val label: String,
    val title: String,
    val body: String,
    val icon: DrawableResource,
)

@Immutable
data class CommitmentSummaryModel(
    val headline: String,
    val subheadline: String,
    val summaryBullets: List<Pair<DrawableResource, String>>,
    val outcomeBullets: List<Pair<DrawableResource, String>>,
    val pricingLabel: String,
    val pricingBody: String,
    val primaryCta: String,
)

@Immutable
data class OnboardingDerivedPlan(
    val planTrack: PlanTrack,
    val heroArtVariant: HeroArtVariant,
    val snapshotCards: List<SnapshotCardModel>,
    val roadmapMilestones: List<RoadmapMilestoneModel>,
    val roadmapSubheadline: String,
    val paywallHeadlineVariant: String,
    val primaryCtaVariant: String,
    val commitmentSummary: CommitmentSummaryModel,
)

fun deriveOnboardingPlan(answers: OnboardingAnswers): OnboardingDerivedPlan {
    val recall = answers.recallDaysPerWeek ?: 0
    val farGoal = answers.farGoal ?: FarGoal.SelfUnderstanding
    val blocker = answers.mainBlocker ?: RecallBlocker.ForgetWithinMinutes
    val primaryNearGoal = answers.primaryNearGoal ?: NearGoal.RememberDreams

    val planTrack = when (recall) {
        in 0..2 -> PlanTrack.RecallFirst
        in 3..5 -> PlanTrack.PatternBuilder
        else -> PlanTrack.DeepInsight
    }

    val heroVariant = when (planTrack) {
        PlanTrack.RecallFirst -> HeroArtVariant.Recall
        PlanTrack.PatternBuilder -> HeroArtVariant.Pattern
        PlanTrack.DeepInsight -> HeroArtVariant.Insight
    }

    val snapshotCards = listOf(
        SnapshotCardModel(
            title = "Recall Baseline",
            body = when (planTrack) {
                PlanTrack.RecallFirst -> "You're remembering dreams on about $recall mornings each week, so your first win is capturing them in the first minute after waking."
                PlanTrack.PatternBuilder -> "You're remembering dreams on about $recall mornings each week, which is enough to build a steady capture rhythm and start seeing patterns quickly."
                PlanTrack.DeepInsight -> "You're remembering dreams on about $recall mornings each week, giving you a strong base for deeper interpretation and lucid awareness work."
            },
            progress = ((recall + 1).toFloat() / 8f).coerceIn(0.18f, 0.94f),
            icon = Res.drawable.onboarding_icon_sunrise_glass
        ),
        SnapshotCardModel(
            title = "Biggest Blocker",
            body = "Your biggest friction point is that ${blocker.helperLabel}, so your plan should make dream capture feel lighter and more automatic.",
            progress = when (blocker) {
                RecallBlocker.ForgetWithinMinutes -> 0.42f
                RecallBlocker.WakeGroggy -> 0.50f
                RecallBlocker.UnsureWhatMatters -> 0.56f
                RecallBlocker.Inconsistent -> 0.48f
                RecallBlocker.WantMeaning -> 0.62f
            },
            icon = Res.drawable.onboarding_icon_constellation_glass
        ),
        SnapshotCardModel(
            title = "First Focus",
            body = "Because you want ${primaryNearGoal.title.lowercase()}, we'll guide your first week toward ${primaryNearGoal.shortOutcome}.",
            progress = when (primaryNearGoal) {
                NearGoal.RememberDreams -> 0.66f
                NearGoal.UnderstandSymbols -> 0.63f
                NearGoal.FeelConnected -> 0.58f
                NearGoal.SparkCreativity -> 0.61f
                NearGoal.SleepWithIntention -> 0.56f
                NearGoal.ExploreLucidDreaming -> 0.68f
            },
            icon = Res.drawable.onboarding_icon_compass_glass
        ),
        SnapshotCardModel(
            title = farGoal.snapshotTitle,
            body = farGoal.snapshotBody,
            progress = when (farGoal) {
                FarGoal.SelfUnderstanding -> 0.60f
                FarGoal.EmotionalBalance -> 0.58f
                FarGoal.CreativeBreakthroughs -> 0.63f
                FarGoal.StrongerIntuition -> 0.61f
                FarGoal.LucidMastery -> 0.67f
                FarGoal.CalmerSleep -> 0.55f
            },
            icon = Res.drawable.onboarding_icon_spiral_glass
        )
    )

    val roadmapMilestones = roadmapMilestonesFor(primaryNearGoal)

    val primaryCta = when (answers.startedAtTimeOfDay) {
        OnboardingTimeOfDay.EveningNight -> "Start tonight's plan"
        OnboardingTimeOfDay.MorningDay -> "Start my 7-night plan"
    }

    val commitmentSummary = CommitmentSummaryModel(
        headline = "Start your full Dream Path",
        subheadline = "Everything in your personalized 7-night plan to help you remember, understand, and act on your dreams.",
        summaryBullets = listOf(
            Res.drawable.onboarding_icon_open_journal_glass to "More dream recall",
            primaryNearGoal.icon to primaryNearGoal.title,
            farGoal.icon to farGoal.title
        ),
        outcomeBullets = listOf(
            Res.drawable.onboarding_icon_open_journal_glass to "Capture dreams before they disappear",
            Res.drawable.onboarding_icon_star_cluster_glass to "See recurring symbols and themes faster",
            Res.drawable.onboarding_icon_heart_glass to "Get reflections shaped around your goals",
            Res.drawable.onboarding_icon_spark_glass to "Build a night ritual you'll actually keep"
        ),
        pricingLabel = "Premium plan preview",
        pricingBody = "Plan options appear after sign in. Keep the flow calm now, then choose the plan that fits.",
        primaryCta = primaryCta
    )

    return OnboardingDerivedPlan(
        planTrack = planTrack,
        heroArtVariant = heroVariant,
        snapshotCards = snapshotCards,
        roadmapMilestones = roadmapMilestones,
        roadmapSubheadline = "A simple path built around ${primaryNearGoal.title.lowercase()}.",
        paywallHeadlineVariant = "Start your full Dream Path",
        primaryCtaVariant = primaryCta,
        commitmentSummary = commitmentSummary
    )
}

fun recallHelperText(recallDaysPerWeek: Int): String {
    return when (recallDaysPerWeek) {
        in 0..1 -> "There's a lot of dream material still slipping away."
        in 2..4 -> "You already have enough recall to build momentum fast."
        else -> "Great foundation — you're ready for deeper insight."
    }
}

fun roadmapMilestonesFor(nearGoal: NearGoal): List<RoadmapMilestoneModel> {
    return when (nearGoal) {
        NearGoal.RememberDreams -> listOf(
            RoadmapMilestoneModel("Night 1", "Catch more before it fades", "Recall more before it disappears and capture the parts that would usually be gone by breakfast.", Res.drawable.onboarding_icon_moon_glass),
            RoadmapMilestoneModel("Night 3", "Build a wake-up capture habit", "Use a lighter morning rhythm so dream capture starts to feel natural instead of forced.", Res.drawable.onboarding_icon_open_journal_glass),
            RoadmapMilestoneModel("Night 7", "Notice recall getting easier", "See your first signs that remembering dreams is starting to take less effort.", Res.drawable.onboarding_icon_sunrise_glass)
        )
        NearGoal.UnderstandSymbols -> listOf(
            RoadmapMilestoneModel("Night 1", "Keep the right details", "Log the dream details that usually get lost before they can become useful.", Res.drawable.onboarding_icon_eye_glass),
            RoadmapMilestoneModel("Night 3", "Group recurring images and feelings", "Start noticing which symbols, moods, or settings keep showing up together.", Res.drawable.onboarding_icon_constellation_glass),
            RoadmapMilestoneModel("Night 7", "See first patterns emerge", "Let those repeated images begin to form a story you can actually reflect on.", Res.drawable.onboarding_icon_compass_glass)
        )
        NearGoal.FeelConnected -> listOf(
            RoadmapMilestoneModel("Night 1", "Catch one feeling that lingers", "Hold onto the part of the dream that still feels emotionally alive after waking.", Res.drawable.onboarding_icon_heart_glass),
            RoadmapMilestoneModel("Night 3", "Notice emotional themes", "Start seeing which moods, tensions, or comforts are repeating underneath the dream scenes.", Res.drawable.onboarding_icon_mirror_glass),
            RoadmapMilestoneModel("Night 7", "See what your inner world is pointing toward", "Use that reflection to feel more connected to what your mind has been trying to show you.", Res.drawable.onboarding_icon_spiral_glass)
        )
        NearGoal.SparkCreativity -> listOf(
            RoadmapMilestoneModel("Night 1", "Capture one vivid scene or idea", "Keep a dream fragment before it fades so it can become something creative later.", Res.drawable.onboarding_icon_lightbulb_glass),
            RoadmapMilestoneModel("Night 3", "Spot charged fragments", "Notice the dream images with the most emotional or visual energy.", Res.drawable.onboarding_icon_spark_glass),
            RoadmapMilestoneModel("Night 7", "Turn dreams into inspiration", "Use what repeats to spark scenes, ideas, metaphors, or creative breakthroughs.", Res.drawable.onboarding_icon_star_cluster_glass)
        )
        NearGoal.SleepWithIntention -> listOf(
            RoadmapMilestoneModel("Night 1", "Set a calmer intention", "Bring one simple dream intention into the night without making bedtime feel like work.", Res.drawable.onboarding_icon_feather_glass),
            RoadmapMilestoneModel("Night 3", "Build a steadier rhythm", "Let dream capture and reflection become part of a calmer sleep ritual.", Res.drawable.onboarding_icon_thread_glass),
            RoadmapMilestoneModel("Night 7", "Feel more grounded around sleep", "Notice how a small ritual can make sleep and waking feel more connected.", Res.drawable.onboarding_icon_sunrise_glass)
        )
        NearGoal.ExploreLucidDreaming -> listOf(
            RoadmapMilestoneModel("Night 1", "Strengthen recall first", "Build the recall foundation lucidity depends on before chasing control.", Res.drawable.onboarding_icon_portal_glass),
            RoadmapMilestoneModel("Night 3", "Notice recurring dream cues", "Start recognizing the settings, symbols, and feelings that return at night.", Res.drawable.onboarding_icon_constellation_glass),
            RoadmapMilestoneModel("Night 7", "Recognize repeating dream signs", "Begin spotting the clues your mind repeats, the first real step toward lucidity.", Res.drawable.onboarding_icon_star_cluster_glass)
        )
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
fun detectOnboardingTimeOfDay(): OnboardingTimeOfDay {
    val hour = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .hour
    return if (hour in 18..23 || hour in 0..4) {
        OnboardingTimeOfDay.EveningNight
    } else {
        OnboardingTimeOfDay.MorningDay
    }
}
