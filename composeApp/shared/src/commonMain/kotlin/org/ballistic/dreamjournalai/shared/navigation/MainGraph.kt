package org.ballistic.dreamjournalai.shared.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModel
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModel
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModelState
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.dream_main.presentation.MainScreenView
import org.ballistic.dreamjournalai.shared.dream_main.presentation.markMainBackgroundIntroPlayed
import org.ballistic.dreamjournalai.shared.dream_main.presentation.prepareMainBackgroundAfterOnboardingExit
import org.ballistic.dreamjournalai.shared.dream_main.presentation.prepareMainBackgroundForFreshPanDown
import org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_onboarding.data.OnboardingPreferencesRepository
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.OnboardingScreen
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingAuthCard
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingPageBackground
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumEntrySource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private suspend fun resolveInitialStartDestination(
    onboardingPreferences: OnboardingPreferencesRepository,
): Route {
    val currentUser = Firebase.auth.currentUser
    val canEnterMain = currentUser != null &&
        (currentUser.isAnonymous || currentUser.isEmailVerified == true)

    if (!canEnterMain) return Route.OnboardingScreen()
    if (onboardingPreferences.hasCompletedOnboardingForCurrentUser()) return Route.MainScreen

    return Route.OnboardingScreen()
}

private const val RouteHandoffVeilMillis = 700

private fun String?.isAuthRoute(): Boolean =
    this?.contains(Route.AuthScreen::class.qualifiedName.orEmpty()) == true

private fun String?.isOnboardingRoute(): Boolean =
    this?.contains(Route.OnboardingScreen::class.qualifiedName.orEmpty()) == true

private fun String?.isMainRoute(): Boolean =
    this?.contains(Route.MainScreen::class.qualifiedName.orEmpty()) == true

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainGraph(
    onDataLoaded: () -> Unit,
    requestInAppReview: () -> Unit = {},
) {
    val loginViewModel = koinViewModel<LoginViewModel>()
    val signupViewModel = koinViewModel<SignupViewModel>()
    val loginViewModelState = loginViewModel.state.collectAsStateWithLifecycle().value
    val signupViewModelState = signupViewModel.state.collectAsStateWithLifecycle().value

    val onboardingPreferences = koinInject<OnboardingPreferencesRepository>()
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    var premiumEntrySource by remember { mutableStateOf<PremiumEntrySource?>(null) }
    var initialStartDestination by remember { mutableStateOf<Route?>(null) }
    var resetMainToDreamJournalRootSignal by remember { mutableStateOf(0) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val routeHandoffVeilAlpha = remember { Animatable(0f) }
    var topLevelHandoffInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val resolvedStartDestination = resolveInitialStartDestination(onboardingPreferences)
        if (resolvedStartDestination == Route.MainScreen) {
            markMainBackgroundIntroPlayed()
        }
        initialStartDestination = resolvedStartDestination
    }

    fun runTopLevelHandoff(
        fadeDestinationIn: Boolean = true,
        navigate: () -> Unit,
    ) {
        if (topLevelHandoffInProgress) return

        coroutineScope.launch {
            topLevelHandoffInProgress = true
            try {
                routeHandoffVeilAlpha.stop()
                routeHandoffVeilAlpha.animateTo(
                    targetValue = 0.90f,
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = FastOutSlowInEasing
                    )
                )
                navigate()
                if (fadeDestinationIn) {
                    routeHandoffVeilAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = RouteHandoffVeilMillis,
                            easing = FastOutSlowInEasing
                        )
                    )
                } else {
                    delay(48)
                    routeHandoffVeilAlpha.snapTo(0f)
                }
            } finally {
                routeHandoffVeilAlpha.snapTo(0f)
                topLevelHandoffInProgress = false
            }
        }
    }

    fun navigateToMainScreenClearingOnboarding(
        skipMainBackgroundIntro: Boolean = true,
        resetMainToDreamJournalRoot: Boolean = false,
        playMainBackgroundIntro: Boolean = false,
        useTopLevelHandoff: Boolean = true,
    ) {
        premiumEntrySource = null
        when {
            playMainBackgroundIntro -> prepareMainBackgroundForFreshPanDown()
            skipMainBackgroundIntro -> {
                org.ballistic.dreamjournalai.shared.dream_main.presentation.markMainBackgroundIntroPlayed()
            }
            else -> prepareMainBackgroundAfterOnboardingExit()
        }
        if (resetMainToDreamJournalRoot) {
            resetMainToDreamJournalRootSignal += 1
        }

        val currentTopLevelRoute = currentBackStackEntry?.destination?.route
        val navigateToMain = {
            if (currentTopLevelRoute.isAuthRoute()) {
                navController.navigate(Route.MainScreen) {
                    popUpTo(Route.AuthScreen) {
                        inclusive = true
                        saveState = false
                    }
                    launchSingleTop = true
                    restoreState = false
                }
            } else {
                navController.navigate(Route.MainScreen) {
                    popUpTo(Route.OnboardingScreen::class) {
                        inclusive = true
                        saveState = false
                    }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        }

        if (useTopLevelHandoff) {
            runTopLevelHandoff(navigate = navigateToMain)
        } else {
            navigateToMain()
        }
    }

    fun navigateToOnboardingWithoutBackgroundHandoff() {
        premiumEntrySource = null
        navController.navigate(Route.OnboardingScreen()) {
            popUpTo(Route.AuthScreen) {
                inclusive = true
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun navigateAfterStandaloneAuth() {
        coroutineScope.launch {
            if (!onboardingPreferences.hasCompletedOnboardingForCurrentUser()) {
                onboardingPreferences.markStartedForCurrentUser()
                navigateToOnboardingWithoutBackgroundHandoff()
            } else {
                navigateToMainScreenClearingOnboarding(
                    skipMainBackgroundIntro = true,
                    resetMainToDreamJournalRoot = true,
                    playMainBackgroundIntro = false
                )
            }
        }
    }

    val startDestination = initialStartDestination
    if (startDestination == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF130B32))
        )
        return
    }

    LaunchedEffect(
        currentBackStackEntry,
        loginViewModelState.isLoggedIn,
    ) {
        val topLevelRoute = currentBackStackEntry?.destination?.route
        if (!topLevelRoute.isMainRoute() || loginViewModelState.isLoggedIn) {
            return@LaunchedEffect
        }

        premiumEntrySource = null
        runTopLevelHandoff(fadeDestinationIn = false) {
            navController.navigate(Route.OnboardingScreen(showAuthImmediately = true)) {
                popUpTo(Route.MainScreen) {
                    inclusive = true
                    saveState = false
                }
                launchSingleTop = true
                restoreState = false
            }
        }
    }

    LaunchedEffect(
        currentBackStackEntry,
        loginViewModelState.isLoggedIn,
        loginViewModelState.isEmailVerified,
        loginViewModelState.isUserAnonymous,
        premiumEntrySource,
    ) {
        val topLevelRoute = currentBackStackEntry?.destination?.route
        if (!topLevelRoute.isOnboardingRoute()) {
            return@LaunchedEffect
        }

        val onboardingRoute = currentBackStackEntry
            ?.let { entry -> runCatching { entry.toRoute<Route.OnboardingScreen>() }.getOrNull() }
            ?: return@LaunchedEffect
        if (onboardingRoute.forceOnboarding || premiumEntrySource != null) return@LaunchedEffect

        val currentUser = Firebase.auth.currentUser ?: return@LaunchedEffect
        val canEnterMain = currentUser.isAnonymous || currentUser.isEmailVerified == true
        if (!canEnterMain) return@LaunchedEffect

        if (onboardingPreferences.hasCompletedOnboardingForCurrentUser()) {
            navigateToMainScreenClearingOnboarding(
                skipMainBackgroundIntro = true,
                resetMainToDreamJournalRoot = true,
                playMainBackgroundIntro = false,
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            },
            popEnterTransition = {
                EnterTransition.None
            },
            popExitTransition = {
                ExitTransition.None
            },
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Route.AuthScreen> {
                StandaloneAuthScreen(
                    loginViewModelState = loginViewModelState,
                    signupViewModelState = signupViewModelState,
                    onLoginEvent = { loginViewModel.onEvent(it) },
                    onSignupEvent = { signupViewModel.onEvent(it) },
                    onAuthenticated = {
                        navigateAfterStandaloneAuth()
                    },
                    onGuestAuthenticated = {
                        navigateAfterStandaloneAuth()
                    },
                    onDataLoaded = onDataLoaded
                )
            }

            composable<Route.OnboardingScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.OnboardingScreen>()
                OnboardingScreen(
                    premiumEntrySource = premiumEntrySource,
                    forceOnboarding = args.forceOnboarding || premiumEntrySource != null,
                    showAuthImmediately = args.showAuthImmediately,
                    navigateToDreamJournalScreen = {
                        navigateToMainScreenClearingOnboarding(
                            skipMainBackgroundIntro = true,
                            resetMainToDreamJournalRoot = true,
                            playMainBackgroundIntro = false
                        )
                    },
                    navigateToDreamJournalScreenAfterOnboardingExit = {
                        navigateToMainScreenClearingOnboarding(
                            skipMainBackgroundIntro = false,
                            resetMainToDreamJournalRoot = true,
                            useTopLevelHandoff = false
                        )
                    },
                    debugStartAtLastPage = args.debugStartAtLastPage,
                    onDismissPremiumFlow = {
                        premiumEntrySource = null
                        if (!navController.popBackStack()) {
                            navController.navigate(Route.MainScreen) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onDataLoaded = {
                        onDataLoaded()
                    },
                    loginViewModelState = loginViewModelState,
                    signupViewModelState = signupViewModelState,
                    onLoginEvent = {
                        loginViewModel.onEvent(it)
                    },
                    onSignupEvent = {
                        signupViewModel.onEvent(it)
                    },
                    requestInAppReview = requestInAppReview,
                )
            }

            composable<Route.MainScreen> {
                val mainScreenViewModel = koinViewModel<MainScreenViewModel>()
                val mainScreenViewModelState = mainScreenViewModel.mainScreenViewModelState
                    .collectAsStateWithLifecycle().value

                MainScreenView(
                    mainScreenViewModelState = mainScreenViewModelState,
                    onDataLoaded = {
                        onDataLoaded()
                    },
                    onMainEvent = {
                        mainScreenViewModel.onEvent(it)
                    },
                    onNavigateToOnboardingScreen = {
                        premiumEntrySource = null
                        runTopLevelHandoff(fadeDestinationIn = false) {
                            navController.navigate(Route.OnboardingScreen(showAuthImmediately = true)) {
                                popUpTo(Route.MainScreen) {
                                    inclusive = true
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    },
                    onNavigateToForcedOnboarding = {
                        premiumEntrySource = null
                        navController.navigate(Route.OnboardingScreen(forceOnboarding = true)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToOnboardingLastPage = {
                        premiumEntrySource = null
                        navController.navigate(
                            Route.OnboardingScreen(
                                forceOnboarding = true,
                                debugStartAtLastPage = true
                            )
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPremiumFlow = { entrySource ->
                        premiumEntrySource = entrySource
                        navController.navigate(Route.OnboardingScreen(forceOnboarding = true)) {
                            launchSingleTop = true
                        }
                    },
                    resetToDreamJournalRootSignal = resetMainToDreamJournalRootSignal,
                    requestInAppReview = requestInAppReview,
                )
            }
        }

        if (routeHandoffVeilAlpha.value > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF130B32).copy(alpha = routeHandoffVeilAlpha.value))
            )
        }
    }
}

@Composable
private fun StandaloneAuthScreen(
    loginViewModelState: LoginViewModelState,
    signupViewModelState: SignupViewModelState,
    onLoginEvent: (LoginEvent) -> Unit,
    onSignupEvent: (SignupEvent) -> Unit,
    onAuthenticated: () -> Unit,
    onGuestAuthenticated: () -> Unit,
    onDataLoaded: () -> Unit,
) {
    var hasObservedSignedOutState by remember { mutableStateOf(!loginViewModelState.isLoggedIn) }
    var authExitStarted by remember { mutableStateOf(false) }
    var guestAuthHandoffStarted by remember { mutableStateOf(false) }
    val authContentAlpha by animateFloatAsState(
        targetValue = if (authExitStarted || guestAuthHandoffStarted) 0f else 1f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "standalone-auth-content-alpha"
    )

    LaunchedEffect(Unit) {
        onDataLoaded()
        onLoginEvent(LoginEvent.BeginAuthStateListener)
        onLoginEvent(LoginEvent.ShowSignUpLayout)
    }

    LaunchedEffect(loginViewModelState.isLoggedIn) {
        if (!loginViewModelState.isLoggedIn) {
            hasObservedSignedOutState = true
        }
    }

    LaunchedEffect(
        guestAuthHandoffStarted,
        signupViewModelState.isLoading,
        signupViewModelState.error,
        loginViewModelState.isLoggedIn,
    ) {
        if (!guestAuthHandoffStarted) return@LaunchedEffect
        if (loginViewModelState.isLoggedIn || signupViewModelState.isLoading) return@LaunchedEffect
        if (signupViewModelState.error != StringValue.Empty) {
            guestAuthHandoffStarted = false
        }
    }

    LaunchedEffect(
        loginViewModelState.isLoggedIn,
        loginViewModelState.isEmailVerified,
        loginViewModelState.isUserAnonymous,
        hasObservedSignedOutState
    ) {
        if (!hasObservedSignedOutState || !loginViewModelState.isLoggedIn) {
            return@LaunchedEffect
        }

        val canEnter = loginViewModelState.isUserAnonymous || loginViewModelState.isEmailVerified
        if (!canEnter) return@LaunchedEffect
        if (authExitStarted) return@LaunchedEffect

        authExitStarted = true
        delay(220)

        if (loginViewModelState.isUserAnonymous) {
            onGuestAuthenticated()
        } else if (loginViewModelState.isEmailVerified) {
            onAuthenticated()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OnboardingPageBackground(
            cameraScale = 1f,
            cameraBiasY = -1f,
            panProgress = 0f,
            shootingStarTrigger = 0,
            overlayAlpha = 1f,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(top = paddingValues.calculateTopPadding())
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 28.dp, end = 16.dp, bottom = 32.dp)
                    .graphicsLayer { alpha = authContentAlpha },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnboardingAuthCard(
                    enteredName = "",
                    loginViewModelState = loginViewModelState,
                    signupViewModelState = signupViewModelState,
                    isLoading = loginViewModelState.isLoading || signupViewModelState.isLoading,
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
                    subtitleOverride = "Use email, Google, or continue as guest. Then we'll build your personalized dream path.",
                    showGuestButton = true,
                    playEntryAnimation = false,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                )
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}
