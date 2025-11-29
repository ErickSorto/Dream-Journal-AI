package org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.interpret_vector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.BottomNavigationController
import org.ballistic.dreamjournalai.shared.BottomNavigationEvent
import org.ballistic.dreamjournalai.shared.DrawerController
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.core.util.BackHandler
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_tools.domain.MassInterpretationTabs
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.InterpretDreamsToolEvent
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.DreamToolButton
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.DreamToolScreenWithNavigateUpTopBar
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.components.MassInterpretationHistoryPage
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.components.MassInterpretationResultPage
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.components.SelectDreamsPage
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.interpret_dreams_screen.viewmodel.InterpretDreamsScreenState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.DarkBlue
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MassInterpretDreamToolScreen(
    interpretDreamsScreenState: InterpretDreamsScreenState,
    onEvent: (InterpretDreamsToolEvent) -> Unit,
    onMainScreenEvent: (MainScreenEvent) -> Unit,
    navigateUp: () -> Unit
) {
    val chosenDreams = interpretDreamsScreenState.chosenDreams
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isGlowVisible by remember { mutableStateOf(false) }

    if (interpretDreamsScreenState.isLoading) {
        BackHandler(true) {
            // Do nothing
        }
    }

    // Lock the drawer when this screen is active
    LaunchedEffect(Unit) {
        DrawerController.disable()
    }

    DisposableEffect(Unit) {
        onDispose {
            DrawerController.enable()
        }
    }

    LaunchedEffect(Unit) {
        BottomNavigationController.sendEvent(BottomNavigationEvent.SetVisibility(false))
        onEvent(InterpretDreamsToolEvent.GetDreamTokens)
        onEvent(InterpretDreamsToolEvent.GetDreams)
        onEvent(InterpretDreamsToolEvent.GetMassInterpretations)
        delay(1000)
        isGlowVisible = true
    }

    val pages = MassInterpretationTabs.entries.map { it }
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Scaffold(
        topBar = {
            DreamToolScreenWithNavigateUpTopBar(
                title = "Interpret Dreams",
                navigateUp = {
                    DrawerController.enable()
                    navigateUp()
                },
                onEvent = { onEvent(InterpretDreamsToolEvent.TriggerVibration) }, // Added TriggerVibration
                enabledBack = !interpretDreamsScreenState.isLoading,
            )
        },
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .dynamicBottomNavigationPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        color = White,
                        modifier = Modifier.tabIndicatorOffset(pagerState.currentPage)
                    )
                },
                divider = {},
                contentColor = White,
                containerColor = DarkBlue.copy(alpha = 0.5f),
            ) {
                pages.forEachIndexed { index, page ->
                    val isSelected = pagerState.currentPage == index
                    val transition =
                        updateTransition(targetState = isSelected, label = "TabTransition")

                    val scale by transition.animateFloat(
                        label = "Scale",
                        transitionSpec = {
                            if (targetState) {
                                keyframes {
                                    durationMillis = 2300
                                    1.25f at 1500 using LinearOutSlowInEasing
                                    1.25f at 2000 using LinearEasing
                                    0f at 2300 using FastOutLinearInEasing
                                }
                            } else {
                                tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            }
                        }
                    ) { state ->
                        if (state) 0f else 1f
                    }

                    val iconColor by transition.animateColor(
                        label = "Color",
                        transitionSpec = {
                            tween(durationMillis = 500)
                        }
                    ) { state ->
                        if (state) Color.White else Color.Gray.copy(.6f)
                    }

                    Tab(
                        selected = isSelected,
                        onClick = {
                            onEvent(InterpretDreamsToolEvent.TriggerVibration)
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },

                        text = {
                            if (!isSelected || scale > 0) {
                                Icon(
                                    painter = painterResource(page.icon),
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .graphicsLayer {
                                            scaleX = if (isSelected) scale else 1f
                                            scaleY = if (isSelected) scale else 1f
                                        }
                                )
                            }

                            if (isSelected) {
                                AnimatedVisibility(
                                    visible = scale == 0f,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 400)) + expandVertically(
                                        animationSpec = tween(durationMillis = 300)
                                    ),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 0)),
                                ) {
                                    Text(
                                        text = page.title,
                                        style = typography.titleSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
            ) { page ->
                when (page) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            SelectDreamsPage(
                                interpretDreamsScreenState = interpretDreamsScreenState,
                                snackbarHostState = snackbarHostState,
                                scope = scope,
                                chosenDreams = chosenDreams,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(bottom = 0.dp),
                                onEvent = onEvent
                            )
                            DreamToolButton(
                                text = if (chosenDreams.isEmpty()) "Select Dreams" else "Interpret Dreams (${chosenDreams.size}/15)",
                                icon = Res.drawable.interpret_vector,
                                onClick = {
                                    onEvent(InterpretDreamsToolEvent.TriggerVibration)
                                    if (chosenDreams.isEmpty()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Please select dreams to interpret",
                                                actionLabel = "Dismiss",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else if (chosenDreams.size < 2) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Select at least two dreams to interpret",
                                                actionLabel = "Dismiss",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        scope.launch {
                                            pagerState.animateScrollToPage(1)
                                            onEvent(
                                                InterpretDreamsToolEvent.ToggleBottomMassInterpretationSheetState(
                                                    true
                                                )
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                buttonModifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                isGlowVisible = isGlowVisible
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    1 -> {
                        MassInterpretationResultPage(
                            interpretDreamsScreenState = interpretDreamsScreenState,
                            onEvent = onEvent,
                            onMainScreenEvent = onMainScreenEvent,
                            snackBarHostState = snackbarHostState,
                            scope = scope,
                            pagerState = pagerState,
                        )
                    }

                    2 -> {
                        MassInterpretationHistoryPage(
                            interpretDreamsScreenState = interpretDreamsScreenState,
                            snackbarHostState = snackbarHostState,
                            onEvent = onEvent,
                            scope = scope,
                            pagerState = pagerState,
                        )
                    }
                }
            }
        }
    }
}
