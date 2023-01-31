package org.ballistic.dreamjournalai.feature_dream.presentation.store_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.AuthViewModel

//import all compose-

@Composable
fun StoreScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navController: NavHostController,
    mainScreenViewModel: MainScreenViewModel
) {
    mainScreenViewModel.setBottomBarState(true)
    mainScreenViewModel.setFloatingActionButtonState(true)


    Column(
        modifier = Modifier.padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Store Screen",
        )
    }

}


