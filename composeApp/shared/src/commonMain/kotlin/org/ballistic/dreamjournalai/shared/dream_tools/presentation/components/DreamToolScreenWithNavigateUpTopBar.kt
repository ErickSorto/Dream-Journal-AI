package org.ballistic.dreamjournalai.shared.dream_tools.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.platform.isIos
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.DarkBlue
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamToolScreenWithNavigateUpTopBar(
    title: String? = null,
    titleComposable: (@Composable () -> Unit)? = null,
    onEvent: () -> Unit,
    enabledBack: Boolean = true,
    navigateUp: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    // Read the system status bar inset
    val rawStatusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Desired rule: top bar height = statusBarInset + 24.dp, bounded to reasonable min/max
    val minTopBar = if (isIos) 56.dp else 56.dp
    val maxTopBar = 80.dp
    val initialTopBar = (rawStatusBarTop + 24.dp).coerceIn(minTopBar, maxTopBar)
    var topBarHeight by remember { mutableStateOf(initialTopBar) }
    var layoutReady by remember { mutableStateOf(false) }

    // Observe inset briefly and set the topBarHeight before showing the bar to avoid flicker
    LaunchedEffect(Unit) {
        val observed = withTimeoutOrNull(500L) {
            snapshotFlow { rawStatusBarTop }
                .filter { it >= 0.dp }
                .first()
        } ?: rawStatusBarTop

        val recomputed = (observed + 48.dp).coerceIn(minTopBar, maxTopBar)
        topBarHeight = recomputed
        layoutReady = true
        Logger.d { "[DJAI/ToolBar] observedInset=$observed topBarHeight=$recomputed" }
    }

    val topBarAlpha by animateFloatAsState(targetValue = if (layoutReady) 1f else 0f, animationSpec = tween(180))

    // Draw a small background behind the status area and the TopAppBar; fade them in once ready.
    Box(modifier = Modifier
        .fillMaxWidth()
        .graphicsLayer { alpha = topBarAlpha }
    ) {
        // Build a custom top bar whose total height = rawStatusBarTop + 24.dp.
        val contentHeight = 48.dp
        val totalHeight = rawStatusBarTop + contentHeight

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(DarkBlue.copy(alpha = 0.5f))
            .dynamicBottomNavigationPadding()
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = rawStatusBarTop)
                    .height(contentHeight)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            onEvent()
                            navigateUp()
                        }
                    },
                    enabled = enabledBack,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                }

                // Title centered: use weight to take remaining space
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (titleComposable != null) {
                        titleComposable()
                    } else if (title != null) {
                        Text(text = title, color = White, style = typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Transparent)
                }
            }
        }
    }
}
