package org.ballistic.dreamjournalai.feature_dream.presentation.main_screen

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.estimateAnimationDurationMillis
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import org.ballistic.dreamjournalai.feature_dream.navigation.MainGraph
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.onboarding.data.DataStoreRepository
import org.ballistic.dreamjournalai.onboarding.presentation.viewmodel.SplashViewModel
import javax.inject.Inject

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun MainScreenView(
    mainScreenViewModel: MainScreenViewModel = hiltViewModel(),
    splashViewModel: SplashViewModel = hiltViewModel(),
    onDataLoaded : () -> Unit
) {

  //  lateinit var splashViewModel: SplashViewModel
//        installSplashScreen().setKeepOnScreenCondition {
//            !splashViewModel.isLoading.value
//        }

    LaunchedEffect(key1 = Unit){
        delay(1500)
        onDataLoaded()
    }
    val screen by splashViewModel.state
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
        topBar = {
                 AnimatedVisibility(visible = false) {

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
            MainGraph(
                navController = navController,
                startDestination = screen.startDestination,
                mainScreenViewModel = mainScreenViewModel,
            )
        }
    }
}
