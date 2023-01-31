package org.ballistic.dreamjournalai.feature_dream.presentation.main_screen

import androidx.compose.animation.*
import androidx.compose.animation.core.estimateAnimationDurationMillis
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ballistic.dreamjournalai.feature_dream.navigation.MainGraph
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.onboarding.presentation.viewmodel.SplashViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun MainScreenView(
    splashViewModel: SplashViewModel,
    mainScreenViewModel: MainScreenViewModel = hiltViewModel()
) {
    val screen by splashViewModel.startDestination
    val navController = rememberNavController()


    Scaffold(
        bottomBar = {
            if (mainScreenViewModel.getBottomBarState()) {
                AnimatedVisibility(
                    visible = true,
                )
                {
                    BottomNavigation(navController = navController)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        floatingActionButton = {
            if (mainScreenViewModel.getFloatingActionButtonState()) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screens.AddEditDreamScreen.route)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(3.dp, 4.dp),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)

                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add dream")
                }
            }
        },
        modifier = Modifier.navigationBarsWithImePadding()
    ) {
        it
        AnimatedVisibility(visible = true) {
            if (screen == Screens.Welcome.route) {
                MainGraph(
                    navController = navController,
                    startDestination = screen,
                    mainScreenViewModel = mainScreenViewModel
                )
            } else {
                MainGraph(
                    navController = navController,
                    startDestination = screen,
                    mainScreenViewModel = mainScreenViewModel
                )
            }
        }


    }
}
