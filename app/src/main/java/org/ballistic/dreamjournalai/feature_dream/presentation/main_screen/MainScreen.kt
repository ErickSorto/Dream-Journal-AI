package org.ballistic.dreamjournalai.feature_dream.presentation.main_screen


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.navigation.ScreenGraph
import org.ballistic.dreamjournalai.navigation.Screens
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.TransparentHintTextField
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.DreamsEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.viewmodel.DreamsViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.components.BottomNavigation
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.store_billing.presentation.store_screen.StoreEvent

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MainScreenView(
    mainScreenViewModelState: MainScreenViewModelState,
    dreamsViewModel: DreamsViewModel = hiltViewModel(),
    onMainEvent : (MainScreenEvent) -> Unit = {},
    onDreamsEvent : (DreamsEvent) -> Unit = {},
    onStoreEvent: (StoreEvent) -> Unit = {},
    onDataLoaded: () -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        delay(1500)
        onDataLoaded()
    }

    val navController = rememberNavController()

    val searchedText = dreamsViewModel.searchedText.collectAsStateWithLifecycle()


    Image(
        painter = rememberAsyncImagePainter(model = R.drawable.blue_lighthouse),
        modifier = Modifier.fillMaxSize(),
        contentDescription = "Lighthouse",
        contentScale = ContentScale.Crop
    )

    Scaffold(
        modifier = if (!mainScreenViewModelState.scaffoldState.bottomBarState) {
            Modifier
                .padding(bottom = 16.dp)
        } else {
            Modifier
                .navigationBarsPadding()
        },
        snackbarHost = {
            SnackbarHost(mainScreenViewModelState.scaffoldState.snackBarHostState.value)
        },
        topBar = {
            AnimatedVisibility(
                visible = mainScreenViewModelState.scaffoldState.topBarState,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut()
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        ) {
                            if (!mainScreenViewModelState.scaffoldState.isUserSearching) {
                                Text(
                                    text = "Dream Journal AI",
                                    color = Color.Black,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(start = 16.dp)
                                )
                            }
                            AnimatedVisibility(
                                visible = mainScreenViewModelState.scaffoldState.isUserSearching,
                                //slide from left to right
                                enter = slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ),
                                exit = slideOutHorizontally(
                                    targetOffsetX = { -it - 400 },
                                    animationSpec = tween(500)
                                ),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(start = 16.dp, end = 16.dp)

                            ) {
                                TransparentHintTextField(
                                    text = searchedText.value,
                                    hint = "Search dream...",
                                    onValueChange = {
                                        dreamsViewModel.onEvent(DreamsEvent.SearchDreams(it))
                                    },
                                    onFocusChange = {
                                    },
                                    isHintVisible = searchedText.value.isBlank(),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.4f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = Color.Black,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    },
                    actions = {
                        if (!mainScreenViewModelState.scaffoldState.isUserSearching){
                            IconButton(
                                onClick = {
                                    onMainEvent(MainScreenEvent.SetSearchingState(!mainScreenViewModelState.scaffoldState.isUserSearching))
                                },
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = Color.Black
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    onMainEvent(MainScreenEvent.SetSearchingState(!mainScreenViewModelState.scaffoldState.isUserSearching))
                                    dreamsViewModel.onEvent(DreamsEvent.SearchDreams(""))
                                },
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = Color.Black
                                )
                            }
                        }


                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        //animate content color
                        containerColor = Color.White.copy(alpha = 0.4f),
                        navigationIconContentColor = Color.Black,
                        titleContentColor = Color.Black,
                        actionIconContentColor = Color.Black
                    ),
                )
            }
        },

        bottomBar = {

            AnimatedVisibility(
                visible = mainScreenViewModelState.scaffoldState.bottomBarState,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            )
            {
                BottomNavigation(navController = navController)
                Box(
                    modifier = Modifier
                        .offset(y = 4.dp)
                        .fillMaxWidth()
                ) {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screens.AddEditDreamScreen.route)
                        },
                        containerColor = colorResource(id = R.color.Yellow),
                        elevation = FloatingActionButtonDefaults.elevation(3.dp, 4.dp),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)

                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add dream")
                    }
                }
            }
        },
        containerColor = Color.Transparent,

        ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current

        val newPadding = remember(innerPadding) {
            PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = 56.dp, // Set the bottom padding to 96.dp
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection)
            )
        }

        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
            ScreenGraph(
                navController = navController,
                mainScreenViewModelState = mainScreenViewModelState,
                dreamsViewModel = dreamsViewModel,
                innerPadding = newPadding,
                onMainEvent = { onMainEvent(it) },
                onDreamsEvent = { onDreamsEvent(it) },
                onStoreEvent = { onStoreEvent(it) },
            )
        }
    }
}