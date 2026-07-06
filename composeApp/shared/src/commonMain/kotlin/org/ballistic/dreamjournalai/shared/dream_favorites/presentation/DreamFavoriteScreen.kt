package org.ballistic.dreamjournalai.shared.dream_favorites.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dreamjournalai.composeapp.shared.generated.resources.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.components.ActionBottomSheet
import org.ballistic.dreamjournalai.shared.core.components.PremiumIllustratedEmptyState
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.core.util.formatCustomDate
import org.ballistic.dreamjournalai.shared.core.util.parseCustomDate
import org.ballistic.dreamjournalai.shared.dream_favorites.domain.FavoriteEvent
import org.ballistic.dreamjournalai.shared.dream_favorites.presentation.components.DreamFavoriteScreenTopBar
import org.ballistic.dreamjournalai.shared.dream_favorites.presentation.viewmodel.DreamFavoriteScreenState
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.components.DateHeader
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.components.DreamItem
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DreamFavoriteScreen(
    dreamFavoriteScreenState: DreamFavoriteScreenState,
    bottomPaddingValue: Dp,
    onMainEvent: (MainScreenEvent) -> Unit = {},
    onEvent: (FavoriteEvent) -> Unit,
    onNavigateToDream: (dreamID: String?, backgroundID: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isOpeningDream by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isOpeningDream = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Load dreams once
    LaunchedEffect(Unit) {
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(true))
        onMainEvent(MainScreenEvent.SetBottomBarEnabledState(true))
        onEvent(FavoriteEvent.LoadDreams)
    }

    // Bottom sheet if deleting a dream
    if (dreamFavoriteScreenState.bottomDeleteCancelSheetState) {
        ActionBottomSheet(
            title = stringResource(Res.string.delete_dream_title),
            message = stringResource(Res.string.delete_dream_message),
            buttonText = stringResource(Res.string.delete),
            onClick = {
                scope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = StringValue.Resource(Res.string.dream_deleted), // Updated to StringValue
                            action = SnackbarAction(StringValue.Resource(Res.string.restore_dream_action)) { onEvent(FavoriteEvent.RestoreDream) } // Updated to StringValue
                        )
                    )
                    onEvent(FavoriteEvent.DeleteDream(dreamFavoriteScreenState.dreamToDelete!!))
                    onEvent(FavoriteEvent.ToggleBottomDeleteCancelSheetState(false))
                }
            },
            onClickOutside = {
                onEvent(FavoriteEvent.ToggleBottomDeleteCancelSheetState(false))
            },
        )
    }

    Scaffold(
        topBar = {
            DreamFavoriteScreenTopBar(
                onEvent = onEvent
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        if (dreamFavoriteScreenState.dreamFavoriteList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = bottomPaddingValue,
                        start = 16.dp,
                        end = 16.dp
                    )
                    .dynamicBottomNavigationPadding(),
                contentAlignment = Alignment.Center
            ) {
                PremiumIllustratedEmptyState(
                    image = Res.drawable.favorites_empty_hero,
                    eyebrow = "Favorites",
                    title = "Your favorite dreams can live here.",
                    body = "Mark the dreams you want to revisit, and this space will turn into your personal collection of unforgettable nights.",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            return@Scaffold
        }

        // Otherwise, show the favorites list
        LazyColumn(
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = bottomPaddingValue
                )
                .dynamicBottomNavigationPadding()
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1) Parse and sort by LocalDate, descending, like DreamJournalListScreen
            val sortedGroupedDreams = dreamFavoriteScreenState.dreamFavoriteList
                .mapNotNull { dream ->
                    // parseCustomDate(...) might throw an exception if invalid
                    // or return null if you wrap it in a try/catch
                    try {
                        val parsedDate = parseCustomDate(dream.date)
                        // Return a pair (LocalDate, Dream)
                        parsedDate to dream
                    } catch (_: IllegalArgumentException) {
                        null
                    }
                }
                // Sort descending by date, then descending by dream.timestamp
                .sortedWith(
                    compareByDescending<Pair<LocalDate, Dream>> { it.first }
                        .thenByDescending { it.second.timestamp }
                )
                // Group by the parsed LocalDate
                .groupBy { it.first }

            // 2) For each date group, display a sticky header + items
            sortedGroupedDreams.forEach { (date, dreams) ->
                stickyHeader {
                    DateHeader(dateString = formatCustomDate(date), paddingStart = 20)
                }

                items(dreams) { (_, dream) ->
                    DreamItem(
                        dream = dream,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .padding(horizontal = 20.dp),
                        onClick = {
                            if (!isOpeningDream) {
                                isOpeningDream = true
                                onEvent(FavoriteEvent.TriggerVibration)
                                onNavigateToDream(dream.id, dream.backgroundImage)
                            }
                        },
                        onDeleteClick = {
                            onEvent(FavoriteEvent.TriggerVibration)
                            onEvent(FavoriteEvent.DreamToDelete(dream))
                            onEvent(FavoriteEvent.ToggleBottomDeleteCancelSheetState(true))
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
