package org.ballistic.dreamjournalai.feature_dream.presentation.store_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.store_screen.components.CustomButtonLayout
import org.ballistic.dreamjournalai.feature_dream.presentation.store_screen.components.SubscriptionInfo

//import all compose-

@Composable
fun StoreScreen(
    mainScreenViewModel: MainScreenViewModel
) {
    mainScreenViewModel.setBottomBarState(true)
    mainScreenViewModel.setFloatingActionButtonState(true)
    mainScreenViewModel.setTopBarState(false)
    
    Box(modifier = Modifier.fillMaxSize()) {
        SubscriptionInfo(modifier = Modifier.padding(16.dp))

        Column(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .padding(bottom = 68.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomButtonLayout()
        }
    }
}