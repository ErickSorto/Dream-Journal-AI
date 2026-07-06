package org.ballistic.dreamjournalai.shared.dream_premium.domain

import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit
import com.revenuecat.purchases.kmp.models.DiscountPaymentMode
import com.revenuecat.purchases.kmp.models.billingPeriod
import com.revenuecat.purchases.kmp.models.freePhase
import com.revenuecat.purchases.kmp.models.introPhase
import dreamjournalai.composeapp.shared.generated.resources.*
import org.ballistic.dreamjournalai.shared.core.platform.getPlatformName
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.OnboardingAnswers
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.OnboardingTimeOfDay
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

const val PremiumEntitlementId = "premium"

enum class PremiumPlacement(val placementId: String) {
    PostAuthPrimary("post_auth_primary"),
    DailyOpenOffer("daily_open_offer"),
    ThirdDreamSaved("third_dream_saved"),
    AiAnalysisGate("ai_analysis_gate"),
    PatternInsightsGate("pattern_insights_gate"),
    SettingsUpgrade("settings_upgrade"),
    PurchaseAbandonment("purchase_abandonment"),
    WinbackOffer("winback_offer"),
}

enum class PremiumPageKind {
    TrialClarity,
    Promise,
    Unlocks,
    OfferSheet,
    GiftReveal,
    OneTimeOffer,
}

enum class PremiumEntrySource {
    OnboardingIntro,
    AutoLaunchOnce,
    JournalGiftBubble,
    DailyTokens,
    DailyLessons,
    RealityChecker,
    StoreScreen,
}

enum class PremiumStageId {
    TrialClarity,
    Promise,
    Unlocks,
    OfferSheet,
    GiftReveal,
    OneTimeOffer,
}

enum class PremiumPlanOption {
    Monthly,
    Annual,
}

enum class PremiumEntryMode {
    FeatureGate,
    InitialOnboarding,
}

data class PremiumCalloutUiModel(
    val title: String,
    val description: String,
)

data class PremiumTimelineItemUiModel(
    val label: String,
    val title: String,
    val description: String,
)

data class PremiumStageUiModel(
    val id: PremiumStageId,
    val headline: String,
    val body: String,
    val image: DrawableResource? = null,
    val callouts: List<PremiumCalloutUiModel> = emptyList(),
    val timeline: List<PremiumTimelineItemUiModel> = emptyList(),
    val primaryActionLabel: String,
    val secondaryActionLabel: String? = null,
    val warmVisual: Boolean = false,
)

data class PremiumUiState(
    val displayName: String,
    val selectedPlan: PremiumPlanOption = PremiumPlanOption.Annual,
    val currentStageId: PremiumStageId,
    val stages: List<PremiumStageUiModel>,
    val isPurchaseInProgress: Boolean = false,
    val hasPremium: Boolean = false,
) {
    val currentStage: PremiumStageUiModel
        get() = stages.first { it.id == currentStageId }

    val canGoBackInFlow: Boolean
        get() = currentStageId != PremiumStageId.Promise &&
            currentStageId != PremiumStageId.TrialClarity
}

enum class PremiumSocialMode {
    PremiumBenefits,
    Reviews,
    Hybrid,
}

enum class PremiumRescueSource(val analyticsValue: String) {
    OfferSheetDismiss("offer_sheet_dismiss"),
    PurchaseCancel("purchase_cancel"),
}

enum class PremiumRescueVariant(val analyticsValue: String) {
    GiftBox("gift_box"),
    DirectRescue("direct_rescue"),
}

data class PremiumTimelineItem(
    val title: String,
    val body: String,
)

data class MembershipBenefitModel(
    val icon: DrawableResource,
    val title: String,
    val body: String,
)

data class PremiumPackageModel(
    val packageToPurchase: Package,
    val title: String,
    val badge: String?,
    val priceText: String,
    val cadenceText: String,
    val trialText: String?,
    val hasTrial: Boolean,
    val isAnnual: Boolean,
    val isMonthly: Boolean,
)

fun PremiumPackageModel.monthlyDisplayPriceText(): String {
    return if (isAnnual) {
        priceText.formatPriceMicrosLike(packageToPurchase.storeProduct.price.amountMicros / 12.0)
    } else {
        priceText
    }
}

fun PremiumPackageModel.planPriceLabel(): String {
    return when {
        isAnnual -> "$priceText/year"
        isMonthly -> "$priceText/month"
        else -> priceText
    }
}

fun PremiumPackageModel.planTogglePriceLabel(): String {
    if (getPlatformName() != "iOS") {
        return when {
            isAnnual -> "${monthlyDisplayPriceText()}/mo"
            isMonthly -> "$priceText/mo"
            else -> priceText
        }
    }

    return when {
        isAnnual -> "$priceText/year"
        isMonthly -> "$priceText/month"
        else -> priceText
    }
}

fun PremiumPackageModel.planToggleBillingLabel(): String {
    if (getPlatformName() == "iOS") {
        return when {
            isAnnual -> "Works out to ${monthlyDisplayPriceText()}/mo, paid yearly"
            isMonthly -> "$priceText/mo, paid monthly"
            else -> cadenceText
        }
    }

    return when {
        isAnnual -> "Billed as $priceText yearly"
        isMonthly -> "Billed as $priceText monthly"
        else -> cadenceText
    }
}

fun PremiumPackageModel.billingPriceLabel(): String {
    return when {
        isAnnual -> "$priceText/year"
        isMonthly -> "$priceText/month"
        else -> {
            val cadence = cadenceText.lowercase()
                .removeSuffix(" plan")
                .ifBlank { "billing period" }
            "$priceText/$cadence"
        }
    }
}

fun PremiumPackageModel.offerCtaSubtext(): String {
    val billingPrice = billingPriceLabel()
    val freeTrialPeriod = freeTrialDisplayPeriod()
    return if (freeTrialPeriod != null) {
        "Unlimited free access for $freeTrialPeriod, then $billingPrice"
    } else {
        "Unlimited access, $billingPrice"
    }
}

@OptIn(ExperimentalTime::class)
fun PremiumPackageModel.trialEndingReminderTriggerAtEpochMillis(
    daysBeforeTrialEnds: Int = 2,
): Long? {
    val trialDurationMillis = trialDurationMillis() ?: return null
    val reminderOffsetMillis = daysBeforeTrialEnds.days.inWholeMilliseconds
    val reminderDelayMillis = (trialDurationMillis - reminderOffsetMillis).coerceAtLeast(60_000L)
    return Clock.System.now().toEpochMilliseconds() + reminderDelayMillis
}

private fun PremiumPackageModel.trialDurationMillis(): Long? {
    packageToPurchase.storeProduct.defaultOption?.freePhase?.let { freePhase ->
        val cycleCount = freePhase.billingCycleCount ?: 1
        return freePhase.billingPeriod.toDurationMillis(cycleCount)
    }

    packageToPurchase.storeProduct.introductoryDiscount?.let { discount ->
        val isFreeTrial = discount.paymentMode == DiscountPaymentMode.FREE_TRIAL ||
            discount.price.amountMicros == 0L
        if (isFreeTrial) {
            return discount.subscriptionPeriod.toDurationMillis(discount.numberOfPeriods.toInt())
        }
    }

    return null
}

private fun PremiumPackageModel.freeTrialDisplayPeriod(): String? {
    packageToPurchase.storeProduct.defaultOption?.freePhase?.let { freePhase ->
        return freePhase.billingPeriod.toDisplayPeriod()
    }

    packageToPurchase.storeProduct.introductoryDiscount?.let { discount ->
        val isFreeTrial = discount.paymentMode == DiscountPaymentMode.FREE_TRIAL ||
            discount.price.amountMicros == 0L
        if (isFreeTrial) {
            return discount.subscriptionPeriod.toDisplayPeriod()
        }
    }

    return null
}

private fun Period.toDurationMillis(cycleCount: Int = 1): Long? {
    val cycles = cycleCount.coerceAtLeast(1)
    val days = when (unit) {
        PeriodUnit.DAY -> value
        PeriodUnit.WEEK -> value * 7
        PeriodUnit.MONTH -> value * 30
        PeriodUnit.YEAR -> value * 365
        PeriodUnit.UNKNOWN -> return null
    }
    return (days * cycles).days.inWholeMilliseconds
}

private fun String.formatPriceMicrosLike(amountMicros: Double): String {
    val match = Regex("""\d+(?:[.,]\d+)?""").find(this) ?: return this
    val rawAmount = match.value
    val decimalSeparator = when {
        ',' in rawAmount && '.' !in rawAmount -> ','
        '.' in rawAmount -> '.'
        else -> null
    }
    val decimalPlaces = decimalSeparator?.let { rawAmount.substringAfter(it).length } ?: 0
    val factor = powerOfTen(decimalPlaces)
    val amount = amountMicros / 1_000_000.0
    val scaledAmount = (amount * factor).roundToLong()
    val whole = scaledAmount / factor
    val fraction = scaledAmount % factor
    val amountText = if (decimalPlaces == 0) {
        whole.toString()
    } else {
        "$whole${decimalSeparator ?: '.'}${fraction.toString().padStart(decimalPlaces, '0')}"
    }
    return replaceRange(match.range, amountText)
}

private fun powerOfTen(exponent: Int): Long {
    var value = 1L
    repeat(exponent.coerceAtLeast(0)) {
        value *= 10L
    }
    return value
}

data class PremiumMetadata(
    val paywallStyle: String = "dream_midnight_v1",
    val heroAssetKey: String = "",
    val promiseHeadline: String = "Unlock DreamNorth Premium",
    val promiseSubheadline: String = "Turn dream capture, interpretation, and pattern tracking into a calmer daily ritual.",
    val timelineEnabled: Boolean = true,
    val timelineCopyToday: String = "Save more detail the moment you wake and explore the dream while it is still vivid.",
    val timelineCopyReminder: String = "Recurring symbols, moods, and themes become easier to notice across your entries.",
    val timelineCopyBilling: String = "Your journal starts feeling like a steady practice instead of a scattered memory.",
    val showTrialToggle: Boolean = false,
    val trialToggleDefault: Boolean = true,
    val ctaPromise: String = "Continue",
    val ctaTimeline: String = "Show me Premium",
    val ctaTrial: String = "Try for free",
    val ctaDirect: String = "Start DreamNorth Premium",
    val annualBadge: String = "Best value",
    val monthlyBadge: String = "",
    val showMonthly: Boolean = true,
    val defaultPackageType: String = "annual",
    val promoBadgeText: String = "DreamNorth Gift",
    val variantName: String = "default",
    val giftboxEnabled: Boolean = true,
)

data class PremiumPaywallModel(
    val placement: PremiumPlacement,
    val entrySource: PremiumEntrySource,
    val offering: Offering,
    val metadata: PremiumMetadata,
    val artBundle: PremiumArtBundle,
    val promoBadgeText: String,
    val benefitRows: List<MembershipBenefitModel>,
    val timelineItems: List<PremiumTimelineItem>,
    val packages: List<PremiumPackageModel>,
)

fun initialStage(entryMode: PremiumEntryMode): PremiumStageId = when (entryMode) {
    PremiumEntryMode.FeatureGate -> PremiumStageId.TrialClarity
    PremiumEntryMode.InitialOnboarding -> PremiumStageId.Promise
}

fun PremiumEntrySource.toPremiumEntryMode(): PremiumEntryMode = when (this) {
    PremiumEntrySource.OnboardingIntro -> PremiumEntryMode.InitialOnboarding
    PremiumEntrySource.AutoLaunchOnce,
    PremiumEntrySource.JournalGiftBubble,
    PremiumEntrySource.DailyTokens,
    PremiumEntrySource.DailyLessons,
    PremiumEntrySource.RealityChecker,
    PremiumEntrySource.StoreScreen -> PremiumEntryMode.FeatureGate
}

fun buildPremiumStages(
    displayName: String,
    hasPremium: Boolean,
    model: PremiumPaywallModel,
): List<PremiumStageUiModel> {
    val name = displayName.trim().ifBlank { "Dreamer" }
    val unlockCallouts = model.benefitRows.map { benefit ->
        PremiumCalloutUiModel(
            title = benefit.title,
            description = benefit.body,
        )
    }

    return listOf(
        PremiumStageUiModel(
            id = PremiumStageId.TrialClarity,
            headline = "This dream tool is part of Premium.",
            body = "Unlock deeper interpretation, richer capture, and calmer guidance when you want more from a dream.",
            image = model.artBundle.icons.unlockIcon,
            primaryActionLabel = "Continue",
        ),
        PremiumStageUiModel(
            id = PremiumStageId.Promise,
            headline = "$name, keep more of your dreams before they fade.",
            body = "DreamNorth Premium helps your journal become a steadier place for recall, meaning, and patterns.",
            image = Res.drawable.premium_hero,
            callouts = listOf(
                PremiumCalloutUiModel(
                    title = "Deeper recall",
                    description = "Save more of the scene, feeling, and strange details.",
                ),
                PremiumCalloutUiModel(
                    title = "Clearer meaning",
                    description = "Turn scattered dream notes into symbols and insight.",
                ),
            ),
            primaryActionLabel = "Continue",
        ),
        PremiumStageUiModel(
            id = PremiumStageId.Unlocks,
            headline = "Stronger tools for your dream practice.",
            body = "More room to capture, interpret, visualize, and understand what keeps showing up.",
            image = model.artBundle.timelineHero,
            callouts = unlockCallouts,
            primaryActionLabel = "See the offer",
        ),
        PremiumStageUiModel(
            id = PremiumStageId.OfferSheet,
            headline = "",
            body = "",
            image = model.artBundle.offerSupportArt,
            primaryActionLabel = if (hasPremium) "Continue with Premium" else "Start membership",
        ),
        PremiumStageUiModel(
            id = PremiumStageId.GiftReveal,
            headline = "Before you leave, here's the simplest yearly option.",
            body = "If you want one clean decision, we can keep it to the annual plan only.",
            image = Res.drawable.paywall_gift_box_hero,
            callouts = listOf(
                PremiumCalloutUiModel(
                    title = "Included if you continue",
                    description = "Deeper interpretations, more dream tools, and a calmer first-week rhythm.",
                ),
            ),
            primaryActionLabel = "See annual option",
            warmVisual = true,
        ),
        PremiumStageUiModel(
            id = PremiumStageId.OneTimeOffer,
            headline = if (hasPremium) {
                "Your journal already has Premium."
            } else {
                "Keep your dream practice on one steady plan."
            },
            body = "Yearly pricing loads from billing.",
            callouts = listOf(
                PremiumCalloutUiModel("Annual only", "One clean decision for the full Premium journal."),
                PremiumCalloutUiModel("Cancel if it is not right", "Try the first week, then decide."),
            ),
            primaryActionLabel = if (hasPremium) "Continue with Premium" else "Start yearly membership",
            secondaryActionLabel = "Go back to the main offer",
            warmVisual = true,
        ),
    )
}

sealed interface PremiumPurchaseResult {
    data class Success(val packageId: String) : PremiumPurchaseResult
    data object UserCancelled : PremiumPurchaseResult
    data object Pending : PremiumPurchaseResult
    data object AlreadySubscribed : PremiumPurchaseResult
    data class Error(val message: String) : PremiumPurchaseResult
}

fun Offering.toPremiumPaywallModel(
    placement: PremiumPlacement,
    answers: OnboardingAnswers,
    entrySource: PremiumEntrySource,
): PremiumPaywallModel {
    val rawMetadata = metadata
    val metadata = rawMetadata.toPremiumMetadata()
    val resolvedMetadata = metadata.copy(
        promiseHeadline = resolveTemplate(metadata.promiseHeadline, answers),
        promiseSubheadline = resolveTemplate(metadata.promiseSubheadline, answers),
        timelineCopyToday = resolveTemplate(metadata.timelineCopyToday, answers),
        timelineCopyReminder = resolveTemplate(metadata.timelineCopyReminder, answers),
        timelineCopyBilling = resolveTemplate(metadata.timelineCopyBilling, answers),
    )

    val packages = availablePackages
        .mapNotNull { it.toPremiumPackageModel(resolvedMetadata) }
        .sortedWith(
            compareByDescending<PremiumPackageModel> { it.isAnnual }
                .thenByDescending { it.hasTrial }
                .thenBy { it.title }
        )
    val artBundle = resolvePremiumArtBundle(
        paywallStyle = resolvedMetadata.paywallStyle,
        heroAssetKey = resolvedMetadata.heroAssetKey,
    )
    val defaultBenefits = defaultBenefitRows(answers, artBundle)
    val configuredBenefits = configuredBenefitRows(rawMetadata, artBundle)
    val benefitRows = (configuredBenefits + defaultBenefits)
        .distinctBy { it.title }
        .take(6)

    return PremiumPaywallModel(
        placement = placement,
        entrySource = entrySource,
        offering = this,
        metadata = resolvedMetadata,
        artBundle = artBundle,
        promoBadgeText = resolvedMetadata.promoBadgeText,
        benefitRows = benefitRows,
        timelineItems = listOf(
            PremiumTimelineItem(
                title = "Capture richer dreams",
                body = resolvedMetadata.timelineCopyToday
            ),
            PremiumTimelineItem(
                title = "Notice what repeats",
                body = resolvedMetadata.timelineCopyReminder
            ),
            PremiumTimelineItem(
                title = "See clearer insight",
                body = resolvedMetadata.timelineCopyBilling
            ),
            PremiumTimelineItem(
                title = "Build a rhythm that lasts",
                body = "DreamNorth starts feeling like a calm place to return to, not another task to remember."
            ),
        ),
        packages = packages,
    )
}

fun PremiumPaywallModel.visiblePackages(trialEnabled: Boolean): List<PremiumPackageModel> {
    val basePackages = packages.filter { metadata.showMonthly || !it.isMonthly }
    val trialFiltered = when {
        !metadata.showTrialToggle -> basePackages
        trialEnabled -> {
            val withTrial = basePackages.filter { it.hasTrial }
            if (withTrial.isNotEmpty()) withTrial else basePackages
        }
        else -> {
            val withoutTrial = basePackages.filter { !it.hasTrial }
            if (withoutTrial.isNotEmpty()) withoutTrial else basePackages
        }
    }
    return trialFiltered
}

fun PremiumPaywallModel.defaultPackageId(trialEnabled: Boolean): String? {
    val visible = visiblePackages(trialEnabled)
    val defaultType = metadata.defaultPackageType.lowercase()
    return visible.firstOrNull { model ->
        when (defaultType) {
            "annual" -> model.isAnnual
            "monthly" -> model.isMonthly
            else -> model.packageToPurchase.identifier.equals(defaultType, ignoreCase = true)
        }
    }?.packageToPurchase?.identifier ?: visible.firstOrNull()?.packageToPurchase?.identifier
}

fun PremiumPaywallModel.defaultPlanOption(): PremiumPlanOption {
    return when (metadata.defaultPackageType.lowercase()) {
        "monthly" -> PremiumPlanOption.Monthly
        else -> PremiumPlanOption.Annual
    }
}

fun PremiumPaywallModel.packageForPlan(option: PremiumPlanOption): PremiumPackageModel? {
    return when (option) {
        PremiumPlanOption.Annual -> packages.firstOrNull { it.isAnnual }
            ?: packages.firstOrNull { !it.isMonthly }
            ?: packages.firstOrNull()
        PremiumPlanOption.Monthly -> packages.firstOrNull { it.isMonthly }
            ?: packages.firstOrNull()
    }
}

fun PremiumPaywallModel.primaryCta(
    selectedPackageId: String?,
    trialEnabled: Boolean,
): String {
    val selectedPackage = packages.firstOrNull { it.packageToPurchase.identifier == selectedPackageId }
    return if (metadata.showTrialToggle && trialEnabled && selectedPackage?.hasTrial == true) {
        metadata.ctaTrial
    } else {
        metadata.ctaDirect
    }
}

fun defaultPremiumPrimaryCta(timeOfDay: OnboardingTimeOfDay): String {
    return when (timeOfDay) {
        OnboardingTimeOfDay.EveningNight -> "Start tonight's plan"
        OnboardingTimeOfDay.MorningDay -> "Start my 7-night plan"
    }
}

private fun defaultBenefitRows(
    answers: OnboardingAnswers,
    artBundle: PremiumArtBundle,
): List<MembershipBenefitModel> {
    return listOf(
        MembershipBenefitModel(
            icon = artBundle.icons.dreamToken,
            title = "Capture more of each dream",
            body = "Save more details, explore more entries, and return to the dreams that matter."
        ),
        MembershipBenefitModel(
            icon = artBundle.icons.lessonBook,
            title = "Unlock deeper interpretations",
            body = "Connect symbols, emotions, and themes with richer guidance as your journal grows."
        ),
        MembershipBenefitModel(
            icon = artBundle.icons.toolUnlock,
            title = "Unlock every dream tool",
            body = "Interpret, paint, analyze, and explore dreams without running into locked moments."
        ),
        MembershipBenefitModel(
            icon = artBundle.icons.audioWave,
            title = "Record dreams with more room",
            body = "Capture the feeling, setting, and strange details before the morning moves on."
        ),
        MembershipBenefitModel(
            icon = artBundle.icons.membershipStar,
            title = "See patterns sooner",
            body = "Understand recurring themes, emotions, and symbols as they build over time."
        ),
        MembershipBenefitModel(
            icon = artBundle.icons.dreamBell,
            title = "Build a calmer ritual",
            body = "Use gentle reminders and guided moments to make dream reflection easier to return to."
        ),
    )
}

private fun configuredBenefitRows(
    metadata: Map<String, Any>,
    artBundle: PremiumArtBundle,
): List<MembershipBenefitModel> {
    val fallbackIcons = listOf(
        artBundle.icons.dreamToken,
        artBundle.icons.lessonBook,
        artBundle.icons.toolUnlock,
        artBundle.icons.audioWave,
        artBundle.icons.membershipStar,
        artBundle.icons.dreamBell,
    )

    return (1..6).mapNotNull { index ->
        val title = metadata.getNullableString("benefit_${index}_title")?.trim().orEmpty()
        val body = metadata.getNullableString("benefit_${index}_body")?.trim().orEmpty()
        if (title.isBlank() || body.isBlank()) {
            null
        } else {
            MembershipBenefitModel(
                icon = fallbackIcons.getOrElse(index - 1) { artBundle.icons.membershipStar },
                title = title,
                body = body,
            )
        }
    }
}

private fun Map<String, Any>.toPremiumMetadata(): PremiumMetadata {
    return PremiumMetadata(
        paywallStyle = getString("paywall_style", "dream_midnight_v1"),
        heroAssetKey = getString("hero_asset_key", ""),
        promiseHeadline = getString("promise_headline", "Unlock DreamNorth Premium"),
        promiseSubheadline = getString(
            "promise_subheadline",
            "Turn dream capture, interpretation, and pattern tracking into a calmer daily ritual."
        ),
        timelineEnabled = getBoolean("timeline_enabled", true),
        timelineCopyToday = getString("timeline_copy_today", "Save more detail the moment you wake and explore the dream while it is still vivid."),
        timelineCopyReminder = getString("timeline_copy_reminder", "Recurring symbols, moods, and themes become easier to notice across your entries."),
        timelineCopyBilling = getString("timeline_copy_billing", "Your journal starts feeling like a steady practice instead of a scattered memory."),
        showTrialToggle = getBoolean("show_trial_toggle", false),
        trialToggleDefault = getBoolean("trial_toggle_default", true),
        ctaPromise = getString("cta_promise", "Continue"),
        ctaTimeline = getString("cta_timeline", "Show me Premium"),
        ctaTrial = getString("cta_trial", "Try for free"),
        ctaDirect = getString("cta_direct", "Start DreamNorth Premium"),
        annualBadge = getString("annual_badge", "Best value"),
        monthlyBadge = getString("monthly_badge", ""),
        showMonthly = getBoolean("show_monthly", true),
        defaultPackageType = getString("default_package_type", "annual"),
        promoBadgeText = getString("promo_badge_text", "DreamNorth Gift"),
        variantName = getString("variant_name", "default"),
        giftboxEnabled = getBoolean("giftbox_enabled", true),
    )
}

private fun Package.toPremiumPackageModel(metadata: PremiumMetadata): PremiumPackageModel? {
    val isAnnual = isAnnualPlan()
    val isMonthly = isMonthlyPlan()
    val cadenceText = storeProduct.period?.toCadenceLabel() ?: when {
        isAnnual -> "Annual plan"
        isMonthly -> "Monthly plan"
        else -> "Premium plan"
    }
    val iosFreeTrialDiscount = storeProduct.introductoryDiscount?.takeIf { discount ->
        discount.paymentMode == DiscountPaymentMode.FREE_TRIAL ||
            discount.price.amountMicros == 0L
    }
    val hasTrial = storeProduct.introductoryDiscount != null ||
        storeProduct.defaultOption?.freePhase != null ||
        storeProduct.defaultOption?.introPhase != null

    val trialText = when {
        !hasTrial -> null
        storeProduct.defaultOption?.freePhase != null -> {
            val freePhase = storeProduct.defaultOption?.freePhase
            if (freePhase != null) "Free for ${freePhase.billingPeriod.toDisplayPeriod()}" else "Trial available"
        }
        iosFreeTrialDiscount != null -> "Free for ${iosFreeTrialDiscount.subscriptionPeriod.toDisplayPeriod()}"
        storeProduct.introductoryDiscount != null -> "Includes intro offer"
        else -> "Trial available"
    }

    val title = when {
        isAnnual -> "Annual"
        isMonthly -> "Monthly"
        else -> storeProduct.title.ifBlank { "Premium" }
    }

    return PremiumPackageModel(
        packageToPurchase = this,
        title = title,
        badge = when {
            isAnnual -> metadata.annualBadge.takeIf { it.isNotBlank() }
            isMonthly -> metadata.monthlyBadge.takeIf { it.isNotBlank() }
            else -> null
        },
        priceText = storeProduct.price.formatted,
        cadenceText = cadenceText,
        trialText = trialText,
        hasTrial = hasTrial,
        isAnnual = isAnnual,
        isMonthly = isMonthly,
    )
}

private fun Package.isAnnualPlan(): Boolean {
    val searchableValues = listOf(
        identifier,
        storeProduct.id,
        storeProduct.defaultOption?.id.orEmpty(),
    ).map { it.lowercase() }

    return packageType == PackageType.ANNUAL ||
        searchableValues.any { value ->
            "annual" in value ||
                "yearly" in value ||
                value.endsWith(":year") ||
                value.endsWith(":yearly")
        } ||
        storeProduct.period?.unit == PeriodUnit.YEAR ||
        storeProduct.defaultOption?.billingPeriod?.unit == PeriodUnit.YEAR
}

private fun Package.isMonthlyPlan(): Boolean {
    val searchableValues = listOf(
        identifier,
        storeProduct.id,
        storeProduct.defaultOption?.id.orEmpty(),
    ).map { it.lowercase() }

    return packageType == PackageType.MONTHLY ||
        searchableValues.any { value ->
            "monthly" in value ||
                value.endsWith(":month") ||
                value.endsWith(":monthly")
        } ||
        storeProduct.period?.let { it.unit == PeriodUnit.MONTH && it.value == 1 } == true ||
        storeProduct.defaultOption?.billingPeriod?.let {
            it.unit == PeriodUnit.MONTH && it.value == 1
        } == true
}

private fun Map<String, Any>.getString(key: String, default: String): String =
    when (val value = this[key]) {
        is String -> value
        else -> default
    }

private fun Map<String, Any>.getNullableString(key: String): String? =
    when (val value = this[key]) {
        is String -> value
        else -> null
    }

private fun Map<String, Any>.getBoolean(key: String, default: Boolean): Boolean =
    when (val value = this[key]) {
        is Boolean -> value
        is String -> value.equals("true", ignoreCase = true)
        else -> default
    }

private fun Map<String, Any>.getInt(key: String): Int? =
    when (val value = this[key]) {
        is Int -> value
        is Long -> value.toInt()
        is Double -> value.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }

private fun Period.toCadenceLabel(): String = when (unit) {
    PeriodUnit.DAY -> if (value == 1) "Daily plan" else "Every $value days"
    PeriodUnit.WEEK -> if (value == 1) "Weekly plan" else "Every $value weeks"
    PeriodUnit.MONTH -> if (value == 1) "Monthly plan" else "Every $value months"
    PeriodUnit.YEAR -> if (value == 1) "Annual plan" else "Every $value years"
    PeriodUnit.UNKNOWN -> "Premium plan"
}

private fun Period.toDisplayPeriod(): String = when (unit) {
    PeriodUnit.DAY -> if (value == 1) "1 day" else "$value days"
    PeriodUnit.WEEK -> if (value == 1) "1 week" else "$value weeks"
    PeriodUnit.MONTH -> if (value == 1) "1 month" else "$value months"
    PeriodUnit.YEAR -> if (value == 1) "1 year" else "$value years"
    PeriodUnit.UNKNOWN -> "trial"
}

private fun resolveTemplate(value: String, answers: OnboardingAnswers): String {
    val firstName = answers.displayName.ifBlank { "Dreamer" }
    val nearGoal = answers.primaryNearGoal?.title ?: "remember dreams more often"
    val farGoal = answers.farGoal?.title ?: "clearer self-understanding"
    return value
        .replace("{firstName}", firstName)
        .replace("{selectedNearGoal}", nearGoal)
        .replace("{selectedFarGoal}", farGoal)
}
