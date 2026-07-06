package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.*
import kotlinx.coroutines.delay
import org.ballistic.dreamjournalai.shared.dream_premium.domain.MembershipBenefitModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumCalloutUiModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumIconBundle
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPackageModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPaywallModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPlanOption
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumStageId
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumStageUiModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumTimelineItemUiModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumUiState
import org.ballistic.dreamjournalai.shared.dream_premium.domain.buildPremiumStages
import org.ballistic.dreamjournalai.shared.dream_premium.domain.packageForPlan
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumTimelineItem
import org.ballistic.dreamjournalai.shared.dream_premium.domain.offerCtaSubtext
import org.ballistic.dreamjournalai.shared.dream_premium.domain.planToggleBillingLabel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.planTogglePriceLabel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private const val DreamNorthPrivacyUrl = "https://dream-north.com/privacy"
private const val DreamNorthTermsUrl = "https://dream-north.com/terms"

@Composable
fun OnboardingPremiumStagePage(
    model: PremiumPaywallModel,
    displayName: String,
    currentStageId: PremiumStageId,
    selectedPlan: PremiumPlanOption,
    isLoading: Boolean,
    hasPremium: Boolean,
    onBack: () -> Unit,
    onRestore: () -> Unit,
    onPlanSelected: (PremiumPlanOption) -> Unit,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stages = remember(model.offering.identifier, displayName, hasPremium) {
        buildPremiumStages(
            displayName = displayName,
            hasPremium = hasPremium,
            model = model
        )
    }
    val state = PremiumUiState(
        displayName = displayName,
        selectedPlan = selectedPlan,
        currentStageId = currentStageId,
        stages = stages,
        isPurchaseInProgress = isLoading,
        hasPremium = hasPremium,
    )
    val stage = state.currentStage

    OnboardingStandardPage(
        modifier = modifier,
        currentStep = currentStageId.premiumProgressStep(),
        totalSteps = 6,
        title = "DreamNorth Premium",
        showProgressTracker = false,
        scrollResetKey = stage.id,
        autoScrollToBottomDelayMs = if (stage.id == PremiumStageId.OfferSheet) 3150L else null,
        autoScrollToBottomDurationMs = 2200,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (stage.id == PremiumStageId.OfferSheet) 12.dp else 16.dp)
        ) {
            PremiumTopBar(
                title = "DreamNorth Premium",
                isBusy = isLoading,
                onBack = onBack,
                onRestore = onRestore
            )

            stage.image?.let { drawable ->
                PremiumStageHero(stage = stage, drawable = drawable)
            }

            if (stage.id != PremiumStageId.OfferSheet) {
                PremiumHeading(stage = stage)
            }

            when (stage.id) {
                PremiumStageId.Promise,
                PremiumStageId.GiftReveal -> BalancedCalloutGrid(stage.callouts)

                PremiumStageId.Unlocks -> CompactUnlocksSection(stage.callouts)

                PremiumStageId.OfferSheet -> OfferSheetSection(
                    model = model,
                    selectedPlan = selectedPlan,
                    onPlanSelected = onPlanSelected
                )

                PremiumStageId.OneTimeOffer -> OneTimeOfferSection(
                    model = model,
                    callouts = stage.callouts
                )

                PremiumStageId.TrialClarity -> Unit
            }

            if (stage.id == PremiumStageId.OfferSheet) {
                val selectedOfferPackage = model.packageForPlan(selectedPlan)
                OfferCtaButton(
                    text = if (isLoading) "Starting membership..." else "Try for $0.00",
                    subtext = selectedOfferPackage?.offerCtaSubtext()
                        ?: "Unlimited free access for 7 days, then billing starts.",
                    showBanner = !isLoading,
                    enabled = !isLoading,
                    onClick = onPrimary,
                )
                OfferAppliedPill()
                OfferFooterActions(onRestore = onRestore)
            } else {
                PremiumCtaButton(
                    text = if (isLoading) "Starting membership..." else stage.primaryActionLabel,
                    isLoading = isLoading,
                    enabled = !isLoading,
                    onClick = onPrimary,
                )
            }

            stage.secondaryActionLabel?.let { label ->
                OnboardingSecondaryAction(
                    text = label,
                    onClick = onSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun PremiumStageHero(
    stage: PremiumStageUiModel,
    drawable: DrawableResource,
) {
    var visible by remember(stage.id) { mutableStateOf(false) }

    LaunchedEffect(stage.id) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "premiumStageHeroAlpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 18.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "premiumStageHeroOffset"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "premiumStageHeroScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(
                min = if (stage.id == PremiumStageId.TrialClarity) 132.dp else 176.dp,
                max = if (stage.id == PremiumStageId.OfferSheet) 238.dp else 270.dp
            )
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY.toPx()
                scaleX = scale
                scaleY = scale
            },
        color = Color.Transparent,
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x1FFFC39A),
                            Color(0x141A1740),
                            Color.Transparent,
                        )
                    )
                )
                .padding(if (stage.id == PremiumStageId.TrialClarity) 26.dp else 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(drawable),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PremiumHeading(
    stage: PremiumStageUiModel,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stage.headline,
            style = headerStyle().copy(
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stage.body,
            style = bodyStyle().copy(
                color = Color.White.copy(alpha = 0.84f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BalancedCalloutGrid(
    callouts: List<PremiumCalloutUiModel>,
) {
    if (callouts.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        callouts.chunked(2).forEach { rowCallouts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowCallouts.forEach { callout ->
                    PremiumCalloutCard(
                        callout = callout,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowCallouts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompactUnlocksSection(
    callouts: List<PremiumCalloutUiModel>,
) {
    var visibleCount by remember(callouts) { mutableIntStateOf(0) }

    LaunchedEffect(callouts) {
        visibleCount = 0
        callouts.indices.forEach { index ->
            delay(if (index == 0) 80 else 65)
            visibleCount = index + 1
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MembershipSectionLabel(text = "Everything Premium unlocks")
        callouts.chunked(2).forEachIndexed { rowIndex, rowCallouts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowCallouts.forEachIndexed { columnIndex, callout ->
                    val index = rowIndex * 2 + columnIndex
                    AnimatedVisibility(
                        visible = visibleCount > index,
                        enter = fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) +
                            slideInVertically(
                                initialOffsetY = { it / 5 },
                                animationSpec = tween(260, easing = FastOutSlowInEasing)
                            ),
                        modifier = Modifier.weight(1f)
                    ) {
                        PremiumCalloutCard(
                            callout = callout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(126.dp)
                        )
                    }
                }
                if (rowCallouts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PremiumCalloutCard(
    callout: PremiumCalloutUiModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.heightIn(min = 112.dp),
        color = Color.White.copy(alpha = 0.055f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MembershipCheckBadge(positive = true)
            Text(
                text = callout.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = callout.description,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.76f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun OfferSheetSection(
    model: PremiumPaywallModel,
    selectedPlan: PremiumPlanOption,
    onPlanSelected: (PremiumPlanOption) -> Unit,
) {
    val annualPackage = model.packages.firstOrNull { it.isAnnual } ?: model.packageForPlan(PremiumPlanOption.Annual)
    val monthlyPackage = model.packages.firstOrNull { it.isMonthly }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReminderTimelineSection()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PremiumOfferPlanCard(
                modifier = Modifier.weight(1f),
                title = "Annual",
                priceLabel = annualPackage?.offerPlanPriceLabel() ?: "Unavailable",
                billingLabel = annualPackage?.offerPlanBillingLabel() ?: "Not in current offer",
                badge = annualPackage?.badge?.takeIf { it.isNotBlank() },
                selected = selectedPlan == PremiumPlanOption.Annual,
                enabled = annualPackage != null,
                onClick = { onPlanSelected(PremiumPlanOption.Annual) },
            )
            PremiumOfferPlanCard(
                modifier = Modifier.weight(1f),
                title = "Monthly",
                priceLabel = monthlyPackage?.offerPlanPriceLabel() ?: "Unavailable",
                billingLabel = monthlyPackage?.offerPlanBillingLabel() ?: "Not in current offer",
                badge = monthlyPackage?.badge?.takeIf { it.isNotBlank() },
                selected = selectedPlan == PremiumPlanOption.Monthly,
                enabled = monthlyPackage != null,
                onClick = { onPlanSelected(PremiumPlanOption.Monthly) },
            )
        }
    }
}

@Composable
private fun ReminderTimelineSection() {
    SimpleReminderTimelineSection(
        items = listOf(
            PremiumTimelineItemUiModel(
                label = "Today",
                title = "Start premium",
                description = "Get full, unlimited access right away.",
            ),
            PremiumTimelineItemUiModel(
                label = "In 5 days",
                title = "Reminder",
                description = "We'll send a reminder before your trial ends.",
            ),
            PremiumTimelineItemUiModel(
                label = "In 7 days",
                title = "Trial ends",
                description = "You will be charged. You can still cancel anytime before.",
            ),
        ),
    )
}

@Composable
private fun SimpleReminderTimelineSection(
    items: List<PremiumTimelineItemUiModel>,
) {
    GlassInfoCard {
        Text(
            text = "We'll send you a reminder\nbefore your FREE trial ends",
            style = TextStyle(
                color = Color.White,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEachIndexed { index, item ->
                SimpleReminderTimelineRow(
                    index = index,
                    item = item,
                    isLast = index == items.lastIndex,
                    showGlow = index == items.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun SimpleReminderTimelineRow(
    index: Int,
    item: PremiumTimelineItemUiModel,
    isLast: Boolean,
    showGlow: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            SimpleReminderNode(
                index = index,
                icon = if (showGlow) Icons.Rounded.Stars else premiumTimelineIcon(item.title),
                showGlow = showGlow,
            )

            if (!isLast) {
                val lineProgress = remember(index) { Animatable(0f) }

                LaunchedEffect(index) {
                    lineProgress.snapTo(0f)
                    delay(400L + (index * 980L))
                    lineProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 760),
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .widthIn(min = 10.dp, max = 10.dp)
                        .height(54.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 4.dp, max = 4.dp)
                            .height(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCBC6FF).copy(alpha = 0.36f)),
                    )

                    Box(
                        modifier = Modifier
                            .widthIn(min = 6.dp, max = 6.dp)
                            .height(54.dp * lineProgress.value)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF8B84FF),
                                        Color(0xFF6E69F8),
                                    ),
                                ),
                            ),
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 42.dp * lineProgress.value)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7B75F8)),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.label,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
            )

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.76f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SimpleReminderNode(
    index: Int,
    icon: ImageVector,
    showGlow: Boolean,
) {
    val glowTransition = rememberInfiniteTransition(label = "premium_star_glow")
    val nodeScale = remember(index) { Animatable(0.72f) }
    val nodeOffset = remember(index) { Animatable(16f) }
    val nodeAlpha = remember(index) { Animatable(0.25f) }

    LaunchedEffect(index) {
        val delayMillis = when (index) {
            0 -> 80L
            1 -> 1020L
            else -> 1840L
        }

        delay(delayMillis)

        nodeAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 220),
        )

        nodeScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.5f,
                stiffness = 420f,
            ),
        )

        nodeOffset.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = 0.58f,
                stiffness = 360f,
            ),
        )
    }

    val glowScale by glowTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "premium_star_glow_scale",
    )

    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "premium_star_glow_alpha",
    )

    Box(
        modifier = Modifier
            .size(46.dp)
            .graphicsLayer {
                scaleX = nodeScale.value
                scaleY = nodeScale.value
                alpha = nodeAlpha.value
                translationY = nodeOffset.value
            },
        contentAlignment = Alignment.Center,
    ) {
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size((46.dp * glowScale).coerceAtLeast(46.dp))
                    .clip(CircleShape)
                    .background(Color(0xFFFDC830).copy(alpha = glowAlpha)),
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (showGlow) {
                        premiumTimelineAccentBrush()
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF747DFF),
                                Color(0xFF646EF6),
                            ),
                        )
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(if (showGlow) 20.dp else 18.dp),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun PremiumOfferPlanCard(
    title: String,
    priceLabel: String,
    billingLabel: String,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected && enabled) 1.015f else 1f,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = 260f),
        label = "premium_offer_plan_scale"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (selected && enabled) 0.95f else 0.22f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "premium_offer_plan_glow"
    )
    val contentAlpha = if (enabled) 1f else 0.52f

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics { this.selected = selected },
        color = if (selected && enabled) Color(0x24FFC39A) else Color.White.copy(alpha = 0.055f),
        shape = RoundedCornerShape(22.dp),
        onClick = if (enabled) onClick else ({}),
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = if (selected && enabled) 1.5.dp else 1.dp,
            color = if (selected && enabled) {
                Color(0xFFFFD4B8).copy(alpha = 0.86f)
            } else {
                Color.White.copy(alpha = 0.14f)
            },
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x44FFCAA3).copy(alpha = glowAlpha * 0.72f),
                            Color(0x33A793FF).copy(alpha = glowAlpha * 0.48f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 132.dp)
                    .padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (enabled && selected && !badge.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF8DB4), Color(0xFFFFD0A3)),
                                    ),
                                ),
                        ) {
                            Text(
                                text = badge.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = TextStyle(
                                    color = Color(0xFF231437),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                            )
                        }
                    } else {
                        Spacer(Modifier.height(19.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (selected && enabled) Color(0xFFFFBFA6) else Color.Transparent)
                            .border(
                                1.5.dp,
                                if (selected && enabled) Color(0xFFFFD4B8) else Color.White.copy(alpha = 0.24f),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected && enabled) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color(0xFF231437),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Text(
                    text = title,
                    style = TextStyle(
                        color = Color.White.copy(alpha = contentAlpha),
                        fontSize = 16.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = priceLabel,
                    style = TextStyle(
                        color = Color(0xFFFFD6BE).copy(alpha = contentAlpha),
                        fontSize = 22.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = billingLabel,
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.72f * contentAlpha),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OneTimeOfferSection(
    model: PremiumPaywallModel,
    callouts: List<PremiumCalloutUiModel>,
) {
    val annualPackage = model.packages.firstOrNull { it.isAnnual } ?: model.packageForPlan(PremiumPlanOption.Annual)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        annualPackage?.let { packageModel ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFFCF7),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, Color(0xFFE4BE74))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = packageModel.priceText,
                        style = TextStyle(
                            color = Color(0xFFF28A37),
                            fontSize = 30.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = packageModel.trialText ?: packageModel.cadenceText,
                        style = TextStyle(
                            color = Color(0xFF6B6259),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }

        BalancedCalloutGrid(callouts = callouts)
    }
}

@Composable
private fun OfferCtaButton(
    text: String,
    subtext: String,
    showBanner: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showBanner) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x24FDC830))
                    .border(1.dp, Color(0x55FDC830), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Free today. Reminder before billing.",
                    style = TextStyle(
                        color = Color(0xFFFFE4C0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

        PremiumCtaButton(
            text = text,
            isLoading = !enabled,
            enabled = enabled,
            onClick = onClick,
        )

        Text(
            text = subtext,
            style = TextStyle(
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
    }
}

private fun PremiumPackageModel.offerPlanPriceLabel(): String = planTogglePriceLabel()

private fun PremiumPackageModel.offerPlanBillingLabel(): String =
    if (hasTrial || trialText != null) {
        "${planToggleBillingLabel()} after 7-day free trial"
    } else {
        planToggleBillingLabel()
    }

@Composable
private fun OfferAppliedPill() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = "Trial reminder included",
            style = TextStyle(
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun OfferFooterActions(
    onRestore: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OnboardingSecondaryAction(
            text = "Restore purchases",
            onClick = onRestore,
            color = Color.White.copy(alpha = 0.76f)
        )
        PremiumLegalText()
    }
}

private fun premiumTimelineIcon(title: String): ImageVector = when {
    title.contains("start", ignoreCase = true) -> Icons.Rounded.LockOpen
    title.contains("reminder", ignoreCase = true) -> Icons.Rounded.NotificationsActive
    else -> Icons.Rounded.CreditCard
}

private fun premiumTimelineAccentBrush(): Brush = Brush.horizontalGradient(
    listOf(
        Color(0xFFF37335),
        Color(0xFFFDC830),
    ),
)

private fun PremiumStageId.premiumProgressStep(): Int = when (this) {
    PremiumStageId.TrialClarity -> 1
    PremiumStageId.Promise -> 2
    PremiumStageId.Unlocks -> 3
    PremiumStageId.OfferSheet -> 4
    PremiumStageId.GiftReveal -> 5
    PremiumStageId.OneTimeOffer -> 6
}

@Composable
fun OnboardingPremiumIntroPage(
    onSeePremium: () -> Unit,
    onNotYet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val benefits = remember {
        listOf(
            "Capture more dream detail",
            "Unlock deeper interpretations",
            "Spot patterns over time"
        )
    }
    var stage by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        stage = 0
        kotlinx.coroutines.delay(120)
        stage = 1
        kotlinx.coroutines.delay(120)
        stage = 2
        benefits.indices.forEach { index ->
            kotlinx.coroutines.delay(70)
            stage = 3 + index
        }
        kotlinx.coroutines.delay(120)
        stage = 7
    }

    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 17,
        totalSteps = 17,
        title = "Premium",
    ) {
        AnimatedVisibility(
            visible = stage >= 1,
            enter = fadeIn(animationSpec = tween(450, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                )
        ) {
            PremiumHero(
                modifier = Modifier.heightIn(min = 150.dp, max = 214.dp)
            )
        }

        AnimatedVisibility(
            visible = stage >= 2,
            enter = fadeIn(animationSpec = tween(360, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { it / 10 },
                    animationSpec = tween(360, easing = FastOutSlowInEasing)
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Want to see what Premium adds?",
                    style = headerStyle().copy(
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "You're ready. DreamNorth Premium can help you capture more, understand more, and build a steadier dream ritual.",
                    style = bodyStyle().copy(
                        color = Color.White.copy(alpha = 0.84f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        GlassInfoCard {
            benefits.forEachIndexed { index, benefit ->
                AnimatedVisibility(
                    visible = stage >= 3 + index,
                    enter = fadeIn(animationSpec = tween(260, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 5 },
                            animationSpec = tween(260, easing = FastOutSlowInEasing)
                        )
                ) {
                    PremiumIntroBenefitRow(text = benefit)
                }
            }
        }

        AnimatedVisibility(
            visible = stage >= 7,
            enter = fadeIn(animationSpec = tween(320, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { it / 7 },
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OnboardingPrimaryButton(
                    text = "See Premium",
                    onClick = onSeePremium
                )
                OnboardingSecondaryAction(
                    text = "Not yet",
                    onClick = onNotYet,
                    color = Color.White.copy(alpha = 0.74f)
                )
            }
        }
    }
}

@Composable
private fun PremiumIntroBenefitRow(
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        MembershipCheckBadge(positive = true)
        Text(
            text = text,
            style = TextStyle(
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PremiumTopBar(
    title: String,
    isBusy: Boolean,
    onBack: () -> Unit,
    onRestore: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close Premium",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = title,
            style = TextStyle(
                color = Color(0xFFFFE4CE),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        )

        Surface(
            modifier = Modifier.clickable(enabled = !isBusy, onClick = onRestore),
            color = Color.White.copy(alpha = if (isBusy) 0.06f else 0.10f),
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFFFD7BE)
                    )
                }
                Text(
                    text = if (isBusy) "Working" else "Restore",
                    style = TextStyle(
                        color = Color.White.copy(alpha = if (isBusy) 0.68f else 0.90f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun PremiumHero(
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "premiumHeroAlpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 18.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "premiumHeroOffset"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "premiumHeroScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp, max = 248.dp)
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY.toPx()
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.premium_intro_hero_glass),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun PremiumFeatureList(
    benefits: List<MembershipBenefitModel>,
) {
    var visibleBenefits by remember(benefits) { mutableIntStateOf(0) }

    LaunchedEffect(benefits) {
        visibleBenefits = 0
        benefits.indices.forEach { index ->
            kotlinx.coroutines.delay(if (index == 0) 80 else 70)
            visibleBenefits = index + 1
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        benefits.forEachIndexed { index, benefit ->
            AnimatedVisibility(
                visible = visibleBenefits > index,
                enter = fadeIn(animationSpec = tween(260)) + slideInVertically(
                    initialOffsetY = { it / 5 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            ) {
                MembershipBenefitCard(benefit = benefit)
            }
        }
    }
}

@Composable
private fun PremiumTimelineSection(
    items: List<PremiumTimelineItem>,
    iconBundle: PremiumIconBundle,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MembershipSectionLabel(text = "A smoother first week")
        MembershipTimelineInfographic(
            items = items.take(4),
            bellIcon = iconBundle.dreamBell,
            nodeIcon = iconBundle.timelineNode,
            connectorProgress = rememberPremiumTimelineProgress(items = items),
            visibleNodes = rememberPremiumTimelineVisibleCount(items = items)
        )
    }
}

@Composable
private fun rememberPremiumTimelineProgress(
    items: List<PremiumTimelineItem>,
): Float {
    val progress = remember(items) { Animatable(0f) }

    LaunchedEffect(items) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    return progress.value
}

@Composable
private fun rememberPremiumTimelineVisibleCount(
    items: List<PremiumTimelineItem>,
): Int {
    var visibleCount by remember(items) { mutableIntStateOf(0) }

    LaunchedEffect(items) {
        visibleCount = 0
        items.take(4).indices.forEach { index ->
            kotlinx.coroutines.delay(130)
            visibleCount = index + 1
        }
    }

    return visibleCount
}

@Composable
private fun PremiumCtaButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = if (enabled) {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFFB07C),
                                Color(0xFFFF8FB8),
                                Color(0xFFB7A1FF)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (enabled) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = text,
                    modifier = Modifier.padding(horizontal = 18.dp),
                    style = TextStyle(
                        color = if (enabled) Color.White else Color.White.copy(alpha = 0.56f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

@Composable
private fun PremiumLegalText() {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = "Cancel anytime. Subscriptions renew automatically unless canceled before the renewal date.",
            style = TextStyle(
                color = Color.White.copy(alpha = 0.54f),
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PremiumLegalLink(
                text = "Privacy Policy",
                onClick = { uriHandler.openUri(DreamNorthPrivacyUrl) }
            )
            Text(
                text = "  |  ",
                color = Color.White.copy(alpha = 0.48f),
                fontSize = 11.sp
            )
            PremiumLegalLink(
                text = "Terms of Use",
                onClick = { uriHandler.openUri(DreamNorthTermsUrl) }
            )
        }
    }
}

@Composable
private fun PremiumLegalLink(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = Color(0xFFFFD8B8),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun MembershipSectionLabel(
    text: String,
) {
    Text(
        text = text,
        style = TextStyle(
            color = Color(0xFFFFDABF),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp
        )
    )
}

@Composable
private fun MembershipComparisonCard(
    iconBundle: PremiumIconBundle,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x16FFFFFF),
                            Color(0x16848DFF),
                            Color(0x14FFD8BC)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Free vs membership",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MembershipComparisonSection(
                        label = "Free",
                        value = "30",
                        valueCaption = "dream tokens / month",
                        points = listOf(
                            "Basic access",
                            "Locked articles",
                            "Shorter recordings"
                        ),
                        highlighted = false,
                        topIcon = iconBundle.dreamToken,
                    )

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.07f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFFFDDC3),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                MembershipComparisonSection(
                    label = "Membership",
                    value = "100",
                    valueCaption = "dream tokens / month",
                    points = listOf(
                        "All lessons & articles",
                        "All tools unlocked",
                        "Longer recordings"
                    ),
                    highlighted = true,
                    topIcon = iconBundle.membershipStar
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    MembershipMiniStat(
                        title = "100 monthly",
                        caption = "Dream tokens",
                        modifier = Modifier.weight(1f)
                    )
                    MembershipMiniStat(
                        title = "All access",
                        caption = "Tools + lessons",
                        modifier = Modifier.weight(1f)
                    )
                    MembershipMiniStat(
                        title = "Longer",
                        caption = "Recordings",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MembershipComparisonSection(
    label: String,
    value: String,
    valueCaption: String,
    points: List<String>,
    highlighted: Boolean,
    topIcon: DrawableResource,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (highlighted) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0x28FFE0C4),
                            Color(0x1F8D90FF),
                            Color(0x14FFFFFF)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (highlighted) Color(0x55FFE0C4) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroIconBadge(
                    icon = topIcon,
                    selected = highlighted
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (highlighted) "$label  BEST" else label,
                        style = TextStyle(
                            color = if (highlighted) Color(0xFFFFE3C8) else Color(0xFFFFD7BE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = value,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = valueCaption,
                            style = bodyStyle(fontSize = 12.sp),
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                points.forEach { point ->
                    MembershipCheckRow(
                        text = point,
                        positive = highlighted
                    )
                }
            }
        }
    }
}

@Composable
private fun MembershipMiniStat(
    title: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = caption,
            style = TextStyle(
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun MembershipCheckRow(
    text: String,
    positive: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        MembershipCheckBadge(positive = positive)
        Text(
            text = text,
            style = TextStyle(
                color = Color.White.copy(alpha = if (positive) 0.90f else 0.74f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun MembershipCheckBadge(
    positive: Boolean,
) {
    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .size(18.dp)
            .clip(CircleShape)
            .background(
                if (positive) Color(0xFF2ED47A).copy(alpha = 0.22f)
                else Color.White.copy(alpha = 0.08f)
            )
            .border(
                width = 1.dp,
                color = if (positive) Color(0xFF51E58C) else Color.White.copy(alpha = 0.10f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (positive) "✓" else "•",
            style = TextStyle(
                color = if (positive) Color(0xFF8BF0AE) else Color.White.copy(alpha = 0.70f),
                fontSize = if (positive) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun BoxScope.MembershipAmbientBackdrop(
    drawableRes: DrawableResource,
    alignment: Alignment,
    artSize: androidx.compose.ui.unit.Dp,
    glowColors: List<Color>,
    alpha: Float,
) {
    val floatTransition = rememberInfiniteTransition(label = "membership_ambient_backdrop")
    val offsetY by floatTransition.animateFloat(
        initialValue = 8f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "membership_ambient_backdrop_offset"
    )

    Box(
        modifier = Modifier
            .align(alignment)
            .padding(top = 22.dp, end = 10.dp)
            .size(artSize)
            .graphicsLayer {
                translationY = offsetY
                this.alpha = alpha
                scaleX = 1.02f
                scaleY = 1.02f
                transformOrigin = TransformOrigin.Center
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(30.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColors.firstOrNull() ?: Color(0x24FFFFFF),
                            glowColors.getOrNull(1) ?: Color.Transparent,
                            Color.Transparent
                        )
                    )
                )
        )

        Image(
            painter = painterResource(drawableRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun MembershipPromoChip(
    text: String,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0x2BFFE0C4),
                        Color(0x18FFFFFF)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = Color(0xFFFFDFC4),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun MembershipSocialProofSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.04f),
                            Color(0x12FFCFAE),
                            Color(0x0EA0A2FF)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) {
                    Image(
                        painter = painterResource(Res.drawable.onboarding_icon_review_star_glass),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(
                    text = "Loved by dreamers building a nightly ritual",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Text(
                text = "\"The habit finally felt beautiful instead of like homework.\"",
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun MembershipBenefitCard(
    benefit: MembershipBenefitModel,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0x1AFFFFFF),
                            Color(0x10B19DFF),
                            Color(0x08FFD8C2)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                MembershipCheckBadge(positive = true)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = benefit.title,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = benefit.body,
                        style = bodyStyle(fontSize = 12.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MembershipTimelineInfographic(
    items: List<PremiumTimelineItem>,
    bellIcon: DrawableResource,
    nodeIcon: DrawableResource,
    connectorProgress: Float,
    visibleNodes: Int,
) {
    GlassInfoCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            val dayLabels = listOf("Day 1", "Day 3", "Day 7", "Day 14")
            items.forEachIndexed { index, item ->
                MembershipTimelineRow(
                    item = item,
                    dayLabel = dayLabels.getOrElse(index) { "Day ${index + 1}" },
                    icon = if (index == 0) bellIcon else nodeIcon,
                    showConnector = index < items.lastIndex,
                    connectorProgress = timelineConnectorSegment(
                        progress = connectorProgress,
                        segmentIndex = index,
                        totalSegments = (items.size - 1).coerceAtLeast(1)
                    ),
                    visible = visibleNodes > index
                )
            }
        }
    }
}

@Composable
private fun MembershipTimelineRow(
    item: PremiumTimelineItem,
    dayLabel: String,
    icon: DrawableResource,
    showConnector: Boolean,
    connectorProgress: Float,
    visible: Boolean,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(240)) + scaleIn(
            initialScale = 0.97f,
            animationSpec = tween(260, easing = FastOutSlowInEasing)
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroIconBadge(
                        icon = icon,
                        selected = true
                    )
                    if (showConnector) {
                        Box(
                            modifier = Modifier
                                .height(64.dp)
                                .width(4.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.09f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((52.dp * connectorProgress.coerceIn(0f, 1f)))
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFFFFD6BA),
                                                Color(0xFFA6A2FF)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0x24FFD7BD))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(999.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = dayLabel,
                                style = TextStyle(
                                    color = Color(0xFFFFD9C0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Text(
                            text = item.title,
                            style = TextStyle(
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Text(
                        text = item.body,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 21.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumPackageCard(
    packageModel: PremiumPackageModel,
    selected: Boolean,
    onClick: () -> Unit,
    highlightIcon: DrawableResource,
) {
    val borderAlpha by animateFloatAsState(
        targetValue = if (selected) 0.82f else 0.14f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "membership_package_border"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.22f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 260f),
        label = "membership_package_glow"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.015f else 1f,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = 260f),
        label = "membership_package_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics { this.selected = selected }
            .clickable(onClick = onClick),
        color = if (selected) Color(0x24FFC39A) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.dp,
            if (selected) Color(0xFFFFD4B8).copy(alpha = borderAlpha) else Color.White.copy(alpha = borderAlpha)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x44FFCAA3).copy(alpha = glowAlpha * 0.8f),
                            Color(0x33A793FF).copy(alpha = glowAlpha * 0.55f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = packageModel.title,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            packageModel.badge?.takeIf { it.isNotBlank() }?.let { badge ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(Color(0x22FFD5B6))
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.16f),
                                            shape = RoundedCornerShape(100.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(highlightIcon),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = badge,
                                        style = TextStyle(
                                            color = Color(0xFFFFD6BE),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                        Text(
                            text = packageModel.cadenceText,
                            style = bodyStyle(fontSize = 12.sp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = packageModel.priceText,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (selected) Color(0xFFFFDBBF) else Color.White.copy(alpha = 0.54f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                packageModel.trialText?.let { trial ->
                    Text(
                        text = trial,
                        style = TextStyle(
                            color = Color(0xFFFFDEBF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        )
                    )
                }
            }
        }
    }
}

private fun timelineConnectorSegment(
    progress: Float,
    segmentIndex: Int,
    totalSegments: Int,
): Float {
    if (totalSegments <= 0) return 0f
    val segmentSize = 1f / totalSegments.toFloat()
    val start = segmentIndex * segmentSize
    val end = start + segmentSize
    return ((progress - start) / (end - start)).coerceIn(0f, 1f)
}
