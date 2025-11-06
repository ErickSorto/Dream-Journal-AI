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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import org.ballistic.dreamjournalai.shared.dream_account.AccountSettingsScreen
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.AddEditDreamScreen
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AddEditDreamViewModel
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModel
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModel
import org.ballistic.dreamjournalai.shared.dream_favorites.presentation.DreamFavoriteScreen
import org.ballistic.dreamjournalai.shared.dream_favorites.presentation.viewmodel.DreamFavoriteScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_fullscreen.FullScreenImageScreen
import org.ballistic.dreamjournalai.shared.dream_fullscreen.FullScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.DreamJournalListScreen
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.viewmodel.DreamJournalListViewModel
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.shared.dream_maintenance.MaintenanceScreen
import org.ballistic.dreamjournalai.shared.dream_nightmares.presentation.DreamNightmareScreen
import org.ballistic.dreamjournalai.shared.dream_nightmares.presentation.viewmodel.DreamNightmareScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.DreamStatisticScreen
import org.ballistic.dreamjournalai.shared.dream_statistics.presentation.viewmodel.DreamStatisticScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.StoreScreen
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.viewmodel.StoreScreenViewModel
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.SymbolScreen
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.viewmodel.DictionaryScreenViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ScreenGraph(
    navControllerProvider: () -> NavHostController,
    mainScreenViewModelState: MainScreenViewModelState,
    bottomPaddingValue: Dp,
    onMainEvent: (MainScreenEvent) -> Unit = {},
    onNavigateToOnboardingScreen: () -> Unit = {},
) {

    // Accept a provider lambda instead of a raw NavHostController parameter so the
    // composable signature stays stable. We then read the controller and keep a
    // rememberUpdatedState reference to it for safe, up-to-date usage inside the
    // NavHost and any navigation lambdas.
    val providedNavController = navControllerProvider()
    val currentNavController by rememberUpdatedState(providedNavController)

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
                DreamJournalListScreen(
                    dreamJournalListState = dreamJournalListState,
                    bottomPaddingValue = bottomPaddingValue,
                    onMainEvent = { onMainEvent(it) },
                    onDreamListEvent = { dreamJournalListViewModel.onEvent(it) },
                    onNavigateToDream = { dreamID, backgroundID ->
                        currentNavController.popBackStack()
                        currentNavController.navigate(
                            Route.AddEditDreamScreen(
                                dreamID = dreamID,
                                backgroundID = backgroundID
                            )
                        )
                    },
                    mainScreenViewModelState = mainScreenViewModelState,
                    searchTextFieldState = searchTextFieldState
                )
            }

            //store
            composable<Route.StoreScreen> {
                val storeScreenViewModel = koinViewModel<StoreScreenViewModel>()
                val storeScreenViewModelState = storeScreenViewModel.storeScreenViewModelState
                    .collectAsStateWithLifecycle().value
                StoreScreen(
                    storeScreenViewModelState = storeScreenViewModelState,
                    bottomPaddingValue = bottomPaddingValue,
                    onMainEvent = { onMainEvent(it) },
                    onStoreEvent = { storeScreenViewModel.onEvent(it) },
                    navigateToAccountScreen = {
                        currentNavController.popBackStack()
                        currentNavController.navigate(Route.AccountSettings)
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
                        currentNavController.popBackStack()
                        currentNavController.navigate(Route.DreamJournalScreen)
                    },
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
                    onMainEvent = { onMainEvent(it) }
                )
            }

            composable<Route.Favorites> {
                val dreamFavoriteScreenViewModel = koinViewModel<DreamFavoriteScreenViewModel>()
                val dreamFavoriteScreenState = dreamFavoriteScreenViewModel.dreamFavoriteScreenState
                    .collectAsStateWithLifecycle()
                DreamFavoriteScreen(
                    dreamFavoriteScreenState = dreamFavoriteScreenState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onEvent = { dreamFavoriteScreenViewModel.onEvent(it) },
                    onNavigateToDream = { dreamID, backgroundID ->
                        currentNavController.popBackStack()
                        currentNavController.navigate(
                            Route.AddEditDreamScreen(
                                dreamID = dreamID,
                                backgroundID = backgroundID
                            )
                        )
                    },
                )
            }

            composable<Route.AccountSettings> {
                val loginViewModel = koinViewModel<LoginViewModel>()
                val signupViewModel = koinViewModel<SignupViewModel>()

                val loginViewModelState = loginViewModel.state.collectAsStateWithLifecycle().value
                val signupViewModelState = signupViewModel.state.collectAsStateWithLifecycle().value

                AccountSettingsScreen(
                     loginViewModelState = loginViewModelState,
                     signupViewModelState = signupViewModelState,
                     navigateToOnboardingScreen = onNavigateToOnboardingScreen,
                     onLoginEvent = { loginViewModel.onEvent(it) },
                     onSignupEvent = { signupViewModel.onEvent(it) },
                     navigateToDreamJournalScreen = {
                        currentNavController.navigate(Route.DreamJournalScreen) {
                             // Clear up to DreamJournalScreen so it becomes the root after login
                             popUpTo(Route.DreamJournalScreen) {
                                 inclusive = true
                             }
                             launchSingleTop = true
                         }
                     }
                 )
             }

            composable<Route.DreamToolGraphScreen> {
                DreamToolsGraph(
                    mainScreenViewModelState = mainScreenViewModelState,
                    bottomPaddingValue = bottomPaddingValue,
                    onMainEvent = onMainEvent,
                    onNavigate = { dreamID, backgroundID ->
                        currentNavController.popBackStack()
                        currentNavController.navigate(
                             Route.AddEditDreamScreen(dreamID = dreamID, backgroundID = backgroundID)
                         ) {
                             popUpTo(Route.DreamJournalScreen) {
                                 saveState = true
                                 inclusive = true
                             }
                         }
                     }
                 )
             }

            composable<Route.Statistics> {
                val dreamStatisticScreenViewModel = koinViewModel<DreamStatisticScreenViewModel>()
                val dreamStatisticScreenState = dreamStatisticScreenViewModel.dreamStatisticScreen
                    .collectAsStateWithLifecycle()

                DreamStatisticScreen(
                    dreamStatisticScreenState = dreamStatisticScreenState.value,
                    mainScreenViewModelState = mainScreenViewModelState,
                    bottomPaddingValue = bottomPaddingValue,
                    onEvent = {
                        dreamStatisticScreenViewModel.onEvent(it)
                    },
                    onMainEvent = onMainEvent
                )
            }

            //TODO: This is for notifications
            composable<Route.AboutMeScreen> {
//                val dreamNotificationScreenViewModel = koinViewModel<NotificationScreenViewModel>()
//                val dreamNotificationScreenState =
//                    dreamNotificationScreenViewModel.notificationScreenState
//                        .collectAsStateWithLifecycle()
//
//                DreamNotificationSettingScreen(
//                    mainScreenViewModelState = mainScreenViewModelState,
//                    notificationScreenState = dreamNotificationScreenState.value,
//                    bottomPaddingValue = bottomPaddingValue,
//                ) {
//                    dreamNotificationScreenViewModel.onEvent(it)
//                }
                //TODO implement notifications
                MaintenanceScreen()
            }

            composable<Route.Nightmares> {
                val dreamNightmareScreenViewModel = koinViewModel<DreamNightmareScreenViewModel>()
                val dreamNightmareScreenState =
                    dreamNightmareScreenViewModel.dreamNightmareScreenState
                        .collectAsStateWithLifecycle()
                DreamNightmareScreen(
                    dreamNightmareScreenState = dreamNightmareScreenState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onEvent = { dreamNightmareScreenViewModel.onEvent(it) },
                    onNavigateToDream = { dreamID, backgroundID ->
                        currentNavController.popBackStack()
                        currentNavController.navigate(
                            Route.AddEditDreamScreen(
                                dreamID = dreamID,
                                backgroundID = backgroundID
                            )
                        )
                    }
                )
            }

            composable<Route.Symbol> {
                val dictionaryScreenViewModel = koinViewModel<DictionaryScreenViewModel>()
                val dictionaryScreenState = dictionaryScreenViewModel.symbolScreenState
                    .collectAsStateWithLifecycle()
                val searchTextFieldState = dictionaryScreenViewModel.searchTextFieldState
                    .collectAsStateWithLifecycle()
                SymbolScreen(
                    symbolScreenState = dictionaryScreenState.value,
                    mainScreenViewModelState = mainScreenViewModelState,
                    searchTextFieldState = searchTextFieldState.value,
                    bottomPaddingValue = bottomPaddingValue,
                    onMainEvent = { onMainEvent(it) },
                    onEvent = { dictionaryScreenViewModel.onEvent(it) },
                )
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
