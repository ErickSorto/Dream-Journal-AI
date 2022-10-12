package org.ballistic.dreamjournalai.feature_dream.presentation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.dreams.DreamsScreen
import org.ballistic.dreamjournalai.feature_dream.presentation.util.Screen
import org.ballistic.dreamjournalai.ui.theme.DreamCatcherAITheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DreamCatcherAITheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.DreamsScreen.route
                    ) {
                        composable(route = Screen.DreamsScreen.route) {
                            DreamsScreen(navController = navController)
                        }
                        composable(
                            route = Screen.AddEditDreamScreen.route +
                                "?dreamId={dreamId}&dreamColor={dreamColor}",
                            arguments = listOf(
                                navArgument(
                                    name = "dreamId") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                },
                                navArgument(
                                    name = "dreamColor") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                },
                            )

                        ) {
                            val color = it.arguments?.getInt("dreamColor") ?: -1
                            AddEditDreamScreen(
                                navController = navController,
                                dreamColor = color)
                        }
                    }
                }
            }
        }
    }
}

