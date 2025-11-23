package org.ballistic.dreamjournalai.shared.dream_journal_list.presentation

// Animation
// Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.new_dream_prompt
import kotlinx.datetime.LocalDate
import org.ballistic.dreamjournalai.shared.core.components.ActionBottomSheet
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.core.util.formatCustomDate
import org.ballistic.dreamjournalai.shared.core.util.parseCustomDate
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.DreamListEvent
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.components.DateHeader
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.components.DreamItem
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.components.DreamListScreenTopBar
import org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.viewmodel.DreamJournalListState
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.shared.utils.singleClick
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DreamJournalListScreen(
    mainScreenViewModelState: MainScreenViewModelState,
    searchTextFieldState: TextFieldState,
    dreamJournalListState: DreamJournalListState,
    bottomPaddingValue: Dp,
    onMainEvent: (MainScreenEvent) -> Unit = {},
    onDreamListEvent: (DreamListEvent) -> Unit = {},
    onNavigateToDream: (dreamID: String?, backgroundID: Int) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onDreamListEvent(DreamListEvent.FetchDreams)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(true))
        onMainEvent(MainScreenEvent.SetFloatingActionButtonState(true))
        onMainEvent(MainScreenEvent.SetDrawerState(true))
    }


    // compute an empty-prompt boolean from the available state fields
    val showEmptyPrompt = remember(dreamJournalListState.dreams, dreamJournalListState.isSearching, dreamJournalListState.isLoading) {
        dreamJournalListState.dreams.isEmpty() && !dreamJournalListState.isSearching && !dreamJournalListState.isLoading
    }

    // Delete confirmation bottom sheet (outside Scaffold, as before)
    if (dreamJournalListState.bottomDeleteCancelSheetState) {
        ActionBottomSheet(
            modifier = Modifier.padding(),
            title = "Delete this Dream?",
            message = "Are you sure you want to delete this dream?",
            buttonText = "Delete",
            onClick = {
                onDreamListEvent(DreamListEvent.ToggleBottomDeleteCancelSheetState(false))
                onDreamListEvent(
                    DreamListEvent.DeleteDream(dream = dreamJournalListState.chosenDreamToDelete!!)
                )
                // Undo is handled in the viewmodel via events now
            },
            onClickOutside = {
                onDreamListEvent(DreamListEvent.ToggleBottomDeleteCancelSheetState(false))
            }
        )
    }

    // Wrap content in a Scaffold to show the top app bar
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            DreamListScreenTopBar(
                dreamJournalListState = dreamJournalListState,
                searchTextFieldState = searchTextFieldState,
                onDreamListEvent = onDreamListEvent,
            )
        }
    ) { innerPadding ->
        val topPadding = innerPadding.calculateTopPadding()
        val lastClickTime = remember { mutableLongStateOf(0L) }
        // Wrap list in a Box so we can overlay the empty-state prompt above bottom nav
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .dynamicBottomNavigationPadding()
                    .padding(top = topPadding, bottom = bottomPaddingValue),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {

                // Step 1: Parse and Sort Dreams
                val sortedGroupedDreams = dreamJournalListState.dreams
                    .mapNotNull { dream ->
                        try {
                            val parsedDate = parseCustomDate(dream.date)
                            Pair(parsedDate, dream)
                        } catch (_: IllegalArgumentException) {
                            null
                        }
                    }
                    .sortedWith(
                        compareByDescending<Pair<LocalDate, Dream>> { it.first }
                            .thenByDescending { it.second.timestamp }
                    )
                    .groupBy { it.first }

                // Step 2: Iterate Through Groups
                sortedGroupedDreams.forEach { (date, dreams) ->

                    // Sticky Header for the Date
                    stickyHeader {
                        DateHeader(dateString = formatCustomDate(date), paddingStart = 20)
                    }

                    // Items for Each Dream in the Date Group
                    items(dreams) { (/*date*/ _, dream): Pair<LocalDate, Dream> ->
                         DreamItem(
                             dream = dream,
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(bottom = 10.dp)
                                 .padding(horizontal = 20.dp)
                             ,
                             onClick = singleClick(lastClickTime) {
                                 onDreamListEvent(DreamListEvent.TriggerVibration)
                                 onNavigateToDream(dream.id, dream.backgroundImage)
                             },
                             onDeleteClick = {
                                 onDreamListEvent(DreamListEvent.TriggerVibration)
                                 onDreamListEvent(
                                     DreamListEvent.DreamToDelete(dream)
                                 )
                                 onDreamListEvent(
                                     DreamListEvent.ToggleBottomDeleteCancelSheetState(true)
                                 )
                             }
                         )
                         Spacer(modifier = Modifier.height(4.dp))
                     }
                 }
             }

             // Empty state prompt: floaty hint above FAB/bottom nav (single instance)
            AnimatedVisibility(
                visible = showEmptyPrompt,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = 40f
                    )
                ) + fadeIn(),
                exit = ExitTransition.None,
                modifier = Modifier.fillMaxSize()
            ) {
                 // Use a Box scope so .align works correctly inside AnimatedVisibility
                 Box(modifier = Modifier.fillMaxSize()) {
                     val infiniteTransition = rememberInfiniteTransition(label = "float_prompt")
                     val floatOffset by infiniteTransition.animateFloat(
                         initialValue = 0f,
                         targetValue = -8f,
                         animationSpec = infiniteRepeatable(
                             animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
                             repeatMode = RepeatMode.Reverse
                         ),
                         label = "float_offset"
                     )

                     Image(
                         painter = painterResource(Res.drawable.new_dream_prompt),
                         contentDescription = null,
                         modifier = Modifier
                             .align(Alignment.BottomCenter)
                             .zIndex(1f)
                             .fillMaxWidth(0.65f)
                             .padding(bottom = bottomPaddingValue)
                             .padding(bottom = 8.dp)
                             .offset(y = floatOffset.dp)
                     )
                 }
             }
         }
     }
}
