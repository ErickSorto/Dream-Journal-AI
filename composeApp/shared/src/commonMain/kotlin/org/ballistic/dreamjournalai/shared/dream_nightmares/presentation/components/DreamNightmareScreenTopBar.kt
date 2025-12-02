package org.ballistic.dreamjournalai.shared.dream_nightmares.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.menu
import dreamjournalai.composeapp.shared.generated.resources.nightmares_title
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.DrawerCommand
import org.ballistic.dreamjournalai.shared.DrawerController
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.dream_nightmares.domain.NightmareEvent
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamNightmareScreenTopBar(
    onEvent: (NightmareEvent) -> Unit = {}
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
                Text(
                    text = stringResource(Res.string.nightmares_title),
                    color = OriginalXmlColors.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                onEvent(NightmareEvent.TriggerVibration)
                scope.launch {
                    Logger.d("TopBar") { "Nightmare: Menu icon clicked -> request open drawer" }
                    DrawerController.send(DrawerCommand.Open)
                }
            }) {
                Icon(
                    Icons.Filled.Menu,
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
        modifier = Modifier.dynamicBottomNavigationPadding()
    )
}
