package org.ballistic.dreamjournalai.feature_dream.presentation.main_screen


import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color


import androidx.compose.ui.res.colorResource

import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.AuthViewModel

@Composable
fun BottomNavigation(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {

    val items = if (viewModel.hasSignedIn) {
        listOf(
            Screens.DreamListScreen,
            Screens.StoreScreen,
        )
    } else {
        listOf(
            Screens.DreamListScreen,
            Screens.SignInScreen,
        )
    }

    androidx.compose.material.BottomNavigation(
        backgroundColor = colorResource(id = R.color.RedPink),
        contentColor = Color.Black
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon((item.icon!!), contentDescription = item.title) },
                label = {
                    item.title?.let {
                        Text(text = it,
                            fontSize = 9.sp)
                    }
                },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Black.copy(0.4f),
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}