package org.ballistic.dreamjournalai.shared.dream_journal_list.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dreamjournalai.composeapp.shared.generated.resources.*
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DreamJournalListScreen(
    searchTextFieldState: TextFieldState,
    dreamJournalListState: DreamJournalListState,
    bottomPaddingValue: Dp,
    isBackgroundIntroComplete: Boolean,
    isBackgroundBlurComplete: Boolean,
    requestReviewAfterSavedDream: Boolean = false,
    onReviewAfterSavedDreamConsumed: () -> Unit = {},
    requestInAppReview: () -> Unit = {},
    onMainEvent: (MainScreenEvent) -> Unit = {},
    onDreamListEvent: (DreamListEvent) -> Unit = {},
    onNavigateToDream: (dreamID: String?, backgroundID: Int) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isOpeningDream by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isOpeningDream = false
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

    LaunchedEffect(requestReviewAfterSavedDream, dreamJournalListState.hasLoadedDreams, dreamJournalListState.dreams.size) {
        if (requestReviewAfterSavedDream && dreamJournalListState.hasLoadedDreams && dreamJournalListState.dreams.size >= 2) {
            onDreamListEvent(DreamListEvent.TriggerReview(requestInAppReview))
            onReviewAfterSavedDreamConsumed()
        }
    }

    // Delete confirmation bottom sheet (outside Scaffold, as before)
    if (dreamJournalListState.bottomDeleteCancelSheetState) {
        ActionBottomSheet(
            modifier = Modifier.padding(),
            title = stringResource(Res.string.delete_this_dream),
            message = stringResource(Res.string.are_you_sure_delete_dream),
            buttonText = stringResource(Res.string.delete),
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

        // Wrap list in a Box so we can overlay the empty-state prompt above bottom nav
        Box(modifier = Modifier.fillMaxSize()) {
            val dreamListReady = dreamJournalListState.hasLoadedDreams

            AnimatedVisibility(
                visible =
                    isBackgroundBlurComplete &&
                        !dreamJournalListState.hasAnyDreams &&
                        dreamJournalListState.hasLoadedDreams,
                enter = fadeIn(animationSpec = tween(1150)) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(durationMillis = 1100)
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    EmptyDreamPromptBubble(
                        text = stringResource(Res.string.empty_dream_prompt_bubble),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = maxHeight * 0.53f)
                            .padding(horizontal = 28.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = dreamListReady,
                enter = fadeIn(animationSpec = tween(220)),
                modifier = Modifier.fillMaxSize()
            ) {
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
                                 onClick = {
                                     if (!isOpeningDream) {
                                         isOpeningDream = true
                                         onDreamListEvent(DreamListEvent.TriggerVibration)
                                         onNavigateToDream(dream.id, dream.backgroundImage)
                                     }
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
            }
         }
    }
}

@Composable
private fun EmptyDreamPromptBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    val bubbleShape = remember {
        SpeechBubbleShape(
            cornerRadius = 28.dp,
            tailWidth = 34.dp,
            tailHeight = 18.dp
        )
    }
    val bubbleBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF4EFFF).copy(alpha = 0.9f),
            Color(0xFFD7CBFF).copy(alpha = 0.78f)
        )
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 292.dp)
                .clip(bubbleShape)
                .background(bubbleBrush)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = bubbleShape
                )
                .padding(start = 22.dp, top = 15.dp, end = 22.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color(0xFF34245E),
                fontSize = 19.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class SpeechBubbleShape(
    val cornerRadius: Dp,
    val tailWidth: Dp,
    val tailHeight: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val tailHeightPx = with(density) { tailHeight.toPx() }
            .coerceIn(0f, size.height * 0.35f)
        val tailWidthPx = with(density) { tailWidth.toPx() }
            .coerceIn(0f, size.width * 0.45f)
        val bubbleBottom = size.height - tailHeightPx
        val corner = with(density) { cornerRadius.toPx() }
            .coerceAtMost(size.width / 2f)
            .coerceAtMost(bubbleBottom / 2f)
        val tailCenterX = size.width / 2f

        val path = Path().apply {
            moveTo(corner, 0f)
            lineTo(size.width - corner, 0f)
            quadraticTo(size.width, 0f, size.width, corner)
            lineTo(size.width, bubbleBottom - corner)
            quadraticTo(size.width, bubbleBottom, size.width - corner, bubbleBottom)
            lineTo(tailCenterX + tailWidthPx / 2f, bubbleBottom)
            lineTo(tailCenterX, size.height)
            lineTo(tailCenterX - tailWidthPx / 2f, bubbleBottom)
            lineTo(corner, bubbleBottom)
            quadraticTo(0f, bubbleBottom, 0f, bubbleBottom - corner)
            lineTo(0f, corner)
            quadraticTo(0f, 0f, corner, 0f)
            close()
        }

        return Outline.Generic(path)
    }
}

private data class VerticalBiasAlignment(
    val verticalBias: Float
) : Alignment {
    override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset {
        val x = (space.width - size.width) / 2
        val biasFraction = (verticalBias + 1f) / 2f
        val y = ((space.height - size.height) * biasFraction).toInt()
        return IntOffset(x, y)
    }
}
