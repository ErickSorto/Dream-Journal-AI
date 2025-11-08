package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation


import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import co.touchlab.kermit.Logger
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.save_dream
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.ballistic.dreamjournalai.shared.core.util.BackHandler
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.AlertSave
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.TabLayout
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AddEditDreamState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.ballistic.dreamjournalai.shared.platform.isIos

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AddEditDreamScreen(
    dreamImage: Int,
    dreamTitleState: TextFieldState,
    dreamContentState: TextFieldState,
    addEditDreamState: AddEditDreamState,
    animateVisibilityScope: AnimatedVisibilityScope,
    onMainEvent: (MainScreenEvent) -> Unit = {},
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit = {},
    onNavigateToDreamJournalScreen: () -> Unit = {},
    onImageClick: (String) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    listOf(
        MainScreenEvent.SetBottomBarVisibilityState(false),
        MainScreenEvent.SetFloatingActionButtonState(false),
        MainScreenEvent.SetSearchingState(false),
        MainScreenEvent.SetDrawerState(false)
    ).forEach(onMainEvent)

    val scope = rememberCoroutineScope()

    BackHandler(true, onBack = {
        val isLoading = addEditDreamState.aiStates.any { it.value.isLoading }
        if (!addEditDreamState.isDreamExitOff && !addEditDreamState.dreamIsSavingLoading && !isLoading) {
            if (addEditDreamState.dreamHasChanged) {
                onAddEditDreamEvent(AddEditDreamEvent.TriggerVibration)
                onAddEditDreamEvent(AddEditDreamEvent.ToggleDialogState(true))
            } else {
                onAddEditDreamEvent(AddEditDreamEvent.TriggerVibration)
                onNavigateToDreamJournalScreen()
            }
        } else {
            //TODO: Show a snackbar that the dream is saving
        }
    })

    val dreamBackgroundImage = remember {
        val initialIndex = when {
            dreamImage >= 0 && dreamImage < Dream.dreamBackgroundImages.size -> {
                // dreamImage is a valid index within the list range
                dreamImage
            }

            addEditDreamState.dreamInfo.dreamBackgroundImage >= 0 &&
                    addEditDreamState.dreamInfo.dreamBackgroundImage < Dream.dreamBackgroundImages.size -> {
                // Fallback to the stored dream background image index if within the valid range
                addEditDreamState.dreamInfo.dreamBackgroundImage
            }

            else -> {
                Dream.dreamBackgroundImages.indices.random()
            }
        }
        mutableIntStateOf(initialIndex)
    }

    Crossfade(
        targetState = dreamBackgroundImage.intValue,
        label = "Background Image Crossfade"
    ) { index ->
        Image(
            painter = painterResource(Dream.dreamBackgroundImages[index]),
            contentDescription = "Dream Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(15.dp)
        )
    }

    if (addEditDreamState.dialogState) {
        focusManager.clearFocus()
        keyboardController?.hide()
        AlertSave(
            onDismiss = {
                onAddEditDreamEvent(AddEditDreamEvent.TriggerVibration)
                onAddEditDreamEvent(AddEditDreamEvent.ToggleDialogState(false))
                scope.launch {
                    onNavigateToDreamJournalScreen()
                }
            },
            onConfirm = {
                onAddEditDreamEvent(AddEditDreamEvent.TriggerVibration)
                onAddEditDreamEvent(AddEditDreamEvent.ToggleDialogState(false))

                onAddEditDreamEvent(AddEditDreamEvent.SaveDream(onSaveSuccess = {
                    onMainEvent(MainScreenEvent.SetDreamRecentlySaved(true))
                    onMainEvent(MainScreenEvent.ShowSnackBar("Dream Saved Successfully :)"))
                    onNavigateToDreamJournalScreen()
                }))
            },
            onClickOutside = {
                onAddEditDreamEvent(AddEditDreamEvent.ToggleDialogState(false))
            }
        )
    }

    val tint = if (!addEditDreamState.dreamIsSavingLoading && !addEditDreamState.isDreamExitOff
    ) White
    else Color.Gray.copy(alpha = 0.1f)

    // Read the raw system status bar inset
    val rawStatusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // New rule: top bar height = statusBarInset + 24.dp, bounded to reasonable min/max
    val minTopBar = if (isIos) 56.dp else 56.dp
    val maxTopBar = 80.dp
    val initialTopBar = (rawStatusBarTop + 24.dp).coerceIn(minTopBar, maxTopBar)
    var topBarHeight by remember { mutableStateOf(initialTopBar) }
    var layoutReady by remember { mutableStateOf(false) }

    // Observe inset briefly and set topBarHeight before showing the bar to avoid flicker
    LaunchedEffect(Unit) {
        val observedInset = withTimeoutOrNull(500L) {
            snapshotFlow { rawStatusBarTop }
                .filter { it >= 0.dp }
                .first()
        } ?: rawStatusBarTop

        val recomputed = (observedInset + 32.dp).coerceIn(minTopBar, maxTopBar)
        topBarHeight = recomputed
        layoutReady = true
        Logger.d { "[DJAI/AddEdit] observedInset=$observedInset topBarHeight=$recomputed" }
    }

    // Draw a small background box at the top (height = cappedStatusBarTop) behind the Scaffold so the app bar background extends into the status area without increasing the topBar measured height. Then set the Scaffold.topBar to a CenterAlignedTopAppBar with a fixed 72.dp height and the same background color. This prevents the topBar from growing too tall and avoids wrapping TabLayout in a Box that broke its layout.
    Box(modifier = Modifier.fillMaxSize()) {
        // Top background that visually extends into the status area (doesn't affect layout)


        // Delay rendering the Scaffold until we've computed the initial top adjustment to
        // avoid a visible growth animation on first render. The background image is already
        // visible above, so the user won't see a blank screen — only the top bar won't flicker.
        if (layoutReady) {
            Scaffold(
                topBar = {
                    // Keep the TopAppBar measured height exactly 72.dp so it doesn't push content down.
                    // Height = base 72.dp + any extra adjustment detected at runtime
                    val topBarAlpha by animateFloatAsState(targetValue = if (layoutReady) 1f else 0f, animationSpec = tween(durationMillis = 180))
                    CenterAlignedTopAppBar(
                        modifier = Modifier
                            .height(topBarHeight)
                            .graphicsLayer { alpha = topBarAlpha },
                        title = {
                            Box(modifier = Modifier.height(topBarHeight)) {
                                Text(
                                    text = "Dream Journal AI",
                                    color = White,
                                    style = typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center),
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    if (addEditDreamState.dreamHasChanged) {
                                        onAddEditDreamEvent(AddEditDreamEvent.ToggleDialogState(true))
                                    } else {
                                        onAddEditDreamEvent(AddEditDreamEvent.TriggerVibration)
                                        onNavigateToDreamJournalScreen()
                                    }
                                },
                                enabled = !addEditDreamState.isDreamExitOff && !addEditDreamState.dreamIsSavingLoading,
                            ) {
                                Icon(
                                    modifier = Modifier.rotate(180f),
                                    imageVector = Icons.AutoMirrored.Filled.ArrowRightAlt,
                                    contentDescription = "Back",
                                    tint = tint
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    onMainEvent(MainScreenEvent.SetDreamRecentlySaved(true))
                                    onAddEditDreamEvent(AddEditDreamEvent.TriggerVibration)
                                    onAddEditDreamEvent(AddEditDreamEvent.SaveDream(onSaveSuccess = {
                                        keyboardController?.hide()
                                        onMainEvent(MainScreenEvent.ShowSnackBar("Dream Saved Successfully :)") )
                                        onNavigateToDreamJournalScreen()
                                    }))
                                },
                                enabled = !addEditDreamState.dreamIsSavingLoading && !addEditDreamState.isDreamExitOff,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Save,
                                    contentDescription = stringResource(Res.string.save_dream),
                                    tint = tint
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = LightBlack.copy(alpha = 0.7f),
                            navigationIconContentColor = Color.Black,
                            titleContentColor = Color.Black,
                            actionIconContentColor = Color.Black
                        ),
                    )
                },
                snackbarHost = {
                    SnackbarHost(
                        addEditDreamState.snackBarHostState.value, modifier = Modifier
                            .consumeWindowInsets(
                                WindowInsets.navigationBars
                            )
                            .imePadding()
                            .padding(16.dp)
                    )
                },
                containerColor = Color.Transparent,
            ) { padding ->

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Color.Transparent)
                ) {
                    // Keep original TabLayout placement (no Box wrapper) so its internal Column sizing behaves correctly.
                    TabLayout(
                        dreamBackgroundImage = dreamBackgroundImage,
                        dreamTitleState = dreamTitleState,
                        dreamContentState = dreamContentState,
                        addEditDreamState = addEditDreamState,
                        onAddEditDreamEvent = onAddEditDreamEvent,
                        keyboardController = keyboardController,
                        animatedVisibilityScope = animateVisibilityScope,
                        onImageClick = onImageClick
                    )
                }
            }
        }
    }
}