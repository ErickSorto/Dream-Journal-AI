package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.pages.aipage

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.RewardedAd
import coil3.compose.LocalPlatformContext
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.baseline_report_24
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.components.ActionBottomSheet
import org.ballistic.dreamjournalai.shared.core.components.DreamTokenLayout
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AIPageType
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AITool
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.DreamInterpretationPopUp
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.ImageGenerationPopUp
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.QuestionAIGenerationBottomSheet
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.pages.AIPage.AISubPages.UniversalAIPage
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AIPage
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AddEditDreamState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.BrighterWhite
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import org.jetbrains.compose.resources.painterResource

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    DependsOnGoogleMobileAds::class
)
@Composable
fun SharedTransitionScope.AIPage(
    pages: ImmutableList<String>,
    pagerState2: PagerState,
    addEditDreamState: AddEditDreamState,
    textFieldState: TextFieldState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit,
    onImageClick: (String) -> Unit = {},
) {

    val dreamTokens = addEditDreamState.dreamTokens
    val flagContentBottomSheetState = remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scope = rememberCoroutineScope()

    if (flagContentBottomSheetState.value) {
        ActionBottomSheet(
            title = "Flag Content",
            message = "Are you sure you want to flag this content?",
            buttonText = "Flag",
            onClick = {
                onAddEditDreamEvent(AddEditDreamEvent.FlagDreamContent)
                flagContentBottomSheetState.value = false
            },
            onClickOutside = {
                flagContentBottomSheetState.value = false
            }
        )
    }

    when (addEditDreamState.aiPage) {
        AIPage.IMAGE -> {
            ImageGenerationPopUp(
                addEditDreamState = addEditDreamState,
                onDreamTokenClick = { amount, style ->
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    if (dreamTokens < amount) {
                        scope.launch {
                            SnackbarController.sendEvent(
                                SnackbarEvent(
                                    message = "Not enough dream tokens",
                                    action = SnackbarAction(
                                        name = "Dismiss",
                                        action = {}
                                    )
                                )
                            )
                        }
                    } else {
                        scope.launch {
                            onAddEditDreamEvent(
                                AddEditDreamEvent.ClickGenerateAIImage(
                                    style = style,
                                    cost = amount
                                )
                            )
                        }
                    }
                },
                onAdClick = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    scope.launch {
                        onAddEditDreamEvent(
                            AddEditDreamEvent.AdAIImageToggle(
                                true
                            )
                        )
                    }
                },
                onClickOutside = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                },
                onImageStyleChange = {
                    onAddEditDreamEvent(AddEditDreamEvent.OnImageStyleChanged(it))
                }
            )
        }

        AIPage.INTERPRETATION -> {
            DreamInterpretationPopUp(
                title = "Dream Interpreter",
                dreamTokens = dreamTokens,
                onAdClick = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    scope.launch {
                        onAddEditDreamEvent(
                            AddEditDreamEvent.AdAIResponseToggle(
                                true
                            )
                        )
                    }
                },
                onDreamTokenClick = { amount ->
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    if (dreamTokens < amount) {
                        scope.launch {
                            SnackbarController.sendEvent(
                                SnackbarEvent(
                                    message = "Not enough dream tokens",
                                    action = SnackbarAction(
                                        name = "Dismiss",
                                        action = {}
                                    )
                                )
                            )
                        }
                    } else {
                        scope.launch {
                            onAddEditDreamEvent(
                                AddEditDreamEvent.ClickGenerateAIResponse(
                                    content = textFieldState.text.toString(),
                                    cost = amount
                                )
                            )
                        }
                    }

                },
                onClickOutside = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                },
            )
        }

        AIPage.ADVICE -> {
            DreamInterpretationPopUp(
                title = "Dream Advice",
                dreamTokens = dreamTokens,
                onAdClick = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    scope.launch {
                        onAddEditDreamEvent(
                            AddEditDreamEvent.AdAIAdviceToggle(
                                true
                            )
                        )
                    }
                },
                onDreamTokenClick = { amount ->
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    if (dreamTokens < amount) {
                        scope.launch {
                            SnackbarController.sendEvent(
                                SnackbarEvent(
                                    message = "Not enough dream tokens",
                                    action = SnackbarAction(
                                        name = "Dismiss",
                                        action = {}
                                    )
                                )
                            )
                        }
                    } else {
                        scope.launch {
                            onAddEditDreamEvent(
                                AddEditDreamEvent.ClickGenerateAIAdvice(
                                    content = textFieldState.text.toString(),
                                    cost = amount
                                )
                            )
                        }
                    }

                },
                onClickOutside = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                },
            )
        }

        AIPage.QUESTION -> {
            QuestionAIGenerationBottomSheet(
                addEditDreamState = addEditDreamState,
                onAddEditDreamEvent = onAddEditDreamEvent,
                onDreamTokenClick = { amount ->
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    if (dreamTokens < amount) {
                        scope.launch {
                            SnackbarController.sendEvent(
                                SnackbarEvent(
                                    message = "Not enough dream tokens",
                                    action = SnackbarAction(
                                        name = "Dismiss",
                                        action = {}
                                    )
                                )
                            )
                        }
                    } else {
                        scope.launch {
                            onAddEditDreamEvent(
                                AddEditDreamEvent.ClickGenerateFromQuestion(
                                    content = textFieldState.text.toString(),
                                    cost = amount
                                )
                            )
                        }
                    }
                },
                onAdClick = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    scope.launch {
                        onAddEditDreamEvent(
                            AddEditDreamEvent.AdQuestionToggle(
                                true
                            )
                        )
                    }
                },
                onClickOutside = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                },
            )
        }

        AIPage.STORY -> {
            DreamInterpretationPopUp(
                title = "Dream Story",
                dreamTokens = dreamTokens,
                onDreamTokenClick = { amount ->
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    if (dreamTokens < amount) {
                        scope.launch {
                            SnackbarController.sendEvent(
                                SnackbarEvent(
                                    message = "Not enough dream tokens",
                                    action = SnackbarAction(
                                        name = "Dismiss",
                                        action = {}
                                    )
                                )
                            )
                        }
                    } else {
                        scope.launch {
                            onAddEditDreamEvent(
                                AddEditDreamEvent.ClickGenerateStory(
                                    content = textFieldState.text.toString(),
                                    cost = amount
                                )
                            )
                        }
                    }
                },
                onAdClick = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    scope.launch {
                        onAddEditDreamEvent(
                            AddEditDreamEvent.AdStoryToggle(
                                true
                            )
                        )
                    }
                },
                onClickOutside = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                },
            )
        }

        AIPage.MOOD -> {
            DreamInterpretationPopUp(
                title = "Dream Mood",
                dreamTokens = dreamTokens,
                onAdClick = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    scope.launch {
                        onAddEditDreamEvent(
                            AddEditDreamEvent.AdMoodToggle(
                                true
                            )
                        )
                    }
                },
                onDreamTokenClick = { amount ->
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                    if (dreamTokens < amount) {
                        scope.launch {
                            SnackbarController.sendEvent(
                                SnackbarEvent(
                                    message = "Not enough dream tokens",
                                    action = SnackbarAction(
                                        name = "Dismiss",
                                        action = {}
                                    )
                                )
                            )
                        }
                    } else {
                        scope.launch {
                            onAddEditDreamEvent(
                                AddEditDreamEvent.ClickGenerateMood(
                                    content = textFieldState.text.toString(),
                                    cost = amount
                                )
                            )
                        }
                    }

                },
                onClickOutside = {
                    onAddEditDreamEvent(AddEditDreamEvent.SetAIPage(null))
                },
            )
        }

        null -> {
            // Do nothing
        }
    }

    if (addEditDreamState.isAdImage) {
        RewardedAd(
            activity = LocalPlatformContext.current,
            adUnitId = "ca-app-pub-8710979310678386/8178296701",
            onRewardEarned = {
                onAddEditDreamEvent(
                    AddEditDreamEvent.ClickGenerateAIImage(
                        style = addEditDreamState.imageStyle.promptAffix,
                        cost = 0
                    )
                )
                onAddEditDreamEvent(AddEditDreamEvent.AdAIImageToggle(false))
            },
            onDismissed = {
                onAddEditDreamEvent(AddEditDreamEvent.AdAIImageToggle(false))
            }
        )
    }

    if (addEditDreamState.isAdResponse) {
        RewardedAd(
            activity = LocalPlatformContext.current,
            adUnitId = "ca-app-pub-8710979310678386/8178296701",
            onRewardEarned = {
                onAddEditDreamEvent(
                    AddEditDreamEvent.ClickGenerateAIResponse(
                        content = textFieldState.text.toString(),
                        cost = 0
                    )
                )
                onAddEditDreamEvent(AddEditDreamEvent.AdAIResponseToggle(false))
            },
            onDismissed = {
                onAddEditDreamEvent(AddEditDreamEvent.AdAIResponseToggle(false))
            }
        )
    }

    if (addEditDreamState.isAdAdvice) {
        RewardedAd(
            activity = LocalPlatformContext.current,
            adUnitId = "ca-app-pub-8710979310678386/8178296701",
            onRewardEarned = {
                onAddEditDreamEvent(
                    AddEditDreamEvent.ClickGenerateAIAdvice(
                        content = textFieldState.text.toString(),
                        cost = 0
                    )
                )
                onAddEditDreamEvent(AddEditDreamEvent.AdAIAdviceToggle(false))
            },
            onDismissed = {
                onAddEditDreamEvent(AddEditDreamEvent.AdAIAdviceToggle(false))
            }
        )
    }

    if (addEditDreamState.isAdQuestion) {
        RewardedAd(
            activity = LocalPlatformContext.current,
            adUnitId = "ca-app-pub-8710979310678386/8178296701",
            onRewardEarned = {
                onAddEditDreamEvent(
                    AddEditDreamEvent.ClickGenerateFromQuestion(
                        content = textFieldState.text.toString(),
                        cost = 0
                    )
                )
                onAddEditDreamEvent(AddEditDreamEvent.AdQuestionToggle(false))
            },
            onDismissed = {
                onAddEditDreamEvent(AddEditDreamEvent.AdQuestionToggle(false))
            }
        )
    }

    if (addEditDreamState.isAdStory) {
        RewardedAd(
            activity = LocalPlatformContext.current,
            adUnitId = "ca-app-pub-8710979310678386/8178296701",
            onRewardEarned = {
                onAddEditDreamEvent(
                    AddEditDreamEvent.ClickGenerateStory(
                        content = textFieldState.text.toString(),
                        cost = 0
                    )
                )
                onAddEditDreamEvent(AddEditDreamEvent.AdStoryToggle(false))
            },
            onDismissed = {
                onAddEditDreamEvent(AddEditDreamEvent.AdStoryToggle(false))
            }
        )
    }

    if (addEditDreamState.isAdMood) {
        RewardedAd(
            activity = LocalPlatformContext.current,
            adUnitId = "ca-app-pub-8710979310678386/8178296701",
            onRewardEarned = {
                onAddEditDreamEvent(
                    AddEditDreamEvent.ClickGenerateMood(
                        content = textFieldState.text.toString(),
                        cost = 0
                    )
                )
                onAddEditDreamEvent(AddEditDreamEvent.AdMoodToggle(false))
            },
            onDismissed = {
                onAddEditDreamEvent(AddEditDreamEvent.AdMoodToggle(false))
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(LightBlack.copy(alpha = 0.7f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI Tools Selection",
            style = typography.labelMedium,
            color = White,
            modifier = Modifier.padding(4.dp, 8.dp, 4.dp, 4.dp)
        )

        SecondaryTabRow(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 0.dp, bottom = 8.dp)
                .clip(
                    RoundedCornerShape(8.dp)
                ),
            selectedTabIndex = pagerState2.currentPage,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    color = White,
                    modifier = Modifier.tabIndicatorOffset(pagerState2.currentPage)
                )
            },
            divider = {},
            contentColor = White,
            containerColor = LightBlack.copy(alpha = 0.5f),
        ) {
            pages.forEachIndexed { index, page ->
                val isSelected = pagerState2.currentPage == index

                // Setup for on-click and selection-based scaling
                val targetScale = if (isSelected) 1.15f else .97f
                val scale = remember { Animatable(targetScale) }

                // Continuous floating effect setup
                val floatingScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = ""
                )

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (textFieldState.text.length >= 20) {
                            AITool.entries[index].color
                        } else White
                    } else Color.LightGray.copy(alpha = 0.6f),
                    animationSpec = tween(durationMillis = 500), label = ""
                )

                // Apply immediate scale change on selection
                LaunchedEffect(isSelected) {
                    scale.animateTo(
                        targetValue = targetScale,
                        animationSpec = tween(durationMillis = 400)
                    )
                }

                val gradientBackground = if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            BrighterWhite.copy(alpha = 0.12f),
                            BrighterWhite.copy(alpha = 0.02f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(100f, 100f)
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.1f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                }

                Tab(
                    text = {
                        Icon(
                            painter = painterResource(AITool.entries[index].icon),
                            contentDescription = page,
                            tint = iconColor,
                            modifier = Modifier
                                .graphicsLayer {
                                    val finalScale =
                                        if (isSelected) floatingScale * scale.value else scale.value
                                    scaleX = finalScale
                                    scaleY = finalScale
                                }
                                .size(26.dp)
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        onAddEditDreamEvent(AddEditDreamEvent.TriggerVibration)
                        scope.launch {
                            pagerState2.animateScrollToPage(
                                index,
                            )
                        }
                    },
                    modifier = Modifier
                        .background(
                            brush = gradientBackground
                        )
                )
            }
        }
        // Replace scrolling Column with a Box that fills remaining space
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(LightBlack.copy(alpha = 0.7f))
                .weight(1f)
                .fillMaxWidth(),
        ) {

            HorizontalPager(
                state = pagerState2,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .background(
                        color = LightBlack.copy(alpha = 0.0f)
                    ),
                beyondViewportPageCount = 0,
                verticalAlignment = Alignment.Top
            ) { page ->

                // Get the AI page type corresponding to the current page index
                val aiPageType =
                    AIPageType.entries[page]

                // Render the universal AI page for the current type
                UniversalAIPage(
                    contentType = aiPageType,
                    addEditDreamState = addEditDreamState,
                    textFieldState = textFieldState,
                    onAddEditEvent = onAddEditDreamEvent,
                    snackBarState = {
                        scope.launch {
                            SnackbarController.sendEvent(
                                SnackbarEvent(
                                    message = "Dream is too short",
                                    action = SnackbarAction(
                                        name = "Dismiss",
                                        action = {}
                                    )
                                )
                            )
                        }
                    },
                    animatedVisibilityScope = animatedVisibilityScope,
                    onImageClick = {
                        onImageClick(it)
                    }
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )
                Text(
                    text = "Tokens",
                    style = typography.titleMedium.copy(BrighterWhite),
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                DreamTokenLayout(
                    totalDreamTokens = dreamTokens,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            IconButton(
                onClick = {
                    flagContentBottomSheetState.value = true
                },
                modifier = Modifier.align(
                    Alignment.CenterEnd
                )

            ) {
                Icon(
                    painterResource(Res.drawable.baseline_report_24),
                    contentDescription = "Report",
                    tint = Color.White,
                )
            }
        }

        AIButton(
            text = "Generate " + AIPage.entries[pagerState2.currentPage].name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            color = AITool.entries[pagerState2.currentPage].color,
            onClick = {
                if (textFieldState.text.length >= 20) {
                    onAddEditDreamEvent(
                        AddEditDreamEvent.SetAIPage(
                            AIPage.entries[pagerState2.currentPage]
                        )
                    )
                } else {
                    scope.launch {
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = "Dream is too short",
                                action = SnackbarAction(
                                    name = "Dismiss",
                                    action = {}
                                )
                            )
                        )
                    }
                }
            },
            onAddEditDreamEvent = onAddEditDreamEvent
        )
    }
}


@Composable
fun AIButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit,
) {
    Button(
        onClick = {
            onClick()
            onAddEditDreamEvent(AddEditDreamEvent.TriggerVibration)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp, 16.dp, 8.dp)
            .border(
                width = 4.dp,
                color = LightBlack.copy(alpha = 0.3f),
                shape = RoundedCornerShape(9.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
        ),

        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 5.dp,
            pressedElevation = 10.dp,
        ),
    ) {
        Text(
            text = text,
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}
