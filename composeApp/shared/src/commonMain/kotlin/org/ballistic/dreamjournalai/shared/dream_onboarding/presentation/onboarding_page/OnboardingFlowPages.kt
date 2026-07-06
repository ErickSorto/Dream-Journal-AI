package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.*
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.CommitmentSummaryModel
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.DreamMood
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.DreamTheme
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.FarGoal
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.NearGoal
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.OnboardingCommitment
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.RecallBlocker
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.RoadmapMilestoneModel
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.SnapshotCardModel
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.recallHelperText
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val OnboardingPanelShape = RoundedCornerShape(30.dp)
private data class ReviewStory(
    val name: String,
    val title: String,
    val quote: String,
    val avatar: DrawableResource,
)

@Composable
fun OnboardingDreamOutcomePage(
    startAnimation: Boolean,
    contentAnimationStarted: Boolean = startAnimation,
    onHeroCenterChanged: (Offset) -> Unit,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    val panelAlpha by animateFloatAsState(
        targetValue = if (contentAnimationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
        label = "dream_outcome_panel_alpha"
    )
    val panelScale by animateFloatAsState(
        targetValue = if (contentAnimationStarted) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 210f),
        label = "dream_outcome_panel_scale"
    )
    var headlineVisible by remember { mutableStateOf(false) }
    var subheadlineVisible by remember { mutableStateOf(false) }
    var actionsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(contentAnimationStarted) {
        if (contentAnimationStarted) {
            kotlinx.coroutines.delay(280)
            headlineVisible = true
            kotlinx.coroutines.delay(180)
            subheadlineVisible = true
            kotlinx.coroutines.delay(180)
            actionsVisible = true
        } else {
            headlineVisible = false
            subheadlineVisible = false
            actionsVisible = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutcomeHero(
            startAnimation = startAnimation,
            onHeroCenterChanged = onHeroCenterChanged,
            modifier = Modifier.fillMaxWidth()
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(
                    alpha = panelAlpha,
                    scaleX = panelScale,
                    scaleY = panelScale,
                    transformOrigin = TransformOrigin(0.5f, 0f)
                ),
            color = LightBlack.copy(alpha = 0.56f),
            shape = OnboardingPanelShape,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OnboardingProgressTracker(
                    currentStep = 1,
                    totalSteps = 17,
                    title = "Dream path"
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AnimatedVisibility(
                        visible = headlineVisible,
                        enter = fadeIn(animationSpec = tween(760, easing = FastOutSlowInEasing)) +
                            scaleIn(
                                initialScale = 0.985f,
                                animationSpec = tween(760, easing = FastOutSlowInEasing)
                            ) +
                            slideInVertically(
                                initialOffsetY = { it / 6 },
                                animationSpec = tween(760, easing = FastOutSlowInEasing)
                            )
                    ) {
                        Text(
                            text = "Welcome to DreamNorth.",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 30.sp,
                                lineHeight = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    AnimatedVisibility(
                        visible = subheadlineVisible,
                        enter = fadeIn(animationSpec = tween(820, easing = FastOutSlowInEasing)) +
                            scaleIn(
                                initialScale = 0.99f,
                                animationSpec = tween(820, easing = FastOutSlowInEasing)
                            ) +
                            slideInVertically(
                                initialOffsetY = { it / 7 },
                                animationSpec = tween(820, easing = FastOutSlowInEasing)
                            )
                    ) {
                        Text(
                            text = "Your dream companion is ready to help you remember more, find patterns, and make mornings feel a little more magical.",
                            style = TextStyle(
                                color = Color.White.copy(alpha = 0.80f),
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }

                AnimatedVisibility(
                    visible = actionsVisible,
                    enter = fadeIn(animationSpec = tween(620, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 8 },
                            animationSpec = tween(620, easing = FastOutSlowInEasing)
                        )
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OnboardingPrimaryButton(
                            text = "Start my dream path",
                            onClick = onPrimaryClick
                        )
                    }
                }
            }
        }
    }

    OnboardingScrollHint(
        scrollState = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 36.dp)
    )
}

@Composable
fun OnboardingDreamFrictionIntroPage(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 2,
        title = "The problem",
    ) {
        SmallIconCluster(
            icons = listOf(
                Res.drawable.onboarding_icon_cloud_glass,
                Res.drawable.onboarding_icon_hourglass_glass,
                Res.drawable.onboarding_icon_open_journal_glass
            )
        )

        Text(
            text = "Ever wake up with a dream that feels important… then lose it by breakfast?",
            style = headerStyle()
        )

        Text(
            text = "Dreams fade fast. DreamNorth gives you a beautiful morning ritual for catching the fragments, feelings, and symbols while they are still alive.",
            style = bodyStyle()
        )

        GlassInfoCard {
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_moon_glass,
                text = "Capture the dream in the first quiet minute."
            )
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_star_cluster_glass,
                text = "Turn repeated images into patterns you can actually see."
            )
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_heart_glass,
                text = "Reflect without making your morning feel heavy."
            )
        }

        OnboardingPrimaryButton(
            text = "Build my ritual",
            onClick = onContinue
        )
    }
}

@Composable
fun OnboardingNamePage(
    firstName: String,
    onNameChanged: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = imeBottom > 0
    var keyboardWasVisible by remember { mutableStateOf(false) }
    var isNameFocused by remember { mutableStateOf(false) }
    val heroHeight = if (isKeyboardVisible) 96.dp else 188.dp

    LaunchedEffect(imeBottom) {
        if (isKeyboardVisible) {
            keyboardWasVisible = true
        } else if (keyboardWasVisible) {
            keyboardWasVisible = false
            isNameFocused = false
            focusManager.clearFocus()
        }
    }

    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 3,
        title = "First things first",
        autoScrollOnIme = true,
        resetScrollOnImeDismiss = true,
    ) {
        DreamHero(
            drawableRes = Res.drawable.onboarding_hero_get_to_know_you,
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
        )

        AnimatedVisibility(visible = !isKeyboardVisible) {
            SmallIconCluster(
                icons = listOf(
                    Res.drawable.onboarding_icon_moon_glass,
                    Res.drawable.onboarding_icon_open_journal_glass,
                    Res.drawable.onboarding_icon_spark_glass
                )
            )
        }

        Text(
            text = "What should we call you?",
            style = headerStyle()
        )

        Text(
            text = "We'll use this to personalize your dream path.",
            style = bodyStyle()
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = onNameChanged,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (firstName.isNotBlank()) {
                        onContinue()
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { focusState ->
                    isNameFocused = focusState.isFocused
                    if (focusState.isFocused) {
                        scope.launch {
                            kotlinx.coroutines.delay(240)
                            bringIntoViewRequester.bringIntoView()
                            kotlinx.coroutines.delay(320)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            placeholder = {
                Text(
                    text = "Your first name",
                    color = Color.White.copy(alpha = 0.56f)
                )
            },
            shape = RoundedCornerShape(20.dp),
            textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                focusedBorderColor = Color(0xFFFFCFA7),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                cursorColor = Color(0xFFFFCFA7),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        OnboardingPrimaryButton(
            text = "Continue",
            onClick = onContinue,
            enabled = firstName.isNotBlank()
        )
    }
}

@Composable
fun OnboardingNearOutcomePage(
    firstName: String,
    selectedGoals: List<NearGoal>,
    onToggleGoal: (NearGoal) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 4,
        title = "Near outcome",
    ) {
        Text(
            text = if (firstName.isBlank()) {
                "What do you want from your dreams first?"
            } else {
                "Nice to meet you, ${firstName.trim()}"
            },
            style = TextStyle(
                color = Color(0xFFFFE1CC),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Text(
            text = "What do you want from your dreams first?",
            style = headerStyle()
        )

        Text(
            text = "Choose up to 2",
            style = bodyStyle()
        )

        NearGoal.entries.forEach { goal ->
            OnboardingChoiceCard(
                title = goal.title,
                description = goal.cardDescription,
                icon = goal.icon,
                selected = selectedGoals.contains(goal),
                onClick = { onToggleGoal(goal) }
            )
        }

        OnboardingPrimaryButton(
            text = "Next",
            onClick = onContinue,
            enabled = selectedGoals.isNotEmpty()
        )
    }
}

@Composable
fun OnboardingFarOutcomePage(
    selectedGoal: FarGoal?,
    onGoalSelected: (FarGoal) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 5,
        title = "Dream outcome",
    ) {
        DreamHero(
            drawableRes = Res.drawable.onboarding_hero_far_outcome,
            modifier = Modifier
                .fillMaxWidth()
                .height(216.dp)
        )

        Text(
            text = "And looking ahead…",
            style = TextStyle(
                color = Color(0xFFFFE1CC),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Text(
            text = "Where do you want this to lead?",
            style = headerStyle()
        )

        Text(
            text = "Choose the deeper result that matters most.",
            style = bodyStyle()
        )

        FarGoal.entries.forEach { goal ->
            OnboardingChoiceCard(
                title = goal.title,
                description = goal.snapshotBody,
                icon = goal.icon,
                selected = selectedGoal == goal,
                onClick = { onGoalSelected(goal) }
            )
        }

        OnboardingPrimaryButton(
            text = "Keep going",
            onClick = onContinue,
            enabled = selectedGoal != null
        )
    }
}

@Composable
fun OnboardingRecallPage(
    recallDaysPerWeek: Int?,
    selectedBlocker: RecallBlocker?,
    onRecallChanged: (Int) -> Unit,
    onBlockerSelected: (RecallBlocker) -> Unit,
    onContinue: () -> Unit,
    onValidationError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recallValue = recallDaysPerWeek ?: 0
    var showRecallValidation by remember { mutableStateOf(false) }
    var showBlockerValidation by remember { mutableStateOf(false) }

    LaunchedEffect(recallDaysPerWeek) {
        if (recallDaysPerWeek != null) showRecallValidation = false
    }

    LaunchedEffect(selectedBlocker) {
        if (selectedBlocker != null) showBlockerValidation = false
    }

    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 6,
        title = "Dream friction",
    ) {
        SmallIconCluster(
            icons = listOf(
                Res.drawable.onboarding_icon_cloud_glass,
                Res.drawable.onboarding_icon_hourglass_glass,
                Res.drawable.onboarding_icon_sunrise_glass
            )
        )

        Text(
            text = "How many mornings a week do you clearly remember a dream?",
            style = headerStyle()
        )

        Text(
            text = "Be honest — this helps us tailor your plan.",
            style = bodyStyle()
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (showRecallValidation) {
                Color(0x22FFB996)
            } else {
                Color.White.copy(alpha = 0.05f)
            },
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(
                1.dp,
                if (showRecallValidation) {
                    Color(0xFFFFC8A2).copy(alpha = 0.85f)
                } else {
                    Color.White.copy(alpha = 0.12f)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "0 mornings",
                        style = TextStyle(color = Color.White.copy(alpha = 0.62f), fontSize = 13.sp)
                    )
                    Text(
                        text = "$recallValue / 7",
                        style = TextStyle(color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "7 mornings",
                        style = TextStyle(color = Color.White.copy(alpha = 0.62f), fontSize = 13.sp)
                    )
                }

                Slider(
                    value = recallValue.toFloat(),
                    onValueChange = { onRecallChanged(it.roundToInt().coerceIn(0, 7)) },
                    valueRange = 0f..7f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFFFFC48F),
                        inactiveTrackColor = Color.White.copy(alpha = 0.16f),
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent,
                        thumbColor = Color.White
                    )
                )

                Text(
                    text = if (showRecallValidation && recallDaysPerWeek == null) {
                        "Choose a number first so we can tailor your plan."
                    } else {
                        recallHelperText(recallValue)
                    },
                    style = TextStyle(
                        color = if (showRecallValidation && recallDaysPerWeek == null) {
                            Color(0xFFFFD4B6)
                        } else {
                            Color(0xFFFFE1C6)
                        },
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontWeight = if (showRecallValidation && recallDaysPerWeek == null) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                    )
                )
            }
        }

        Text(
            text = if (showBlockerValidation && selectedBlocker == null) {
                "Pick what usually gets in the way."
            } else {
                "What usually gets in the way?"
            },
            style = TextStyle(
                color = if (showBlockerValidation && selectedBlocker == null) {
                    Color(0xFFFFD4B6)
                } else {
                    Color.White
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        RecallBlocker.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { blocker ->
                    OnboardingChipCard(
                        text = blocker.title,
                        icon = blocker.icon,
                        selected = selectedBlocker == blocker,
                        modifier = Modifier
                            .weight(1f)
                            .height(112.dp),
                        onClick = { onBlockerSelected(blocker) }
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        OnboardingPrimaryButton(
            text = "That's me",
            onClick = onContinue,
            enabled = recallDaysPerWeek != null && selectedBlocker != null,
            onDisabledClick = {
                onValidationError()
                showRecallValidation = recallDaysPerWeek == null
                showBlockerValidation = selectedBlocker == null
            }
        )
    }
}

@Composable
fun OnboardingDreamMathPage(
    firstName: String,
    recallDaysPerWeek: Int?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rememberedMornings = recallDaysPerWeek ?: 0
    val conservativeDreamsPerYear = 3 * 365
    val rememberedMorningsPerYear = rememberedMornings * 52
    val missedMorningsPerYear = ((7 - rememberedMornings).coerceIn(0, 7) * 52)

    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 7,
        title = "Dream math",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0x55FFD7A8),
                                Color(0x22191626),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.onboarding_icon_lightbulb_glass),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = "You dream far more than you remember.",
                style = headerStyle().copy(
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Most people dream several times a night. At just 3 dreams per night, that adds up to more than a thousand moments a year.",
                style = bodyStyle().copy(
                    color = Color.White.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
        }

        DreamMathHeroStatCard(
            targetValue = conservativeDreamsPerYear,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DreamMathMiniStatCard(
                value = "$rememberedMorningsPerYear",
                label = "captured mornings",
                icon = Res.drawable.onboarding_icon_open_journal_glass,
                modifier = Modifier.weight(1f)
            )
            DreamMathMiniStatCard(
                value = "$missedMorningsPerYear",
                label = "mornings fading",
                icon = Res.drawable.onboarding_icon_cloud_glass,
                modifier = Modifier.weight(1f)
            )
        }

        GlassInfoCard {
            Text(
                text = "${firstName.trim().ifBlank { "Dreamer" }}, the opportunity is not more sleep. It's better capture.",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "A dream can carry a mood, a symbol, a fear, an idea, or a pattern. When it vanishes in the first few minutes of the morning, the insight and benefit can disappear with it too.",
                style = bodyStyle()
            )
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_compass_glass,
                text = "DreamNorth helps you catch the dreams that could actually mean something."
            )
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_spark_glass,
                text = "The next step is seeing what your answers say about what you're missing."
            )
        }

        OnboardingPrimaryButton(
            text = "Show me what I'm missing",
            onClick = onContinue
        )
    }
}

@Composable
fun OnboardingSnapshotPage(
    firstName: String,
    cards: List<SnapshotCardModel>,
    lostDreamsEstimate: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 8,
        title = "Dream Snapshot",
    ) {
        DreamHero(
            drawableRes = Res.drawable.onboarding_hero_snapshot_cluster,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        )

        Text(
            text = "${firstName.trim().ifBlank { "Dreamer" }}, here's your Dream Snapshot",
            style = headerStyle()
        )

        Text(
            text = "Based on your answers, you may be losing around $lostDreamsEstimate dream mornings a year. That is a lot of inner material to leave behind.",
            style = bodyStyle()
        )

        cards.forEachIndexed { index, card ->
            SnapshotInsightCard(
                card = card,
                animationDelay = index * 120
            )
        }

        OnboardingPrimaryButton(
            text = "Show me my plan",
            onClick = onContinue
        )
    }
}

@Composable
fun OnboardingPlanBridgePage(
    firstName: String,
    insight: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 9,
        title = "The bridge",
    ) {
        DreamHero(
            drawableRes = Res.drawable.onboarding_hero_commitment,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        )

        Text(
            text = "It doesn't have to keep disappearing.",
            style = headerStyle()
        )

        Text(
            text = insight,
            style = bodyStyle()
        )

        GlassInfoCard {
            Text(
                text = "Your plan starts small, ${firstName.trim().ifBlank { "dreamer" }}",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_feather_glass,
                text = "One simple check-in after waking."
            )
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_mirror_glass,
                text = "One guided reflection that makes the dream feel useful."
            )
        }

        OnboardingPrimaryButton(
            text = "Try the ritual",
            onClick = onContinue
        )
    }
}

@Composable
fun OnboardingDreamCheckInPage(
    selectedTheme: DreamTheme?,
    selectedMood: DreamMood?,
    onThemeSelected: (DreamTheme) -> Unit,
    onMoodSelected: (DreamMood) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 10,
        title = "First check-in",
    ) {
        Text(
            text = "Let's do one dream check-in now.",
            style = headerStyle()
        )

        Text(
            text = "Pick the kind of dream you remember most often, or the one you wish you understood better.",
            style = bodyStyle()
        )

        DreamTheme.entries.forEach { theme ->
            OnboardingChoiceCard(
                title = theme.title,
                description = theme.reflection.replaceFirstChar { it.uppercase() },
                icon = theme.icon,
                selected = selectedTheme == theme,
                onClick = { onThemeSelected(theme) }
            )
        }

        Text(
            text = "How do those dreams usually feel?",
            style = TextStyle(
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        DreamMood.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { mood ->
                    OnboardingChipCard(
                        text = mood.title,
                        icon = mood.icon,
                        selected = selectedMood == mood,
                        modifier = Modifier.weight(1f),
                        onClick = { onMoodSelected(mood) }
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        OnboardingPrimaryButton(
            text = "Reveal my first insight",
            onClick = onContinue,
            enabled = selectedTheme != null && selectedMood != null
        )
    }
}

@Composable
fun OnboardingFirstInsightPage(
    firstName: String,
    reflection: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var lineCount by remember { mutableStateOf(0) }

    LaunchedEffect(reflection) {
        lineCount = 0
        repeat(3) {
            kotlinx.coroutines.delay(420)
            lineCount = it + 1
        }
    }

    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 11,
        title = "First insight",
    ) {
        DreamHero(
            drawableRes = Res.drawable.onboarding_hero_snapshot_cluster,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        )

        Text(
            text = "${firstName.trim().ifBlank { "Dreamer" }}, here's what we'd reflect back",
            style = headerStyle()
        )

        AnimatedVisibility(visible = lineCount >= 1, enter = fadeIn(animationSpec = tween(520))) {
            GlassInfoCard {
                SummaryBullet(
                    icon = Res.drawable.onboarding_icon_eye_glass,
                    text = "Your dream is not just a note. It has a feeling, a symbol, and a pattern."
                )
            }
        }
        AnimatedVisibility(visible = lineCount >= 2, enter = fadeIn(animationSpec = tween(520))) {
            GlassInfoCard {
                Text(
                    text = reflection,
                    style = bodyStyle()
                )
            }
        }
        AnimatedVisibility(visible = lineCount >= 3, enter = fadeIn(animationSpec = tween(520))) {
            GlassInfoCard {
                SummaryBullet(
                    icon = Res.drawable.onboarding_icon_open_journal_glass,
                    text = "Tomorrow, the app helps you save the dream before it fades."
                )
            }
        }

        OnboardingPrimaryButton(
            text = "Save my first insight",
            onClick = onContinue,
            enabled = lineCount >= 3
        )
    }
}

@Composable
fun OnboardingFirstStreakPage(
    firstName: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 12,
        title = "Day one",
    ) {
        DreamHero(
            drawableRes = Res.drawable.onboarding_hero_seven_nights,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        )

        Text(
            text = "Beautiful. Your dream streak starts now.",
            style = headerStyle()
        )

        Text(
            text = "${firstName.trim().ifBlank { "You" }} completed the first dream reflection. This is the exact moment we want tomorrow morning to feel easy.",
            style = bodyStyle()
        )

        GlassInfoCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryBullet(
                    icon = Res.drawable.onboarding_icon_sunrise_glass,
                    text = "Current streak"
                )
                Text(
                    text = "1 day",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        OnboardingPrimaryButton(
            text = "Keep building my path",
            onClick = onContinue
        )
    }
}

@Composable
fun OnboardingCommitmentPage(
    selectedCommitment: OnboardingCommitment?,
    onCommitmentSelected: (OnboardingCommitment) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 14,
        title = "Commitment",
    ) {
        Text(
            text = "How committed are you to remembering and understanding your dreams?",
            style = headerStyle()
        )

        Text(
            text = "Small commitments matter. When you name the future you want, it becomes easier to follow through.",
            style = bodyStyle()
        )

        OnboardingCommitment.entries.forEach { commitment ->
            OnboardingChoiceCard(
                title = commitment.title,
                description = commitment.affirmation,
                icon = commitment.icon,
                selected = selectedCommitment == commitment,
                onClick = { onCommitmentSelected(commitment) }
            )
        }

        selectedCommitment?.let { commitment ->
            GlassInfoCard {
                Text(
                    text = commitment.affirmation,
                    style = bodyStyle()
                )
            }
        }

        OnboardingPrimaryButton(
            text = "I commit to my dream path",
            onClick = onContinue,
            enabled = selectedCommitment != null
        )
    }
}

@Composable
fun OnboardingSocialProofPage(
    firstName: String,
    nearGoalLabel: String,
    farGoalLabel: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 16,
        title = "You're not alone",
    ) {
        Text(
            text = "Your path is ready to join the dream ritual.",
            style = headerStyle()
        )

        Text(
            text = "The last step is saving ${firstName.trim().ifBlank { "your" }} personalized path so DreamNorth can keep your $nearGoalLabel and $farGoalLabel plan ready.",
            style = bodyStyle()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SocialProofStat(
                value = "1 min",
                label = "morning capture",
                modifier = Modifier.weight(1f)
            )
            SocialProofStat(
                value = "7",
                label = "night starter path",
                modifier = Modifier.weight(1f)
            )
        }

        GlassInfoCard {
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_review_star_glass,
                text = "Built for a calm daily habit, not a generic journal."
            )
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_compass_glass,
                text = "Your answers shape the plan that appears after sign in."
            )
        }

        OnboardingPrimaryButton(
            text = "Save my dream path",
            onClick = onContinue
        )
    }
}

@Composable
fun OnboardingNotificationPermissionPage(
    onEnableNotifications: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        currentStep = org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.OnboardingFlowStep.NotificationPermission.progressStep,
        totalSteps = org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.OnboardingFlowStep.NotificationPermission.totalSteps,
        title = "Dream reminders",
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Image(
            painter = painterResource(Res.drawable.onboarding_notification_permission_hero),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Let DreamNorth remind you gently.",
            style = headerStyle(),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "A quiet nudge can help you save dreams before they fade, check reality during the day, and return to your ritual at the right time.",
            style = bodyStyle(),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NotificationBenefitLine("Morning dream capture prompts")
            NotificationBenefitLine("Reality checks for lucid dreaming")
            NotificationBenefitLine("Daily dream token reminders")
        }

        OnboardingPrimaryButton(
            text = "Enable notifications",
            onClick = onEnableNotifications
        )

        OnboardingSecondaryAction(
            text = "Do later",
            onClick = onLater,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )
    }
}

@Composable
private fun NotificationBenefitLine(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFC269))
        )
        Text(
            text = text,
            style = bodyStyle(fontSize = 13.sp),
            color = Color.White.copy(alpha = 0.84f)
        )
    }
}

@Composable
fun OnboardingSevenNightsPage(
    firstName: String,
    subheadline: String,
    milestones: List<RoadmapMilestoneModel>,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 15,
        title = "First 7 nights",
    ) {
        DreamHero(
            drawableRes = Res.drawable.onboarding_hero_seven_nights,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        )

        Text(
            text = "Your first 7 nights, ${firstName.trim().ifBlank { "dreamer" }}",
            style = headerStyle()
        )

        Text(
            text = subheadline,
            style = bodyStyle()
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            milestones.forEachIndexed { index, milestone ->
                RoadmapMilestoneCard(
                    milestone = milestone,
                    showConnector = index != milestones.lastIndex
                )
            }
        }

        OnboardingPrimaryButton(
            text = "Show me my full plan",
            onClick = onContinue
        )
    }
}

@Composable
fun OnboardingPaywallPage(
    firstName: String,
    summary: CommitmentSummaryModel,
    selectedNearGoalLabel: String,
    selectedFarGoalLabel: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    onDeclineClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAllPlans by remember { mutableStateOf(false) }

    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 8,
        title = "Commitment",
    ) {
        DreamHero(
            drawableRes = Res.drawable.onboarding_hero_commitment,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        )

        Text(
            text = summary.headline,
            style = headerStyle()
        )

        Text(
            text = summary.subheadline,
            style = bodyStyle()
        )

        GlassInfoCard {
            Text(
                text = "${firstName.trim().ifBlank { "Dreamer" }} — 7 nights from now",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            summary.summaryBullets.forEach { (icon, text) ->
                SummaryBullet(
                    icon = icon,
                    text = text
                )
            }
        }

        GlassInfoCard {
            summary.outcomeBullets.forEach { (icon, text) ->
                SummaryBullet(
                    icon = icon,
                    text = text
                )
            }
        }

        GlassInfoCard {
            Text(
                text = summary.pricingLabel,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = summary.pricingBody,
                style = bodyStyle(fontSize = 13.sp)
            )

            if (showAllPlans) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryBullet(
                        icon = Res.drawable.onboarding_icon_open_journal_glass,
                        text = "Monthly and annual plan choices appear after sign in."
                    )
                    SummaryBullet(
                        icon = Res.drawable.onboarding_icon_star_cluster_glass,
                        text = "Your personalized $selectedNearGoalLabel and $selectedFarGoalLabel path stays front and center."
                    )
                }
            }
        }

        OnboardingPrimaryButton(
            text = summary.primaryCta,
            onClick = onPrimaryClick
        )

        OnboardingSecondaryAction(
            text = if (showAllPlans) "Hide plan details" else "See all plans",
            onClick = {
                showAllPlans = !showAllPlans
                onSecondaryClick()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        OnboardingSecondaryAction(
            text = "Continue with basic mode",
            onClick = onDeclineClick,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.White.copy(alpha = 0.68f)
        )
    }
}

@Composable
fun OnboardingReviewStoriesPage(
    currentStep: Int = 13,
    totalSteps: Int = 17,
    title: String = "Dreamers love this",
    onReachedReviewPromptMoment: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reviews = remember {
        listOf(
            ReviewStory(
                name = "Luna",
                title = "Dream journal lover",
                quote = "I started remembering full scenes again, and the app made the habit feel beautiful instead of like homework.",
                avatar = Res.drawable.onboarding_review_avatar_luna
            ),
            ReviewStory(
                name = "Nova",
                title = "Pattern seeker",
                quote = "The prompts helped me notice symbols I had been repeating for months without realizing it.",
                avatar = Res.drawable.onboarding_review_avatar_nova
            ),
            ReviewStory(
                name = "Sage",
                title = "Night ritual builder",
                quote = "This made dream capture feel calm, personal, and easy enough to keep by my bed every morning.",
                avatar = Res.drawable.onboarding_review_avatar_sage
            )
        )
    }
    var visibleStars by remember { mutableStateOf(0) }
    var visibleCards by remember { mutableStateOf(0) }
    var promptDispatched by remember { mutableStateOf(false) }
    var actionsVisible by remember { mutableStateOf(false) }
    var sequenceFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visibleStars = 0
        visibleCards = 0
        actionsVisible = false
        sequenceFinished = false
        kotlinx.coroutines.delay(180)
        repeat(5) { index ->
            kotlinx.coroutines.delay(210)
            visibleStars = index + 1
        }
        kotlinx.coroutines.delay(420)
        repeat(reviews.size) { index ->
            visibleCards = index + 1
            kotlinx.coroutines.delay(520)
        }
        kotlinx.coroutines.delay(520)
        actionsVisible = true
        kotlinx.coroutines.delay(520)
        sequenceFinished = true
    }

    OnboardingStandardPage(
        modifier = modifier,
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = title,
        onScrolledToBottom = if (sequenceFinished) {
            {
                if (!promptDispatched) {
                    promptDispatched = true
                    onReachedReviewPromptMoment()
                }
            }
        } else {
            null
        },
    ) {
        Text(
            text = "A few things dreamers keep coming back for",
            style = TextStyle(
                color = Color(0xFFFFE1CC),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Text(
            text = "A calm ritual people actually keep.",
            style = headerStyle()
        )

        Text(
            text = "Let this feel like your next gentle yes before you save your full plan.",
            style = bodyStyle()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            repeat(5) { index ->
                val visible = visibleStars > index
                val alpha by animateFloatAsState(
                    targetValue = if (visible) 1f else 0.18f,
                    animationSpec = tween(520, easing = FastOutSlowInEasing),
                    label = "review_star_alpha_$index"
                )
                val scale by animateFloatAsState(
                    targetValue = if (visible) 1f else 0.72f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessVeryLow
                    ),
                    label = "review_star_scale_$index"
                )
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0x66FFD8A6),
                                    Color(0x33FF98C8),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.onboarding_icon_review_star_glass),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        reviews.forEachIndexed { index, review ->
            AnimatedVisibility(
                visible = visibleCards > index,
                enter = fadeIn(animationSpec = tween(620, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        initialOffsetY = { it / 5 },
                        animationSpec = tween(620, easing = FastOutSlowInEasing)
                    ) +
                    scaleIn(
                        initialScale = 0.94f,
                        animationSpec = tween(620, easing = FastOutSlowInEasing)
                    )
            ) {
                GlassInfoCard {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeroIconBadge(
                            icon = review.avatar,
                            selected = true
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = review.name,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = review.title,
                                style = TextStyle(
                                    color = Color(0xFFFFDAB7),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Text(
                        text = "\"${review.quote}\"",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.84f),
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = actionsVisible,
            enter = fadeIn(animationSpec = tween(520, easing = FastOutSlowInEasing)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(520, easing = FastOutSlowInEasing)
                ) +
                slideInVertically(
                    initialOffsetY = { it / 5 },
                    animationSpec = tween(520, easing = FastOutSlowInEasing)
                )
        ) {
            OnboardingPrimaryButton(
                text = "Continue to my full plan",
                onClick = onContinue
            )
        }

    }
}

@Composable
fun OnboardingRescuePage(
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStandardPage(
        modifier = modifier,
        currentStep = 9,
        title = "A lighter start",
    ) {
        SmallIconCluster(
            icons = listOf(
                Res.drawable.onboarding_icon_open_journal_glass,
                Res.drawable.onboarding_icon_spark_glass,
                Res.drawable.onboarding_icon_star_cluster_glass
            )
        )

        Text(
            text = "Try a lighter start",
            style = headerStyle()
        )

        Text(
            text = "You can still begin building your dream habit with a softer free path.",
            style = bodyStyle()
        )

        GlassInfoCard {
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_open_journal_glass,
                text = "3 free dream entries"
            )
            SummaryBullet(
                icon = Res.drawable.onboarding_icon_spark_glass,
                text = "1 free Dream Snapshot refresh"
            )
        }

        OnboardingPrimaryButton(
            text = "Start free basics",
            onClick = onPrimaryClick
        )

        OnboardingSecondaryAction(
            text = "Back to my full plan",
            onClick = onSecondaryClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
internal fun OnboardingStandardPage(
    currentStep: Int,
    title: String,
    modifier: Modifier = Modifier,
    totalSteps: Int = 17,
    onScrolledToBottom: (() -> Unit)? = null,
    autoScrollOnIme: Boolean = false,
    resetScrollOnImeDismiss: Boolean = false,
    scrollResetKey: Any? = null,
    autoScrollToBottomDelayMs: Long? = null,
    autoScrollToBottomDurationMs: Int = 2200,
    showProgressTracker: Boolean = true,
    backgroundContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    var bottomReported by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value, scrollState.maxValue, onScrolledToBottom) {
        if (
            onScrolledToBottom != null &&
            !bottomReported &&
            scrollState.maxValue > 0 &&
            scrollState.value >= scrollState.maxValue - 8
        ) {
            bottomReported = true
            onScrolledToBottom()
        }
    }

    LaunchedEffect(autoScrollOnIme, imeBottom, scrollState.maxValue) {
        if (autoScrollOnIme && imeBottom > 0 && scrollState.maxValue > 0) {
            kotlinx.coroutines.delay(120)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(resetScrollOnImeDismiss, imeBottom) {
        if (resetScrollOnImeDismiss && imeBottom == 0 && scrollState.value > 0) {
            kotlinx.coroutines.delay(80)
            scrollState.scrollTo(0)
        }
    }

    LaunchedEffect(scrollResetKey) {
        if (scrollResetKey != null) {
            scrollState.scrollTo(0)
        }
    }

    LaunchedEffect(scrollResetKey, autoScrollToBottomDelayMs, scrollState.maxValue) {
        val delayMs = autoScrollToBottomDelayMs ?: return@LaunchedEffect
        if (scrollState.maxValue > 0) {
            kotlinx.coroutines.delay(delayMs)
            scrollState.animateScrollTo(
                value = scrollState.maxValue,
                animationSpec = tween(durationMillis = autoScrollToBottomDurationMs)
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        color = LightBlack.copy(alpha = 0.58f),
        shape = OnboardingPanelShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            backgroundContent()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (showProgressTracker) {
                    OnboardingProgressTracker(
                        currentStep = currentStep,
                        totalSteps = totalSteps,
                        title = title
                    )
                }
                content()
                Spacer(modifier = Modifier.height(24.dp))
            }

            OnboardingScrollHint(
                scrollState = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 14.dp, bottom = 56.dp)
            )
        }
    }
}

@Composable
private fun DreamMathHeroStatCard(
    targetValue: Int,
    modifier: Modifier = Modifier,
) {
    var started by remember { mutableStateOf(false) }
    val animatedValue by animateFloatAsState(
        targetValue = if (started) targetValue.toFloat() else 0f,
        animationSpec = tween(durationMillis = 2300, easing = FastOutSlowInEasing),
        label = "dream_math_count"
    )
    val glow by rememberInfiniteTransition(label = "dream_math_hero_glow").animateFloat(
        initialValue = 0.32f,
        targetValue = 0.54f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dream_math_hero_glow_alpha"
    )

    LaunchedEffect(Unit) {
        started = true
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.06f),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(218.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0x55FFD29E).copy(alpha = glow),
                            Color(0x33FF8FBD).copy(alpha = glow * 0.56f),
                            Color(0x222B255F),
                            Color.Transparent
                        ),
                        radius = 620f
                    )
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(70.dp)
                    .offset(x = (-16).dp, y = (-14).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(86.dp)
                    .offset(x = 22.dp, y = 22.dp)
                    .clip(CircleShape)
                    .background(Color(0x24B8A4FF))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "3 dreams/night x 365 nights",
                    style = TextStyle(
                        color = Color(0xFFFFDEBF),
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${formatDreamMathNumber(animatedValue.roundToInt())}+",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 56.sp,
                            lineHeight = 60.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "dreams passing through your mind each year",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.055f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Text(
                        text = "Most disappear before you have time to reflect, and the insight can disappear with them.",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.84f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DreamMathMiniStatCard(
    value: String,
    label: String,
    icon: DrawableResource,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp),
        color = Color(0xFF171421).copy(alpha = 0.86f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.11f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = value,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 28.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = label,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.76f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Start
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatDreamMathNumber(value: Int): String {
    val clamped = value.coerceAtLeast(0)
    return if (clamped >= 1000) {
        "${clamped / 1000},${(clamped % 1000).toString().padStart(3, '0')}"
    } else {
        clamped.toString()
    }
}

@Composable
private fun SocialProofStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    GlassInfoCard(modifier = modifier) {
        Text(
            text = value,
            style = TextStyle(
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = label,
            style = bodyStyle(fontSize = 12.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun OnboardingScrollHint(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val shouldShow by remember(scrollState) {
        derivedStateOf {
            scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue - 64
        }
    }
    val floatTransition = rememberInfiniteTransition(label = "scroll_hint_float")
    val floatOffset by floatTransition.animateFloat(
        initialValue = -8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scroll_hint_offset"
    )
    val glowAlpha by floatTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scroll_hint_glow"
    )

    AnimatedVisibility(
        visible = shouldShow,
        enter = fadeIn(animationSpec = tween(260)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .offset(y = floatOffset.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0x99FFE0B8).copy(alpha = glowAlpha),
                                Color(0x665F79FF).copy(alpha = glowAlpha * 0.9f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable {
                        coroutineScope.launch {
                            scrollState.animateScrollBy(scrollState.maxValue.toFloat())
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Scroll",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.86f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Scroll down",
                        tint = Color(0xFFFFE1C1),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OutcomeHero(
    startAnimation: Boolean,
    onHeroCenterChanged: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val revealProgress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 280f),
        label = "outcome_hero_reveal"
    )

    Box(
        modifier = modifier
            .height(326.dp)
            .onGloballyPositioned { coordinates ->
                onHeroCenterChanged(coordinates.boundsInRoot().center)
            }
            .graphicsLayer(
                alpha = revealProgress,
                scaleX = 0.10f + (0.90f * revealProgress),
                scaleY = 0.10f + (0.90f * revealProgress)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .padding(horizontal = 4.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0x33FFD8B5),
                            Color(0x22BAABFF),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0xEE1C1232),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp)
            ) {
                Text(
                    text = "Welcome",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }

            Image(
                painter = painterResource(Res.drawable.onboarding_mascot_bubbly_welcome),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(246.dp)
                    .align(Alignment.Center)
                    .padding(top = 32.dp)
            )
        }
    }
}

@Composable
internal fun DreamHero(
    drawableRes: DrawableResource,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "dream_hero_float")
    val floatOffset by transition.animateFloat(
        initialValue = 10f,
        targetValue = -2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dream_hero_float_offset"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .padding(horizontal = 4.dp)
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0x33FFD8B5),
                        Color(0x22BAABFF),
                        Color.Transparent
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = floatOffset.dp),
            contentScale = contentScale
        )
    }
}

@Composable
private fun OnboardingChoiceCard(
    title: String,
    description: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) Color(0x24FFC39A) else Color.White.copy(alpha = 0.06f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            if (selected) Color(0x88FFDAB8) else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroIconBadge(
                icon = icon,
                selected = selected
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = description,
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.74f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFD7B6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
internal fun HeroIconBadge(
    icon: DrawableResource,
    selected: Boolean,
) {
    Box(
        modifier = Modifier
            .size(78.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        if (selected) Color(0x66FFD0A9) else Color(0x44FFD0A9),
                        Color(0x22B59DFF),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.White.copy(alpha = 0.26f) else Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(22.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun OnboardingChipCard(
    text: String,
    icon: DrawableResource,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) Color(0x22FFC39A) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            if (selected) Color(0x88FFDAB8) else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun SnapshotInsightCard(
    card: SnapshotCardModel,
    animationDelay: Int,
) {
    var animateBar by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animateBar) card.progress else 0f,
        animationSpec = tween(durationMillis = 620, delayMillis = animationDelay, easing = FastOutSlowInEasing),
        label = "snapshot_progress"
    )

    LaunchedEffect(Unit) {
        animateBar = true
    }

    GlassInfoCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroIconBadge(icon = card.icon, selected = true)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = card.title,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = card.body,
                    style = bodyStyle(fontSize = 14.sp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFFC08D),
                                Color(0xFFFF99C7),
                                Color(0xFFB8A4FF)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun RoadmapMilestoneCard(
    milestone: RoadmapMilestoneModel,
    showConnector: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0x55FFD4AC),
                                Color(0x22BBA8FF),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(milestone.icon),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }

            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(52.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0x66FFC9A2),
                                    Color(0x33BAA7FF),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(999.dp)
                        )
                )
            }
        }

        GlassInfoCard(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = milestone.label,
                style = TextStyle(
                    color = Color(0xFFFFDEBF),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = milestone.title,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = milestone.body,
                style = bodyStyle(fontSize = 14.sp)
            )
        }
    }
}

@Composable
internal fun GlassInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 17.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
            content = content
        )
    }
}

@Composable
internal fun SummaryBullet(
    icon: DrawableResource,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = text,
            style = bodyStyle(fontSize = 14.sp)
        )
    }
}

@Composable
private fun SmallIconCluster(
    icons: List<DrawableResource>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icons.forEachIndexed { index, icon ->
            Box(
                modifier = Modifier
                        .size(if (index == 1) 84.dp else 72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0x55FFD4AC),
                                Color(0x22BBA8FF),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
internal fun OnboardingSecondaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFDFC6),
) {
    Text(
        text = text,
        modifier = modifier.clickable(onClick = onClick),
        style = TextStyle(
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    )
}

internal fun headerStyle(): TextStyle = TextStyle(
    color = Color.White,
    fontSize = 29.sp,
    lineHeight = 35.sp,
    fontWeight = FontWeight.Bold
)

internal fun bodyStyle(fontSize: androidx.compose.ui.unit.TextUnit = 14.sp): TextStyle = TextStyle(
    color = Color.White.copy(alpha = 0.78f),
    fontSize = fontSize,
    lineHeight = if (fontSize.value >= 14f) 23.sp else 20.sp
)
