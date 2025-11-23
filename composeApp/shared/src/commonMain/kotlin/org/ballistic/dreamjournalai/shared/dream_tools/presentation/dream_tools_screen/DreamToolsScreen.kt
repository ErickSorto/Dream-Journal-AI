package org.ballistic.dreamjournalai.shared.dream_tools.presentation.dream_tools_screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.ToolsEvent
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.DreamToolsGrid
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.DreamToolsScreenTopBar
import org.ballistic.dreamjournalai.shared.navigation.ToolRoute

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DreamToolsScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigate: (ToolRoute) -> Unit,
    onEvent: (ToolsEvent) -> Unit
) {
    Scaffold(
        topBar = {
            DreamToolsScreenTopBar(
                onEvent = onEvent
            )
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