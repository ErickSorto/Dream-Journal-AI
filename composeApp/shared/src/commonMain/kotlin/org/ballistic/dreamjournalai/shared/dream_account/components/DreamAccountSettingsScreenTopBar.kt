package org.ballistic.dreamjournalai.shared.dream_account.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.account_settings
import dreamjournalai.composeapp.shared.generated.resources.menu
import dreamjournalai.composeapp.shared.generated.resources.premium_label
import org.ballistic.dreamjournalai.shared.DrawerCommand
import org.ballistic.dreamjournalai.shared.DrawerController
import org.ballistic.dreamjournalai.shared.dream_main.presentation.components.NotificationPermissionMenuIcon
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamAccountSettingsScreenTopBar(
    isPremiumMember: Boolean = false,
) {
    val scope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = stringResource(Res.string.account_settings),
                        color = OriginalXmlColors.White
                    )
                    if (isPremiumMember) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFFFFF0A8),
                                            Color(0xFFFFB545),
                                            Color(0xFF7A3F00)
                                        )
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = stringResource(Res.string.premium_label),
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    } else {
                        Box(modifier = Modifier.width(0.dp))
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    Logger.d("TopBar") { "AccountSettings: Menu icon clicked -> request open drawer" }
                    DrawerController.send(DrawerCommand.Open)
                }
            }) {
                NotificationPermissionMenuIcon(
                    contentDescription = stringResource(Res.string.menu),
                    tint = OriginalXmlColors.White
                )
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = stringResource(Res.string.menu),
                    tint = Color.Transparent
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = OriginalXmlColors.DarkBlue.copy(alpha = 0.5f),
            navigationIconContentColor = Color.Black,
            titleContentColor = Color.Black,
            actionIconContentColor = Color.Black
        ),
    )
}
