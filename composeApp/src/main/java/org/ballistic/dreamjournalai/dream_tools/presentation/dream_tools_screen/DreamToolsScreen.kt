package org.ballistic.dreamjournalai.dream_tools.presentation.dream_tools_screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.ballistic.dreamjournalai.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.dream_tools.presentation.components.DreamToolsGrid
import org.ballistic.dreamjournalai.dream_tools.presentation.components.DreamToolsScreenTopBar
import org.ballistic.dreamjournalai.dream_main.presentation.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.navigation.DreamTools
import org.ballistic.dreamjournalai.navigation.Route
import org.ballistic.dreamjournalai.navigation.ToolRoute

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DreamToolsScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigate: (ToolRoute) -> Unit,
    mainScreenViewModelState: MainScreenViewModelState,
) {
    Scaffold(
        topBar = {
            DreamToolsScreenTopBar(mainScreenViewModelState = mainScreenViewModelState)
        },
        containerColor = Color.Transparent,
    ) {
        DreamToolsGrid(
            onNavigate = onNavigate,
            animatedVisibilityScope = animatedVisibilityScope,
            modifier = Modifier
                .padding(it)
                .dynamicBottomNavigationPadding()
                .fillMaxSize()
        )
    }
}