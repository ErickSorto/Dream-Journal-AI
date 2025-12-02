package org.ballistic.dreamjournalai.shared.dream_symbols.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.close
import dreamjournalai.composeapp.shared.generated.resources.menu
import dreamjournalai.composeapp.shared.generated.resources.search
import dreamjournalai.composeapp.shared.generated.resources.search_symbol
import dreamjournalai.composeapp.shared.generated.resources.symbols_title
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.DrawerCommand
import org.ballistic.dreamjournalai.shared.DrawerController
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.TransparentHintTextField
import org.ballistic.dreamjournalai.shared.dream_symbols.domain.SymbolEvent
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.viewmodel.SymbolScreenState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymbolScreenTopBar(
    symbolScreenState: SymbolScreenState,
    searchedTextFieldState: TextFieldState,
    onDictionaryEvent: (SymbolEvent) -> Unit = {}
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
                if (!symbolScreenState.isSearching) {
                    Text(
                        text = stringResource(Res.string.symbols_title),
                        color = OriginalXmlColors.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
                AnimatedVisibility(
                    visible = symbolScreenState.isSearching,
                    //slide from left to right
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { -it - 400 },
                        animationSpec = tween(500)
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)

                ) {
                    TransparentHintTextField(
                        textFieldState = searchedTextFieldState,
                        hint = stringResource(Res.string.search_symbol),
                        isHintVisible = searchedTextFieldState.text.toString().isEmpty(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(OriginalXmlColors.White),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                color = OriginalXmlColors.White.copy(
                                    alpha = 0.2f
                                )
                            )
                            .padding(4.dp, 2.dp, 0.dp, 2.dp)
                            .fillMaxWidth()
                            .padding(4.dp)
                            .focusable(),
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                onDictionaryEvent(SymbolEvent.TriggerVibration)
                scope.launch {
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
            if (!symbolScreenState.isSearching) {
                IconButton(
                    onClick = {
                        onDictionaryEvent(SymbolEvent.TriggerVibration)
                        onDictionaryEvent(SymbolEvent.ListenForSearchChange)
                        onDictionaryEvent(SymbolEvent.SetSearchingState(true))
                    },
                    content = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = stringResource(Res.string.search),
                            tint = OriginalXmlColors.White
                        )
                    }
                )
            } else {
                IconButton(
                    onClick = {
                        onDictionaryEvent(SymbolEvent.TriggerVibration)
                        onDictionaryEvent(SymbolEvent.SetSearchingState(false))
                    },
                    content = {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(Res.string.close),
                            tint = OriginalXmlColors.White
                        )
                    }
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
