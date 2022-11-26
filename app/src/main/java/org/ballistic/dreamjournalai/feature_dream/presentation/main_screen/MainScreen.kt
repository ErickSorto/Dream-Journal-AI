package org.ballistic.dreamjournalai.feature_dream.presentation.main_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.navigationBarsWithImePadding
import org.ballistic.dreamjournalai.feature_dream.navigation.MainGraph
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens

@Composable
fun MainScreenView(){
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigation(navController = navController) },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        floatingActionButton = {

            FloatingActionButton(onClick = {
                navController.navigate(Screens.AddEditDreamScreen.route)
            },
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(3.dp, 4.dp),
                shape = CircleShape,
                modifier = Modifier.size(64.dp)

            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add dream", )
            }
        },
        modifier = Modifier.navigationBarsWithImePadding()
    ) {
        MainGraph(navController = navController, it)
    }
}