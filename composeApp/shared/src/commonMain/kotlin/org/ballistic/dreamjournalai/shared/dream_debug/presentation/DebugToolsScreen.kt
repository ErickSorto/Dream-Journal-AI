package org.ballistic.dreamjournalai.shared.dream_debug.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.dream_debug.presentation.viewmodel.DebugToolsEvent
import org.ballistic.dreamjournalai.shared.dream_debug.presentation.viewmodel.DebugToolsScreenState
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumEntrySource

@Composable
fun DebugToolsScreen(
    state: DebugToolsScreenState,
    bottomPaddingValue: Dp,
    onMainEvent: (MainScreenEvent) -> Unit,
    onEvent: (DebugToolsEvent) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToOnboardingLastPage: () -> Unit,
    onNavigateToPremiumFlow: (PremiumEntrySource) -> Unit,
) {
    LaunchedEffect(Unit) {
        onMainEvent(MainScreenEvent.SetTopBarState(false))
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(true))
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding(), bottom = bottomPaddingValue)
                .fillMaxSize()
                .dynamicBottomNavigationPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.BugReport,
                    contentDescription = null,
                    tint = Color(0xFFFFC269)
                )
                Text(
                    text = "Developer Tools",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            DebugCard(
                title = "Navigation",
                description = "Jump into important flows quickly while testing.",
            ) {
                DebugButton(
                    icon = Icons.Filled.PlayArrow,
                    label = "Start onboarding",
                    onClick = onNavigateToOnboarding
                )
                DebugButton(
                    icon = Icons.Filled.SkipNext,
                    label = "Skip to last onboarding page",
                    onClick = onNavigateToOnboardingLastPage
                )
                DebugButton(
                    icon = Icons.Filled.WorkspacePremium,
                    label = "Open premium flow",
                    onClick = { onNavigateToPremiumFlow(PremiumEntrySource.AutoLaunchOnce) }
                )
            }

            DebugCard(
                title = "Test notifications",
                description = "Send immediate local notifications using the production notification artwork.",
            ) {
                DebugButton(
                    icon = Icons.Filled.CardGiftcard,
                    label = "Dream token",
                    onClick = { onEvent(DebugToolsEvent.TestDreamTokenNotification) }
                )
                DebugButton(
                    icon = Icons.Filled.Notifications,
                    label = "Dream journal reminder",
                    onClick = { onEvent(DebugToolsEvent.TestDreamJournalNotification) }
                )
                DebugButton(
                    icon = Icons.Filled.Notifications,
                    label = "Reality checker",
                    onClick = { onEvent(DebugToolsEvent.TestRealityCheckNotification) }
                )
            }

            state.lastAction?.let { lastAction ->
                Text(
                    text = lastAction,
                    color = Color(0xFFD8CBFF),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(12.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DebugCard(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF211047),
                        Color(0xFF38135E),
                        Color(0xFF181033)
                    )
                )
            )
            .border(1.dp, Color(0xFFB66CFF).copy(alpha = 0.34f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = description,
            color = Color(0xFFD8CBFF),
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content()
        }
    }
}

@Composable
private fun DebugButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = Color.White.copy(alpha = 0.08f),
        )
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(
            text = label,
            modifier = Modifier.padding(start = 10.dp),
            fontWeight = FontWeight.SemiBold,
        )
    }
}
