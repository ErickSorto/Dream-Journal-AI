package org.ballistic.dreamjournalai.shared.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.InterpretDreamsToolEvent
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.PaintDreamWorldEvent
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.ToolsEvent
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.ToolFullScreenImageScreen
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.dream_tools_screen.DreamToolsScreen
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.InterpretDreamsDetailScreen
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.MassInterpretDreamToolScreen
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.viewmodel.InterpretDreamsViewModel
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.paint_dreams_screen.PaintDreamWorldDetailScreen
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.paint_dreams_screen.PaintDreamWorldScreen
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.paint_dreams_screen.viewmodel.PaintDreamWorldViewModel
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.random_dream_screen.RandomDreamToolScreen
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.random_dream_screen.RandomDreamToolScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DreamToolsGraph(
    bottomPaddingValue: Dp,
    onNavigate: (dreamID: String?, backgroundID: Int) -> Unit,
    onMainEvent: (MainScreenEvent) -> Unit,
    onToolsEvent: (ToolsEvent) -> Unit
) {
    val navController = rememberNavController()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if we are in a route that requires a black background to mask the MainScreen background
    // We exclude "ToolRoute" for PaintDreamWorld because that corresponds to the Detail screen, which should show the blue lighthouse.
    val isPaintScreen = currentRoute?.contains("PaintDreamWorld") == true && currentRoute?.contains("ToolRoute") == false
    val isFullScreen = currentRoute?.contains("FullScreenImage") == true
    
    val showBlackBackground = isPaintScreen || isFullScreen

    val alpha by animateFloatAsState(
        targetValue = if (showBlackBackground) 1f else 0f,
        animationSpec = tween(500)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Mask MainScreen background
        if (alpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = alpha))
            )
        }

        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = Route.Tools,
                enterTransition = { fadeIn(animationSpec = tween(500)) },
                exitTransition = { fadeOut(animationSpec = tween(500)) },
                popEnterTransition = { fadeIn(animationSpec = tween(500)) },
                popExitTransition = { fadeOut(animationSpec = tween(500)) }
            ) {
                composable<Route.Tools> {
                    DreamToolsScreen(
                        animatedVisibilityScope = this,
                        onNavigate = { route ->
                            navController.navigate(route)
                        },
                        onEvent = onToolsEvent
                    )
                }
                composable<ToolRoute.RandomDreamPicker> {
                    val args = it.toRoute<ToolRoute.RandomDreamPicker>()
                    val dreamDrawable = DreamDrawable.valueOf(args.imageID)

                    val randomDreamToolScreenViewModel = koinViewModel<RandomDreamToolScreenViewModel>()
                    val randomDreamToolScreenState =
                        randomDreamToolScreenViewModel.randomDreamToolScreenState
                            .collectAsStateWithLifecycle()

                    RandomDreamToolScreen(
                        animatedVisibilityScope = this,
                        imageID =  dreamDrawable.toDrawableResource(),
                        imagePath = dreamDrawable.name,
                        bottomPadding = bottomPaddingValue,
                        randomDreamToolScreenState = randomDreamToolScreenState.value,
                        onEvent = {
                            randomDreamToolScreenViewModel.onEvent(it)
                        },
                        onNavigateToDream = { dreamID, backgroundID ->
                            onNavigate(dreamID, backgroundID)
                        },
                        navigateUp = {
                            navController.navigateUp()
                        }
                    )
                }

                composable<ToolRoute.AnalyzeMultipleDreamsDetails> { backStackEntry ->
                    val route = backStackEntry.toRoute<ToolRoute.AnalyzeMultipleDreamsDetails>()
                    val dreamDrawable = DreamDrawable.valueOf(route.imageID)
                    val interpretDreamsViewModel = koinViewModel<InterpretDreamsViewModel>()

                    InterpretDreamsDetailScreen(
                        imageID = dreamDrawable.toDrawableResource(),
                        imagePath = dreamDrawable.name,
                        animatedVisibilityScope = this,  // from SharedTransitionScope
                        bottomPadding = bottomPaddingValue,
                        onNavigate = {
                            navController.navigate(Route.AnalyzeMultipleDreams)
                        },
                        navigateUp = {
                            navController.navigateUp()
                        },
                        onEvent = { event ->
                            if (event is InterpretDreamsToolEvent.TriggerVibration) {
                                interpretDreamsViewModel.onEvent(event)
                            }
                        }
                    )
                }

                composable<Route.AnalyzeMultipleDreams> {
                    val interpretDreamsViewModel = koinViewModel<InterpretDreamsViewModel>()
                    val interpretDreamsScreenState = interpretDreamsViewModel.interpretDreamsScreenState
                        .collectAsStateWithLifecycle()
                    MassInterpretDreamToolScreen(
                        interpretDreamsScreenState = interpretDreamsScreenState.value,
                        onEvent = { interpretDreamsViewModel.onEvent(it) },
                        onMainScreenEvent = { onMainEvent(it) },
                        navigateUp = {
                            navController.navigateUp()
                        }
                    )
                }

                composable<ToolRoute.PaintDreamWorld> { backStackEntry ->
                    val route = backStackEntry.toRoute<ToolRoute.PaintDreamWorld>()
                    val dreamDrawable = DreamDrawable.valueOf(route.imageID)
                    val paintDreamWorldViewModel = koinViewModel<PaintDreamWorldViewModel>()

                    PaintDreamWorldDetailScreen(
                        imageID = dreamDrawable.toDrawableResource(),
                        imagePath = dreamDrawable.name,
                        animatedVisibilityScope = this,
                        bottomPadding = bottomPaddingValue,
                        onNavigate = {
                            navController.navigate(Route.PaintDreamWorld)
                        },
                        navigateUp = {
                            navController.navigateUp()
                        },
                        onEvent = { event ->
                            if (event is PaintDreamWorldEvent.TriggerVibration) {
                                paintDreamWorldViewModel.onEvent(event)
                            }
                        }
                    )
                }

                composable<Route.PaintDreamWorld> {
                    val viewModel = koinViewModel<PaintDreamWorldViewModel>()
                    val state = viewModel.state.collectAsStateWithLifecycle()

                    PaintDreamWorldScreen(
                        paintDreamWorldScreenState = state.value,
                        animatedVisibilityScope = this,
                        onEvent = viewModel::onEvent,
                        onImageClick = { imageUrl ->
                            navController.navigate(ToolRoute.FullScreenImage(imageUrl))
                        },
                        onMainEvent = onMainEvent,
                        navigateUp = {
                            navController.navigateUp()
                        }
                    )
                }
                
                composable<ToolRoute.FullScreenImage> { backStackEntry ->
                    val route = backStackEntry.toRoute<ToolRoute.FullScreenImage>()
                    
                    ToolFullScreenImageScreen(
                        imageID = route.imageURL,
                        animatedVisibilityScope = this,
                        onBackPress = {
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
}
