package org.ballistic.dreamjournalai.shared.dream_main.presentation


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.add_dream
import dreamjournalai.composeapp.shared.generated.resources.animated_heart
import dreamjournalai.composeapp.shared.generated.resources.background_image
import dreamjournalai.composeapp.shared.generated.resources.daily_token_bonus_badge
import dreamjournalai.composeapp.shared.generated.resources.daily_token_bonus_ready
import dreamjournalai.composeapp.shared.generated.resources.daily_token_current_streak_many
import dreamjournalai.composeapp.shared.generated.resources.daily_token_current_streak_one
import dreamjournalai.composeapp.shared.generated.resources.daily_token_day_complete
import dreamjournalai.composeapp.shared.generated.resources.daily_token_day_short
import dreamjournalai.composeapp.shared.generated.resources.daily_token_keep_going
import dreamjournalai.composeapp.shared.generated.resources.daily_token_next_bonus_many
import dreamjournalai.composeapp.shared.generated.resources.daily_token_next_bonus_one
import dreamjournalai.composeapp.shared.generated.resources.daily_token_ready_for_day
import dreamjournalai.composeapp.shared.generated.resources.daily_token_start_streak
import dreamjournalai.composeapp.shared.generated.resources.daily_token_title
import dreamjournalai.composeapp.shared.generated.resources.daily_token_claim_1
import dreamjournalai.composeapp.shared.generated.resources.daily_token_claim_amount
import dreamjournalai.composeapp.shared.generated.resources.daily_token_claimed_today
import dreamjournalai.composeapp.shared.generated.resources.daily_token_claiming
import dreamjournalai.composeapp.shared.generated.resources.daily_token_completed_week_many
import dreamjournalai.composeapp.shared.generated.resources.daily_token_completed_week_one
import dreamjournalai.composeapp.shared.generated.resources.daily_token_countdown_ready
import dreamjournalai.composeapp.shared.generated.resources.daily_token_countdown_ready_body
import dreamjournalai.composeapp.shared.generated.resources.daily_token_countdown_title
import dreamjournalai.composeapp.shared.generated.resources.daily_token_countdown_waiting
import dreamjournalai.composeapp.shared.generated.resources.daily_token_countdown_waiting_body
import dreamjournalai.composeapp.shared.generated.resources.daily_token_premium_double
import dreamjournalai.composeapp.shared.generated.resources.daily_token_store_body
import dreamjournalai.composeapp.shared.generated.resources.daily_token_store_button
import dreamjournalai.composeapp.shared.generated.resources.daily_token_store_title
import dreamjournalai.composeapp.shared.generated.resources.daily_token_token_per_day
import dreamjournalai.composeapp.shared.generated.resources.daily_token_tokens_per_day
import dreamjournalai.composeapp.shared.generated.resources.daily_token_double_rewards
import dreamjournalai.composeapp.shared.generated.resources.daily_token_upgrade
import dreamjournalai.composeapp.shared.generated.resources.daily_token_week_label
import dreamjournalai.composeapp.shared.generated.resources.dream_token_content_description
import dreamjournalai.composeapp.shared.generated.resources.daily_token_streak_title
import dreamjournalai.composeapp.shared.generated.resources.date_prefix
import dreamjournalai.composeapp.shared.generated.resources.dismiss
import dreamjournalai.composeapp.shared.generated.resources.dream_journal_empty_background
import dreamjournalai.composeapp.shared.generated.resources.dream_separator
import dreamjournalai.composeapp.shared.generated.resources.dream_token
import dreamjournalai.composeapp.shared.generated.resources.export_dreams_pdf_filename
import dreamjournalai.composeapp.shared.generated.resources.export_dreams_txt_filename
import dreamjournalai.composeapp.shared.generated.resources.export_failed
import dreamjournalai.composeapp.shared.generated.resources.export_successful
import dreamjournalai.composeapp.shared.generated.resources.free
import dreamjournalai.composeapp.shared.generated.resources.heart
import dreamjournalai.composeapp.shared.generated.resources.journal_membership_gift_bubble
import dreamjournalai.composeapp.shared.generated.resources.must_sign_in
import dreamjournalai.composeapp.shared.generated.resources.others
import dreamjournalai.composeapp.shared.generated.resources.pages
import dreamjournalai.composeapp.shared.generated.resources.premium_active
import dreamjournalai.composeapp.shared.generated.resources.premium_label
import dreamjournalai.composeapp.shared.generated.resources.settings
import dreamjournalai.composeapp.shared.generated.resources.sign_in_action
import dreamjournalai.composeapp.shared.generated.resources.title_prefix
import dreamjournalai.composeapp.shared.generated.resources.transcript_prefix
import dreamjournalai.composeapp.shared.generated.resources.version
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import org.ballistic.dreamjournalai.shared.DrawerCommand
import org.ballistic.dreamjournalai.shared.DrawerController
import org.ballistic.dreamjournalai.shared.ObserveAsEvents
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.analytics.AnalyticsUserProperty
import org.ballistic.dreamjournalai.shared.core.analytics.AppAnalytics
import org.ballistic.dreamjournalai.shared.core.components.ExportDreamsBottomSheet
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.core.platform.getPlatformName
import org.ballistic.dreamjournalai.shared.core.platform.isDebugBuild
import org.ballistic.dreamjournalai.shared.core.platform.rememberDreamExporter
import org.ballistic.dreamjournalai.shared.core.util.ExperimentalFeatureFlags
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.util.OrderType
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_onboarding.data.OnboardingPreferencesRepository
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumAnalytics
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumAnalyticsEvent
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumEntrySource
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPlacement
import org.ballistic.dreamjournalai.shared.dream_premium.domain.repository.PremiumPaywallRepository
import org.ballistic.dreamjournalai.shared.dream_main.presentation.components.BottomNavigation
import org.ballistic.dreamjournalai.shared.dream_main.presentation.components.DreamPetOverlay
import org.ballistic.dreamjournalai.shared.dream_main.presentation.components.DrawerGroupHeading
import org.ballistic.dreamjournalai.shared.dream_main.presentation.components.NotificationPermissionAlertBadge
import org.ballistic.dreamjournalai.shared.dream_main.presentation.components.NotificationPermissionMenuIcon
import org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationDestination
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationNavigationController
import org.ballistic.dreamjournalai.shared.navigation.BottomNavigationRoutes
import org.ballistic.dreamjournalai.shared.navigation.DrawerNavigation
import org.ballistic.dreamjournalai.shared.navigation.Route
import org.ballistic.dreamjournalai.shared.navigation.ScreenGraph
import org.ballistic.dreamjournalai.shared.navigation.matchesDrawerRoute
import org.ballistic.dreamjournalai.shared.navigation.matchesRoute
import org.ballistic.dreamjournalai.shared.navigation.toBottomNavigationRoute
import org.ballistic.dreamjournalai.shared.navigation.toDrawerNavigationRoute
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private var hasPlayedMainBackgroundIntro = false
private var shouldStartMainBackgroundAtFinalPan = false

fun markMainBackgroundIntroPlayed() {
    hasPlayedMainBackgroundIntro = true
    shouldStartMainBackgroundAtFinalPan = false
}

fun prepareMainBackgroundAfterOnboardingExit() {
    hasPlayedMainBackgroundIntro = false
    shouldStartMainBackgroundAtFinalPan = true
}

fun prepareMainBackgroundForFreshPanDown() {
    hasPlayedMainBackgroundIntro = false
    shouldStartMainBackgroundAtFinalPan = false
}

@Composable
fun MainScreenView(
    mainScreenViewModelState: MainScreenViewModelState,
    onMainEvent: (MainScreenEvent) -> Unit,
    onNavigateToOnboardingScreen: () -> Unit = {},
    onNavigateToForcedOnboarding: () -> Unit = {},
    onNavigateToOnboardingLastPage: () -> Unit = {},
    onNavigateToPremiumFlow: (PremiumEntrySource) -> Unit = {},
    resetToDreamJournalRootSignal: Int = 0,
    replayMainBackgroundIntroSignal: Int = 0,
    requestInAppReview: () -> Unit = {},
    onDataLoaded: () -> Unit
) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val notificationPermissionState = rememberNotificationPermissionState()
    val onboardingPreferences = koinInject<OnboardingPreferencesRepository>()
    val premiumPaywallRepository = koinInject<PremiumPaywallRepository>()
    val premiumAnalytics = koinInject<PremiumAnalytics>()
    val appAnalytics = koinInject<AppAnalytics>()
    var checkedPremiumEntry by remember { mutableStateOf(false) }
    var premiumEntryAvailable by remember { mutableStateOf(false) }
    var isPremiumMember by remember { mutableStateOf(false) }
    var giftBubbleText by remember { mutableStateOf("Dream Gift") }
    var giftBubbleEnabled by remember { mutableStateOf(true) }
    var dailyTokenUtcDays by remember { mutableStateOf(currentDailyTokenUtcDays()) }
    val isIos = getPlatformName() == "iOS"
    val requiresSignIn = mainScreenViewModelState.isUserAnonymous || Firebase.auth.currentUser == null

    suspend fun refreshPremiumMembershipState(): Boolean {
        val currentUser = Firebase.auth.currentUser ?: run {
            appAnalytics.setUserId(null)
            appAnalytics.setUserProperty(AnalyticsUserProperty.AccountType, "signed_out")
            appAnalytics.setUserProperty(AnalyticsUserProperty.PremiumStatus, "free")
            isPremiumMember = false
            return false
        }
        appAnalytics.setUserId(currentUser.uid)
        if (currentUser.isAnonymous) {
            appAnalytics.setUserProperty(AnalyticsUserProperty.AccountType, "anonymous")
            appAnalytics.setUserProperty(AnalyticsUserProperty.PremiumStatus, "free")
            isPremiumMember = false
            return false
        }
        appAnalytics.setUserProperty(AnalyticsUserProperty.AccountType, "registered")

        return runCatching {
            premiumPaywallRepository.syncAppUser(currentUser.uid)
            val customerInfo = premiumPaywallRepository.getCurrentCustomerInfo()
            premiumPaywallRepository.hasPremiumEntitlement(customerInfo)
        }.getOrDefault(false).also { hasPremium ->
            isPremiumMember = hasPremium
            appAnalytics.setUserProperty(
                AnalyticsUserProperty.PremiumStatus,
                if (hasPremium) "active" else "free"
            )
            if (hasPremium) {
                premiumEntryAvailable = false
            }
        }
    }

    LaunchedEffect(Unit) {
        onDataLoaded()
    }

    LaunchedEffect(Unit) {
        while (true) {
            dailyTokenUtcDays = currentDailyTokenUtcDays()
            delay(60_000)
        }
    }

    LaunchedEffect(Unit) {
        onMainEvent(MainScreenEvent.GetAuthState)
        onMainEvent(MainScreenEvent.UserInteracted)
        DrawerController.enable()
    }

    LaunchedEffect(Unit) {
        if (checkedPremiumEntry) return@LaunchedEffect
        checkedPremiumEntry = true

        val currentUser = Firebase.auth.currentUser ?: return@LaunchedEffect
        if (currentUser.isAnonymous) return@LaunchedEffect
        if (isIos) return@LaunchedEffect

        val alreadyPremium = refreshPremiumMembershipState()
        if (alreadyPremium) return@LaunchedEffect

        val completionMode = onboardingPreferences.completionMode.first()
        if (completionMode == "premium_unlocked") return@LaunchedEffect

        val offering = runCatching {
            premiumPaywallRepository.getPlacementOffering(PremiumPlacement.PostAuthPrimary.placementId)
        }.getOrNull()

        if (offering == null || offering.availablePackages.isEmpty()) {
            premiumEntryAvailable = false
            premiumAnalytics.track(PremiumAnalyticsEvent.OfferingMissing(PremiumPlacement.PostAuthPrimary))
            return@LaunchedEffect
        }

        premiumEntryAvailable = true
        giftBubbleText = (offering.metadata["promo_badge_text"] as? String)
            ?.takeIf { it.isNotBlank() }
            ?: "Dream Gift"
        giftBubbleEnabled = when (val value = offering.metadata["giftbox_enabled"]) {
            is Boolean -> value
            is String -> value.equals("true", ignoreCase = true)
            else -> true
        }
    }

    var showExportSheet by remember { mutableStateOf(false) }
    val dreamExporter = rememberDreamExporter()
    var dreamsToExport by remember { mutableStateOf<List<Dream>>(emptyList()) }

    val exportDreamsPdfFilename = stringResource(Res.string.export_dreams_pdf_filename)
    val exportSuccessfulMessage = StringValue.Resource(Res.string.export_successful)
    val exportFailedMessage = StringValue.Resource(Res.string.export_failed)
    val exportDreamsTxtFilename = stringResource(Res.string.export_dreams_txt_filename)

    val titlePrefix = stringResource(Res.string.title_prefix)
    val datePrefix = stringResource(Res.string.date_prefix)
    val transcriptPrefix = stringResource(Res.string.transcript_prefix)
    val dreamSeparator = stringResource(Res.string.dream_separator)
    val dismissString = StringValue.Resource(Res.string.dismiss)

    if (showExportSheet) {
        LaunchedEffect(Unit) {
            onMainEvent(MainScreenEvent.GetAllDreamsForExport(OrderType.Descending) { dreams ->
                dreamsToExport = dreams
            })
        }
        ExportDreamsBottomSheet(
            onPdfClick = {
                showExportSheet = false
                dreamExporter.exportToPdf(dreamsToExport, exportDreamsPdfFilename) { success ->
                    val message = if (success) exportSuccessfulMessage else exportFailedMessage
                    coroutineScope.launch { // Launch in coroutineScope for suspend function
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = message,
                                action = SnackbarAction(name = dismissString, action = {}), // Use dismissString
                            )
                        )
                    }
                }
            },
            onTxtClick = {
                showExportSheet = false
                val formattedDreams = formatDreams(
                    dreams = dreamsToExport,
                    titlePrefix = titlePrefix,
                    datePrefix = datePrefix,
                    transcriptPrefix = transcriptPrefix,
                    dreamSeparator = dreamSeparator
                )
                dreamExporter.exportToTxt(formattedDreams, exportDreamsTxtFilename) { success ->
                    val message = if (success) exportSuccessfulMessage else exportFailedMessage
                    coroutineScope.launch { // Launch in coroutineScope for suspend function
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = message,
                                action = SnackbarAction(name = dismissString, action = {}), // Use dismissString
                            )
                        )
                    }
                }
            },
            onClickOutside = {
                showExportSheet = false
            }
        )
    }

    val navController = rememberNavController()
    val drawerGroups = remember {
        listOf(
            DrawerGroup(
                title = Res.string.pages,
                items = listOf(
                    DrawerNavigation.DreamJournalScreen,
                    DrawerNavigation.StoreScreen,
                    DrawerNavigation.DailyLessons,
                    DrawerNavigation.Favorites,
                    DrawerNavigation.Nightmares,
                    DrawerNavigation.DreamToolGraphScreen,
                    DrawerNavigation.Statistics,
                    DrawerNavigation.Symbol,
                )
            ),
            DrawerGroup(
                title = Res.string.settings,
                items = buildList {
                    add(DrawerNavigation.AccountSettings)
                    add(DrawerNavigation.NotificationSettings)
                    if (isDebugBuild()) {
                        add(DrawerNavigation.DeveloperTools)
                    }
                    //    Screens.DreamSettings,
                }
            ),
            DrawerGroup(
                title = Res.string.others,
                items = listOf(
                    DrawerNavigation.ExportDreams,
                    DrawerNavigation.RateMyApp,
                    //  Screens.AboutMe
                )
            )
        )
    }
    val selectedItem = remember { mutableStateOf(drawerGroups.first().items.first()) }
    val drawerItems = remember(drawerGroups) {
        drawerGroups.flatMap { it.items }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var pendingNotificationRoute by remember { mutableStateOf<Route?>(null) }
    var previousRoute by remember { mutableStateOf<String?>(null) }
    var storeExitDrawerGuard by remember { mutableStateOf(false) }
    var localReplayMainBackgroundIntroSignal by remember { mutableStateOf(0) }
    var selectedBottomRoute by remember {
        mutableStateOf<BottomNavigationRoutes?>(BottomNavigationRoutes.DreamJournalScreen)
    }
    var addDreamNavigationInFlight by remember { mutableStateOf(false) }
    val isStoreRoute = currentRoute.matchesRoute(Route.StoreScreen())

    fun navigateToAccountSettings() {
        selectedItem.value = DrawerNavigation.AccountSettings
        selectedBottomRoute = null
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(false))
        navController.navigate(Route.AccountSettings) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun showSignInRequiredSnackbar() {
        coroutineScope.launch {
            SnackbarController.sendEvent(
                SnackbarEvent(
                    message = StringValue.Resource(Res.string.must_sign_in),
                    action = SnackbarAction(StringValue.Resource(Res.string.sign_in_action)) {
                        navigateToAccountSettings()
                    },
                    duration = SnackbarDuration.Long
                )
            )
        }
    }

    fun requestPremiumFlow(entrySource: PremiumEntrySource) {
        if (requiresSignIn) {
            showSignInRequiredSnackbar()
        } else {
            onNavigateToPremiumFlow(entrySource)
        }
    }

    fun navigateToDreamJournalRoot() {
        selectedItem.value = DrawerNavigation.DreamJournalScreen
        selectedBottomRoute = BottomNavigationRoutes.DreamJournalScreen
        navController.navigate(Route.DreamJournalScreen) {
            popUpTo(Route.DreamJournalScreen) {
                inclusive = false
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun replayMainBackgroundIntro() {
        hasPlayedMainBackgroundIntro = false
        shouldStartMainBackgroundAtFinalPan = false
        onMainEvent(MainScreenEvent.SetAuthTransitionInProgress(false))
        onMainEvent(MainScreenEvent.SetMainContentHandoffInProgress(false))
        onMainEvent(MainScreenEvent.SetBackgroundIntroComplete(false))
        onMainEvent(MainScreenEvent.SetBackgroundBlurComplete(false))
        localReplayMainBackgroundIntroSignal += 1
    }

    fun completeMainAuthTransitionWithoutReplay() {
        markMainBackgroundIntroPlayed()
        onMainEvent(MainScreenEvent.SetBackgroundIntroComplete(true))
        onMainEvent(MainScreenEvent.SetBackgroundBlurComplete(true))
        onMainEvent(MainScreenEvent.SetAuthTransitionInProgress(false))
        onMainEvent(MainScreenEvent.SetMainContentHandoffInProgress(false))
    }

    LaunchedEffect(resetToDreamJournalRootSignal) {
        if (resetToDreamJournalRootSignal > 0) {
            navigateToDreamJournalRoot()
        }
    }

    fun navigateToMainRoute(route: Route) {
        if (route == Route.DreamJournalScreen) {
            navigateToDreamJournalRoot()
            return
        }

        route.toDrawerNavigationRoute()?.let { selectedItem.value = it }
        selectedBottomRoute = route.toBottomNavigationRoute()
        navController.navigate(route) {
            popUpTo(Route.DreamJournalScreen) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openNotificationRoute(route: Route) {
        if (currentRoute.matchesRoute(Route.AddEditDreamScreen(dreamID = "", backgroundID = -1))) {
            pendingNotificationRoute = route
        } else {
            navigateToMainRoute(route)
        }
    }

    fun syncNavigationSelection(route: String?) {
        if (route == null) return

        val matchedScreen = drawerItems.firstOrNull { route.matchesDrawerRoute(it.route) }
        val matchedBottomRoute = route.toBottomNavigationRoute()

        matchedScreen?.let { selectedItem.value = it }
        selectedBottomRoute = when {
            matchedBottomRoute != null -> matchedBottomRoute
            matchedScreen != null ||
                route.matchesRoute(Route.AddEditDreamScreen(dreamID = "", backgroundID = -1)) ||
                route.matchesRoute(Route.FullScreenImageScreen(imageID = "")) ||
                route.matchesRoute(Route.DailyTokensScreen) ||
                route.matchesRoute(Route.DailyLessonDetailScreen(lessonId = "")) -> null
            else -> selectedBottomRoute
        }
    }

    LaunchedEffect(currentRoute) {
        if (!currentRoute.matchesRoute(Route.AddEditDreamScreen(dreamID = "", backgroundID = -1))) {
            addDreamNavigationInFlight = false
        }
        refreshPremiumMembershipState()
    }

    LaunchedEffect(currentRoute) {
        syncNavigationSelection(currentRoute)
    }

    LaunchedEffect(currentRoute) {
        val wasStoreRoute = previousRoute.matchesRoute(Route.StoreScreen())
        previousRoute = currentRoute

        when {
            isStoreRoute -> storeExitDrawerGuard = true
            wasStoreRoute -> {
                storeExitDrawerGuard = true
                delay(650)
                storeExitDrawerGuard = false
            }
            else -> storeExitDrawerGuard = false
        }
    }

    LaunchedEffect(currentRoute) {
        NotificationNavigationController.destinations.collectLatest { pendingDestination ->
            val request = pendingDestination ?: return@collectLatest
            val destination = request.destination
            val route = when (destination) {
                NotificationDestination.DailyTokens -> Route.DailyTokensScreen
                NotificationDestination.DreamJournal -> Route.AddEditDreamScreen(
                    dreamID = request.dreamId,
                    backgroundID = -1
                )
                NotificationDestination.RealityCheck -> Route.DreamJournalScreen
                NotificationDestination.PaintDreamWorld -> Route.PaintDreamWorld
                NotificationDestination.Store -> Route.StoreScreen()
            }
            openNotificationRoute(route)
            NotificationNavigationController.clear(request)
        }
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            syncNavigationSelection(destination.route)
        }

        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    val startBackgroundAtFinalPan = remember { shouldStartMainBackgroundAtFinalPan }
    val backgroundBiasY = remember {
        Animatable(if (hasPlayedMainBackgroundIntro || startBackgroundAtFinalPan) 1f else -1f)
    }
    val backgroundBlur = remember {
        Animatable(if (hasPlayedMainBackgroundIntro) 15f else 0f)
    }
    val backgroundAlpha = remember {
        Animatable(1f)
    }
    var isLocalBackgroundIntroReady by remember {
        mutableStateOf(hasPlayedMainBackgroundIntro || startBackgroundAtFinalPan)
    }
    var displayedBackgroundResource by remember {
        mutableStateOf(mainScreenViewModelState.backgroundResource)
    }
    var outgoingBackgroundResource by remember {
        mutableStateOf(mainScreenViewModelState.backgroundResource)
    }
    val backgroundResourceTransition = remember { Animatable(1f) }
    val displayedBackgroundBiasY = remember {
        Animatable(
            if (mainScreenViewModelState.backgroundResource == Res.drawable.dream_journal_empty_background) {
                -1f
            } else {
                1f
            }
        )
    }
    var pinEmptyBackgroundAtTop by remember { mutableStateOf(false) }

    LaunchedEffect(mainScreenViewModelState.isBackgroundResourceResolved) {
        if (!mainScreenViewModelState.isBackgroundResourceResolved) return@LaunchedEffect

        if (hasPlayedMainBackgroundIntro) {
            backgroundBiasY.snapTo(1f)
            backgroundBlur.snapTo(15f)
            backgroundAlpha.snapTo(1f)
            isLocalBackgroundIntroReady = true
            onMainEvent(MainScreenEvent.SetBackgroundIntroComplete(true))
            onMainEvent(MainScreenEvent.SetBackgroundBlurComplete(true))
            return@LaunchedEffect
        }

        if (startBackgroundAtFinalPan) {
            backgroundBiasY.snapTo(1f)
            backgroundAlpha.snapTo(1f)
            shouldStartMainBackgroundAtFinalPan = false
            isLocalBackgroundIntroReady = true
            onMainEvent(MainScreenEvent.SetBackgroundIntroComplete(true))
            backgroundBlur.animateTo(
                targetValue = 15f,
                animationSpec = tween(durationMillis = 420, easing = LinearEasing)
            )
            hasPlayedMainBackgroundIntro = true
            onMainEvent(MainScreenEvent.SetBackgroundBlurComplete(true))
            return@LaunchedEffect
        }

        backgroundAlpha.snapTo(1f)
        backgroundBiasY.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2200,
                easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
            )
        )
        isLocalBackgroundIntroReady = true
        onMainEvent(MainScreenEvent.SetBackgroundIntroComplete(true))
        backgroundBlur.animateTo(
            targetValue = 15f,
            animationSpec = tween(durationMillis = 420, easing = LinearEasing)
        )
        hasPlayedMainBackgroundIntro = true
        onMainEvent(MainScreenEvent.SetBackgroundBlurComplete(true))
    }

    LaunchedEffect(replayMainBackgroundIntroSignal, localReplayMainBackgroundIntroSignal) {
        if (replayMainBackgroundIntroSignal <= 0 && localReplayMainBackgroundIntroSignal <= 0) return@LaunchedEffect

        hasPlayedMainBackgroundIntro = false
        shouldStartMainBackgroundAtFinalPan = false
        isLocalBackgroundIntroReady = false
        onMainEvent(MainScreenEvent.SetBackgroundIntroComplete(false))
        onMainEvent(MainScreenEvent.SetBackgroundBlurComplete(false))

        backgroundBlur.snapTo(0f)
        backgroundBiasY.snapTo(-1f)
        backgroundAlpha.snapTo(1f)
        backgroundBiasY.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2200,
                easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
            )
        )
        isLocalBackgroundIntroReady = true
        onMainEvent(MainScreenEvent.SetBackgroundIntroComplete(true))
        backgroundBlur.animateTo(
            targetValue = 15f,
            animationSpec = tween(durationMillis = 420, easing = LinearEasing)
        )
        hasPlayedMainBackgroundIntro = true
        onMainEvent(MainScreenEvent.SetBackgroundBlurComplete(true))
    }

    LaunchedEffect(
        mainScreenViewModelState.isBackgroundResourceResolved,
        mainScreenViewModelState.backgroundResource
    ) {
        if (!mainScreenViewModelState.isBackgroundResourceResolved) return@LaunchedEffect
        val nextResource = mainScreenViewModelState.backgroundResource
        if (nextResource == displayedBackgroundResource) return@LaunchedEffect

        outgoingBackgroundResource = displayedBackgroundResource
        displayedBackgroundResource = nextResource
        val targetBiasY = if (nextResource == Res.drawable.dream_journal_empty_background) -1f else 1f
        pinEmptyBackgroundAtTop = nextResource == Res.drawable.dream_journal_empty_background
        displayedBackgroundBiasY.snapTo(targetBiasY)
        if (!isLocalBackgroundIntroReady || !mainScreenViewModelState.isBackgroundIntroComplete) {
            backgroundResourceTransition.snapTo(1f)
            outgoingBackgroundResource = displayedBackgroundResource
            return@LaunchedEffect
        }

        backgroundResourceTransition.snapTo(0f)
        backgroundResourceTransition.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = if (mainScreenViewModelState.isAuthTransitionInProgress) 520 else 820,
                easing = FastOutSlowInEasing
            )
        )
        outgoingBackgroundResource = displayedBackgroundResource
    }

    val backgroundImageBaseModifier = Modifier
        .fillMaxSize()
        .blur(backgroundBlur.value.dp)

    if (mainScreenViewModelState.isBackgroundResourceResolved) {
        val currentEmptyBackgroundBiasY =
            if (pinEmptyBackgroundAtTop) displayedBackgroundBiasY.value else backgroundBiasY.value

        if (backgroundResourceTransition.value < 1f) {
            Image(
                painter = painterResource(outgoingBackgroundResource),
                contentScale = ContentScale.Crop,
                alignment = VerticalBiasAlignment(backgroundBiasY.value),
                modifier = backgroundImageBaseModifier.graphicsLayer {
                    alpha = backgroundAlpha.value * (1f - backgroundResourceTransition.value)
                },
                contentDescription = stringResource(Res.string.background_image)
            )
        }

        Image(
            painter = painterResource(displayedBackgroundResource),
            contentScale = ContentScale.Crop,
            alignment = VerticalBiasAlignment(
                if (displayedBackgroundResource == Res.drawable.dream_journal_empty_background) {
                    currentEmptyBackgroundBiasY
                } else {
                    backgroundBiasY.value
                }
            ),
            modifier = backgroundImageBaseModifier.graphicsLayer {
                alpha = backgroundAlpha.value * backgroundResourceTransition.value
            },
            contentDescription = stringResource(Res.string.background_image)
        )

        val emptyMaskProgress = (backgroundBlur.value / 15f).coerceIn(0f, 1f)
        val emptyMaskEasedAlpha = (
            emptyMaskProgress * emptyMaskProgress * (3f - 2f * emptyMaskProgress)
            ) * backgroundAlpha.value * backgroundResourceTransition.value * 0.82f
        if (
            currentRoute.matchesRoute(Route.DreamJournalScreen) &&
            mainScreenViewModelState.isBackgroundIntroComplete &&
            displayedBackgroundResource == Res.drawable.dream_journal_empty_background &&
            emptyMaskEasedAlpha > 0.001f
        ) {
            Image(
                painter = painterResource(Res.drawable.dream_journal_empty_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = VerticalBiasAlignment(currentEmptyBackgroundBiasY),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = emptyMaskEasedAlpha
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithCache {
                        val clearCenter = Offset(size.width * 0.5f, size.height * 0.74f)
                        val clearRadius = size.minDimension * 0.48f
                        val focusMask = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.White,
                                0.42f to Color.White.copy(alpha = 0.95f),
                                0.72f to Color.White.copy(alpha = 0.38f),
                                1.0f to Color.Transparent,
                            ),
                            center = clearCenter,
                            radius = clearRadius
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush = focusMask, blendMode = BlendMode.DstIn)
                        }
                    }
            )
        }
    }

    // Local drawer state owned by the composable for Compose stability. The ViewModel exposes
    // an intent flag `isDrawerOpen`; we react to it below.
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val isDrawerEnabled by DrawerController.isEnabled.collectAsState()
    val drawerGesturesEnabled = isDrawerEnabled && !isStoreRoute && !storeExitDrawerGuard

    // Track when we're driving the drawer programmatically to avoid echo-loops
    val programmaticChangeInProgress = remember { mutableStateOf(false) }

    // Drive the drawer from a simple controller to avoid VM<->UI races
    ObserveAsEvents(
        flow = DrawerController.events,
        key1 = drawerState,
        key2 = Triple(isDrawerEnabled, isStoreRoute, storeExitDrawerGuard)
    ) { cmd ->
        Logger.d("MainScreen") { "DrawerCommand: $cmd | current=${drawerState.currentValue} anim=${drawerState.isAnimationRunning} | programmatic=${programmaticChangeInProgress.value}" }
        programmaticChangeInProgress.value = true
        coroutineScope.launch {
            try {
                when (cmd) {
                    is DrawerCommand.Open -> {
                        if (isDrawerEnabled && !isStoreRoute && !storeExitDrawerGuard && drawerState.currentValue != DrawerValue.Open) {
                            drawerState.open()
                        }
                    }
                    is DrawerCommand.Close -> if (drawerState.currentValue != DrawerValue.Closed) drawerState.close()
                    is DrawerCommand.Toggle -> {
                        if (drawerState.currentValue == DrawerValue.Open) {
                            drawerState.close()
                        } else if (isDrawerEnabled && !isStoreRoute && !storeExitDrawerGuard) {
                            drawerState.open()
                        }
                    }
                }
            } finally {
                programmaticChangeInProgress.value = false
            }
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarHandler(snackbarHostState = snackbarHostState)
    val drawerDailyTokenProgress = dailyTokenTrackerProgress(
        streak = mainScreenViewModelState.dailyTokenStreak,
        lastClaimDay = mainScreenViewModelState.lastDailyTokenClaimDay,
        todayUtcDay = dailyTokenUtcDays.first,
        yesterdayUtcDay = dailyTokenUtcDays.second,
        hasClaimedToday = mainScreenViewModelState.hasClaimedDailyToken,
    )
    val shouldShowMainContent =
        isLocalBackgroundIntroReady &&
            mainScreenViewModelState.isBackgroundIntroComplete &&
            mainScreenViewModelState.isBackgroundBlurComplete &&
            !mainScreenViewModelState.isMainContentHandoffInProgress
    val revealDensity = LocalDensity.current
    val mainContentAlpha by animateFloatAsState(
        targetValue = if (shouldShowMainContent) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (shouldShowMainContent) 460 else 220,
            delayMillis = if (shouldShowMainContent) 30 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "main-content-reveal-alpha"
    )
    val mainContentScale by animateFloatAsState(
        targetValue = if (shouldShowMainContent) 1f else 0.985f,
        animationSpec = tween(
            durationMillis = if (shouldShowMainContent) 500 else 220,
            delayMillis = if (shouldShowMainContent) 30 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "main-content-reveal-scale"
    )
    val mainContentOffsetY by animateFloatAsState(
        targetValue = if (shouldShowMainContent) 0f else with(revealDensity) { 18.dp.toPx() },
        animationSpec = tween(
            durationMillis = if (shouldShowMainContent) 500 else 220,
            delayMillis = if (shouldShowMainContent) 30 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "main-content-reveal-y"
    )


    val mainContentModifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
            alpha = mainContentAlpha
            scaleX = mainContentScale
            scaleY = mainContentScale
            translationY = mainContentOffsetY
        }
    val activeDrawerItem = drawerItems.firstOrNull { currentRoute.matchesDrawerRoute(it.route) }
        ?: selectedItem.value
    val activeBottomRoute = currentRoute.toBottomNavigationRoute() ?: selectedBottomRoute

    Box(
        modifier = mainContentModifier
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerGesturesEnabled && shouldShowMainContent,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.fillMaxHeight()) {
                    Column(modifier = Modifier.fillMaxHeight()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(Modifier.height(12.dp))

                            DailyDreamTokensDrawerSection(
                                dreamTokens = mainScreenViewModelState.dreamTokens,
                                requiresSignIn = requiresSignIn,
                                isPremium = isPremiumMember,
                                tokensClaimedToday = mainScreenViewModelState.dailyTokensClaimedToday,
                                trackerStatusText = dailyTokenTrackerStatusText(drawerDailyTokenProgress),
                                isClaiming = mainScreenViewModelState.isDailyTokenClaimInProgress,
                                onOpenPage = {
                                    onMainEvent(MainScreenEvent.TriggerVibration)
                                    coroutineScope.launch { drawerState.close() }
                                    if (requiresSignIn) {
                                        showSignInRequiredSnackbar()
                                    } else {
                                        selectedItem.value = DrawerNavigation.DreamJournalScreen
                                        selectedBottomRoute = null
                                        navController.navigate(Route.DailyTokensScreen) {
                                            popUpTo(Route.DreamJournalScreen) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                onClaim = {
                                    onMainEvent(MainScreenEvent.TriggerVibration)
                                    if (requiresSignIn) {
                                        coroutineScope.launch { drawerState.close() }
                                        showSignInRequiredSnackbar()
                                    } else {
                                        onMainEvent(MainScreenEvent.ClaimDailyDreamTokens(isPremiumMember))
                                    }
                                }
                            )

                            drawerGroups.forEach { group ->
                                DrawerGroupHeading(title = group.title)

                                group.items.forEach { item ->
                                    NavigationDrawerItem(
                                        icon = {
                                            if (item == DrawerNavigation.RateMyApp) {
                                                AnimatedHeartIcon(animate = drawerState.currentValue == DrawerValue.Open)
                                            } else {
                                                Icon(
                                                    item.icon,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        label = {
                                            item.title?.let { Text(stringResource(it)) }
                                        },
                                        badge = if (item == DrawerNavigation.AccountSettings && isPremiumMember) {
                                            { DrawerPremiumIndicator() }
                                        } else if (item == DrawerNavigation.NotificationSettings && notificationPermissionState.shouldShowPrompt) {
                                            { NotificationPermissionAlertBadge() }
                                        } else {
                                            null
                                        },
                                        selected = item == activeDrawerItem,
                                        onClick = {
                                            onMainEvent(MainScreenEvent.TriggerVibration)
                                            coroutineScope.launch {
                                                drawerState.close()
                                            }

                                            if (item != DrawerNavigation.ExportDreams) {
                                                selectedItem.value = item
                                            }

                                            when (item) {
                                                DrawerNavigation.RateMyApp -> {
                                                    onMainEvent(MainScreenEvent.OpenStoreLink)
                                                }
                                                DrawerNavigation.ExportDreams -> {
                                                    showExportSheet = true
                                                }
                                                else -> {
                                                    navigateToMainRoute(item.route)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(Res.string.version),
                            color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                            modifier = Modifier
                                .padding(bottom = 16.dp, top = 8.dp)
                                .align(Alignment.CenterHorizontally),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            content = {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            snackbarHostState,
                            modifier = Modifier.imePadding()
                        )
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = mainScreenViewModelState.scaffoldState.bottomBarState,
                            enter = slideInVertically(initialOffsetY = { it + 100 }),
                            exit = slideOutVertically(targetOffsetY = { it + 100 })
                        ) {
                            val bottomPadding = if (isIos) 0.dp else 16.dp
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp, bottom = bottomPadding)
                                    .navigationBarsPadding()
                                    .height(72.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {

                                // Glassmorphic background with gradient
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(128, 0, 128, 153), // Vibrant Purple with transparency
                                                    Color(255, 20, 147, 128),  // Deep Pink with transparency
                                                    Color(128, 0, 128, 153) // Vibrant Purple with transparency
                                                )
                                            )
                                        )
                                        .blur(10.dp)
                                )

                                // BottomNavigation aligned to the bottom of the Box
                                BottomNavigation(
                                    selectedRoute = activeBottomRoute,
                                    isNavigationEnabled = mainScreenViewModelState.isBottomBarEnabledState,
                                    onMainEvent = onMainEvent,
                                    onNavigate = { route ->
                                        navigateToMainRoute(route)
                                    },
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )

                                // FAB aligned to the top-center of the Box
                                FloatingActionButton(
                                    onClick = {
                                        if (mainScreenViewModelState.isBottomBarEnabledState && !addDreamNavigationInFlight) {
                                            addDreamNavigationInFlight = true
                                            onMainEvent(MainScreenEvent.TriggerVibration)
                                            // Temporarily disable bottom navigation to avoid rapid taps navigating elsewhere
                                            onMainEvent(MainScreenEvent.SetBottomBarEnabledState(false))
                                            selectedBottomRoute = null
                                            navController.navigate(
                                                Route.AddEditDreamScreen(
                                                    dreamID = "",
                                                    backgroundID = -1
                                                )
                                            ) {
                                                popUpTo(Route.DreamJournalScreen) {
                                                    saveState = false
                                                }
                                                launchSingleTop = true
                                            }
                                            // Re-enable bottom bar shortly after navigation starts
                                            coroutineScope.launch {
                                                delay(700)
                                                onMainEvent(MainScreenEvent.SetBottomBarEnabledState(true))
                                            }
                                        }
                                    },
                                    elevation = FloatingActionButtonDefaults.elevation(3.dp, 4.dp),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-18).dp)
                                        .size(68.dp),
                                    containerColor = Color.Transparent
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(255, 105, 180),
                                                        Color(110, 40, 110)
                                                    )
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Add,
                                            tint = Color.White,
                                            contentDescription = stringResource(Res.string.add_dream),
                                            modifier = Modifier.size(32.dp)
                                        )
                                     }
                                }
                            }
                        }
                    },
                    containerColor = Color.Transparent,
                ) { innerPadding ->
                    val innerTopPadding = innerPadding.calculateTopPadding()
                    val innerBottomPadding = innerPadding.calculateBottomPadding()
                    LaunchedEffect(innerTopPadding, innerBottomPadding) {
                        onMainEvent(MainScreenEvent.UpdatePaddingValues(innerPadding))
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        ScreenGraph(
                            navControllerProvider = { navController },
                            bottomPaddingValue = mainScreenViewModelState.paddingValues.calculateBottomPadding(),
                            mainScreenViewModelState = mainScreenViewModelState,
                            isPremiumMember = isPremiumMember,
                            onMainEvent = { onMainEvent(it) },
                            onNavigateToOnboardingScreen = { onNavigateToOnboardingScreen() },
                            onNavigateToForcedOnboarding = { onNavigateToForcedOnboarding() },
                            onNavigateToOnboardingLastPage = { onNavigateToOnboardingLastPage() },
                            onNavigateToPremiumFlow = { entrySource ->
                                requestPremiumFlow(entrySource)
                            },
                            onSignInRequired = {
                                showSignInRequiredSnackbar()
                            },
                            pendingNotificationRoute = pendingNotificationRoute,
                            onPendingNotificationRouteHandled = {
                                pendingNotificationRoute = null
                            },
                            onDreamJournalRootSelected = {
                                selectedItem.value = DrawerNavigation.DreamJournalScreen
                                selectedBottomRoute = BottomNavigationRoutes.DreamJournalScreen
                            },
                            onAuthenticatedToDreamJournal = {
                                completeMainAuthTransitionWithoutReplay()
                                selectedItem.value = DrawerNavigation.DreamJournalScreen
                                selectedBottomRoute = BottomNavigationRoutes.DreamJournalScreen
                            },
                            onDreamToolsRootSelected = {
                                selectedItem.value = DrawerNavigation.DreamToolGraphScreen
                                selectedBottomRoute = BottomNavigationRoutes.DreamToolGraphScreen
                            },
                            requestInAppReview = requestInAppReview
                        )

                        if (ExperimentalFeatureFlags.FloatingDreamPetEnabled) {
                            DreamPetOverlay(modifier = Modifier.fillMaxSize())
                        }

                        val shouldShowGiftBubble =
                            ExperimentalFeatureFlags.JournalGiftBubbleEnabled &&
                                premiumEntryAvailable &&
                                giftBubbleEnabled &&
                                currentRoute.matchesRoute(Route.DreamJournalScreen)

                        AnimatedVisibility(
                            visible = shouldShowGiftBubble,
                            enter = fadeIn(animationSpec = tween(260)) + slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(420, easing = FastOutSlowInEasing)
                            ),
                            exit = fadeOut(animationSpec = tween(180)),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            MembershipGiftBubble(
                                badgeText = giftBubbleText,
                                onClick = {
                                    onMainEvent(MainScreenEvent.TriggerVibration)
                                    requestPremiumFlow(PremiumEntrySource.JournalGiftBubble)
                                }
                            )
                        }
                    }

                }
            }
        )
    }
}

private data class VerticalBiasAlignment(
    val verticalBias: Float
) : Alignment {
    override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset {
        val x = (space.width - size.width) / 2
        val biasFraction = (verticalBias + 1f) / 2f
        val y = ((space.height - size.height) * biasFraction).toInt()
        return IntOffset(x, y)
    }
}

private data class DailyTokenTrackerProgress(
    val displayDay: Int,
    val completedCycleDays: Int,
    val highlightedCycleDay: Int,
    val hasClaimedToday: Boolean,
    val displayWeek: Int,
    val completedWeeks: Int,
)

private const val DailyTokenStreakBonusInterval = 7

private const val DailyTokenDayMillis = 86_400_000L

@OptIn(ExperimentalTime::class)
private fun currentDailyTokenUtcDays(): Pair<String, String> {
    val today = kotlin.time.Clock.System.todayIn(TimeZone.UTC)
    return today.toString() to today.minus(DatePeriod(days = 1)).toString()
}

private fun dailyTokenSecondsUntilUtcReset(): Long {
    val nowMillis = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val millisIntoUtcDay = ((nowMillis % DailyTokenDayMillis) + DailyTokenDayMillis) % DailyTokenDayMillis
    val millisRemaining = DailyTokenDayMillis - millisIntoUtcDay
    return (millisRemaining / 1_000L).coerceAtLeast(0L)
}

private fun dailyTokenCountdownText(totalSeconds: Long): String {
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return listOf(hours, minutes, seconds).joinToString(":") {
        it.toString().padStart(2, '0')
    }
}

private fun dailyTokenTrackerProgress(
    streak: Int,
    lastClaimDay: String?,
    todayUtcDay: String,
    yesterdayUtcDay: String,
    hasClaimedToday: Boolean,
): DailyTokenTrackerProgress {
    val currentStreak = streak.coerceAtLeast(0)
    val displayStreak = when {
        hasClaimedToday -> currentStreak.coerceAtLeast(1)
        lastClaimDay == yesterdayUtcDay && currentStreak > 0 -> currentStreak + 1
        else -> 1
    }
    val completedStreak = when {
        hasClaimedToday -> currentStreak
        lastClaimDay == yesterdayUtcDay -> currentStreak
        lastClaimDay == todayUtcDay -> currentStreak
        else -> 0
    }.coerceAtLeast(0)
    val displayDay = ((displayStreak - 1) % DailyTokenStreakBonusInterval) + 1
    val completedCycleDays = when {
        completedStreak <= 0 -> 0
        completedStreak % DailyTokenStreakBonusInterval == 0 && !hasClaimedToday -> 0
        else -> {
            val cycleDay = completedStreak % DailyTokenStreakBonusInterval
            if (cycleDay == 0) DailyTokenStreakBonusInterval else cycleDay
        }
    }
    val highlightedCycleDay = if (hasClaimedToday) {
        completedCycleDays.coerceIn(1, 7)
    } else {
        displayDay.coerceIn(1, 7)
    }

    return DailyTokenTrackerProgress(
        displayDay = displayDay,
        completedCycleDays = completedCycleDays,
        highlightedCycleDay = highlightedCycleDay,
        hasClaimedToday = hasClaimedToday,
        displayWeek = ((displayStreak - 1) / DailyTokenStreakBonusInterval) + 1,
        completedWeeks = currentStreak / DailyTokenStreakBonusInterval,
    )
}

@Composable
private fun dailyTokenTrackerStatusText(progress: DailyTokenTrackerProgress): String {
    return if (progress.hasClaimedToday) {
        stringResource(Res.string.daily_token_day_complete, progress.displayDay)
    } else {
        stringResource(Res.string.daily_token_ready_for_day, progress.displayDay)
    }
}

@Composable
private fun DrawerPremiumIndicator() {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFFF0A8),
                        Color(0xFFFFB84D),
                        Color(0xFFFF6FCB)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.34f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.WorkspacePremium,
            contentDescription = stringResource(Res.string.premium_label),
            tint = Color(0xFF2B143E),
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun DailyDreamTokensDrawerSection(
    dreamTokens: Int,
    requiresSignIn: Boolean,
    isPremium: Boolean,
    tokensClaimedToday: Int,
    trackerStatusText: String,
    isClaiming: Boolean,
    onOpenPage: () -> Unit,
    onClaim: () -> Unit,
) {
    val dailyAllowance = if (isPremium) 2 else 1
    val remainingTokens = (dailyAllowance - tokensClaimedToday).coerceAtLeast(0)
    val hasClaimed = !requiresSignIn && remainingTokens == 0 && tokensClaimedToday > 0
    val claimEnabled = (requiresSignIn || remainingTokens > 0) && !isClaiming
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF211047),
                        Color(0xFF42135C),
                        Color(0xFF231045)
                    )
                )
            )
            .border(
                BorderStroke(1.dp, Color(0xFFB66CFF).copy(alpha = 0.36f)),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onOpenPage)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFF83D3),
                            Color(0xFF8667FF),
                            Color(0xFF211047)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.24f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.dream_token),
                contentDescription = "Dream Token",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(36.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(Res.string.daily_token_title),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "$trackerStatusText • $dreamTokens tokens",
                color = Color(0xFFD8CBFF),
                fontSize = 12.sp,
                maxLines = 1
            )
        }
        Row(
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (claimEnabled) {
                        Brush.horizontalGradient(listOf(Color(0xFFB728C9), Color(0xFFFF6D8E)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF55436D), Color(0xFF3E3158)))
                    }
                )
                .clickable(enabled = claimEnabled) { onClaim() }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (isClaiming) {
                CircularProgressIndicator(
                    modifier = Modifier.size(13.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = if (hasClaimed) Icons.Filled.Check else Icons.Filled.CardGiftcard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = when {
                    requiresSignIn -> stringResource(Res.string.sign_in_action)
                    hasClaimed -> "Done"
                    else -> "Claim"
                },
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DreamTokenBalancePill(
    dreamTokens: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.34f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.dream_token),
            contentDescription = "Dream Token",
            modifier = Modifier.size(26.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = dreamTokens.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun DailyTokenPlanCard(
    title: String,
    amount: String,
    caption: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    footer: String? = null,
) {
    Column(
        modifier = modifier
            .heightIn(min = 116.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (highlighted) {
                    Brush.verticalGradient(listOf(Color(0xFF732063), Color(0xFF35124E)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF211941), Color(0xFF181433)))
                }
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (highlighted) Color(0xFFFF7B74) else Color(0xFF7466B5).copy(alpha = 0.55f)
                ),
                RoundedCornerShape(18.dp)
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = title,
            color = if (highlighted) Color(0xFFFFD36F) else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = amount,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = caption,
                color = Color(0xFFE7DFFF),
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.dream_token),
                contentDescription = "Dream Token",
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit
            )
            footer?.let {
                Text(
                    text = it,
                    color = Color(0xFFFFB6DD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DrawerGradientButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    warm: Boolean = false,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (!enabled) {
                    Brush.horizontalGradient(listOf(Color(0xFF55436D), Color(0xFF3E3158)))
                } else if (warm) {
                    Brush.horizontalGradient(listOf(Color(0xFFFF3F70), Color(0xFFFFB545)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFFB728C9), Color(0xFF6B31DD)))
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun DailyTokenStreakCard(
    trackerProgress: DailyTokenTrackerProgress,
    streak: Int,
    completedWeeks: Int,
) {
    val daysToBonus = (7 - trackerProgress.completedCycleDays).coerceIn(0, 7)
    val visibleCompletedWeeks = maxOf(completedWeeks, trackerProgress.completedWeeks)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF110B32).copy(alpha = 0.88f))
            .border(1.dp, Color(0xFF763D96).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF6D8E),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(Res.string.daily_token_streak_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF8D245D).copy(alpha = 0.56f))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = Color(0xFFFFC269), modifier = Modifier.size(14.dp))
                Text(
                    text = stringResource(Res.string.daily_token_bonus_badge),
                    color = Color(0xFFFFC269),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text(
            text = stringResource(Res.string.daily_token_week_label, trackerProgress.displayWeek),
            color = Color(0xFFFFD6A4),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (1..7).forEach { day ->
                StreakDay(
                    day = day,
                    active = day <= trackerProgress.completedCycleDays,
                    today = day == trackerProgress.highlightedCycleDay,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF24103B).copy(alpha = 0.76f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = dailyTokenTrackerStatusText(trackerProgress),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (streak > 0) {
                    stringResource(
                        if (streak == 1) {
                            Res.string.daily_token_current_streak_one
                        } else {
                            Res.string.daily_token_current_streak_many
                        },
                        streak
                    )
                } else {
                    stringResource(Res.string.daily_token_start_streak)
                },
                color = Color(0xFFD8CBFF),
                fontSize = 12.sp
            )
            if (visibleCompletedWeeks > 0) {
                Text(
                    text = stringResource(
                        if (visibleCompletedWeeks == 1) {
                            Res.string.daily_token_completed_week_one
                        } else {
                            Res.string.daily_token_completed_week_many
                        },
                        visibleCompletedWeeks
                    ),
                    color = Color(0xFFFFD6A4),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF4A174F).copy(alpha = 0.62f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD36F), modifier = Modifier.size(16.dp))
            Text(
                text = if (daysToBonus == 0) {
                    stringResource(Res.string.daily_token_bonus_ready)
                } else if (daysToBonus == 1) {
                    stringResource(Res.string.daily_token_next_bonus_one, daysToBonus)
                } else {
                    stringResource(Res.string.daily_token_next_bonus_many, daysToBonus)
                },
                color = Color(0xFFFFD6A4),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StreakDay(day: Int, active: Boolean, today: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    if (active) {
                        Brush.radialGradient(listOf(Color(0xFFFF7BC4), Color(0xFFFF5F73)))
                    } else {
                        Brush.radialGradient(listOf(Color(0xFF35304F), Color(0xFF1E1B35)))
                    }
                )
                .border(
                    1.dp,
                    if (today) Color(0xFFFFC269) else Color.White.copy(alpha = if (active) 0.24f else 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (active) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            } else {
                Text(day.toString(), color = Color(0xFF9C95B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            text = stringResource(Res.string.daily_token_day_short, day),
            color = if (today) Color(0xFFFFC269) else Color(0xFFC8BFE7),
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
fun DailyTokensScreen(
    mainScreenViewModelState: MainScreenViewModelState,
    bottomPaddingValue: Dp,
    isPremium: Boolean,
    onMainEvent: (MainScreenEvent) -> Unit,
    onSignInRequired: () -> Unit = {},
    onUpgrade: () -> Unit,
    onGetMoreDreamTokens: () -> Unit,
) {
    val requiresSignIn = mainScreenViewModelState.isUserAnonymous || Firebase.auth.currentUser == null
    val dailyAllowance = if (isPremium) 2 else 1
    val claimedToday = mainScreenViewModelState.dailyTokensClaimedToday
    val remainingTokens = (dailyAllowance - claimedToday).coerceAtLeast(0)
    val hasClaimed = !requiresSignIn && remainingTokens == 0 && claimedToday > 0
    val isClaiming = mainScreenViewModelState.isDailyTokenClaimInProgress
    val claimEnabled = (requiresSignIn || remainingTokens > 0) && !isClaiming
    val streak = mainScreenViewModelState.dailyTokenStreak
    var dailyTokenUtcDays by remember { mutableStateOf(currentDailyTokenUtcDays()) }
    var countdownSeconds by remember { mutableStateOf(dailyTokenSecondsUntilUtcReset()) }
    val trackerProgress = dailyTokenTrackerProgress(
        streak = streak,
        lastClaimDay = mainScreenViewModelState.lastDailyTokenClaimDay,
        todayUtcDay = dailyTokenUtcDays.first,
        yesterdayUtcDay = dailyTokenUtcDays.second,
        hasClaimedToday = hasClaimed,
    )

    LaunchedEffect(Unit) {
        onMainEvent(MainScreenEvent.SetTopBarState(false))
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(true))
    }

    LaunchedEffect(Unit) {
        while (true) {
            dailyTokenUtcDays = currentDailyTokenUtcDays()
            countdownSeconds = dailyTokenSecondsUntilUtcReset()
            delay(60_000)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            countdownSeconds = dailyTokenSecondsUntilUtcReset()
            delay(1_000)
        }
    }

    Scaffold(
        topBar = {
            DailyTokensTopBar(dreamTokens = mainScreenViewModelState.dreamTokens)
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = bottomPaddingValue,
                    start = 16.dp,
                    end = 16.dp
                )
                .dynamicBottomNavigationPadding()
                .verticalScroll(rememberScrollState())
                .padding(top = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1A0D47).copy(alpha = 0.96f),
                                Color(0xFF351062).copy(alpha = 0.96f),
                                Color(0xFF120B30).copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFFB66CFF).copy(alpha = 0.34f), RoundedCornerShape(28.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFFFD38D),
                                        Color(0xFFFF75C6),
                                        Color(0xFF8D6CFF),
                                        Color(0xFF211047)
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.dream_token),
                            contentDescription = "Dream Token",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(92.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = dailyTokenTrackerStatusText(trackerProgress),
                            color = Color.White,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 31.sp
                        )
                        Text(
                            text = stringResource(Res.string.daily_token_keep_going),
                            color = Color(0xFFE2D8FF),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DailyTokenPlanCard(
                        title = stringResource(Res.string.free),
                        amount = "1",
                        caption = stringResource(Res.string.daily_token_token_per_day),
                        modifier = Modifier.weight(1f)
                    )
                    DailyTokenPlanCard(
                        title = stringResource(Res.string.premium_label),
                        amount = "2",
                        caption = stringResource(Res.string.daily_token_tokens_per_day),
                        highlighted = true,
                        modifier = Modifier.weight(1f),
                        footer = if (isPremium) {
                            stringResource(Res.string.premium_active)
                        } else {
                            stringResource(Res.string.daily_token_double_rewards)
                        }
                    )
                }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DrawerGradientButton(
                    text = when {
                        requiresSignIn -> stringResource(Res.string.sign_in_action)
                        isClaiming -> stringResource(Res.string.daily_token_claiming)
                        hasClaimed -> stringResource(Res.string.daily_token_claimed_today)
                        remainingTokens == 1 -> stringResource(Res.string.daily_token_claim_1)
                        else -> stringResource(Res.string.daily_token_claim_amount, remainingTokens)
                    },
                    enabled = claimEnabled,
                    onClick = {
                        onMainEvent(MainScreenEvent.TriggerVibration)
                        if (requiresSignIn) {
                            onSignInRequired()
                        } else {
                            onMainEvent(MainScreenEvent.ClaimDailyDreamTokens(isPremium))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    leading = {
                        if (isClaiming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = if (hasClaimed) Icons.Filled.Check else Icons.Filled.CardGiftcard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
                if (!isPremium) {
                    DrawerGradientButton(
                        text = stringResource(Res.string.daily_token_upgrade),
                        enabled = true,
                        onClick = {
                            onMainEvent(MainScreenEvent.TriggerVibration)
                            if (requiresSignIn) {
                                onSignInRequired()
                            } else {
                                onUpgrade()
                            }
                        },
                        warm = true,
                        modifier = Modifier.weight(1f),
                        leading = {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF24103B).copy(alpha = 0.76f))
                    .border(1.dp, Color(0xFFFF7BD5).copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD36F),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(Res.string.daily_token_premium_double),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

            DailyTokenStreakCard(
                trackerProgress = trackerProgress,
                streak = streak,
                completedWeeks = mainScreenViewModelState.dailyTokenCompletedWeeks,
            )
            DailyTokenCountdownSection(
                hasClaimed = hasClaimed,
                countdownText = dailyTokenCountdownText(countdownSeconds)
            )
            DailyTokenGetMoreSection(
                onClick = {
                    onMainEvent(MainScreenEvent.TriggerVibration)
                    if (requiresSignIn) {
                        onSignInRequired()
                    } else {
                        onGetMoreDreamTokens()
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DailyTokenCountdownSection(
    hasClaimed: Boolean,
    countdownText: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF120A33).copy(alpha = 0.96f),
                        Color(0xFF2B1457).copy(alpha = 0.94f),
                        Color(0xFF160E34).copy(alpha = 0.96f)
                    )
                )
            )
            .border(1.dp, Color(0xFF8D6CFF).copy(alpha = 0.30f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFFD36F),
                            Color(0xFFFF6FCB),
                            Color(0xFF3A1B70)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasClaimed) Icons.Filled.LocalFireDepartment else Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(25.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = stringResource(Res.string.daily_token_countdown_title),
                color = Color(0xFFE9DEFF),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = if (hasClaimed) {
                    stringResource(Res.string.daily_token_countdown_waiting, countdownText)
                } else {
                    stringResource(Res.string.daily_token_countdown_ready)
                },
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp
            )
            Text(
                text = if (hasClaimed) {
                    stringResource(Res.string.daily_token_countdown_waiting_body)
                } else {
                    stringResource(Res.string.daily_token_countdown_ready_body)
                },
                color = Color(0xFFCDBFFF),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun DailyTokenGetMoreSection(
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF27104A).copy(alpha = 0.98f),
                        Color(0xFF3B1254).copy(alpha = 0.96f),
                        Color(0xFF160A2F).copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, Color(0xFFFF9BD8).copy(alpha = 0.26f), RoundedCornerShape(26.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.dream_token),
                    contentDescription = stringResource(Res.string.dream_token_content_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(42.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = stringResource(Res.string.daily_token_store_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 23.sp
                )
                Text(
                    text = stringResource(Res.string.daily_token_store_body),
                    color = Color(0xFFE2D8FF),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        DrawerGradientButton(
            text = stringResource(Res.string.daily_token_store_button),
            enabled = true,
            onClick = onClick,
            warm = true,
            leading = {
                Icon(
                    imageVector = Icons.Filled.CardGiftcard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyTokensTopBar(dreamTokens: Int) {
    val coroutineScope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.daily_token_title),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    coroutineScope.launch { DrawerController.send(DrawerCommand.Open) }
                }
            ) {
                NotificationPermissionMenuIcon(
                    contentDescription = "Open drawer",
                    tint = Color.White
                )
            }
        },
        actions = {
            DreamTokenBalancePill(
                dreamTokens = dreamTokens,
                modifier = Modifier.padding(end = 12.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF090020).copy(alpha = 0.58f),
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
fun SnackbarHandler(
    snackbarHostState: SnackbarHostState
) {
    var eventToShow by remember { mutableStateOf<SnackbarEvent?>(null) }

    // Stage 1: Collect events from the controller
    LaunchedEffect(Unit) {
        SnackbarController.events.collect { event ->
            eventToShow = event
        }
    }

    // Stage 2: Resolve and show the snackbar when an event is available
    val currentEvent = eventToShow
    if (currentEvent != null) {
        // Part A: Resolve strings in the Composable scope
        val message = currentEvent.message.asString()
        val actionLabel = currentEvent.action?.name?.asString()

        // Part B: Show the snackbar in a separate LaunchedEffect
        LaunchedEffect(currentEvent, message, actionLabel) {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = currentEvent.duration
            )
            if (result == SnackbarResult.ActionPerformed) {
                currentEvent.action?.action?.invoke()
            }
            // Reset the state to be ready for the next event
            eventToShow = null
        }
    }
}

@Composable
private fun MembershipGiftBubble(
    badgeText: String,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    var position by remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    val floatTransition = rememberInfiniteTransition(label = "membership_gift_bubble")
    val offsetY by floatTransition.animateFloat(
        initialValue = -2f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "membership_gift_bubble_offset"
    )
    val rotation by floatTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "membership_gift_bubble_rotation"
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val bubbleSize = 104.dp
        val edgePaddingPx = with(density) { 16.dp.toPx() }
        val topPaddingPx = with(density) { 22.dp.toPx() }
        val bottomPaddingPx = with(density) { 118.dp.toPx() }
        val bubblePx = with(density) { bubbleSize.toPx() }
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val minX = edgePaddingPx
        val maxX = (containerWidthPx - bubblePx - edgePaddingPx).coerceAtLeast(minX)
        val minY = topPaddingPx
        val maxY = (containerHeightPx - bubblePx - bottomPaddingPx).coerceAtLeast(minY)
        val currentPosition = position ?: Offset(maxX, maxY)

        LaunchedEffect(containerWidthPx, containerHeightPx, bottomPaddingPx, edgePaddingPx) {
            if (position == null) {
                position = Offset(maxX, maxY)
            } else {
                position = currentPosition.copy(
                    x = currentPosition.x.coerceIn(minX, maxX),
                    y = currentPosition.y.coerceIn(minY, maxY),
                )
            }
        }

        val animatedX by animateFloatAsState(
            targetValue = currentPosition.x,
            animationSpec = if (isDragging) {
                tween(durationMillis = 0)
            } else {
                spring(dampingRatio = 0.72f, stiffness = 320f)
            },
            label = "membership-gift-x",
        )
        val animatedY by animateFloatAsState(
            targetValue = currentPosition.y,
            animationSpec = if (isDragging) {
                tween(durationMillis = 0)
            } else {
                spring(dampingRatio = 0.72f, stiffness = 320f)
            },
            label = "membership-gift-y",
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedX.roundToInt(), animatedY.roundToInt()) }
                .size(bubbleSize)
                .graphicsLayer {
                    if (!isDragging) {
                        translationY = offsetY
                        rotationZ = rotation
                    }
                }
                .pointerInput(containerWidthPx, containerHeightPx) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            val dragPosition = position ?: currentPosition
                            val leftDistance = abs(dragPosition.x - minX)
                            val rightDistance = abs(maxX - dragPosition.x)
                            val snapX = if (leftDistance < rightDistance) minX else maxX
                            position = dragPosition.copy(x = snapX)
                        },
                        onDragCancel = {
                            isDragging = false
                            val dragPosition = position ?: currentPosition
                            val leftDistance = abs(dragPosition.x - minX)
                            val rightDistance = abs(maxX - dragPosition.x)
                            val snapX = if (leftDistance < rightDistance) minX else maxX
                            position = dragPosition.copy(x = snapX)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragPosition = position ?: currentPosition
                            position = Offset(
                                x = (dragPosition.x + dragAmount.x).coerceIn(minX, maxX),
                                y = (dragPosition.y + dragAmount.y).coerceIn(minY, maxY),
                            )
                        },
                    )
                }
                .clickable(onClick = onClick)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.journal_membership_gift_bubble),
                contentDescription = badgeText,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(92.dp)
            )
        }
    }
}

fun formatDreams(
    dreams: List<Dream>,
    titlePrefix: String,
    datePrefix: String,
    transcriptPrefix: String,
    dreamSeparator: String
): String {
    val builder = StringBuilder()
    dreams.forEach { dream ->
        builder.append(titlePrefix + dream.title + "\n")
        builder.append(datePrefix + dream.date + "\n\n")
        builder.append(dream.content)

        if (dream.audioTranscription.isNotBlank()) {
            builder.append(transcriptPrefix)
            builder.append(dream.audioTranscription)
        }

        builder.append(dreamSeparator)
    }
    return builder.toString()
}


data class DrawerGroup(
    val title: StringResource,
    val items: List<DrawerNavigation>
)

@Composable
fun AnimatedHeartIcon(animate: Boolean = true) {
    if (animate) {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val size by infiniteTransition.animateFloat(
            initialValue = 24f,
            targetValue = 28f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800),  // Slower animation
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = stringResource(Res.string.animated_heart),
            tint = Color.Red,
            modifier = Modifier.size(size.dp)
        )
    } else {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = stringResource(Res.string.heart),
            tint = Color.Red,
            modifier = Modifier.size(24.dp)
        )
    }
}

data class NotificationPermissionState(
    val isRequired: Boolean,
    val isGranted: Boolean,
    val requestPermission: () -> Unit,
) {
    val shouldShowPrompt: Boolean
        get() = isRequired && !isGranted
}

@Composable
expect fun rememberNotificationPermissionState(): NotificationPermissionState
