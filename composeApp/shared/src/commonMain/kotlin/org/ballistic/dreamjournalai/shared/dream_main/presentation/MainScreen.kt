package org.ballistic.dreamjournalai.shared.dream_main.presentation


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.add_dream
import dreamjournalai.composeapp.shared.generated.resources.animated_heart
import dreamjournalai.composeapp.shared.generated.resources.background_image
import dreamjournalai.composeapp.shared.generated.resources.blue_lighthouse
import dreamjournalai.composeapp.shared.generated.resources.date_prefix
import dreamjournalai.composeapp.shared.generated.resources.dismiss
import dreamjournalai.composeapp.shared.generated.resources.dream_separator
import dreamjournalai.composeapp.shared.generated.resources.export_dreams_pdf_filename
import dreamjournalai.composeapp.shared.generated.resources.export_dreams_txt_filename
import dreamjournalai.composeapp.shared.generated.resources.export_failed
import dreamjournalai.composeapp.shared.generated.resources.export_successful
import dreamjournalai.composeapp.shared.generated.resources.heart
import dreamjournalai.composeapp.shared.generated.resources.others
import dreamjournalai.composeapp.shared.generated.resources.pages
import dreamjournalai.composeapp.shared.generated.resources.settings
import dreamjournalai.composeapp.shared.generated.resources.title_prefix
import dreamjournalai.composeapp.shared.generated.resources.transcript_prefix
import dreamjournalai.composeapp.shared.generated.resources.version
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.DrawerCommand
import org.ballistic.dreamjournalai.shared.DrawerController
import org.ballistic.dreamjournalai.shared.ObserveAsEvents
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.components.ExportDreamsBottomSheet
import org.ballistic.dreamjournalai.shared.core.platform.getPlatformName
import org.ballistic.dreamjournalai.shared.core.platform.rememberDreamExporter
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.util.OrderType
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_main.presentation.components.BottomNavigation
import org.ballistic.dreamjournalai.shared.dream_main.presentation.components.DrawerGroupHeading
import org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.shared.navigation.DrawerNavigation
import org.ballistic.dreamjournalai.shared.navigation.Route
import org.ballistic.dreamjournalai.shared.navigation.ScreenGraph
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainScreenView(
    mainScreenViewModelState: MainScreenViewModelState,
    onMainEvent: (MainScreenEvent) -> Unit,
    onNavigateToOnboardingScreen: () -> Unit = {},
    onDataLoaded: () -> Unit
) {
    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    BindEffect(controller)
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // Check if permission is already granted
            val alreadyGranted = controller.isPermissionGranted(Permission.REMOTE_NOTIFICATION)
            if (!alreadyGranted) {
                try {
                    // Request the permission
                    controller.providePermission(Permission.REMOTE_NOTIFICATION)
                } catch (_: DeniedAlwaysException) {
                    // The user has denied the permission *always* (Don’t ask again).
                    // Handle your fallback scenario here — e.g., show a dialog explaining
                    // that notifications won't work, or navigate the user somewhere else.
                } catch (_: DeniedException) {
                    // The user has denied the permission (but not “don’t ask again”).
                    // You could decide to ask again or show rationale to the user.
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        onDataLoaded()
    }

    LaunchedEffect(Unit) {
        onMainEvent(MainScreenEvent.GetAuthState)
        onMainEvent(MainScreenEvent.UserInteracted)
        DrawerController.enable()
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
                    DrawerNavigation.Favorites,
                    DrawerNavigation.Nightmares,
                    DrawerNavigation.DreamToolGraphScreen,
                    DrawerNavigation.Statistics,
                    DrawerNavigation.Symbol,
                )
            ),
            DrawerGroup(
                title = Res.string.settings,
                items = listOf(
                    DrawerNavigation.AccountSettings,
                    //   DrawerNavigation.NotificationSettings,
                    //    Screens.DreamSettings,
                )
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

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route ?: return@OnDestinationChangedListener
            val matchedScreen = drawerGroups.flatMap { it.items }.firstOrNull {
                it.route::class.qualifiedName == route
            }
            if (matchedScreen != null) {
                selectedItem.value = matchedScreen
            }
        }

        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    Image(
        painter = painterResource(mainScreenViewModelState.backgroundResource),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .blur(15.dp),
        contentDescription = stringResource(Res.string.background_image)
    )

    // Local drawer state owned by the composable for Compose stability. The ViewModel exposes
    // an intent flag `isDrawerOpen`; we react to it below.
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Track when we're driving the drawer programmatically to avoid echo-loops
    val programmaticChangeInProgress = remember { mutableStateOf(false) }

    // Drive the drawer from a simple controller to avoid VM<->UI races
    ObserveAsEvents(DrawerController.events, key1 = drawerState) { cmd ->
        Logger.d("MainScreen") { "DrawerCommand: $cmd | current=${drawerState.currentValue} anim=${drawerState.isAnimationRunning} | programmatic=${programmaticChangeInProgress.value}" }
        programmaticChangeInProgress.value = true
        coroutineScope.launch {
            try {
                when (cmd) {
                    is DrawerCommand.Open -> if (drawerState.currentValue != DrawerValue.Open) drawerState.open()
                    is DrawerCommand.Close -> if (drawerState.currentValue != DrawerValue.Closed) drawerState.close()
                    is DrawerCommand.Toggle -> if (drawerState.currentValue == DrawerValue.Open) drawerState.close() else drawerState.open()
                }
            } finally {
                programmaticChangeInProgress.value = false
            }
        }
    }
    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.currentValue }
            .collectLatest { current ->
                Logger.d("MainScreen") { "Drawer currentValue=$current | anim=${drawerState.isAnimationRunning} | programmatic=${programmaticChangeInProgress.value}" }
            }
    }
    val isDrawerEnabled by DrawerController.isEnabled.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarHandler(snackbarHostState = snackbarHostState)


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isDrawerEnabled,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxHeight()) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(Modifier.height(12.dp))

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
                                    selected = item == selectedItem.value,
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
                                                navController.navigate(item.route) {
                                                    popUpTo(Route.DreamJournalScreen) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
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
                        val bottomPadding = if (getPlatformName() == "iOS") 0.dp else 16.dp
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
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            BottomNavigation(
                                currentRoute = currentRoute,
                                isNavigationEnabled = mainScreenViewModelState.isBottomBarEnabledState,
                                onMainEvent = onMainEvent,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Route.DreamJournalScreen) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )

                            // FAB aligned to the top-center of the Box
                            FloatingActionButton(
                                onClick = {
                                    if (mainScreenViewModelState.isBottomBarEnabledState) {
                                        onMainEvent(MainScreenEvent.TriggerVibration)
                                        // Temporarily disable bottom navigation to avoid rapid taps navigating elsewhere
                                        onMainEvent(MainScreenEvent.SetBottomBarEnabledState(false))
                                        navController.navigate(
                                            Route.AddEditDreamScreen(
                                                dreamID = "",
                                                backgroundID = -1
                                            )
                                        ) {
                                            popUpTo(Route.DreamJournalScreen) {
                                                saveState = false
                                                inclusive = true
                                            }
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
                onMainEvent(MainScreenEvent.UpdatePaddingValues(innerPadding))


                ScreenGraph(
                    navControllerProvider = { navController },
                    mainScreenViewModelState = mainScreenViewModelState,
                    bottomPaddingValue = mainScreenViewModelState.paddingValues.calculateBottomPadding(),
                    onMainEvent = { onMainEvent(it) },
                    onNavigateToOnboardingScreen = { onNavigateToOnboardingScreen() }
                )

            }
        }
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
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                currentEvent.action?.action?.invoke()
            }
            // Reset the state to be ready for the next event
            eventToShow = null
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
