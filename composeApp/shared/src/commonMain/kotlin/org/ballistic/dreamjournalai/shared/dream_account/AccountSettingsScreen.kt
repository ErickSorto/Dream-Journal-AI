package org.ballistic.dreamjournalai.shared.dream_account

import dreamjournalai.composeapp.shared.generated.resources.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import org.ballistic.dreamjournalai.shared.core.analytics.AnalyticsUserProperty
import org.ballistic.dreamjournalai.shared.core.analytics.AppAnalytics
import org.ballistic.dreamjournalai.shared.core.components.TypewriterText
import org.ballistic.dreamjournalai.shared.dream_account.components.DreamAccountSettingsScreenTopBar
import org.ballistic.dreamjournalai.shared.dream_account.components.LogoutDeleteLayout
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.ObserveLoginState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.ObserverLogoutDeleteState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModelState
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.PremiumTrialReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingAuthCard
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumAnalytics
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumAnalyticsEvent
import org.ballistic.dreamjournalai.shared.dream_premium.domain.repository.PremiumPaywallRepository
import org.koin.compose.koinInject
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun AccountSettingsScreen(
    loginViewModelState: LoginViewModelState,
    signupViewModelState: SignupViewModelState,
    isPremiumMember: Boolean = false,
    onLoginEvent: (LoginEvent) -> Unit = {},
    onSignupEvent: (SignupEvent) -> Unit = {},
    onMainEvent: (MainScreenEvent) -> Unit = {},
    navigateToOnboardingScreen: () -> Unit = {},
    navigateToDreamJournalScreen: () -> Unit = {},
    navigateToDreamJournalAfterSignIn: () -> Unit = navigateToDreamJournalScreen
) {
    val isLoading = loginViewModelState.isLoading || signupViewModelState.isLoading
    val isUserAnonymous = loginViewModelState.isUserAnonymous
    val isEmailVerified = loginViewModelState.isEmailVerified
    val isUserLoggedIn = loginViewModelState.isLoggedIn
    val premiumPaywallRepository = koinInject<PremiumPaywallRepository>()
    val premiumAnalytics = koinInject<PremiumAnalytics>()
    val appAnalytics = koinInject<AppAnalytics>()
    val premiumTrialReminderScheduler = koinInject<PremiumTrialReminderScheduler>()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val wasGuestAccountAtEntry = remember { isUserAnonymous }
    var logoutInProgress by remember { mutableStateOf(false) }
    var logoutNavigationStarted by remember { mutableStateOf(false) }
    var guestUpgradeAuthStarted by remember { mutableStateOf(false) }
    var guestUpgradeNavigationStarted by remember { mutableStateOf(false) }
    var guestUpgradeLoadingObserved by remember { mutableStateOf(false) }
    var restorePurchasesInProgress by remember { mutableStateOf(false) }
    val restoreSuccessMessage = stringResource(Res.string.restore_premium_success)
    val restoreNoneMessage = stringResource(Res.string.restore_premium_none)
    val restoreErrorMessage = stringResource(Res.string.restore_premium_error)

    fun showRestoreMessage(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    fun restorePremiumPurchases() {
        if (restorePurchasesInProgress) return
        restorePurchasesInProgress = true
        premiumAnalytics.track(PremiumAnalyticsEvent.RestoreTapped)
        scope.launch {
            runCatching {
                premiumPaywallRepository.restorePurchases()
            }.onSuccess { customerInfo ->
                val premiumActive = premiumPaywallRepository.hasPremiumEntitlement(customerInfo)
                premiumAnalytics.track(PremiumAnalyticsEvent.RestoreCompleted(premiumActive))
                if (premiumActive) {
                    appAnalytics.setUserProperty(AnalyticsUserProperty.PremiumStatus, "active")
                    premiumTrialReminderScheduler.cancelTrialEndingReminder()
                }
                restorePurchasesInProgress = false
                showRestoreMessage(
                    if (premiumActive) {
                        restoreSuccessMessage
                    } else {
                        restoreNoneMessage
                    }
                )
            }.onFailure { error ->
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.RestoreFailed(
                        message = error.message ?: "unknown"
                    )
                )
                restorePurchasesInProgress = false
                showRestoreMessage(error.message ?: restoreErrorMessage)
            }
        }
    }

    // Navigate to Home only when transitioning from not-logged-in to logged-in+verified
    val loggedInAndVerified = isUserLoggedIn && isEmailVerified && !isUserAnonymous

    fun beginGuestUpgradeTransition() {
        if (!wasGuestAccountAtEntry) return
        guestUpgradeAuthStarted = true
        onMainEvent(MainScreenEvent.SetAuthTransitionInProgress(true))
        onMainEvent(MainScreenEvent.SetMainContentHandoffInProgress(true))
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(false))
        onMainEvent(MainScreenEvent.SetTopBarState(false))
    }

    fun releaseGuestUpgradeTransition() {
        guestUpgradeAuthStarted = false
        guestUpgradeNavigationStarted = false
        guestUpgradeLoadingObserved = false
        onMainEvent(MainScreenEvent.SetAuthTransitionInProgress(false))
        onMainEvent(MainScreenEvent.SetMainContentHandoffInProgress(false))
    }

    fun handleAuthLoginEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.SignInWithGoogle,
            is LoginEvent.SignInWithApple,
            is LoginEvent.LoginWithEmailAndPassword,
            is LoginEvent.ReauthAndDelete -> beginGuestUpgradeTransition()
            is LoginEvent.ToggleLoading -> {
                if (!event.isLoading && wasGuestAccountAtEntry && !loggedInAndVerified) {
                    releaseGuestUpgradeTransition()
                }
            }
            else -> Unit
        }
        onLoginEvent(event)
    }

    fun handleAuthSignupEvent(event: SignupEvent) {
        when (event) {
            is SignupEvent.SignUpWithEmailAndPassword -> beginGuestUpgradeTransition()
            else -> Unit
        }
        onSignupEvent(event)
    }

    // Synchronous detection of logout transition (persisted across branches)
    var prevLoggedIn by remember { mutableStateOf(isUserLoggedIn) }
    val justLoggedOut = prevLoggedIn && !isUserLoggedIn
    SideEffect { prevLoggedIn = isUserLoggedIn }
    LaunchedEffect(justLoggedOut) {
        if (justLoggedOut && !logoutNavigationStarted) {
            logoutNavigationStarted = true
            navigateToOnboardingScreen()
        }
    }

    var prevLoggedInAndVerified by remember { mutableStateOf(loggedInAndVerified) }
    var handledVerifiedSignInNavigation by remember { mutableStateOf(false) }
    fun navigateAfterVerifiedSignIn() {
        if (handledVerifiedSignInNavigation) return
        handledVerifiedSignInNavigation = true
        if (wasGuestAccountAtEntry) {
            beginGuestUpgradeTransition()
            guestUpgradeNavigationStarted = true
            scope.launch {
                delay(160)
                navigateToDreamJournalAfterSignIn()
            }
        } else {
            navigateToDreamJournalAfterSignIn()
        }
    }
    LaunchedEffect(loggedInAndVerified) {
        if (!prevLoggedInAndVerified && loggedInAndVerified) {
            navigateAfterVerifiedSignIn()
        }
        if (!loggedInAndVerified) {
            handledVerifiedSignInNavigation = false
        }
        prevLoggedInAndVerified = loggedInAndVerified
    }

    val isGuestUpgradeTransition =
        wasGuestAccountAtEntry && (loggedInAndVerified || guestUpgradeNavigationStarted)

    LaunchedEffect(isLoading, guestUpgradeNavigationStarted) {
        if (guestUpgradeNavigationStarted && isLoading) {
            guestUpgradeLoadingObserved = true
        }
    }

    LaunchedEffect(
        guestUpgradeNavigationStarted,
        guestUpgradeLoadingObserved,
        isLoading,
        loggedInAndVerified
    ) {
        if (
            guestUpgradeAuthStarted &&
            guestUpgradeLoadingObserved &&
            !isLoading &&
            !loggedInAndVerified
        ) {
            releaseGuestUpgradeTransition()
        }
    }

    LaunchedEffect(guestUpgradeAuthStarted, guestUpgradeLoadingObserved, isLoading, loggedInAndVerified) {
        if (guestUpgradeAuthStarted && !guestUpgradeLoadingObserved && !loggedInAndVerified) {
            delay(700)
            if (!isLoading && !guestUpgradeLoadingObserved && !loggedInAndVerified) {
                releaseGuestUpgradeTransition()
            }
        }
    }

    LaunchedEffect(isUserAnonymous, isGuestUpgradeTransition, logoutInProgress, justLoggedOut) {
        val shouldShowMainChrome =
            !isUserAnonymous && !isGuestUpgradeTransition && !logoutInProgress && !justLoggedOut
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(shouldShowMainChrome))
        onMainEvent(MainScreenEvent.SetTopBarState(shouldShowMainChrome))
    }

    Scaffold(
        topBar = {
            if (!isUserAnonymous && !isGuestUpgradeTransition && !logoutInProgress && !justLoggedOut) {
                DreamAccountSettingsScreenTopBar(
                    isPremiumMember = isPremiumMember
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(bottom = 96.dp)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .navigationBarsPadding()
    ) {
        val shouldHoldGuestAuthCard = wasGuestAccountAtEntry &&
            (guestUpgradeAuthStarted || isGuestUpgradeTransition)

        if (logoutInProgress || justLoggedOut) {
            LogoutDeleteLayout(
                onLoginEvent = {},
                onLogoutClick = {},
                actionsEnabled = false,
                isRestorePurchasesInProgress = restorePurchasesInProgress,
                onRestorePurchasesClick = {}
            )
        } else if (!shouldHoldGuestAuthCard &&
            loginViewModelState.isEmailVerified &&
            loginViewModelState.isLoggedIn &&
            !loginViewModelState.isUserAnonymous
        ) {
            LogoutDeleteLayout(
                onLoginEvent = onLoginEvent,
                onLogoutClick = {
                    logoutInProgress = true
                    onMainEvent(MainScreenEvent.SetAuthTransitionInProgress(true))
                    onMainEvent(MainScreenEvent.SetMainContentHandoffInProgress(true))
                    onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(false))
                    onMainEvent(MainScreenEvent.SetTopBarState(false))
                    onSignupEvent(SignupEvent.ResetSignupState)
                    onLoginEvent(LoginEvent.ShowSignUpLayout)
                    onLoginEvent(LoginEvent.SignOut)
                },
                actionsEnabled = !logoutInProgress,
                isRestorePurchasesInProgress = restorePurchasesInProgress,
                onRestorePurchasesClick = ::restorePremiumPurchases
            )
        } else {
            if (!isGuestUpgradeTransition) {
                ObserveLoginState(
                    isLoggedIn = isUserLoggedIn,
                    isEmailVerified = isEmailVerified,
                    isUserAnonymous = isUserAnonymous,
                    isUserAnonymousAlready = true,
                    navigateToDreamJournalScreen = ::navigateAfterVerifiedSignIn,
                )

                ObserverLogoutDeleteState(
                    isLoggedIn = isUserLoggedIn,
                    isAnonymous = isUserAnonymous,
                    navigateToLoginScreen = navigateToOnboardingScreen
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(it)
                    .navigationBarsPadding()
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                val showGuestUpgradeCopy = isUserAnonymous || shouldHoldGuestAuthCard

                Box(
                    modifier = Modifier.widthIn(max = 520.dp)
                ) {
                    OnboardingAuthCard(
                        enteredName = "",
                        loginViewModelState = loginViewModelState,
                        signupViewModelState = signupViewModelState,
                        isLoading = isLoading || isGuestUpgradeTransition,
                        onLoginEvent = ::handleAuthLoginEvent,
                        onSignupEvent = ::handleAuthSignupEvent,
                        onBackClick = null,
                        eyebrowText = if (showGuestUpgradeCopy) "Guest account" else "Account setup",
                        titleOverride = if (showGuestUpgradeCopy) {
                            "Save your guest progress."
                        } else {
                            "Create your account."
                        },
                        subtitleOverride = if (showGuestUpgradeCopy) {
                            "Connect an account to keep your dreams, sync across devices, and make this guest journal permanent."
                        } else {
                            "Create or connect an account to sync your dreams and keep everything safe."
                        },
                        showGuestButton = !showGuestUpgradeCopy,
                        heroDrawableOverride = if (showGuestUpgradeCopy) {
                            Res.drawable.guest_account_upgrade_hero
                        } else {
                            null
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isUserAnonymous && !shouldHoldGuestAuthCard) {
                        GuestAccountDismissBubble(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 12.dp, end = 12.dp),
                            onClick = {
                                onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(true))
                                onMainEvent(MainScreenEvent.SetTopBarState(true))
                                navigateToDreamJournalScreen()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestAccountDismissBubble(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color(0xFF5A3C92).copy(alpha = 0.34f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.22f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = "X",
            style = TextStyle(
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
