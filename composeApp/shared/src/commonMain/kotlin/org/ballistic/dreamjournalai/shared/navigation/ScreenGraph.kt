package org.ballistic.dreamjournalai.shared.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import org.ballistic.dreamjournalai.shared.core.platform.getPlatformName
import org.ballistic.dreamjournalai.shared.core.platform.isDebugBuild
import org.ballistic.dreamjournalai.shared.core.util.BackHandler
import org.ballistic.dreamjournalai.shared.dream_account.AccountSettingsScreen
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.AddEditDreamScreen
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AddEditDreamViewModel
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModel
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModel
import org.ballistic.dreamjournalai.shared.dream_debug.presentation.DebugToolsScreen
import org.ballistic.dreamjournalai.shared.dream_debug.presentation.viewmodel.DebugToolsViewModel
import org.ballistic.dreamjournalai.shared.dream_favorites.presentation.DreamFavoriteScreen
import org.ballistic.dreamjournalai.shared.dream_favorites.presentation.viewmodel.DreamFavoriteScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_fullscreen.FullScreenImageScreen
import org.ballistic.dreamjournalai.shared.dream_fullscreen.FullScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.DreamJournalListScreen
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.viewmodel.DreamJournalListViewModel
import org.ballistic.dreamjournalai.shared.dream_lessons.presentation.DailyLessonDetailScreen
import org.ballistic.dreamjournalai.shared.dream_lessons.presentation.DailyLessonsScreen
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_main.presentation.DailyTokensScreen
import org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.shared.dream_nightmares.presentation.DreamNightmareScreen
import org.ballistic.dreamjournalai.shared.dream_nightmares.presentation.viewmodel.DreamNightmareScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_notifications.presentation.DreamNotificationSettingScreen
import org.ballistic.dreamjournalai.shared.dream_notifications.presentation.viewmodel.NotificationScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumEntrySource
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.DreamStatisticScreen
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel.DreamStatisticScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.StoreScreen
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.viewmodel.StoreScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.SymbolScreen
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.viewmodel.DictionaryScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.dream_tools_screen.viewmodel.DreamToolsScreenViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val ReviewAfterSavedDreamKey = "review_after_saved_dream"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ScreenGraph(
    navControllerProvider: () -> NavHostController,
    bottomPaddingValue: Dp,
    mainScreenViewModelState: MainScreenViewModelState = MainScreenViewModelState(),
    isPremiumMember: Boolean = false,
    onMainEvent: (MainScreenEvent) -> Unit = {},
    onNavigateToOnboardingScreen: () -> Unit = {},
    onNavigateToForcedOnboarding: () -> Unit = onNavigateToOnboardingScreen,
    onNavigateToOnboardingLastPage: () -> Unit = onNavigateToForcedOnboarding,
    onNavigateToPremiumFlow: (PremiumEntrySource) -> Unit = {},
    onSignInRequired: () -> Unit = {},
    pendingNotificationRoute: Route? = null,
    onPendingNotificationRouteHandled: () -> Unit = {},
    onDreamJournalRootSelected: () -> Unit = {},
    onAuthenticatedToDreamJournal: () -> Unit = {},
    onDreamToolsRootSelected: () -> Unit = {},
    requestInAppReview: () -> Unit = {},
) {

    // Accept a provider lambda instead of a raw NavHostController parameter so the
    // composable signature stays stable. We then read the controller and keep a
    // rememberUpdatedState reference to it for safe, up-to-date usage inside the
    // NavHost and any navigation lambdas.
    val providedNavController = navControllerProvider()
    val currentNavController by rememberUpdatedState(providedNavController)
    val isIos = getPlatformName() == "iOS"
    val navBackStackEntry by currentNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var dreamNavigationInFlight by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (!currentRoute.matchesRoute(Route.AddEditDreamScreen(dreamID = "", backgroundID = -1))) {
            dreamNavigationInFlight = false
        }
    }

    fun navigateToDreamOnce(
        dreamID: String?,
        backgroundID: Int,
        optionsBuilder: NavOptionsBuilder.() -> Unit = {},
    ) {
        if (dreamNavigationInFlight) return

        dreamNavigationInFlight = true
        currentNavController.navigate(
            Route.AddEditDreamScreen(dreamID = dreamID, backgroundID = backgroundID),
        ) {
            optionsBuilder()
        }
    }

    fun navigateToDreamJournalRoot() {
        onDreamJournalRootSelected()
        currentNavController.navigate(Route.DreamJournalScreen) {
            popUpTo(Route.DreamJournalScreen) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    fun navigateUpOrDreamJournal() {
        if (currentNavController.navigateUp()) {
            if (currentNavController.currentBackStackEntry?.destination?.route.matchesRoute(Route.DreamJournalScreen)) {
                onDreamJournalRootSelected()
            }
            return
        }

        navigateToDreamJournalRoot()
    }

    fun markReviewAfterNewDreamSave(isExistingDream: Boolean) {
        if (!isExistingDream) {
            currentNavController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(ReviewAfterSavedDreamKey, true)
        }
    }

    @Composable
    fun BackToDreamJournalOnSystemBack() {
        BackHandler(true) {
            navigateToDreamJournalRoot()
        }
    }

    if (isIos) {
        SharedTransitionLayout {
            NavHost(
                navController = currentNavController,
                startDestination = Route.DreamJournalScreen,
                modifier = Modifier.fillMaxSize()
            ) {
            composable<Route.DreamJournalScreen> {
                val dreamJournalListViewModel = koinViewModel<DreamJournalListViewModel>()
                val searchTextFieldState =
                    dreamJournalListViewModel.searchTextFieldState.collectAsStateWithLifecycle().value
                val dreamJournalListState =
                    dreamJournalListViewModel.dreamJournalListState.collectAsStateWithLifecycle().value
                val requestReviewAfterSavedDream =
                    currentNavController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.getStateFlow(ReviewAfterSavedDreamKey, false)
                        ?.collectAsStateWithLifecycle()
                        ?.value == true
                DreamJournalListScreen(
                    dreamJournalListState = dreamJournalListState,
                    bottomPaddingValue = bottomPaddingValue,
                    isBackgroundIntroComplete = mainScreenViewModelState.isBackgroundIntroComplete,
                    isBackgroundBlurComplete = mainScreenViewModelState.isBackgroundBlurComplete,
                    requestReviewAfterSavedDream = requestReviewAfterSavedDream,
                    onReviewAfterSavedDreamConsumed = {
                        currentNavController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set(ReviewAfterSavedDreamKey, false)
                    },
                    requestInAppReview = requestInAppReview,
                    onMainEvent = { onMainEvent(it) },
                    onDreamListEvent = { dreamJournalListViewModel.onEvent(it) },
                    onNavigateToDream = { dreamID, backgroundID ->
                        navigateToDreamOnce(dreamID, backgroundID)
                    },
                    searchTextFieldState = searchTextFieldState
                )
            }

            composable<Route.StoreScreen>(typeMap = storeRouteTypeMap) { backStackEntry ->
                val args = backStackEntry.toRoute<Route.StoreScreen>()
                val storeScreenViewModel = koinViewModel<StoreScreenViewModel>()
                val storeScreenViewModelState = storeScreenViewModel.storeScreenViewModelState
                    .collectAsStateWithLifecycle().value
                StoreScreen(
                    storeScreenViewModelState = storeScreenViewModelState,
                    initialPage = args.initialPage,
                    bottomPaddingValue = bottomPaddingValue,
                    storeBackgroundResource = mainScreenViewModelState.backgroundResource,
                    onMainEvent = { onMainEvent(it) },
                    onStoreEvent = { storeScreenViewModel.onEvent(it) },
                    navigateToAccountScreen = {
                        currentNavController.navigateUp()
                        currentNavController.navigate(Route.AccountSettings)
                    },
                    navigateBack = {
                        navigateToDreamJournalRoot()
                    },
                )
            }

            composable<Route.DailyTokensScreen> {
                DailyTokensScreen(
                    mainScreenViewModelState = mainScreenViewModelState,
                    bottomPaddingValue = bottomPaddingValue,
                    isPremium = isPremiumMember,
                    onMainEvent = onMainEvent,
                    onSignInRequired = onSignInRequired,
                    onUpgrade = {
                        onNavigateToPremiumFlow(PremiumEntrySource.DailyTokens)
                    },
                    onGetMoreDreamTokens = {
                        currentNavController.navigate(Route.StoreScreen(StoreInitialPage.DreamTokens)) {
                            launchSingleTop = true
                        }
                    }
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.DailyLessonsScreen> {
                DailyLessonsScreen(
                    animatedVisibilityScope = this,
                    bottomPaddingValue = bottomPaddingValue,
                    isPremiumMember = isPremiumMember,
                    onMainEvent = onMainEvent,
                    onUpgrade = {
                        onNavigateToPremiumFlow(PremiumEntrySource.DailyLessons)
                    },
                    onNavigateToLesson = { lessonId, imageUrl ->
                        currentNavController.navigate(
                            Route.DailyLessonDetailScreen(
                                lessonId = lessonId,
                                initialImageUrl = imageUrl
                            )
                        )
                    }
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.DailyLessonDetailScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.DailyLessonDetailScreen>()
                DailyLessonDetailScreen(
                    animatedVisibilityScope = this,
                    lessonId = args.lessonId,
                    initialImageUrl = args.initialImageUrl,
                    bottomPaddingValue = bottomPaddingValue,
                    backgroundResource = mainScreenViewModelState.backgroundResource,
                    isPremiumMember = isPremiumMember,
                    onMainEvent = onMainEvent,
                    onUpgrade = {
                        onNavigateToPremiumFlow(PremiumEntrySource.DailyLessons)
                    },
                    onNavigateUp = {
                        currentNavController.navigateUp()
                    }
                )
            }

            composable<Route.AddEditDreamScreen> { backStackEntry ->
                val animatedVisibilityScope = this
                SharedTransitionLayout {
                    val args = backStackEntry.toRoute<Route.AddEditDreamScreen>()
                    val image = args.backgroundID

                    backStackEntry.savedStateHandle["dreamID"] = args.dreamID

                    val addEditDreamViewModel = koinViewModel<AddEditDreamViewModel>(
                        parameters = { parametersOf(backStackEntry.savedStateHandle) }
                    )

                    val addEditDreamState =
                        addEditDreamViewModel.addEditDreamState.collectAsStateWithLifecycle().value
                    val dreamTitle =
                        addEditDreamViewModel.titleTextFieldState.collectAsStateWithLifecycle().value
                    val dreamContent =
                        addEditDreamViewModel.contentTextFieldState.collectAsStateWithLifecycle().value

                    Logger.d("ScreenGraph") { "Dream ID: ${currentNavController.currentBackStackEntry?.savedStateHandle?.get<String>("dreamID")}" }
                    AddEditDreamScreen(
                        dreamImage = image,
                        dreamTitleState = dreamTitle,
                        dreamContentState = dreamContent,
                        addEditDreamState = addEditDreamState,
                        onMainEvent = { onMainEvent(it) },
                        onAddEditDreamEvent = { addEditDreamViewModel.onEvent(it) },
                        animateVisibilityScope = animatedVisibilityScope,
                        onNavigateToDreamJournalScreen = {
                            navigateUpOrDreamJournal()
                        },
                        pendingExitRequested = pendingNotificationRoute != null,
                        onPendingExitCancelled = onPendingNotificationRouteHandled,
                        onNavigateAfterPendingExit = {
                            val route = pendingNotificationRoute ?: return@AddEditDreamScreen
                            navigateUpOrDreamJournal()
                            currentNavController.navigate(route) {
                                popUpTo(Route.DreamJournalScreen) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onDreamSavedBeforeNavigate = {
                            markReviewAfterNewDreamSave(args.dreamID?.isNotBlank() == true)
                        },
                        isExistingDream = args.dreamID?.isNotBlank() == true,
                        onImageClick = { imageID ->
                            currentNavController.navigate(
                                Route.FullScreenImageScreen(imageID)
                            )
                        }
                    )
                }
            }

            composable<Route.FullScreenImageScreen>{ backStackEntry ->
                val animatedVisibilityScope = this
                SharedTransitionLayout {
                    val args = backStackEntry.toRoute<Route.FullScreenImageScreen>()
                    val fullScreenViewModel = koinViewModel<FullScreenViewModel>()

                    FullScreenImageScreen(
                        imageID = args.imageID,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onBackPress = {
                            currentNavController.navigateUp()
                        },
                        onFullScreenEvent = {
                            fullScreenViewModel.onEvent(it)
                        },
                    )
                }
            }

            composable<Route.Favorites> {
                val dreamFavoriteScreenViewModel = koinViewModel<DreamFavoriteScreenViewModel>()
                val dreamFavoriteScreenState = dreamFavoriteScreenViewModel.dreamFavoriteScreenState
                    .collectAsStateWithLifecycle()
                DreamFavoriteScreen(
                    dreamFavoriteScreenState = dreamFavoriteScreenState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onMainEvent = onMainEvent,
                    onEvent = { dreamFavoriteScreenViewModel.onEvent(it) },
                    onNavigateToDream = { dreamID, backgroundID ->
                        navigateToDreamOnce(dreamID, backgroundID)
                    },
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.AccountSettings> {
                val loginViewModel = koinViewModel<LoginViewModel>()
                val signupViewModel = koinViewModel<SignupViewModel>()

                val loginViewModelState = loginViewModel.state.collectAsStateWithLifecycle().value
                val signupViewModelState = signupViewModel.state.collectAsStateWithLifecycle().value

                AccountSettingsScreen(
                     loginViewModelState = loginViewModelState,
                     signupViewModelState = signupViewModelState,
                     isPremiumMember = isPremiumMember,
                     onMainEvent = { onMainEvent(it) },
                     navigateToOnboardingScreen = onNavigateToOnboardingScreen,
                     onLoginEvent = { loginViewModel.onEvent(it) },
                     onSignupEvent = { signupViewModel.onEvent(it) },
                     navigateToDreamJournalAfterSignIn = {
                        navigateToDreamJournalRoot()
                        onAuthenticatedToDreamJournal()
                     },
                     navigateToDreamJournalScreen = {
                        navigateToDreamJournalRoot()
                     }
                 )
                BackToDreamJournalOnSystemBack()
             }

            composable<Route.DreamToolGraphScreen> {
                val dreamToolsScreenViewModel = koinViewModel<DreamToolsScreenViewModel>()
                DreamToolsGraph(
                    bottomPaddingValue = bottomPaddingValue,
                    toolBackgroundResource = mainScreenViewModelState.backgroundResource,
                    onMainEvent = onMainEvent,
                    onToolsEvent = { dreamToolsScreenViewModel.onEvent(it) },
                    onNavigate = { dreamID, backgroundID ->
                         navigateToDreamOnce(dreamID, backgroundID) {
                             popUpTo(Route.DreamJournalScreen) {
                                 saveState = false
                             }
                             launchSingleTop = true
                         }
                     },
                    onNavigateBackToDreamJournal = {
                        navigateToDreamJournalRoot()
                    },
                    onToolsRootSelected = {
                        onDreamToolsRootSelected()
                    }
                 )
             }

            composable<Route.Statistics> {
                val dreamStatisticScreenViewModel = koinViewModel<DreamStatisticScreenViewModel>()
                val dreamStatisticScreenState = dreamStatisticScreenViewModel.dreamStatisticScreen
                    .collectAsStateWithLifecycle()

                DreamStatisticScreen(
                    dreamStatisticScreenState = dreamStatisticScreenState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onEvent = {
                        dreamStatisticScreenViewModel.onEvent(it)
                    },
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.NotificationSettings> {
                val dreamNotificationScreenViewModel = koinViewModel<NotificationScreenViewModel>()
                val dreamNotificationScreenState =
                    dreamNotificationScreenViewModel.notificationScreenState
                        .collectAsStateWithLifecycle()

                DreamNotificationSettingScreen(
                    notificationScreenState = dreamNotificationScreenState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    isPremiumMember = isPremiumMember,
                    onMainEvent = onMainEvent,
                    onEvent = dreamNotificationScreenViewModel::onEvent,
                    onNavigateToPremiumFlow = onNavigateToPremiumFlow,
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.DeveloperTools> {
                if (isDebugBuild()) {
                    val debugToolsViewModel = koinViewModel<DebugToolsViewModel>()
                    val debugToolsScreenState =
                        debugToolsViewModel.state.collectAsStateWithLifecycle()

                    DebugToolsScreen(
                        state = debugToolsScreenState.value,
                        bottomPaddingValue = bottomPaddingValue,
                        onMainEvent = onMainEvent,
                        onEvent = debugToolsViewModel::onEvent,
                        onNavigateToOnboarding = onNavigateToForcedOnboarding,
                        onNavigateToOnboardingLastPage = onNavigateToOnboardingLastPage,
                        onNavigateToPremiumFlow = onNavigateToPremiumFlow,
                    )
                    BackToDreamJournalOnSystemBack()
                } else {
                    LaunchedEffect(Unit) {
                        currentNavController.navigate(Route.DreamJournalScreen) {
                            popUpTo(Route.DreamJournalScreen) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
            }

            composable<Route.Nightmares> {
                val dreamNightmareScreenViewModel = koinViewModel<DreamNightmareScreenViewModel>()
                val dreamNightmareScreenState =
                    dreamNightmareScreenViewModel.dreamNightmareScreenState
                        .collectAsStateWithLifecycle()
                DreamNightmareScreen(
                    dreamNightmareScreenState = dreamNightmareScreenState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onMainEvent = onMainEvent,
                    onEvent = { dreamNightmareScreenViewModel.onEvent(it) },
                    onNavigateToDream = { dreamID, backgroundID ->
                        navigateToDreamOnce(dreamID, backgroundID)
                    }
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.Symbol> {
                val dictionaryScreenViewModel = koinViewModel<DictionaryScreenViewModel>()
                val dictionaryScreenState = dictionaryScreenViewModel.symbolScreenState
                    .collectAsStateWithLifecycle()
                val searchTextFieldState = dictionaryScreenViewModel.searchTextFieldState
                    .collectAsStateWithLifecycle()
                SymbolScreen(
                    symbolScreenState = dictionaryScreenState.value,
                    searchTextFieldState = searchTextFieldState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onMainEvent = { onMainEvent(it) },
                    onEvent = { dictionaryScreenViewModel.onEvent(it) },
                )
                BackToDreamJournalOnSystemBack()
            }
        }
        }
        return
    }

    SharedTransitionLayout{
        NavHost(
            navController = currentNavController,
            startDestination = Route.DreamJournalScreen,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) },
            popEnterTransition = { fadeIn(animationSpec = tween(500)) },
            popExitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            composable<Route.DreamJournalScreen> {
                val dreamJournalListViewModel = koinViewModel<DreamJournalListViewModel>()
                val searchTextFieldState =
                    dreamJournalListViewModel.searchTextFieldState.collectAsStateWithLifecycle().value
                val dreamJournalListState =
                    dreamJournalListViewModel.dreamJournalListState.collectAsStateWithLifecycle().value
                val requestReviewAfterSavedDream =
                    currentNavController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.getStateFlow(ReviewAfterSavedDreamKey, false)
                        ?.collectAsStateWithLifecycle()
                        ?.value == true
                DreamJournalListScreen(
                    dreamJournalListState = dreamJournalListState,
                    bottomPaddingValue = bottomPaddingValue,
                    isBackgroundIntroComplete = mainScreenViewModelState.isBackgroundIntroComplete,
                    isBackgroundBlurComplete = mainScreenViewModelState.isBackgroundBlurComplete,
                    requestReviewAfterSavedDream = requestReviewAfterSavedDream,
                    onReviewAfterSavedDreamConsumed = {
                        currentNavController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set(ReviewAfterSavedDreamKey, false)
                    },
                    requestInAppReview = requestInAppReview,
                    onMainEvent = { onMainEvent(it) },
                    onDreamListEvent = { dreamJournalListViewModel.onEvent(it) },
                    onNavigateToDream = { dreamID, backgroundID ->
                        navigateToDreamOnce(dreamID, backgroundID)
                    },
                    searchTextFieldState = searchTextFieldState
                )
            }

            //store
            composable<Route.StoreScreen>(typeMap = storeRouteTypeMap) { backStackEntry ->
                val args = backStackEntry.toRoute<Route.StoreScreen>()
                val storeScreenViewModel = koinViewModel<StoreScreenViewModel>()
                val storeScreenViewModelState = storeScreenViewModel.storeScreenViewModelState
                    .collectAsStateWithLifecycle().value
                StoreScreen(
                    storeScreenViewModelState = storeScreenViewModelState,
                    initialPage = args.initialPage,
                    bottomPaddingValue = bottomPaddingValue,
                    storeBackgroundResource = mainScreenViewModelState.backgroundResource,
                    onMainEvent = { onMainEvent(it) },
                    onStoreEvent = { storeScreenViewModel.onEvent(it) },
                    navigateToAccountScreen = {
                        currentNavController.navigateUp()
                        currentNavController.navigate(Route.AccountSettings)
                    },
                    navigateBack = {
                        navigateToDreamJournalRoot()
                    },
                )
            }

            composable<Route.DailyTokensScreen> {
                DailyTokensScreen(
                    mainScreenViewModelState = mainScreenViewModelState,
                    bottomPaddingValue = bottomPaddingValue,
                    isPremium = isPremiumMember,
                    onMainEvent = onMainEvent,
                    onSignInRequired = onSignInRequired,
                    onUpgrade = {
                        onNavigateToPremiumFlow(PremiumEntrySource.DailyTokens)
                    },
                    onGetMoreDreamTokens = {
                        currentNavController.navigate(Route.StoreScreen(StoreInitialPage.DreamTokens)) {
                            launchSingleTop = true
                        }
                    }
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.DailyLessonsScreen> {
                DailyLessonsScreen(
                    animatedVisibilityScope = this,
                    bottomPaddingValue = bottomPaddingValue,
                    isPremiumMember = isPremiumMember,
                    onMainEvent = onMainEvent,
                    onUpgrade = {
                        onNavigateToPremiumFlow(PremiumEntrySource.DailyLessons)
                    },
                    onNavigateToLesson = { lessonId, imageUrl ->
                        currentNavController.navigate(
                            Route.DailyLessonDetailScreen(
                                lessonId = lessonId,
                                initialImageUrl = imageUrl
                            )
                        )
                    }
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.DailyLessonDetailScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.DailyLessonDetailScreen>()
                DailyLessonDetailScreen(
                    animatedVisibilityScope = this,
                    lessonId = args.lessonId,
                    initialImageUrl = args.initialImageUrl,
                    bottomPaddingValue = bottomPaddingValue,
                    backgroundResource = mainScreenViewModelState.backgroundResource,
                    isPremiumMember = isPremiumMember,
                    onMainEvent = onMainEvent,
                    onUpgrade = {
                        onNavigateToPremiumFlow(PremiumEntrySource.DailyLessons)
                    },
                    onNavigateUp = {
                        currentNavController.navigateUp()
                    }
                )
            }

            composable<Route.AddEditDreamScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.AddEditDreamScreen>()
                val image = args.backgroundID

                backStackEntry.savedStateHandle["dreamID"] = args.dreamID

                val addEditDreamViewModel = koinViewModel<AddEditDreamViewModel>(
                    parameters = { parametersOf(backStackEntry.savedStateHandle) }
                )



                Box(Modifier.fillMaxSize()) {
                    Text(
                        text = "Dream ID: ${backStackEntry.savedStateHandle.get<String>("dreamID")}",
                        fontSize = 24.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                val addEditDreamState =
                    addEditDreamViewModel.addEditDreamState.collectAsStateWithLifecycle().value
                val dreamTitle =
                    addEditDreamViewModel.titleTextFieldState.collectAsStateWithLifecycle().value
                val dreamContent =
                    addEditDreamViewModel.contentTextFieldState.collectAsStateWithLifecycle().value

                //println("Dream ID: ${navController.currentBackStackEntry?.savedStateHandle?.get<String>("dreamID")}")
                Logger.d("ScreenGraph") { "Dream ID: ${currentNavController.currentBackStackEntry?.savedStateHandle?.get<String>("dreamID")}" }
                AddEditDreamScreen(
                    dreamImage = image,
                    dreamTitleState = dreamTitle,
                    dreamContentState = dreamContent,
                    addEditDreamState = addEditDreamState,
                    onMainEvent = { onMainEvent(it) },
                    onAddEditDreamEvent = { addEditDreamViewModel.onEvent(it) },
                    animateVisibilityScope = this,
                    onNavigateToDreamJournalScreen = {
                        navigateUpOrDreamJournal()
                    },
                    pendingExitRequested = pendingNotificationRoute != null,
                    onPendingExitCancelled = onPendingNotificationRouteHandled,
                    onNavigateAfterPendingExit = {
                        val route = pendingNotificationRoute ?: return@AddEditDreamScreen
                        navigateUpOrDreamJournal()
                        currentNavController.navigate(route) {
                            popUpTo(Route.DreamJournalScreen) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onDreamSavedBeforeNavigate = {
                        markReviewAfterNewDreamSave(args.dreamID?.isNotBlank() == true)
                    },
                    isExistingDream = args.dreamID?.isNotBlank() == true,
                    onImageClick = { imageID ->
                        currentNavController.navigate(
                            Route.FullScreenImageScreen(imageID)
                        )
                    }
                )
            }

            composable<Route.FullScreenImageScreen>{ it ->
                val args = it.toRoute<Route.FullScreenImageScreen>()
                val fullScreenViewModel = koinViewModel<FullScreenViewModel>()

                FullScreenImageScreen(
                    imageID = args.imageID,
                    animatedVisibilityScope = this,
                    onBackPress = {
                        currentNavController.navigateUp()
                    },
                    onFullScreenEvent = {
                        fullScreenViewModel.onEvent(it)
                    },
                )
            }

            composable<Route.Favorites> {
                val dreamFavoriteScreenViewModel = koinViewModel<DreamFavoriteScreenViewModel>()
                val dreamFavoriteScreenState = dreamFavoriteScreenViewModel.dreamFavoriteScreenState
                    .collectAsStateWithLifecycle()
                DreamFavoriteScreen(
                    dreamFavoriteScreenState = dreamFavoriteScreenState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onMainEvent = onMainEvent,
                    onEvent = { dreamFavoriteScreenViewModel.onEvent(it) },
                    onNavigateToDream = { dreamID, backgroundID ->
                        navigateToDreamOnce(dreamID, backgroundID)
                    },
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.AccountSettings> {
                val loginViewModel = koinViewModel<LoginViewModel>()
                val signupViewModel = koinViewModel<SignupViewModel>()

                val loginViewModelState = loginViewModel.state.collectAsStateWithLifecycle().value
                val signupViewModelState = signupViewModel.state.collectAsStateWithLifecycle().value

                AccountSettingsScreen(
                     loginViewModelState = loginViewModelState,
                     signupViewModelState = signupViewModelState,
                     isPremiumMember = isPremiumMember,
                     onMainEvent = { onMainEvent(it) },
                     navigateToOnboardingScreen = onNavigateToOnboardingScreen,
                     onLoginEvent = { loginViewModel.onEvent(it) },
                     onSignupEvent = { signupViewModel.onEvent(it) },
                     navigateToDreamJournalAfterSignIn = {
                        navigateToDreamJournalRoot()
                        onAuthenticatedToDreamJournal()
                     },
                     navigateToDreamJournalScreen = {
                        navigateToDreamJournalRoot()
                     }
                 )
                BackToDreamJournalOnSystemBack()
             }

            composable<Route.DreamToolGraphScreen> {
                val dreamToolsScreenViewModel = koinViewModel<DreamToolsScreenViewModel>()
                DreamToolsGraph(
                    bottomPaddingValue = bottomPaddingValue,
                    toolBackgroundResource = mainScreenViewModelState.backgroundResource,
                    onMainEvent = onMainEvent,
                    onToolsEvent = { dreamToolsScreenViewModel.onEvent(it) },
                    onNavigate = { dreamID, backgroundID ->
                         navigateToDreamOnce(dreamID, backgroundID) {
                             popUpTo(Route.DreamJournalScreen) {
                                 saveState = false
                             }
                             launchSingleTop = true
                         }
                     },
                    onNavigateBackToDreamJournal = {
                        navigateToDreamJournalRoot()
                    },
                    onToolsRootSelected = {
                        onDreamToolsRootSelected()
                    }
                 )
             }

            composable<Route.Statistics> {
                val dreamStatisticScreenViewModel = koinViewModel<DreamStatisticScreenViewModel>()
                val dreamStatisticScreenState = dreamStatisticScreenViewModel.dreamStatisticScreen
                    .collectAsStateWithLifecycle()

                DreamStatisticScreen(
                    dreamStatisticScreenState = dreamStatisticScreenState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onEvent = {
                        dreamStatisticScreenViewModel.onEvent(it)
                    },
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.NotificationSettings> {
                val dreamNotificationScreenViewModel = koinViewModel<NotificationScreenViewModel>()
                val dreamNotificationScreenState =
                    dreamNotificationScreenViewModel.notificationScreenState
                        .collectAsStateWithLifecycle()

                DreamNotificationSettingScreen(
                    notificationScreenState = dreamNotificationScreenState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    isPremiumMember = isPremiumMember,
                    onMainEvent = onMainEvent,
                    onEvent = dreamNotificationScreenViewModel::onEvent,
                    onNavigateToPremiumFlow = onNavigateToPremiumFlow,
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.DeveloperTools> {
                if (isDebugBuild()) {
                    val debugToolsViewModel = koinViewModel<DebugToolsViewModel>()
                    val debugToolsScreenState =
                        debugToolsViewModel.state.collectAsStateWithLifecycle()

                    DebugToolsScreen(
                        state = debugToolsScreenState.value,
                        bottomPaddingValue = bottomPaddingValue,
                        onMainEvent = onMainEvent,
                        onEvent = debugToolsViewModel::onEvent,
                        onNavigateToOnboarding = onNavigateToForcedOnboarding,
                        onNavigateToOnboardingLastPage = onNavigateToOnboardingLastPage,
                        onNavigateToPremiumFlow = onNavigateToPremiumFlow,
                    )
                    BackToDreamJournalOnSystemBack()
                } else {
                    LaunchedEffect(Unit) {
                        currentNavController.navigate(Route.DreamJournalScreen) {
                            popUpTo(Route.DreamJournalScreen) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
            }

            composable<Route.Nightmares> {
                val dreamNightmareScreenViewModel = koinViewModel<DreamNightmareScreenViewModel>()
                val dreamNightmareScreenState =
                    dreamNightmareScreenViewModel.dreamNightmareScreenState
                        .collectAsStateWithLifecycle()
                DreamNightmareScreen(
                    dreamNightmareScreenState = dreamNightmareScreenState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onMainEvent = onMainEvent,
                    onEvent = { dreamNightmareScreenViewModel.onEvent(it) },
                    onNavigateToDream = { dreamID, backgroundID ->
                        navigateToDreamOnce(dreamID, backgroundID)
                    }
                )
                BackToDreamJournalOnSystemBack()
            }

            composable<Route.Symbol> {
                val dictionaryScreenViewModel = koinViewModel<DictionaryScreenViewModel>()
                val dictionaryScreenState = dictionaryScreenViewModel.symbolScreenState
                    .collectAsStateWithLifecycle()
                val searchTextFieldState = dictionaryScreenViewModel.searchTextFieldState
                    .collectAsStateWithLifecycle()
                SymbolScreen(
                    symbolScreenState = dictionaryScreenState.value,
                    searchTextFieldState = searchTextFieldState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onMainEvent = { onMainEvent(it) },
                    onEvent = { dictionaryScreenViewModel.onEvent(it) },
                )
                BackToDreamJournalOnSystemBack()
            }

//        composable(route = Screens.DreamSettings.route) {
//            FeatureComingSoonScreen(
//                onNavigateToAboutMeScreen = {
//                    navController.popBackStack()
//                    navController.navigate(Screens.AboutMe.route)
//                }
//            )
//        }
        }
    }
}
