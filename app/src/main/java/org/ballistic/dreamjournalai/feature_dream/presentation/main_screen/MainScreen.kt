package org.ballistic.dreamjournalai.feature_dream.presentation.main_screen


import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.feature_dream.navigation.MainGraph
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.components.BottomNavigation
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.onboarding.presentation.viewmodel.SplashViewModel

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MainScreenView(
    mainScreenViewModel: MainScreenViewModel = hiltViewModel(),
    splashViewModel: SplashViewModel = hiltViewModel(),
    onDataLoaded: () -> Unit
) {

    LaunchedEffect(key1 = Unit) {
        delay(1500)
        onDataLoaded()
    }
    val screen by splashViewModel.state
    val navController = rememberNavController()
    Image(
        painter = rememberAsyncImagePainter(model = R.drawable.blue_lighthouse),
        modifier = Modifier.fillMaxSize(),
        contentDescription = "Lighthouse",
        contentScale = ContentScale.Crop
    )

    Scaffold(
        modifier = if (!mainScreenViewModel.getBottomBarState()) {
            Modifier
                .padding(bottom = 16.dp)
        } else {
            Modifier
                .navigationBarsPadding()
        },

        bottomBar = {

            AnimatedVisibility(
                visible = mainScreenViewModel.getBottomBarState(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            )
            {
                BottomNavigation(navController = navController)
                Box(modifier = Modifier.offset(y = 4.dp)
                    .fillMaxWidth()) {
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
        topBar = {
            AnimatedVisibility(visible = false) {

            }
        },
        containerColor = Color.Transparent,

        ) {
        it
        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
            MainGraph(
                navController = navController,
                startDestination = screen.startDestination,
                mainScreenViewModel = mainScreenViewModel,
            )
        }
    }
}