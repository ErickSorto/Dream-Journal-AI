package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.core.analytics.AnalyticsUserProperty
import org.ballistic.dreamjournalai.shared.core.analytics.AppAnalytics
import org.ballistic.dreamjournalai.shared.core.analytics.analyticsValue
import org.ballistic.dreamjournalai.shared.core.analytics.countBucket
import org.ballistic.dreamjournalai.shared.core.analytics.durationBucket
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.core.platform.getPlatformName
import org.ballistic.dreamjournalai.shared.core.util.BackHandler
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModelState
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingPremiumIntroPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingPremiumStagePage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingAuthCard
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingCommitmentPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingDreamCheckInPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingDreamOutcomePage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingDreamFrictionIntroPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingDreamMathPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingFarOutcomePage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingFirstInsightPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingFirstStreakPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingNamePage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingNearOutcomePage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingNotificationPermissionPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingPageBackground
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingPlanBridgePage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingRecallPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingReviewStoriesPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingSevenNightsPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingSnapshotPage
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingSocialProofPage
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.PremiumTrialReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_main.presentation.rememberNotificationPermissionState
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumAnalytics
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumAnalyticsEvent
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumEntrySource
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPackageModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPageKind
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPaywallModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPlanOption
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPlacement
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPurchaseResult
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumRescueSource
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumRescueVariant
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumStageId
import org.ballistic.dreamjournalai.shared.dream_premium.domain.defaultPlanOption
import org.ballistic.dreamjournalai.shared.dream_premium.domain.initialStage
import org.ballistic.dreamjournalai.shared.dream_premium.domain.packageForPlan
import org.ballistic.dreamjournalai.shared.dream_premium.domain.repository.PremiumPaywallRepository
import org.ballistic.dreamjournalai.shared.dream_premium.domain.trialEndingReminderTriggerAtEpochMillis
import org.ballistic.dreamjournalai.shared.dream_premium.domain.toPremiumPaywallModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.toPremiumEntryMode
import org.koin.compose.koinInject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

// Animation timing constants (file-level)
private const val STAGGER_MS: Long = 200
private const val ENTER_DURATION_MS: Int = 300
private const val ONBOARDING_AUTH_EXIT_MS: Int = 180
private const val AppleProviderId = "apple.com"

private enum class OnboardingStep {
    Intro,
    Profile,
    Focus,
    Reviews,
    Login
}

private fun OnboardingFlowStep.toPremiumPageKind(): PremiumPageKind? {
    return toPremiumStageId()?.toPremiumPageKind()
}

private fun OnboardingFlowStep.toPremiumStageId(): PremiumStageId? {
    return when (this) {
        OnboardingFlowStep.PremiumTrialClarity -> PremiumStageId.TrialClarity
        OnboardingFlowStep.PremiumPromise -> PremiumStageId.Promise
        OnboardingFlowStep.PremiumUnlocks -> PremiumStageId.Unlocks
        OnboardingFlowStep.PremiumOfferSheet -> PremiumStageId.OfferSheet
        OnboardingFlowStep.PremiumGiftReveal -> PremiumStageId.GiftReveal
        OnboardingFlowStep.PremiumOneTimeOffer -> PremiumStageId.OneTimeOffer
        else -> null
    }
}

private fun PremiumStageId.toPremiumPageKind(): PremiumPageKind {
    return when (this) {
        PremiumStageId.TrialClarity -> PremiumPageKind.TrialClarity
        PremiumStageId.Promise -> PremiumPageKind.Promise
        PremiumStageId.Unlocks -> PremiumPageKind.Unlocks
        PremiumStageId.OfferSheet -> PremiumPageKind.OfferSheet
        PremiumStageId.GiftReveal -> PremiumPageKind.GiftReveal
        PremiumStageId.OneTimeOffer -> PremiumPageKind.OneTimeOffer
    }
}

private fun PremiumStageId.toOnboardingFlowStep(): OnboardingFlowStep {
    return when (this) {
        PremiumStageId.TrialClarity -> OnboardingFlowStep.PremiumTrialClarity
        PremiumStageId.Promise -> OnboardingFlowStep.PremiumPromise
        PremiumStageId.Unlocks -> OnboardingFlowStep.PremiumUnlocks
        PremiumStageId.OfferSheet -> OnboardingFlowStep.PremiumOfferSheet
        PremiumStageId.GiftReveal -> OnboardingFlowStep.PremiumGiftReveal
        PremiumStageId.OneTimeOffer -> OnboardingFlowStep.PremiumOneTimeOffer
    }
}

private fun isCurrentIosAppleUser(): Boolean {
    val user = Firebase.auth.currentUser ?: return false
    return getPlatformName() == "iOS" &&
        !user.isAnonymous &&
        user.providerData.any { it.providerId == AppleProviderId }
}

private fun currentUserFirstNameOrBlank(): String {
    return Firebase.auth.currentUser
        ?.displayName
        ?.trim()
        ?.substringBefore(" ")
        ?.takeIf { it.isNotBlank() }
        .orEmpty()
}

private suspend fun syncPremiumTrialReminder(
    packageModel: PremiumPackageModel,
    scheduler: PremiumTrialReminderScheduler,
) {
    val triggerAtEpochMillis = packageModel.trialEndingReminderTriggerAtEpochMillis()
    if (triggerAtEpochMillis != null) {
        scheduler.scheduleTrialEndingReminder(triggerAtEpochMillis)
    } else {
        scheduler.cancelTrialEndingReminder()
    }
}

private fun PremiumStageId.nextStage(): PremiumStageId? {
    return when (this) {
        PremiumStageId.TrialClarity -> PremiumStageId.Promise
        PremiumStageId.Promise -> PremiumStageId.Unlocks
        PremiumStageId.Unlocks -> PremiumStageId.OfferSheet
        PremiumStageId.OfferSheet -> null
        PremiumStageId.GiftReveal -> PremiumStageId.OneTimeOffer
        PremiumStageId.OneTimeOffer -> null
    }
}

@Composable
private fun OnboardingAuthDivider(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.14f))
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = TextStyle(
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.14f))
        )
    }
}

// --- Shooting star overlay (defined early so it's always in scope) ---
@Composable
fun ShootingStarLayer(
    trigger: Int,
    durationMs: Int = 1100,
    headRadiusDp: Float = 2.8f,
    trailCount: Int = 14,
    trailStep: Float = 0.04f,
    trailMaxAlpha: Float = 0.9f,
    starColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    val opacity = remember { Animatable(0f) }
    val active = remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            active.value = true
            progress.snapTo(0f)
            opacity.snapTo(1f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = durationMs,
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1f)
                )
            )
            // Post travel fade-out (slightly slower)
            opacity.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = CubicBezierEasing(0.17f, 0.67f, 0.36f, 1f)
                )
            )
            active.value = false
        }
    }

    if (!active.value && opacity.value <= 0.001f) return

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val oa = opacity.value

            // Extended curved path using a quadratic Bezier
            val start = Offset(-0.30f * w, 0.10f * h)
            val end = Offset(1.30f * w, 0.70f * h)
            val control = Offset(0.50f * w, 0.02f * h)

            fun posAt(t: Float): Offset {
                val tt = t.coerceIn(0f, 1f)
                val u = 1f - tt
                val x = u * u * start.x + 2f * u * tt * control.x + tt * tt * end.x
                val y = u * u * start.y + 2f * u * tt * control.y + tt * tt * end.y
                return Offset(x, y)
            }

            val p = progress.value
            val head = posAt(p)

            fun timeFade(tp: Float): Float {
                val t = tp.coerceIn(0f, 1f)
                return t * t * t
            }
            fun spaceFade(falloff: Float): Float {
                val f = falloff.coerceIn(0f, 1f)
                return f * f
            }

            // Glowing line trail (more apparent)
            val lineSegments = maxOf(6, (trailCount * 3) / 4)
            var prev = posAt((p - trailStep).coerceAtLeast(0f))
            for (i in 0 until lineSegments) {
                val tp = p - i * trailStep
                if (tp <= 0f) break
                val curr = posAt(tp)
                val falloff = 1f - i / lineSegments.toFloat()
                val alpha = (trailMaxAlpha * 0.85f) * timeFade(tp) * spaceFade(falloff) * oa
                if (alpha <= 0.01f) continue
                val stroke = (headRadiusDp * (1.15f + 0.65f * falloff)).dp.toPx()
                drawLine(
                    color = starColor.copy(alpha = alpha),
                    start = curr,
                    end = prev,
                    strokeWidth = stroke
                )
                prev = curr
            }

            // Sparkle dot trail (less pronounced)
            for (i in 0 until trailCount) {
                val tp = p - i * trailStep
                if (tp <= 0f) continue
                val center = posAt(tp)
                val falloff = 1f - i / trailCount.toFloat()
                val alpha = (trailMaxAlpha * 0.30f) * timeFade(tp) * spaceFade(falloff) * oa
                if (alpha <= 0.01f) continue
                val radius = headRadiusDp * (0.55f + 0.25f * falloff)
                drawCircle(
                    color = starColor.copy(alpha = alpha),
                    radius = radius.dp.toPx(),
                    center = center
                )
            }

            // Head glow (brighter)
            val headRadiusPx = headRadiusDp.dp.toPx()
            val glowAlpha = 0.45f * oa
            if (glowAlpha > 0.01f) {
                drawCircle(
                    color = starColor.copy(alpha = glowAlpha),
                    radius = headRadiusPx * 3.0f,
                    center = head
                )
            }
            // Head core (slightly brighter)
            val headAlpha = 1.0f * oa
            if (headAlpha > 0.01f) {
                drawCircle(
                    color = starColor.copy(alpha = headAlpha),
                    radius = headRadiusPx,
                    center = head
                )
                val crossLen = headRadiusPx * 2.2f
                val crossAlpha = 0.50f * oa
                if (crossAlpha > 0.01f) {
                    drawLine(
                        color = starColor.copy(alpha = crossAlpha),
                        start = Offset(head.x - crossLen, head.y),
                        end = Offset(head.x + crossLen, head.y),
                        strokeWidth = headRadiusPx * 0.35f
                    )
                    drawLine(
                        color = starColor.copy(alpha = crossAlpha),
                        start = Offset(head.x, head.y - crossLen),
                        end = Offset(head.x, head.y + crossLen),
                        strokeWidth = headRadiusPx * 0.35f
                    )
                }
            }
        }
    }
}

private data class DispersionEmber(
    val angle: Float,
    val maxDist: Float,
    val size: Float,
    val lifeSpan: Float
)

@Composable
private fun ProfileIntroStarOverlay(
    visible: Boolean,
    targetCenter: Offset?,
    onImpact: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val travelProgress = remember { Animatable(0f) }
    val impactProgress = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(0f) }
    val sequenceStarted = remember { mutableStateOf(false) }
    val impactSent = remember { mutableStateOf(false) }
    val finishSent = remember { mutableStateOf(false) }

    val starCore = Color(0xFFFFFBFF)
    val starWarm = Color(0xFFFFE8C8)
    val starPink = Color(0xFFFFC5EE)
    val starLilac = Color(0xFFD4B2FF)
    val starViolet = Color(0xFF7F63FF)

    val embers = remember {
        val random = Random(888)
        List(8) {
            DispersionEmber(
                angle = random.nextFloat() * 2f * PI.toFloat(),
                maxDist = random.nextFloat() * 80f + 20f,
                size = random.nextFloat() * 4f + 1f,
                lifeSpan = random.nextFloat() * 0.4f + 0.6f
            )
        }
    }

    LaunchedEffect(visible) {
        if (!visible) {
            if (overlayAlpha.value > 0f) {
                overlayAlpha.animateTo(0f, tween(250))
            }
            sequenceStarted.value = false
            return@LaunchedEffect
        }

        if (sequenceStarted.value) return@LaunchedEffect
        sequenceStarted.value = true

        travelProgress.snapTo(0f)
        impactProgress.snapTo(0f)
        overlayAlpha.snapTo(1f)
        impactSent.value = false
        finishSent.value = false

        delay(100)

        travelProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1100,
                easing = CubicBezierEasing(0.3f, 0.0f, 0.4f, 1f)
            )
        )

        if (!impactSent.value) {
            impactSent.value = true
            launch {
                delay(60)
                onImpact()
            }
        }

        impactProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 480,
                easing = FastOutSlowInEasing
            )
        )

        overlayAlpha.animateTo(0f, tween(200))

        if (!finishSent.value) {
            finishSent.value = true
            onFinished()
        }
    }

    if (overlayAlpha.value <= 0.001f) return

    Canvas(modifier = modifier.graphicsLayer(alpha = overlayAlpha.value)) {
        val width = size.width
        val height = size.height
        val tp = travelProgress.value
        val impact = impactProgress.value

        val end = targetCenter ?: Offset(width * 0.52f, height * 0.31f)
        val start = Offset(end.x - width * 0.18f, height + 120.dp.toPx())
        val c1 = Offset(end.x - width * 0.22f, height * 0.82f)
        val c2 = Offset(end.x + width * 0.20f, height * 0.56f)

        fun pointAt(t: Float): Offset {
            val u = 1f - t
            return Offset(
                x = u * u * u * start.x + 3f * u * u * t * c1.x + 3f * u * t * t * c2.x + t * t * t * end.x,
                y = u * u * u * start.y + 3f * u * u * t * c1.y + 3f * u * t * t * c2.y + t * t * t * end.y
            )
        }

        if (tp > 0f && tp < 1f && impact == 0f) {
            val tailWindow = 0.16f
            val segments = 18

            for (i in 1..segments) {
                val t1 = (tp - tailWindow * (i - 1) / segments).coerceAtLeast(0f)
                val t0 = (tp - tailWindow * i / segments).coerceAtLeast(0f)
                if (t1 <= 0f) break

                val a = pointAt(t0)
                val b = pointAt(t1)
                val k = 1f - i / segments.toFloat()

                drawLine(
                    color = starViolet.copy(alpha = 0.10f * k),
                    start = a,
                    end = b,
                    strokeWidth = 24.dp.toPx() * (0.55f + 0.45f * k),
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = starPink.copy(alpha = 0.18f * k),
                    start = a,
                    end = b,
                    strokeWidth = 11.dp.toPx() * (0.65f + 0.35f * k),
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = starCore.copy(alpha = 0.55f * k),
                    start = a,
                    end = b,
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        if (impact > 0f && impact < 1f) {
            val flash = 1f - impact
            val bloomRadius = 14.dp.toPx() + 48.dp.toPx() * impact
            val ringRadius = 10.dp.toPx() + 34.dp.toPx() * impact

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        starCore.copy(alpha = 0.85f * flash),
                        starPink.copy(alpha = 0.30f * flash),
                        Color.Transparent
                    ),
                    center = end,
                    radius = bloomRadius
                ),
                radius = bloomRadius,
                center = end
            )

            drawCircle(
                color = starLilac.copy(alpha = 0.34f * flash),
                radius = ringRadius,
                center = end,
                style = Stroke(width = 4.dp.toPx() * flash.coerceAtLeast(0.2f))
            )

            embers.forEach { ember ->
                val emberT = impact / ember.lifeSpan
                if (emberT <= 1f) {
                    val emberEase = 1f - (1f - emberT) * (1f - emberT)
                    val dx = cos(ember.angle) * ember.maxDist * emberEase
                    val dy = sin(ember.angle) * ember.maxDist * emberEase - (emberEase * height * 0.02f)
                    val sparkPos = Offset(end.x + dx, end.y + dy)
                    val sparkSize = ember.size * (1f - emberT)
                    val sparkAlpha = (1f - emberT) * 0.8f

                    if (sparkAlpha > 0.01f && sparkSize > 0.5f) {
                        drawCircle(
                            color = starWarm.copy(alpha = sparkAlpha),
                            radius = sparkSize,
                            center = sparkPos
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TwinklesLayer(
    twinkleCount: Int = 20,
    seed: Int = 81321,
    modifier: Modifier = Modifier,
    minAlpha: Float = 0.05f,
    maxAlpha: Float = 0.22f,
    // Upward drift: panProgress (0..1) mapped to -(height * driftUpFraction + margin) px
    panProgress: Float = 0f,
    driftUpFraction: Float = 1.2f,
) {
    data class Twinkle(
        val xFrac: Float,
        val yFrac: Float,
        val baseDp: Float,
        val color: Color,
        val phase: Float,
        val speed: Float,
        val glowScale: Float,
        val tiltDeg: Float,
    )

    val palette = remember {
        listOf(
            Color(0xFFB49CFF), // purple
            Color(0xFFFF8FD0), // brighter pink
            Color(0xFFFFD04A)  // more yellow
        )
    }
    val rng = remember(seed) { Random(seed) }
    val twinkles = remember {
        val result = mutableListOf<Twinkle>()
        val minDist = 0.075f // fractional distance for spacing (slightly denser)
        val minDist2 = minDist * minDist
        var attempts = 0
        while (result.size < twinkleCount && attempts < twinkleCount * 30) {
            attempts++
            val x = rng.nextFloat().coerceIn(0.03f, 0.97f)
            val y = rng.nextFloat().coerceIn(0.04f, 0.92f)
            var ok = true
            for (e in result) {
                val dx = x - e.xFrac
                val dy = y - e.yFrac
                if (dx * dx + dy * dy < minDist2) { ok = false; break }
            }
            if (!ok) continue
            result += Twinkle(
                xFrac = x,
                yFrac = y,
                baseDp = (0.9f + rng.nextFloat() * 1.3f),
                color = palette[rng.nextInt(palette.size)],
                phase = rng.nextFloat(), // 0..1
                speed = 0.6f + rng.nextFloat() * 1.0f, // 0.6..1.6x
                glowScale = 1.6f + rng.nextFloat() * 0.9f, // 1.6..2.5 (softer/bigger)
                tiltDeg = -12f + rng.nextFloat() * 24f // -12..+12 deg slight tilt
            )
        }
        if (result.isEmpty()) {
            // fallback if spacing rejects everything for some reason
            List(twinkleCount) {
                Twinkle(
                    xFrac = rng.nextFloat().coerceIn(0.03f, 0.97f),
                    yFrac = rng.nextFloat().coerceIn(0.04f, 0.92f),
                    baseDp = (0.9f + rng.nextFloat() * 1.3f),
                    color = palette[rng.nextInt(palette.size)],
                    phase = rng.nextFloat(),
                    speed = 0.6f + rng.nextFloat() * 1.0f,
                    glowScale = 1.6f + rng.nextFloat() * 0.9f,
                    tiltDeg = -12f + rng.nextFloat() * 24f
                )
            }
        } else result
    }

    fun easeInOutCubic(t: Float): Float = if (t < 0.5f) 4f * t * t * t else 1f - (-2f * t + 2f).let { it * it * it } / 2f

    val time by rememberInfiniteTransition(label = "twinkles_time").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle_time_anim"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Linear mapping so all move at the same rate; add margin to ensure fully off-screen
        val marginPx = 24.dp.toPx()
        val yDrift = -panProgress.coerceIn(0f, 1f) * (h * driftUpFraction + marginPx)

        fun curvedDiamondPath(cx: Float, cy: Float, halfW: Float, halfH: Float, curveIn: Float): Path {
            // curveIn: 0..1, how much sides curve inward towards center
            val path = Path()
            val top = Offset(cx, cy - halfH)
            val right = Offset(cx + halfW, cy)
            val bottom = Offset(cx, cy + halfH)
            val left = Offset(cx - halfW, cy)
            val c = Offset(cx, cy)
            // Control points slightly towards center and along edges
            fun lerp(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
            val tMid = 0.55f // where along the edge the control point sits
            val inward = curveIn.coerceIn(0f, 1f)

            val trCtrl = lerp(lerp(top, right, tMid), c, inward)
            val rbCtrl = lerp(lerp(right, bottom, tMid), c, inward)
            val blCtrl = lerp(lerp(bottom, left, tMid), c, inward)
            val ltCtrl = lerp(lerp(left, top, tMid), c, inward)

            path.moveTo(top.x, top.y)
            path.quadraticTo(trCtrl.x, trCtrl.y, right.x, right.y)
            path.quadraticTo(rbCtrl.x, rbCtrl.y, bottom.x, bottom.y)
            path.quadraticTo(blCtrl.x, blCtrl.y, left.x, left.y)
            path.quadraticTo(ltCtrl.x, ltCtrl.y, top.x, top.y)
            path.close()
            return path
        }

        for (t in twinkles) {
            val localT = ((time * t.speed) + t.phase) % 1f
            val eased = easeInOutCubic(localT)
            val pulse = (0.5f * (1f - cos(2f * PI.toFloat() * eased))).coerceIn(0f, 1f)
            val alpha = (minAlpha + (maxAlpha - minAlpha) * pulse).coerceIn(0f, 1f)
            val cx = t.xFrac * w
            val cy = t.yFrac * h + yDrift
            val base = t.baseDp.dp.toPx()

            // Make it less thick: height > width
            val halfHCore = base * 2.6f
            val halfWCore = halfHCore * 0.42f // slightly thinner
            val curveIn = 0.50f // a bit deeper inward curve

            val glowScale = t.glowScale
            val halfHGlow = halfHCore * glowScale
            val halfWGlow = halfWCore * glowScale

            rotate(degrees = t.tiltDeg, pivot = Offset(cx, cy)) {
                val glowPath = curvedDiamondPath(cx, cy, halfWGlow, halfHGlow, curveIn)
                drawPath(
                    path = glowPath,
                    color = t.color.copy(alpha = alpha * 0.26f)
                )
                val corePath = curvedDiamondPath(cx, cy, halfWCore, halfHCore, curveIn)
                drawPath(
                    path = corePath,
                    color = t.color.copy(alpha = alpha)
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@ExperimentalAnimationApi
@Composable
fun OnboardingScreen(
    loginViewModelState: LoginViewModelState,
    signupViewModelState: SignupViewModelState,
    premiumEntrySource: PremiumEntrySource? = null,
    forceOnboarding: Boolean = false,
    showAuthImmediately: Boolean = false,
    navigateToDreamJournalScreen: () -> Unit,
    navigateToDreamJournalScreenAfterOnboardingExit: () -> Unit = navigateToDreamJournalScreen,
    debugStartAtLastPage: Boolean = false,
    onDismissPremiumFlow: () -> Unit = navigateToDreamJournalScreen,
    onLoginEvent: (LoginEvent) -> Unit,
    onSignupEvent: (SignupEvent) -> Unit,
    onDataLoaded: () -> Unit,
    requestInAppReview: () -> Unit = {},
) {
    val vibratorUtil = koinInject<VibratorUtil>()
    val onboardingAnalytics = koinInject<org.ballistic.dreamjournalai.shared.dream_onboarding.domain.OnboardingAnalytics>()
    val onboardingPreferences = koinInject<org.ballistic.dreamjournalai.shared.dream_onboarding.data.OnboardingPreferencesRepository>()
    val premiumPaywallRepository = koinInject<PremiumPaywallRepository>()
    val premiumAnalytics = koinInject<PremiumAnalytics>()
    val appAnalytics = koinInject<AppAnalytics>()
    val dreamUseCases = koinInject<DreamUseCases>()
    val premiumTrialReminderScheduler = koinInject<PremiumTrialReminderScheduler>()

    val isUserAnonymous = loginViewModelState.isUserAnonymous
    val isUserLoggedIn = loginViewModelState.isLoggedIn
    val isLoading = loginViewModelState.isLoading || signupViewModelState.isLoading
    val hasAuthenticatedUser = isUserLoggedIn &&
        (isUserAnonymous || loginViewModelState.isEmailVerified)
    val shouldStartAfterAuth = !showAuthImmediately && hasAuthenticatedUser
    val notificationPermissionState = rememberNotificationPermissionState()

    var currentStep by remember {
        mutableStateOf(
            if (showAuthImmediately) {
                OnboardingFlowStep.Auth
            } else if (premiumEntrySource != null || shouldStartAfterAuth) {
                OnboardingFlowStep.DreamOutcome
            } else if (debugStartAtLastPage) {
                OnboardingFlowStep.PremiumIntro
            } else {
                OnboardingFlowStep.Auth
            }
        )
    }
    var answers by remember { mutableStateOf(OnboardingAnswers()) }
    val derivedPlan = remember(answers) { deriveOnboardingPlan(answers) }

    var authReturnStep by remember { mutableStateOf(OnboardingFlowStep.DreamOutcome) }
    var authGateCompleted by remember { mutableStateOf(premiumEntrySource != null || shouldStartAfterAuth) }
    var authCompletionMode by remember {
        mutableStateOf<String?>(
            if (shouldStartAfterAuth) {
                if (isUserAnonymous) "basic_mode" else "free_plan"
            } else {
                null
            }
        )
    }
    var completionMode by remember { mutableStateOf<String?>(null) }
    var premiumModel by remember { mutableStateOf<PremiumPaywallModel?>(null) }
    var selectedPackageId by remember { mutableStateOf<String?>(null) }
    var selectedPremiumPlan by remember { mutableStateOf(PremiumPlanOption.Annual) }
    var premiumLoading by remember { mutableStateOf(false) }
    var handledAuthToken by remember { mutableStateOf<String?>(null) }
    var persistedOnboardingStartUserId by remember { mutableStateOf<String?>(null) }
    var guestAuthHandoffStarted by remember { mutableStateOf(false) }
    var authExitStarted by remember { mutableStateOf(false) }
    var attemptedDirectPremiumEntry by remember { mutableStateOf(false) }
    var premiumOpenedFromOnboardingIntro by remember { mutableStateOf(false) }
    var outcomeHeroCenter by remember { mutableStateOf<Offset?>(null) }
    val outcomeHeroRevealStarted = remember { mutableStateOf(false) }
    val outcomeContentRevealStarted = remember { mutableStateOf(false) }
    val outcomeOrbVisible = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val authStepAlpha by animateFloatAsState(
        targetValue = if (authExitStarted || guestAuthHandoffStarted) 0f else 1f,
        animationSpec = tween(durationMillis = ONBOARDING_AUTH_EXIT_MS, easing = FastOutSlowInEasing),
        label = "onboarding-auth-exit-alpha"
    )
    val cameraBiasY = remember { Animatable(-1f) }
    val cameraScale = remember { Animatable(1f) }
    val masterOverlayAlpha = remember { Animatable(1f) }
    val isPanningDown = remember { mutableStateOf(false) }
    val exitTransitionStarted = remember { mutableStateOf(false) }
    val screenEnteredAtMs = remember { mutableStateOf(kotlin.time.Clock.System.now().toEpochMilliseconds()) }
    val onboardingStartedAtMs = remember { mutableStateOf(kotlin.time.Clock.System.now().toEpochMilliseconds()) }
    val previousStep = remember { mutableStateOf(currentStep) }
    val premiumScreenEnteredAtMs = remember { mutableStateOf<Long?>(null) }
    val previousPremiumPage = remember { mutableStateOf<PremiumPageKind?>(null) }

    fun trackSelection(field: String, value: String) {
        onboardingAnalytics.track(
            org.ballistic.dreamjournalai.shared.dream_onboarding.domain.OnboardingAnalyticsEvent.SelectionMade(
                step = currentStep,
                field = field,
                value = value
            )
        )
    }

    fun trackCta(label: String) {
        onboardingAnalytics.track(
            org.ballistic.dreamjournalai.shared.dream_onboarding.domain.OnboardingAnalyticsEvent.CtaTapped(
                step = currentStep,
                ctaLabel = label
            )
        )
    }

    fun dispatchAndroidReviewPrompt(triggerStep: OnboardingFlowStep) {
        if (getPlatformName() == "iOS") return
        onboardingAnalytics.track(
            org.ballistic.dreamjournalai.shared.dream_onboarding.domain.OnboardingAnalyticsEvent.ReviewPromptShown(
                triggerStep = triggerStep
            )
        )
        requestInAppReview()
    }

    fun Int?.recallBucket(): String {
        return when (this) {
            null -> "unset"
            0 -> "0"
            in 1..2 -> "1_2"
            in 3..5 -> "3_5"
            else -> "6_7"
        }
    }

    fun dreamStateSnapshot(dreams: List<Dream>): Map<String, Any> {
        val dreamCount = dreams.size
        return mapOf(
            "dream_count" to dreamCount,
            "dream_count_bucket" to countBucket(dreamCount),
            "has_saved_dream" to dreams.isNotEmpty(),
            "ai_interpretation_count" to dreams.count { it.AIResponse.isNotBlank() },
            "ai_art_count" to dreams.count { it.generatedImage.isNotBlank() },
            "audio_dream_count" to dreams.count { it.audioDuration > 0 || it.audioUrl.isNotBlank() },
        )
    }

    suspend fun buildOnboardingSnapshot(
        conversionSource: String,
        model: PremiumPaywallModel? = null,
        packageModel: PremiumPackageModel? = null,
    ): Map<String, Any?> {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val durationMs = now - onboardingStartedAtMs.value
        val dreams = runCatching { dreamUseCases.getDreams().first() }.getOrDefault(emptyList())

        return mapOf(
            "conversion_source" to conversionSource,
            "surface" to "onboarding",
            "onboarding_duration_ms" to durationMs.coerceAtLeast(0),
            "onboarding_duration_bucket" to durationBucket(durationMs),
        ) + dreamStateSnapshot(dreams) + mapOf(
            "premium_entry_source" to (model?.entrySource ?: premiumEntrySource)?.analyticsValue(),
            "premium_placement" to model?.placement?.placementId,
            "offering_id" to model?.offering?.identifier,
            "selected_plan" to selectedPremiumPlan.analyticsValue(),
            "selected_package_id" to packageModel?.packageToPurchase?.identifier,
            "has_trial" to packageModel?.hasTrial,
            "near_goal_primary" to answers.primaryNearGoal?.analyticsValue(),
            "near_goal_secondary" to answers.nearGoals.getOrNull(1)?.analyticsValue(),
            "far_goal" to answers.farGoal?.analyticsValue(),
            "recall_days_per_week" to answers.recallDaysPerWeek,
            "recall_bucket" to answers.recallDaysPerWeek.recallBucket(),
            "main_blocker" to answers.mainBlocker?.analyticsValue(),
            "demo_theme" to answers.demoTheme?.analyticsValue(),
            "demo_mood" to answers.demoMood?.analyticsValue(),
            "commitment" to answers.commitment?.analyticsValue(),
            "started_time_of_day" to answers.startedAtTimeOfDay.analyticsValue(),
        )
    }

    fun beginExitToDreamJournal() {
        if (exitTransitionStarted.value) return
        exitTransitionStarted.value = true
        scope.launch {
            completionMode?.let { mode ->
                onboardingPreferences.markCompleted(mode)
                onboardingAnalytics.track(
                    org.ballistic.dreamjournalai.shared.dream_onboarding.domain.OnboardingAnalyticsEvent.OnboardingCompleted(
                        completionMode = mode,
                        properties = buildOnboardingSnapshot(
                            conversionSource = "onboarding_complete"
                        )
                    )
                )
            }

            isPanningDown.value = true
            masterOverlayAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 320, easing = LinearEasing)
            )
            val pan = launch {
                cameraBiasY.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 2200,
                        easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
                    )
                )
            }
            val zoom = launch {
                cameraScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 2200,
                        easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
                    )
                )
            }
            pan.join()
            zoom.join()
            navigateToDreamJournalScreenAfterOnboardingExit()
        }
    }

    fun finishPremiumFlow() {
        if (premiumEntrySource != null && !premiumOpenedFromOnboardingIntro) {
            if (exitTransitionStarted.value) return
            exitTransitionStarted.value = true
            onDismissPremiumFlow()
        } else {
            beginExitToDreamJournal()
        }
    }

    suspend fun showPremiumSnackbar(message: String) {
        snackbarHostState.showSnackbar(message)
    }

    fun resetPremiumSelection(model: PremiumPaywallModel) {
        val defaultPlan = model.defaultPlanOption()
        selectedPremiumPlan = when (defaultPlan) {
            PremiumPlanOption.Monthly -> if (model.packages.any { it.isMonthly }) {
                PremiumPlanOption.Monthly
            } else {
                PremiumPlanOption.Annual
            }
            PremiumPlanOption.Annual -> if (model.packages.any { it.isAnnual }) {
                PremiumPlanOption.Annual
            } else {
                PremiumPlanOption.Monthly
            }
        }
        selectedPackageId = model.packageForPlan(selectedPremiumPlan)
            ?.packageToPurchase
            ?.identifier
    }

    suspend fun loadPrimaryPremiumFlow(entrySource: PremiumEntrySource) {
        val currentUser = Firebase.auth.currentUser ?: run {
            completionMode = "free_plan"
            finishPremiumFlow()
            return
        }

        val customerInfo = runCatching {
            premiumPaywallRepository.syncAppUser(currentUser.uid)
            premiumPaywallRepository.getCurrentCustomerInfo()
        }.getOrElse {
            completionMode = "free_plan"
            finishPremiumFlow()
            return
        }
        if (premiumPaywallRepository.hasPremiumEntitlement(customerInfo)) {
            completionMode = "premium_unlocked"
            finishPremiumFlow()
            return
        }

        val offering = runCatching {
            premiumPaywallRepository.getPlacementOffering(PremiumPlacement.PostAuthPrimary.placementId)
        }.getOrNull()
        if (offering == null) {
            premiumAnalytics.track(PremiumAnalyticsEvent.OfferingMissing(PremiumPlacement.PostAuthPrimary))
            completionMode = "free_plan"
            finishPremiumFlow()
            return
        }

        val model = offering.toPremiumPaywallModel(
            placement = PremiumPlacement.PostAuthPrimary,
            answers = answers,
            entrySource = entrySource,
        )
        if (model.packages.isEmpty()) {
            premiumAnalytics.track(PremiumAnalyticsEvent.OfferingMissing(PremiumPlacement.PostAuthPrimary))
            completionMode = "free_plan"
            finishPremiumFlow()
            return
        }

        premiumModel = model
        resetPremiumSelection(model)
        premiumAnalytics.track(
            PremiumAnalyticsEvent.PlacementShown(
                placement = PremiumPlacement.PostAuthPrimary,
                offeringId = offering.identifier,
                entrySource = entrySource
            )
        )
        currentStep = initialStage(entrySource.toPremiumEntryMode()).toOnboardingFlowStep()
    }

    suspend fun purchasePremium(
        page: PremiumPageKind,
        model: PremiumPaywallModel?,
        packageId: String?,
    ) {
        val selectedPackage = model?.packages?.firstOrNull { it.packageToPurchase.identifier == packageId }
        if (selectedPackage == null) {
            showPremiumSnackbar("Choose a plan first.")
            return
        }

        premiumLoading = true
        premiumAnalytics.track(
            PremiumAnalyticsEvent.PurchaseStarted(
                page = page,
                packageId = selectedPackage.packageToPurchase.identifier
            )
        )

        when (val result = premiumPaywallRepository.purchasePackage(selectedPackage.packageToPurchase)) {
            is PremiumPurchaseResult.Success -> {
                premiumLoading = false
                val conversionSnapshot = buildOnboardingSnapshot(
                    conversionSource = "onboarding_premium_purchase",
                    model = model,
                    packageModel = selectedPackage
                )
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.PurchaseSucceeded(
                        page = page,
                        packageId = result.packageId,
                        properties = conversionSnapshot
                    )
                )
                appAnalytics.setUserProperty(AnalyticsUserProperty.PremiumStatus, "active")
                syncPremiumTrialReminder(selectedPackage, premiumTrialReminderScheduler)
                completionMode = "premium_unlocked"
                finishPremiumFlow()
            }

            PremiumPurchaseResult.AlreadySubscribed -> {
                premiumLoading = false
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.PurchaseAlreadyActive(
                        page = page,
                        packageId = selectedPackage.packageToPurchase.identifier
                    )
                )
                appAnalytics.setUserProperty(AnalyticsUserProperty.PremiumStatus, "active")
                premiumTrialReminderScheduler.cancelTrialEndingReminder()
                completionMode = "premium_unlocked"
                finishPremiumFlow()
            }

            PremiumPurchaseResult.Pending -> {
                premiumLoading = false
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.PurchasePending(
                        page = page,
                        packageId = selectedPackage.packageToPurchase.identifier
                    )
                )
                showPremiumSnackbar("Your purchase is pending. We'll unlock premium as soon as it clears.")
            }

            PremiumPurchaseResult.UserCancelled -> {
                premiumLoading = false
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.PurchaseCancelled(
                        page = page,
                        packageId = selectedPackage.packageToPurchase.identifier
                    )
                )
                showPremiumSnackbar("Purchase canceled. You can choose a plan when you're ready.")
            }

            is PremiumPurchaseResult.Error -> {
                premiumLoading = false
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.PurchaseFailed(
                        page = page,
                        packageId = selectedPackage.packageToPurchase.identifier,
                        message = result.message
                    )
                )
                showPremiumSnackbar(result.message)
            }
        }
    }

    suspend fun restorePremiumPurchases() {
        premiumLoading = true
        premiumAnalytics.track(PremiumAnalyticsEvent.RestoreTapped)
        runCatching {
            premiumPaywallRepository.restorePurchases()
        }.onSuccess { customerInfo ->
            premiumLoading = false
            val premiumActive = premiumPaywallRepository.hasPremiumEntitlement(customerInfo)
            premiumAnalytics.track(PremiumAnalyticsEvent.RestoreCompleted(premiumActive))
            if (premiumActive) {
                premiumTrialReminderScheduler.cancelTrialEndingReminder()
                completionMode = "premium_unlocked"
                finishPremiumFlow()
            } else {
                showPremiumSnackbar("No premium purchase was found to restore.")
            }
        }.onFailure { error ->
            premiumLoading = false
            premiumAnalytics.track(
                PremiumAnalyticsEvent.RestoreFailed(
                    message = error.message ?: "unknown"
                )
            )
            showPremiumSnackbar(error.message ?: "Unable to restore purchases right now.")
        }
    }

    fun dismissPremiumFlow(page: PremiumPageKind) {
        vibratorUtil.triggerVibration()
        premiumAnalytics.track(PremiumAnalyticsEvent.PaywallDismissed(page))
        if (premiumOpenedFromOnboardingIntro) {
            premiumOpenedFromOnboardingIntro = false
            currentStep = OnboardingFlowStep.PremiumIntro
        } else {
            completionMode = "free_plan"
            finishPremiumFlow()
        }
    }

    BackHandler(
        isEnabled = premiumEntrySource != null && currentStep.toPremiumPageKind() != null
    ) {
        val page = currentStep.toPremiumPageKind() ?: return@BackHandler
        dismissPremiumFlow(page)
    }

    LaunchedEffect(Unit) {
        cameraBiasY.snapTo(-1f)
        cameraScale.snapTo(1f)
        masterOverlayAlpha.snapTo(1f)
        if (showAuthImmediately) {
            onDataLoaded()
            onLoginEvent(LoginEvent.BeginAuthStateListener)
            onLoginEvent(LoginEvent.ShowSignUpLayout)
            outcomeOrbVisible.value = true
            onboardingAnalytics.track(
                org.ballistic.dreamjournalai.shared.dream_onboarding.domain.OnboardingAnalyticsEvent.ScreenViewed(
                    step = currentStep
                )
            )
            return@LaunchedEffect
        }
        delay(900)
        onDataLoaded()
        onLoginEvent(LoginEvent.BeginAuthStateListener)
        if (!authGateCompleted && premiumEntrySource == null) {
            onLoginEvent(LoginEvent.ShowSignUpLayout)
        }
        outcomeOrbVisible.value = true
        onboardingAnalytics.track(
            org.ballistic.dreamjournalai.shared.dream_onboarding.domain.OnboardingAnalyticsEvent.ScreenViewed(
                step = currentStep
            )
        )
    }

    LaunchedEffect(hasAuthenticatedUser, currentStep, premiumEntrySource, forceOnboarding) {
        if (!hasAuthenticatedUser) return@LaunchedEffect
        if (currentStep == OnboardingFlowStep.Auth) return@LaunchedEffect
        if (premiumEntrySource != null || forceOnboarding) return@LaunchedEffect

        val currentUser = Firebase.auth.currentUser ?: return@LaunchedEffect
        if (persistedOnboardingStartUserId == currentUser.uid) return@LaunchedEffect

        if (!onboardingPreferences.hasCompletedOnboardingForCurrentUser()) {
            onboardingPreferences.markStartedForCurrentUser()
        }
        persistedOnboardingStartUserId = currentUser.uid
    }

    LaunchedEffect(
        guestAuthHandoffStarted,
        signupViewModelState.isLoading,
        signupViewModelState.error,
        isUserLoggedIn,
    ) {
        if (!guestAuthHandoffStarted) return@LaunchedEffect
        if (isUserLoggedIn || signupViewModelState.isLoading) return@LaunchedEffect
        if (signupViewModelState.error != StringValue.Empty) {
            guestAuthHandoffStarted = false
        }
    }

    LaunchedEffect(currentStep) {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        if (previousStep.value != currentStep) {
            if (currentStep != OnboardingFlowStep.DreamOutcome) {
                outcomeHeroRevealStarted.value = false
                outcomeContentRevealStarted.value = false
            }
            onboardingAnalytics.track(
                org.ballistic.dreamjournalai.shared.dream_onboarding.domain.OnboardingAnalyticsEvent.ScreenDwellRecorded(
                    step = previousStep.value,
                    durationMs = now - screenEnteredAtMs.value
                )
            )
            screenEnteredAtMs.value = now
            previousStep.value = currentStep
            onboardingAnalytics.track(
                org.ballistic.dreamjournalai.shared.dream_onboarding.domain.OnboardingAnalyticsEvent.ScreenViewed(
                    step = currentStep
                )
            )
        }

        val premiumPage = currentStep.toPremiumPageKind()
        if (previousPremiumPage.value != premiumPage) {
            previousPremiumPage.value?.let { previousPage ->
                premiumScreenEnteredAtMs.value?.let { enteredAt ->
                    premiumAnalytics.track(
                        PremiumAnalyticsEvent.DwellRecorded(
                            page = previousPage,
                            durationMs = now - enteredAt
                        )
                    )
                }
            }

            if (premiumPage != null) {
                premiumScreenEnteredAtMs.value = now
                previousPremiumPage.value = premiumPage
                premiumAnalytics.track(PremiumAnalyticsEvent.PageViewed(premiumPage))
            } else {
                previousPremiumPage.value = null
                premiumScreenEnteredAtMs.value = null
            }
        }
    }

    LaunchedEffect(currentStep, isUserLoggedIn, loginViewModelState.isEmailVerified, isUserAnonymous, authReturnStep, forceOnboarding) {
        if (currentStep != OnboardingFlowStep.Auth) {
            handledAuthToken = null
            return@LaunchedEffect
        }

        val currentUser = Firebase.auth.currentUser ?: run {
            handledAuthToken = null
            authExitStarted = false
            guestAuthHandoffStarted = false
            return@LaunchedEffect
        }
        appAnalytics.setUserId(currentUser.uid)
        appAnalytics.setUserProperty(
            AnalyticsUserProperty.AccountType,
            if (currentUser.isAnonymous) "anonymous" else "registered"
        )
        val authToken = "${currentUser.uid}:${currentUser.isAnonymous}:${currentUser.isEmailVerified}:$authReturnStep"
        if (handledAuthToken == authToken) return@LaunchedEffect
        handledAuthToken = authToken

        if (!currentUser.isAnonymous && currentUser.isEmailVerified != true) {
            return@LaunchedEffect
        }
        if (authExitStarted) return@LaunchedEffect

        if (isCurrentIosAppleUser() && answers.firstName.isBlank()) {
            answers = answers.copy(firstName = currentUserFirstNameOrBlank())
        }

        if (!forceOnboarding && onboardingPreferences.hasCompletedOnboardingForCurrentUser()) {
            navigateToDreamJournalScreen()
            return@LaunchedEffect
        }

        onboardingPreferences.markStartedForCurrentUser()
        persistedOnboardingStartUserId = currentUser.uid

        authExitStarted = true
        delay(ONBOARDING_AUTH_EXIT_MS.toLong())

        if (currentUser.isAnonymous) {
            authCompletionMode = "basic_mode"
            onboardingAnalytics.track(
                org.ballistic.dreamjournalai.shared.dream_onboarding.domain.OnboardingAnalyticsEvent.BasicModeStarted(
                    entryPoint = "auth_guest"
                )
            )
        } else {
            authCompletionMode = "free_plan"
        }

        authGateCompleted = true
        currentStep = OnboardingFlowStep.DreamOutcome
    }

    LaunchedEffect(premiumEntrySource, currentStep) {
        val entrySource = premiumEntrySource ?: return@LaunchedEffect
        if (attemptedDirectPremiumEntry) return@LaunchedEffect
        if (currentStep != OnboardingFlowStep.DreamOutcome) return@LaunchedEffect

        val currentUser = Firebase.auth.currentUser ?: return@LaunchedEffect
        if (currentUser.isAnonymous) return@LaunchedEffect

        attemptedDirectPremiumEntry = true
        loadPrimaryPremiumFlow(entrySource)
    }

    val pageContainerModifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(start = 10.dp, top = 10.dp, end = 10.dp)
        .widthIn(max = 500.dp)

    Box(modifier = Modifier.fillMaxSize()) {
        val panProgress = ((cameraBiasY.value + 1f) / 2f).coerceIn(0f, 1f)
        OnboardingPageBackground(
            cameraScale = cameraScale.value,
            cameraBiasY = cameraBiasY.value,
            panProgress = panProgress,
            shootingStarTrigger = 0,
            overlayAlpha = masterOverlayAlpha.value,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(top = paddingValues.calculateTopPadding())
                    .fillMaxSize()
                    .graphicsLayer(alpha = masterOverlayAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (initialState == OnboardingFlowStep.DreamOutcome && targetState == OnboardingFlowStep.DreamFrictionIntro) {
                            (fadeIn(animationSpec = tween(560, easing = FastOutSlowInEasing)) +
                                scaleIn(
                                    initialScale = 0.98f,
                                    animationSpec = tween(560, easing = FastOutSlowInEasing)
                                )) togetherWith fadeOut(
                                animationSpec = tween(220, easing = FastOutSlowInEasing)
                            )
                        } else {
                            (fadeIn(animationSpec = tween(540, delayMillis = 40, easing = FastOutSlowInEasing)) +
                                slideInHorizontally(
                                    animationSpec = tween(540, easing = FastOutSlowInEasing),
                                    initialOffsetX = { it / 10 }
                                )) togetherWith
                                (fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                    slideOutHorizontally(
                                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                                        targetOffsetX = { -it / 12 }
                                    ))
                        }
                    },
                    label = "emotional_onboarding_flow"
                ) { step ->
                    when (step) {
                        OnboardingFlowStep.DreamOutcome -> {
                            OnboardingDreamOutcomePage(
                                startAnimation = outcomeHeroRevealStarted.value,
                                contentAnimationStarted = outcomeContentRevealStarted.value,
                                onHeroCenterChanged = { outcomeHeroCenter = it },
                                onPrimaryClick = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Start my dream path")
                                    currentStep = OnboardingFlowStep.DreamFrictionIntro
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.DreamFrictionIntro -> {
                            OnboardingDreamFrictionIntroPage(
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Build my ritual")
                                    if (isCurrentIosAppleUser()) {
                                        if (answers.firstName.isBlank()) {
                                            answers = answers.copy(firstName = currentUserFirstNameOrBlank())
                                        }
                                        currentStep = OnboardingFlowStep.NearOutcome
                                    } else {
                                        currentStep = OnboardingFlowStep.Name
                                    }
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.Name -> {
                            OnboardingNamePage(
                                firstName = answers.firstName,
                                onNameChanged = { answers = answers.copy(firstName = it) },
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Continue")
                                    currentStep = OnboardingFlowStep.NearOutcome
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.NearOutcome -> {
                            OnboardingNearOutcomePage(
                                firstName = answers.firstName,
                                selectedGoals = answers.nearGoals,
                                onToggleGoal = { goal ->
                                    vibratorUtil.triggerVibration()
                                    val updated = if (answers.nearGoals.contains(goal)) {
                                        answers.nearGoals - goal
                                    } else {
                                        (answers.nearGoals + goal).take(2)
                                    }
                                    answers = answers.copy(nearGoals = updated)
                                    trackSelection("near_goal", goal.title)
                                },
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Next")
                                    currentStep = OnboardingFlowStep.FarOutcome
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.FarOutcome -> {
                            OnboardingFarOutcomePage(
                                selectedGoal = answers.farGoal,
                                onGoalSelected = {
                                    vibratorUtil.triggerVibration()
                                    answers = answers.copy(farGoal = it)
                                    trackSelection("far_goal", it.title)
                                },
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Keep going")
                                    currentStep = OnboardingFlowStep.Recall
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.Recall -> {
                            OnboardingRecallPage(
                                recallDaysPerWeek = answers.recallDaysPerWeek,
                                selectedBlocker = answers.mainBlocker,
                                onRecallChanged = {
                                    answers = answers.copy(recallDaysPerWeek = it)
                                    trackSelection("recall_days_per_week", it.toString())
                                },
                                onBlockerSelected = {
                                    vibratorUtil.triggerVibration()
                                    answers = answers.copy(mainBlocker = it)
                                    trackSelection("main_blocker", it.title)
                                },
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("That's me")
                                    currentStep = OnboardingFlowStep.DreamMath
                                },
                                onValidationError = {
                                    vibratorUtil.triggerVibration()
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.DreamMath -> {
                            OnboardingDreamMathPage(
                                firstName = answers.firstName,
                                recallDaysPerWeek = answers.recallDaysPerWeek,
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Show me what I'm missing")
                                    currentStep = OnboardingFlowStep.Snapshot
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.Snapshot -> {
                            OnboardingSnapshotPage(
                                firstName = answers.firstName,
                                cards = derivedPlan.snapshotCards,
                                lostDreamsEstimate = derivedPlan.lostDreamsEstimate,
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Show me the bridge")
                                    currentStep = OnboardingFlowStep.PlanBridge
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.PlanBridge -> {
                            OnboardingPlanBridgePage(
                                firstName = answers.firstName,
                                insight = derivedPlan.personalizedInsight,
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Try the ritual")
                                    currentStep = OnboardingFlowStep.DreamCheckIn
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.DreamCheckIn -> {
                            OnboardingDreamCheckInPage(
                                selectedTheme = answers.demoTheme,
                                selectedMood = answers.demoMood,
                                onThemeSelected = {
                                    vibratorUtil.triggerVibration()
                                    answers = answers.copy(demoTheme = it)
                                    trackSelection("demo_theme", it.title)
                                },
                                onMoodSelected = {
                                    vibratorUtil.triggerVibration()
                                    answers = answers.copy(demoMood = it)
                                    trackSelection("demo_mood", it.title)
                                },
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Reveal my first insight")
                                    currentStep = OnboardingFlowStep.FirstInsight
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.FirstInsight -> {
                            OnboardingFirstInsightPage(
                                firstName = answers.firstName,
                                reflection = derivedPlan.demoReflection,
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Save my first insight")
                                    currentStep = OnboardingFlowStep.FirstStreak
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.FirstStreak -> {
                            OnboardingFirstStreakPage(
                                firstName = answers.firstName,
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Keep building my path")
                                    currentStep = OnboardingFlowStep.ReviewStories
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.ReviewStories -> {
                            OnboardingReviewStoriesPage(
                                currentStep = OnboardingFlowStep.ReviewStories.progressStep,
                                totalSteps = OnboardingFlowStep.ReviewStories.totalSteps,
                                onReachedReviewPromptMoment = {
                                    dispatchAndroidReviewPrompt(OnboardingFlowStep.ReviewStories)
                                },
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Continue from reviews")
                                    currentStep = OnboardingFlowStep.Commitment
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.Commitment -> {
                            OnboardingCommitmentPage(
                                selectedCommitment = answers.commitment,
                                onCommitmentSelected = {
                                    vibratorUtil.triggerVibration()
                                    answers = answers.copy(commitment = it)
                                    trackSelection("commitment", it.title)
                                },
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("I commit to my dream path")
                                    currentStep = OnboardingFlowStep.FirstSevenNights
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.FirstSevenNights -> {
                            OnboardingSevenNightsPage(
                                firstName = answers.firstName,
                                subheadline = derivedPlan.roadmapSubheadline,
                                milestones = derivedPlan.roadmapMilestones,
                                onContinue = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Continue to social proof")
                                    currentStep = OnboardingFlowStep.SocialProof
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.SocialProof -> {
                            OnboardingSocialProofPage(
                                firstName = answers.firstName,
                                nearGoalLabel = answers.primaryNearGoal?.title?.lowercase() ?: "dream clarity",
                                farGoalLabel = answers.farGoal?.title?.lowercase() ?: "clearer self-understanding",
	                                onContinue = {
	                                    vibratorUtil.triggerVibration()
	                                    trackCta("Save my dream path")
	                                    currentStep = if (notificationPermissionState.shouldShowPrompt) {
	                                        OnboardingFlowStep.NotificationPermission
	                                    } else {
	                                        OnboardingFlowStep.PremiumIntro
	                                    }
	                                },
	                                modifier = pageContainerModifier
	                            )
	                        }

	                        OnboardingFlowStep.NotificationPermission -> {
	                            OnboardingNotificationPermissionPage(
	                                onEnableNotifications = {
	                                    vibratorUtil.triggerVibration()
	                                    trackCta("Enable notifications")
	                                    notificationPermissionState.requestPermission()
	                                    currentStep = OnboardingFlowStep.PremiumIntro
	                                },
	                                onLater = {
	                                    vibratorUtil.triggerVibration()
	                                    trackCta("Do later notifications")
	                                    currentStep = OnboardingFlowStep.PremiumIntro
	                                },
	                                modifier = pageContainerModifier
	                            )
	                        }

	                        OnboardingFlowStep.PremiumIntro -> {
                            OnboardingPremiumIntroPage(
                                onSeePremium = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("See Premium")
                                    premiumOpenedFromOnboardingIntro = true
                                    scope.launch {
                                        loadPrimaryPremiumFlow(PremiumEntrySource.OnboardingIntro)
                                    }
                                },
                                onNotYet = {
                                    vibratorUtil.triggerVibration()
                                    trackCta("Not yet")
                                    completionMode = authCompletionMode
                                        ?: if (Firebase.auth.currentUser?.isAnonymous == true || isUserAnonymous) {
                                            "basic_mode"
                                        } else {
                                            "free_plan"
                                        }
                                    beginExitToDreamJournal()
                                },
                                modifier = pageContainerModifier
                            )
                        }

                        OnboardingFlowStep.Auth -> {
                            val authScrollState = rememberScrollState()
                            Column(
                                modifier = pageContainerModifier
                                    .fillMaxSize()
                                    .imePadding()
                                    .verticalScroll(authScrollState)
                                    .padding(bottom = 32.dp)
                                    .graphicsLayer { alpha = authStepAlpha },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                OnboardingAuthCard(
                                    enteredName = answers.firstName,
                                    loginViewModelState = loginViewModelState,
                                    signupViewModelState = signupViewModelState,
                                    isLoading = isLoading,
                                    onLoginEvent = onLoginEvent,
                                    onSignupEvent = { event ->
                                        if (event == SignupEvent.AnonymousSignIn) {
                                            guestAuthHandoffStarted = true
                                        }
                                        onSignupEvent(event)
                                    },
                                    onBackClick = null,
                                    eyebrowText = "Start here",
                                    titleOverride = "Sign in to begin",
                                    subtitleOverride = "Use email, Google, or continue as guest. Then we'll build your personalized dream path."
                                )
                                Spacer(modifier = Modifier.height(120.dp))
                            }
                        }

                        OnboardingFlowStep.PremiumTrialClarity,
                        OnboardingFlowStep.PremiumPromise,
                        OnboardingFlowStep.PremiumUnlocks,
                        OnboardingFlowStep.PremiumOfferSheet,
                        OnboardingFlowStep.PremiumGiftReveal,
                        OnboardingFlowStep.PremiumOneTimeOffer -> {
                            val stageId = step.toPremiumStageId()
                            if (stageId != null) {
                                premiumModel?.let { model ->
                                    OnboardingPremiumStagePage(
                                        model = model,
                                        displayName = answers.displayName,
                                        currentStageId = stageId,
                                        selectedPlan = selectedPremiumPlan,
                                        isLoading = premiumLoading,
                                        hasPremium = false,
                                        onBack = {
                                            dismissPremiumFlow(stageId.toPremiumPageKind())
                                        },
                                        onRestore = {
                                            vibratorUtil.triggerVibration()
                                            scope.launch { restorePremiumPurchases() }
                                        },
                                        onPlanSelected = { option ->
                                            selectedPremiumPlan = option
                                            selectedPackageId = model.packageForPlan(option)
                                                ?.packageToPurchase
                                                ?.identifier
                                            selectedPackageId?.let { packageId ->
                                                premiumAnalytics.track(
                                                    PremiumAnalyticsEvent.PackageSelected(
                                                        page = stageId.toPremiumPageKind(),
                                                        packageId = packageId
                                                    )
                                                )
                                            }
                                        },
                                        onPrimary = {
                                            vibratorUtil.triggerVibration()
                                            when (stageId) {
                                                PremiumStageId.TrialClarity,
                                                PremiumStageId.Promise,
                                                PremiumStageId.Unlocks -> {
                                                    premiumAnalytics.track(
                                                        PremiumAnalyticsEvent.CtaTapped(
                                                            page = stageId.toPremiumPageKind(),
                                                            label = when (stageId) {
                                                                PremiumStageId.TrialClarity,
                                                                PremiumStageId.Promise -> "Continue"
                                                                else -> "See the offer"
                                                            }
                                                        )
                                                    )
                                                    stageId.nextStage()?.let { nextStage ->
                                                        currentStep = nextStage.toOnboardingFlowStep()
                                                    }
                                                }

                                                PremiumStageId.OfferSheet -> {
                                                    selectedPackageId = model.packageForPlan(selectedPremiumPlan)
                                                        ?.packageToPurchase
                                                        ?.identifier
                                                    premiumAnalytics.track(
                                                        PremiumAnalyticsEvent.CtaTapped(
                                                            page = PremiumPageKind.OfferSheet,
                                                            label = "Try for $0.00"
                                                        )
                                                    )
                                                    scope.launch {
                                                        purchasePremium(
                                                            page = PremiumPageKind.OfferSheet,
                                                            model = model,
                                                            packageId = selectedPackageId
                                                        )
                                                    }
                                                }

                                                PremiumStageId.GiftReveal -> {
                                                    premiumAnalytics.track(
                                                        PremiumAnalyticsEvent.RescueAccepted(
                                                            source = PremiumRescueSource.OfferSheetDismiss,
                                                            variant = PremiumRescueVariant.GiftBox
                                                        )
                                                    )
                                                    premiumAnalytics.track(
                                                        PremiumAnalyticsEvent.CtaTapped(
                                                            page = PremiumPageKind.GiftReveal,
                                                            label = "See annual option"
                                                        )
                                                    )
                                                    currentStep = PremiumStageId.OneTimeOffer.toOnboardingFlowStep()
                                                }

                                                PremiumStageId.OneTimeOffer -> {
                                                    val annualPackageId = model.packages.firstOrNull { it.isAnnual }
                                                        ?.packageToPurchase
                                                        ?.identifier
                                                        ?: model.packageForPlan(PremiumPlanOption.Annual)
                                                            ?.packageToPurchase
                                                            ?.identifier
                                                    premiumAnalytics.track(
                                                        PremiumAnalyticsEvent.CtaTapped(
                                                            page = PremiumPageKind.OneTimeOffer,
                                                            label = "Start yearly membership"
                                                        )
                                                    )
                                                    scope.launch {
                                                        purchasePremium(
                                                            page = PremiumPageKind.OneTimeOffer,
                                                            model = model,
                                                            packageId = annualPackageId
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onSecondary = {
                                            vibratorUtil.triggerVibration()
                                            if (stageId == PremiumStageId.OneTimeOffer) {
                                                currentStep = PremiumStageId.OfferSheet.toOnboardingFlowStep()
                                            } else {
                                                dismissPremiumFlow(stageId.toPremiumPageKind())
                                            }
                                        },
                                        modifier = pageContainerModifier
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        ProfileIntroStarOverlay(
            visible = currentStep == OnboardingFlowStep.DreamOutcome && outcomeOrbVisible.value,
            targetCenter = outcomeHeroCenter,
            onImpact = {
                outcomeHeroRevealStarted.value = true
                scope.launch {
                    delay(170)
                    outcomeContentRevealStarted.value = true
                }
            },
            onFinished = {
                outcomeOrbVisible.value = false
            },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f)
        )
    }
}
