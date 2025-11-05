package org.ballistic.dreamjournalai.shared.dream_main.presentation


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import co.touchlab.kermit.Logger
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.blue_lighthouse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.ObserveAsEvents
import org.ballistic.dreamjournalai.shared.DrawerController
import org.ballistic.dreamjournalai.shared.DrawerCommand
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.core.platform.getPlatformName
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_main.presentation.components.BottomNavigation
import org.ballistic.dreamjournalai.shared.dream_main.presentation.components.DrawerGroupHeading
import org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.shared.navigation.DrawerNavigation
import org.ballistic.dreamjournalai.shared.navigation.Route
import org.ballistic.dreamjournalai.shared.navigation.ScreenGraph
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainScreenView(
    mainScreenViewModelState: MainScreenViewModelState,
    onMainEvent: (MainScreenEvent) -> Unit = {},
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
                } catch (deniedAlways: DeniedAlwaysException) {
                    // The user has denied the permission *always* (Don’t ask again).
                    // Handle your fallback scenario here — e.g., show a dialog explaining
                    // that notifications won't work, or navigate the user somewhere else.
                } catch (denied: DeniedException) {
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
    }

    val navController = rememberNavController()
    val drawerGroups = listOf(
        DrawerGroup(
            title = "Pages",
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
            title = "Settings",
            items = listOf(
                DrawerNavigation.AccountSettings,
                //   DrawerNavigation.NotificationSettings,
                //    Screens.DreamSettings,
            )
        ),
        DrawerGroup(
            title = "Others",
            items = listOf(
                DrawerNavigation.RateMyApp,
                //  Screens.AboutMe
            )
        )
    )
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
        painter = painterResource(Res.drawable.blue_lighthouse),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .blur(15.dp),
        contentDescription = "Background Image"
    )

    // Local drawer state owned by the composable for Compose stability. The ViewModel exposes
    // an intent flag `isDrawerOpen`; we react to it below.
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Track when we're driving the drawer programmatically to avoid echo-loops
    var programmaticChangeInProgress = remember { mutableStateOf(false) }

    // Drive the drawer from a simple controller to avoid VM<->UI races
    ObserveAsEvents(DrawerController.events, key1 = drawerState) { cmd ->
        Logger.d("MainScreen") { "DrawerCommand: $cmd | current=${drawerState.currentValue} anim=${drawerState.isAnimationRunning}" }
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = mainScreenViewModelState.isDrawerEnabled,
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
                                    label = { Text(item.title ?: "") },
                                    selected = item == selectedItem.value,
                                    onClick = {
                                         onMainEvent(MainScreenEvent.TriggerVibration)
                                         coroutineScope.launch {
                                             drawerState.close()
                                         }
                                     selectedItem.value = item

                                        if (item == DrawerNavigation.RateMyApp) {
                                             onMainEvent(MainScreenEvent.OpenStoreLink)
                                         } else {
                                             navController.navigate(item.route) {
                                                 popUpTo(DrawerNavigation.DreamJournalScreen.route) {
                                                     saveState = true
                                                 }
                                                 launchSingleTop = true
                                                 restoreState = true
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
                        text = "Version: 1.2.8",
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
            // Local snackbar host state — main view model no longer holds snackbar UI state.
            val snackbarHostState = remember { SnackbarHostState() }

            // Observe events from the centralized controller and show them on the local host.
            ObserveAsEvents(SnackbarController.events) { event ->
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.action?.name,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        event.action?.action?.invoke()
                    }
                }
            }

            Scaffold(
                snackbarHost = {
                    SnackbarHost(snackbarHostState)
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
                                        popUpTo(org.ballistic.dreamjournalai.shared.navigation.Route.DreamJournalScreen) {
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
                                        contentDescription = "Add dream",
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
                        navController = navController,
                        mainScreenViewModelState = mainScreenViewModelState,
                        bottomPaddingValue = mainScreenViewModelState.paddingValues.calculateBottomPadding(),
                        onMainEvent = { onMainEvent(it) },
                        onNavigateToOnboardingScreen = { onNavigateToOnboardingScreen() }
                    )

            }
        }
    )
}

data class DrawerGroup(
    val title: String,
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
            contentDescription = "Animated Heart",
            tint = Color.Red,
            modifier = Modifier.size(size.dp)
        )
    } else {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Heart",
            tint = Color.Red,
            modifier = Modifier.size(24.dp)
        )
    }
}
