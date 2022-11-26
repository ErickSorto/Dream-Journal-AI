package org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.insets.navigationBarsWithImePadding
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components.SignInButton

@Composable
fun Signup_Screen(
    navController: NavHostController,
    paddingValues: PaddingValues //this could possibly be a modifier instead
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsWithImePadding()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            SignInButton(
                onClick = {
                    navController.navigate(Screens.DreamListScreen.route)
                }
            )
        }

    }

}