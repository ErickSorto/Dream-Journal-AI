package org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.*
import dreamjournalai.composeapp.shared.generated.resources.blue_lighthouse
import dreamjournalai.composeapp.shared.generated.resources.dream_token_benefit_content_description
import dreamjournalai.composeapp.shared.generated.resources.dream_token_slide_benefit_1
import dreamjournalai.composeapp.shared.generated.resources.dream_token_slide_benefit_2
import dreamjournalai.composeapp.shared.generated.resources.dream_token_slide_benefit_3
import dreamjournalai.composeapp.shared.generated.resources.dream_token_slide_benefit_4
import dreamjournalai.composeapp.shared.generated.resources.store_premium_icon_ad_free
import dreamjournalai.composeapp.shared.generated.resources.store_premium_icon_interpretations
import dreamjournalai.composeapp.shared.generated.resources.store_premium_icon_paintings
import dreamjournalai.composeapp.shared.generated.resources.store_premium_icon_patterns
import dreamjournalai.composeapp.shared.generated.resources.store_premium_subscription_hero
import dreamjournalai.composeapp.shared.generated.resources.store_token_benefit_scene
import org.ballistic.dreamjournalai.shared.DrawerCommand
import org.ballistic.dreamjournalai.shared.DrawerController
import org.ballistic.dreamjournalai.shared.core.components.DreamTokenLayout
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.core.platform.getPlatformName
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPlanOption
import org.ballistic.dreamjournalai.shared.dream_premium.domain.planToggleBillingLabel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.planTogglePriceLabel
import org.ballistic.dreamjournalai.shared.dream_store.domain.StoreAnalytics
import org.ballistic.dreamjournalai.shared.dream_store.domain.StoreAnalyticsEvent
import org.ballistic.dreamjournalai.shared.dream_store.domain.StoreEvent
import org.ballistic.dreamjournalai.shared.dream_store.presentation.anonymous_store_screen.AnonymousStoreScreen
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.components.CustomButtonLayout
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.viewmodel.StoreScreenViewModelState
import org.ballistic.dreamjournalai.shared.navigation.StoreInitialPage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.DrawableResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.core.util.BackHandler
import org.koin.compose.koinInject
import org.ballistic.dreamjournalai.shared.core.util.setTranslucentBars

private const val DreamNorthPrivacyUrl = "https://dream-north.com/privacy"
private const val DreamNorthTermsUrl = "https://dream-north.com/terms"

@Composable
fun StoreScreen(
    storeScreenViewModelState: StoreScreenViewModelState,
    initialPage: StoreInitialPage = StoreInitialPage.Default,
    bottomPaddingValue: Dp,
    storeBackgroundResource: DrawableResource = Res.drawable.blue_lighthouse,
    onMainEvent: (MainScreenEvent) -> Unit = {},
    onStoreEvent: (StoreEvent) -> Unit = {},
    navigateToAccountScreen: () -> Unit = {},
    navigateBack: () -> Unit = {},
) {
    setTranslucentBars(darkTheme = true)
    val storeAnalytics = koinInject<StoreAnalytics>()

    LaunchedEffect(Unit) {
        DrawerController.send(DrawerCommand.Close)
        DrawerController.disable()
        onMainEvent(MainScreenEvent.SetTopBarState(false))
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(false))
        onMainEvent(MainScreenEvent.SetDrawerState(false))
        storeAnalytics.track(
            StoreAnalyticsEvent.StoreViewed(
                initialPage = initialPage.analyticsPageName(),
                isAnonymous = storeScreenViewModelState.isUserAnonymous,
                tokenBalance = storeScreenViewModelState.dreamTokens,
                dreamCount = storeScreenViewModelState.dreamCount
            )
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            DrawerController.enable()
            onMainEvent(MainScreenEvent.SetDrawerState(true))
        }
    }

    val isAnonymous = storeScreenViewModelState.isUserAnonymous
    val tokenTotal = storeScreenViewModelState.dreamTokens
    val closeStore = {
        onMainEvent(MainScreenEvent.TriggerVibration)
        navigateBack()
    }
    BackHandler(true) {
        closeStore()
    }

    if (isAnonymous) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnonymousStoreScreen(
                paddingValues = PaddingValues(bottom = bottomPaddingValue + 16.dp),
                navigateToAccountScreen = navigateToAccountScreen,
            )
            StoreTopSystemScrim()
            StoreCloseButton(
                onClick = {
                    storeAnalytics.track(StoreAnalyticsEvent.StoreClosed(page = StorePageAnonymous))
                    closeStore()
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 12.dp)
            )
        }
    } else {
        StorePagerContent(
            storeScreenViewModelState = storeScreenViewModelState,
            initialPage = initialPage,
            tokenTotal = tokenTotal,
            bottomPaddingValue = bottomPaddingValue,
            storeBackgroundResource = storeBackgroundResource,
            onStoreEvent = onStoreEvent,
            onVibrate = { onMainEvent(MainScreenEvent.TriggerVibration) },
            navigateBack = closeStore,
            storeAnalytics = storeAnalytics,
        )
    }
}

@Composable
private fun StorePagerContent(
    storeScreenViewModelState: StoreScreenViewModelState,
    initialPage: StoreInitialPage,
    tokenTotal: Int,
    bottomPaddingValue: Dp,
    storeBackgroundResource: DrawableResource,
    onStoreEvent: (StoreEvent) -> Unit,
    onVibrate: () -> Unit,
    navigateBack: () -> Unit,
    storeAnalytics: StoreAnalytics,
) {
    val premiumPageIndex = 0
    val dreamTokenPageIndex = 1
    val initialPageIndex = when (initialPage) {
        StoreInitialPage.Default -> 0
        StoreInitialPage.Premium -> premiumPageIndex
        StoreInitialPage.DreamTokens -> dreamTokenPageIndex
    }
    val pagerState = rememberPagerState(initialPage = initialPageIndex, pageCount = { 2 })
    val pagerScope = rememberCoroutineScope()
    val lastTrackedPage = remember { androidx.compose.runtime.mutableStateOf<Int?>(null) }

    LaunchedEffect(initialPage) {
        if (pagerState.currentPage != initialPageIndex) {
            pagerState.scrollToPage(initialPageIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val currentPage = pagerState.currentPage
        if (lastTrackedPage.value != currentPage) {
            storeAnalytics.track(StoreAnalyticsEvent.PageViewed(page = storePageName(currentPage)))
            lastTrackedPage.value = currentPage
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071125))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 0.dp
        ) { page ->
            val isDreamTokenPage = page == dreamTokenPageIndex

            if (isDreamTokenPage) {
                DreamTokenStoreSection(
                    storeScreenViewModelState = storeScreenViewModelState,
                    tokenTotal = tokenTotal,
                    storeBackgroundResource = storeBackgroundResource,
                    bottomPaddingValue = bottomPaddingValue,
                    onStoreEvent = onStoreEvent,
                    onVibrate = onVibrate,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                PremiumStoreSection(
                    storeScreenViewModelState = storeScreenViewModelState,
                    onPlanSelected = { plan ->
                        onVibrate()
                        onStoreEvent(StoreEvent.SelectPremiumPlan(plan))
                    },
                    onTryPremium = { onStoreEvent(StoreEvent.PurchaseSelectedPremiumPlan) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        StoreTopSystemScrim()
        StorePageControl(
            currentPage = pagerState.currentPage,
            pageCount = pagerState.pageCount,
            onPrevious = {
                onVibrate()
                val targetPage = (pagerState.currentPage - 1).coerceAtLeast(0)
                storeAnalytics.track(
                    StoreAnalyticsEvent.PageChanged(
                        fromPage = storePageName(pagerState.currentPage),
                        toPage = storePageName(targetPage),
                        action = "previous"
                    )
                )
                pagerScope.launch {
                    pagerState.animateScrollToPage(
                        page = targetPage,
                        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
                    )
                }
            },
            onNext = {
                onVibrate()
                val targetPage = (pagerState.currentPage + 1).coerceAtMost(pagerState.pageCount - 1)
                storeAnalytics.track(
                    StoreAnalyticsEvent.PageChanged(
                        fromPage = storePageName(pagerState.currentPage),
                        toPage = storePageName(targetPage),
                        action = "next"
                    )
                )
                pagerScope.launch {
                    pagerState.animateScrollToPage(
                        page = targetPage,
                        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 14.dp, top = 12.dp)
        )
        StoreCloseButton(
            onClick = {
                storeAnalytics.track(StoreAnalyticsEvent.StoreClosed(page = storePageName(pagerState.currentPage)))
                navigateBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 12.dp)
        )
    }
}

private const val StorePageAnonymous = "anonymous"

private fun StoreInitialPage.analyticsPageName(): String {
    return when (this) {
        StoreInitialPage.Default,
        StoreInitialPage.Premium -> "premium"
        StoreInitialPage.DreamTokens -> "dream_tokens"
    }
}

private fun storePageName(page: Int): String {
    return when (page) {
        0 -> "premium"
        1 -> "dream_tokens"
        else -> "unknown"
    }
}

@Composable
private fun StoreTopSystemScrim() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020716).copy(alpha = 0.78f),
                        Color(0xFF020716).copy(alpha = 0.36f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun StorePageControl(
    currentPage: Int,
    pageCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canGoPrevious = currentPage > 0
    val canGoNext = currentPage < pageCount - 1

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF071125).copy(alpha = 0.50f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StorePageArrowButton(
            enabled = canGoPrevious,
            onClick = onPrevious,
            direction = StorePageArrowDirection.Left
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .size(width = if (index == currentPage) 18.dp else 6.dp, height = 6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (index == currentPage) {
                                Brush.horizontalGradient(listOf(Color(0xFFFF6FCB), Color(0xFFFFC36B)))
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.34f),
                                        Color.White.copy(alpha = 0.20f)
                                    )
                                )
                            }
                        )
                )
            }
        }

        StorePageArrowButton(
            enabled = canGoNext,
            onClick = onNext,
            direction = StorePageArrowDirection.Right
        )
    }
}

private enum class StorePageArrowDirection {
    Left,
    Right,
}

@Composable
private fun StorePageArrowButton(
    enabled: Boolean,
    onClick: () -> Unit,
    direction: StorePageArrowDirection,
) {
    val icon = when (direction) {
        StorePageArrowDirection.Left -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
        StorePageArrowDirection.Right -> Icons.AutoMirrored.Filled.KeyboardArrowRight
    }
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 0.10f else 0.04f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = if (enabled) 0.88f else 0.26f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun StoreCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF071125).copy(alpha = 0.74f))
            .border(1.dp, Color.White.copy(alpha = 0.30f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(Res.string.store_close_content_description),
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun DreamTokenStoreSection(
    storeScreenViewModelState: StoreScreenViewModelState,
    tokenTotal: Int,
    storeBackgroundResource: DrawableResource,
    bottomPaddingValue: Dp,
    onStoreEvent: (StoreEvent) -> Unit,
    onVibrate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(storeBackgroundResource),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF100032).copy(alpha = 0.58f),
                            Color(0xFF12002F).copy(alpha = 0.45f),
                            Color(0xFF07001D).copy(alpha = 0.68f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .dynamicBottomNavigationPadding()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 42.dp, bottom = bottomPaddingValue + 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StoreDreamTokenHeader()

            Spacer(modifier = Modifier.height(14.dp))
            CustomButtonLayout(
                storeScreenViewModelState = storeScreenViewModelState,
                buy100IsClicked = {
                    onVibrate()
                    onStoreEvent(StoreEvent.ToggleLoading(true))
                    onStoreEvent(StoreEvent.Buy100DreamTokens)
                },
                buy500IsClicked = {
                    onVibrate()
                    onStoreEvent(StoreEvent.ToggleLoading(true))
                    onStoreEvent(StoreEvent.Buy500DreamTokens)
                },
            )

            Spacer(Modifier.weight(1f))

            DreamBenefitInfoLayout(
                totalDreamTokens = tokenTotal,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun PremiumStoreSection(
    storeScreenViewModelState: StoreScreenViewModelState,
    onPlanSelected: (PremiumPlanOption) -> Unit,
    onTryPremium: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedPlan = storeScreenViewModelState.selectedPremiumPlan
    val selectedPackage = storeScreenViewModelState.packageForSelectedPremiumPlan()
    val uriHandler = LocalUriHandler.current
    val ctaEnabled = !storeScreenViewModelState.isPremiumOfferLoading &&
        !storeScreenViewModelState.isPremiumPurchaseInProgress &&
        selectedPackage != null &&
        !storeScreenViewModelState.hasPremium
    val missingPlanPrice = if (storeScreenViewModelState.isPremiumOfferLoading) {
        stringResource(Res.string.premium_loading_plans)
    } else {
        stringResource(Res.string.premium_plans_unavailable)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(Res.drawable.store_premium_subscription_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.18f),
                            Color.Transparent,
                            Color(0xFF071125).copy(alpha = 0.60f),
                            Color(0xFF071125).copy(alpha = 0.94f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 52.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PremiumHeroSection()

            Spacer(Modifier.height(8.dp))

            PremiumBenefitPanel()

            Spacer(Modifier.height(22.dp))

            val annualPackage = storeScreenViewModelState.premiumPackageFor(PremiumPlanOption.Annual)
            val monthlyPackage = storeScreenViewModelState.premiumPackageFor(PremiumPlanOption.Monthly)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PremiumPlanCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.premium_annual),
                    price = annualPackage?.planTogglePriceLabel() ?: missingPlanPrice,
                    detail = annualPackage?.let {
                        if (getPlatformName() == "iOS") {
                            it.planToggleBillingLabel()
                        } else {
                            stringResource(Res.string.premium_billed_as_yearly, it.priceText)
                        }
                    } ?: stringResource(Res.string.premium_billed_yearly),
                    badge = stringResource(Res.string.premium_best_value),
                    selected = selectedPlan == PremiumPlanOption.Annual,
                    enabled = annualPackage != null,
                    onClick = { onPlanSelected(PremiumPlanOption.Annual) },
                )

                PremiumPlanCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(Res.string.premium_monthly),
                    price = monthlyPackage?.planTogglePriceLabel() ?: missingPlanPrice,
                    detail = monthlyPackage?.let {
                        if (getPlatformName() == "iOS") {
                            it.planToggleBillingLabel()
                        } else {
                            stringResource(Res.string.premium_billed_as_monthly, it.priceText)
                        }
                    } ?: stringResource(Res.string.premium_month_to_month),
                    badge = null,
                    selected = selectedPlan == PremiumPlanOption.Monthly,
                    enabled = monthlyPackage != null,
                    onClick = { onPlanSelected(PremiumPlanOption.Monthly) },
                )
            }

            Spacer(Modifier.height(18.dp))

            PremiumCtaButton(
                label = when {
                    storeScreenViewModelState.hasPremium -> stringResource(Res.string.premium_active)
                    storeScreenViewModelState.isPremiumPurchaseInProgress -> stringResource(Res.string.premium_opening_checkout)
                    storeScreenViewModelState.isPremiumOfferLoading -> stringResource(Res.string.premium_loading_plans)
                    selectedPackage == null -> stringResource(Res.string.premium_plans_unavailable)
                    else -> stringResource(Res.string.premium_try_free)
                },
                enabled = ctaEnabled,
                showProgress = storeScreenViewModelState.isPremiumOfferLoading,
                onClick = onTryPremium,
            )

            val premiumStatusText = localizedPremiumStatusMessage(
                message = storeScreenViewModelState.premiumPurchaseMessage
                    ?: storeScreenViewModelState.premiumLoadError
            ) ?: if (selectedPackage?.hasTrial == true) {
                stringResource(Res.string.premium_trial_cancel)
            } else {
                stringResource(Res.string.premium_cancel_anytime)
            }

            Text(
                text = premiumStatusText,
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            PurchasePolicyLinks(
                onPrivacyClick = { uriHandler.openUri(DreamNorthPrivacyUrl) },
                onTermsClick = { uriHandler.openUri(DreamNorthTermsUrl) },
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.weight(0.55f))
        }
    }
}

@Composable
private fun PurchasePolicyLinks(
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PurchasePolicyLink(text = "Privacy Policy", onClick = onPrivacyClick)
        Text(
            text = "  |  ",
            color = Color.White.copy(alpha = 0.52f),
            fontSize = 12.sp
        )
        PurchasePolicyLink(text = "Terms of Use", onClick = onTermsClick)
    }
}

@Composable
private fun PurchasePolicyLink(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = Color(0xFFFFD8B8),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun localizedPremiumStatusMessage(message: String?): String? {
    return when (message) {
        null -> null
        "Premium plans are loading from the store.",
        "Premium plans are still loading." -> stringResource(Res.string.premium_loading_plans)
        "Premium plans are unavailable. Check RevenueCat products and the local StoreKit subscription configuration." ->
            stringResource(Res.string.premium_plans_unavailable_detail)
        "Premium is active." -> stringResource(Res.string.premium_active)
        else -> message
    }
}

@Composable
private fun PremiumHeroSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(386.dp)
            .padding(top = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "DreamNorth",
            style = TextStyle(
                color = Color.White,
                fontSize = 43.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.78f),
                    offset = Offset(0f, 3f),
                    blurRadius = 13f
                )
            ),
            maxLines = 1
        )
        Text(
            text = stringResource(Res.string.premium_label),
            style = TextStyle(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFFF4FF), Color(0xFFFF6FCB), Color(0xFFFFC36B))
                ),
                fontSize = 54.sp,
                lineHeight = 55.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.76f),
                    offset = Offset(0f, 3f),
                    blurRadius = 14f
                )
            ),
            maxLines = 1
        )
        Text(
                text = stringResource(Res.string.premium_tagline),
                color = Color(0xFFE7E0FF),
                fontSize = 17.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.72f),
                        offset = Offset(0f, 2f),
                        blurRadius = 10f
                    )
                ),
                maxLines = 1
            )
    }
}

@Composable
private fun PremiumBenefitPanel() {
    PremiumBenefitSlider()
}

private data class PremiumBenefitSlide(
    val icon: DrawableResource,
    val title: StringResource,
)

@Composable
private fun PremiumBenefitSlider() {
    val benefits = remember {
        listOf(
            PremiumBenefitSlide(
                icon = Res.drawable.store_premium_icon_interpretations,
                title = Res.string.premium_benefit_deeper_interpretations,
            ),
            PremiumBenefitSlide(
                icon = Res.drawable.store_premium_icon_paintings,
                title = Res.string.premium_benefit_ai_paintings,
            ),
            PremiumBenefitSlide(
                icon = Res.drawable.store_premium_icon_patterns,
                title = Res.string.premium_benefit_pattern_insights,
            ),
            PremiumBenefitSlide(
                icon = Res.drawable.store_premium_icon_ad_free,
                title = Res.string.premium_benefit_ad_free_tools,
            ),
        )
    }
    val pagerState = rememberPagerState(pageCount = { benefits.size })

    LaunchedEffect(pagerState, benefits.size) {
        if (benefits.size <= 1) return@LaunchedEffect
        while (true) {
            delay(5_000L)
            val nextPage = (pagerState.currentPage + 1) % benefits.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081A42).copy(alpha = 0.50f), RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFF9AD6FF).copy(alpha = 0.36f), RoundedCornerShape(20.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            pageSpacing = 10.dp
        ) { page ->
            val benefit = benefits[page]
            PremiumBenefitTile(
                icon = benefit.icon,
                title = stringResource(benefit.title),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color(0xFF020716).copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                .padding(horizontal = 5.dp, vertical = 4.dp)
                .padding(end = 4.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(benefits.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .width(if (selected) 13.dp else 5.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                Color(0xFFFF7BD8)
                            } else {
                                Color.White.copy(alpha = 0.34f)
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun PremiumBenefitTile(
    icon: DrawableResource,
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(58.dp)
    ) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFF111A4D).copy(alpha = 0.42f), RoundedCornerShape(14.dp))
                .padding(start = 70.dp, top = 5.dp, end = 54.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }

        Image(
            painter = painterResource(icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-14).dp, y = (-12).dp)
                .requiredSize(96.dp)
                .zIndex(1f)
        )
    }
}

@Composable
private fun PremiumPlanCard(
    title: String,
    price: String,
    detail: String,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(24.dp)
    val cardHeight = 136.dp
    val contentAlpha = if (enabled) 1f else 0.54f
    Box(
        modifier = modifier.height(cardHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .clip(shape)
                .background(
                    if (selected) {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFB02383).copy(alpha = 0.82f),
                                Color(0xFF432078).copy(alpha = 0.82f),
                                Color(0xFFB9663A).copy(alpha = 0.78f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF102A68).copy(alpha = 0.56f),
                                Color(0xFF140B45).copy(alpha = 0.68f),
                                Color(0xFF25105A).copy(alpha = 0.58f)
                            )
                        )
                    }
                )
                .border(
                    BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected && enabled) Color(0xFFFFD0A3) else Color.White.copy(alpha = 0.14f)
                    ),
                    shape
                )
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (selected) Color(0x66FFD0A3) else Color(0x339C83FF),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    if (!badge.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF8DB4), Color(0xFFFFD0A3))
                                    )
                                )
                                .border(
                                    BorderStroke(1.dp, Color.White.copy(alpha = 0.34f)),
                                    RoundedCornerShape(999.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badge.uppercase(),
                                color = Color(0xFF241337),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    } else {
                        Spacer(Modifier.height(20.dp))
                    }
                    PremiumPlanMarker(selected && enabled)
                }

                Text(
                    text = title,
                    color = Color.White.copy(alpha = contentAlpha),
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = price,
                    style = TextStyle(
                        brush = Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFFFE08B), Color(0xFFFF8E58))
                        ),
                        fontSize = 22.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = detail,
                    color = Color.White.copy(alpha = 0.76f * contentAlpha),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PremiumPlanMarker(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (selected) Color(0xFFFFBFA6) else Color.Transparent)
            .border(
                1.5.dp,
                if (selected) Color(0xFFFFD4B8) else Color.White.copy(alpha = 0.28f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFF231437),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PremiumCtaButton(
    label: String,
    enabled: Boolean,
    showProgress: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (enabled) {
                    Brush.horizontalGradient(listOf(Color(0xFFFF58C6), Color(0xFFFFB05E)))
                } else {
                    Brush.horizontalGradient(
                        listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.08f))
                    )
                }
            )
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.16f else 0.08f), RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.82f),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = label,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StoreDreamTokenHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = stringResource(Res.string.store_get_more),
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = "Dream Tokens",
            style = TextStyle(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFFF3FF), Color(0xFFFF58C6), Color(0xFFFFB05E))
                ),
                fontSize = 47.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            maxLines = 1
        )
    }
}

@Composable
fun DreamBenefitInfoLayout(
    totalDreamTokens: Int = 0,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.64f)
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, Color(0xFFFF4FC3).copy(alpha = 0.58f)), RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(Res.drawable.store_token_benefit_scene),
                contentDescription = stringResource(Res.string.dream_token_benefit_content_description),
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomCenter
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF05001B).copy(alpha = 0.34f),
                                Color(0xFF100533).copy(alpha = 0.20f),
                                Color(0xFF3E0C57).copy(alpha = 0.03f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 18.dp, top = 18.dp, end = 112.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = stringResource(Res.string.five_hundred_dream_tokens),
                        style = TextStyle(
                            brush = Brush.verticalGradient(
                                listOf(Color.White, Color(0xFFFFB092))
                            ),
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        lineHeight = 23.sp,
                        maxLines = 1
                    )
                }
                Text(
                    text = stringResource(Res.string.store_can_unlock),
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(1.dp))
                BenefitRows()
            }
            DreamTokenLayout(
                totalDreamTokens = totalDreamTokens,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            )
        }
    }
}

@Composable
private fun BenefitRows() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CheckAndBenefit(Res.string.dream_token_slide_benefit_1)
        BenefitDivider()
        CheckAndBenefit(Res.string.dream_token_slide_benefit_2)
        BenefitDivider()
        CheckAndBenefit(Res.string.dream_token_slide_benefit_3)
        BenefitDivider()
        CheckAndBenefit(Res.string.dream_token_slide_benefit_4)
    }
}

@Composable
private fun BenefitDivider() {
    Box(
        modifier = Modifier
            .padding(start = 40.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.10f))
    )
}

@Composable
fun CheckAndBenefit(benefit: StringResource) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(27.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFFF9B58), Color(0xFF6F2EE4), Color(0xFF2E126F))
                    )
                )
                .border(1.dp, Color(0xFFFF8F7A).copy(alpha = 0.62f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFFFFD36F),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.size(9.dp))
        Text(
            text = stringResource(benefit),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            color = Color.White,
            maxLines = 1
        )
    }
}
