package org.ballistic.dreamjournalai.store_billing.presentation.store_screen

import android.app.Activity
import android.telephony.SubscriptionInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.store_billing.presentation.store_screen.components.CustomButtonLayout


//import all compose-

@Composable
fun StoreScreen(
    mainScreenViewModelState: MainScreenViewModelState,
    onMainEvent : (MainScreenEvent) -> Unit = {},
    onStoreEvent: (StoreEvent) -> Unit = {}
) {
    val activity = LocalContext.current as Activity
    onMainEvent(MainScreenEvent.SetSearchingState(false))
    onMainEvent(MainScreenEvent.SetBottomBarState(true))
    onMainEvent(MainScreenEvent.SetFloatingActionButtonState(true))
    onMainEvent(MainScreenEvent.SetTopBarState(false))

//    mainScreenViewModel.setBottomBarState(true)
//    mainScreenViewModel.setFloatingActionButtonState(true)
//    mainScreenViewModel.setTopBarState(false)
    
    Box(modifier = Modifier.fillMaxSize()) {


        Column(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .padding(bottom = 68.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomButtonLayout(
                buy100IsClicked = {onStoreEvent(StoreEvent.Buy100DreamTokens(activity))},
                buy500IsClicked = {onStoreEvent(StoreEvent.Buy500DreamTokens(activity))},
            )
        }
    }
}