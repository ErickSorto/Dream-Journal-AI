package org.ballistic.dreamjournalai.shared.dream_lessons.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material.RichText
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_all
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_bookmark_content_description
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_completed
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_detail_placeholder
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_detail_placeholder_body
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_facts
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_min_read
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_more_lessons
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_past_lessons
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_premium
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_see_all
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_show_less
import dreamjournalai.composeapp.shared.generated.resources.daily_lessons_tonights_lesson
import dreamjournalai.composeapp.shared.generated.resources.lucid
import dreamjournalai.composeapp.shared.generated.resources.menu
import dreamjournalai.composeapp.shared.generated.resources.nightmare
import dreamjournalai.composeapp.shared.generated.resources.symbols
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.DrawerCommand
import org.ballistic.dreamjournalai.shared.DrawerController
import org.ballistic.dreamjournalai.shared.core.components.ActionBottomSheet
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.core.platform.isDebugBuild
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DAILY_LESSON_ACCESS_PREMIUM
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DAILY_LESSON_IMAGE_REGENERATION_FAILED
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DAILY_LESSON_IMAGE_REGENERATION_QUEUED
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DAILY_LESSON_IMAGE_REGENERATION_RUNNING
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLesson
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuestion
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonRegenerateSection
import org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonResearchSource
import org.ballistic.dreamjournalai.shared.dream_lessons.presentation.viewmodel.DailyLessonEvent
import org.ballistic.dreamjournalai.shared.dream_lessons.presentation.viewmodel.DailyLessonsState
import org.ballistic.dreamjournalai.shared.dream_lessons.presentation.viewmodel.DailyLessonsViewModel
import org.ballistic.dreamjournalai.shared.dream_lessons.presentation.viewmodel.LessonFlowStage
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val MagentaGlow = Color(0xFFFF4DBC)
private val Gold = Color(0xFFFFC46A)
private val CardStroke = Color(0xFFFF5FD2).copy(alpha = 0.22f)
private val LessonCardColor = Color(0xFF281238).copy(alpha = 0.90f)
private val FeaturedCardColor = Color(0xFF321648).copy(alpha = 0.92f)

enum class DailyLessonAccess {
    Free,
    Premium
}

enum class DailyLessonSection {
    Today,
    ThisWeek,
    Past
}

data class DailyLessonUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val readMinutes: Int,
    val access: DailyLessonAccess,
    val section: DailyLessonSection,
    val imageUrl: String = "",
    val createdDateIso: String,
    val topic: String = "",
    val summary: String = "",
    val contentMarkdown: String = "",
    val whatYoullLearn: List<String> = emptyList(),
    val quote: String = "",
    val questions: List<DailyLessonQuestion> = emptyList(),
    val researchSources: List<DailyLessonResearchSource> = emptyList(),
    val selectedAnswers: Map<String, String> = emptyMap(),
    val dreamTokenAward: Int = 0,
    val started: Boolean = false,
    val completed: Boolean = false,
    val bookmarked: Boolean = false,
    val completedAtMillis: Long = 0,
    val updatedAtMillis: Long = 0,
    val adminImageRegenerationStatus: String = "",
    val adminImageRegenerationJobId: String = "",
    val adminImageRegenerationErrorMessage: String = "",
)

private data class DailyLessonFilter(
    val title: String,
    val category: String? = null,
    val premiumOnly: Boolean = false,
    val bookmarkedOnly: Boolean = false,
    val completedOnly: Boolean = false,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DailyLessonsScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomPaddingValue: Dp,
    isPremiumMember: Boolean,
    onMainEvent: (MainScreenEvent) -> Unit,
    onUpgrade: () -> Unit,
    onNavigateToLesson: (String, String) -> Unit,
    viewModel: DailyLessonsViewModel = koinViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val isDebug = remember { isDebugBuild() }
    val allLessons = remember(state.lessons, isDebug) {
        when {
            state.lessons.isNotEmpty() -> state.lessons.toLessonUiModels()
            isDebug -> starterDailyLessonUiModels()
            else -> emptyList()
        }
    }
    val lessons = remember(allLessons) {
        allLessons.filter { it.section != DailyLessonSection.Past }
    }
    val pastLessons = remember(allLessons) {
        allLessons
            .filter { it.section == DailyLessonSection.Past }
            .sortedByDescending { it.lessonActivityMillis() }
    }
    val filters = rememberDailyLessonFilters(allLessons)
    var selectedFilter by remember { mutableStateOf(filters.first()) }
    var showAllMoreLessons by remember { mutableStateOf(false) }
    var showAllPastLessons by remember { mutableStateOf(false) }
    var bookmarkPromptLesson by remember { mutableStateOf<DailyLessonUiModel?>(null) }
    val todayLesson = lessons.firstOrNull { it.section == DailyLessonSection.Today }
        ?: lessons.firstOrNull()
    val filteredLessons = lessons
        .filter { lesson ->
            when {
                selectedFilter.bookmarkedOnly -> lesson.bookmarked && lesson.id != todayLesson?.id
                selectedFilter.completedOnly -> false
                lesson.section != DailyLessonSection.ThisWeek -> false
                selectedFilter.premiumOnly -> lesson.access == DailyLessonAccess.Premium
                selectedFilter.category != null -> lesson.category == selectedFilter.category
                else -> true
            }
        }
    val visibleLessons = remember(filteredLessons, showAllMoreLessons) {
        if (showAllMoreLessons) filteredLessons else filteredLessons.take(5)
    }
    val filteredPastLessons = remember(allLessons, pastLessons, selectedFilter) {
        when {
            selectedFilter.completedOnly -> allLessons
                .filter { lesson -> lesson.completed }
                .distinctBy { lesson -> lesson.id }
                .sortedByDescending { lesson -> lesson.lessonActivityMillis() }
            selectedFilter.bookmarkedOnly -> pastLessons.filter { lesson -> lesson.bookmarked }
            else -> pastLessons
        }
    }
    val visiblePastLessons = remember(filteredPastLessons, showAllPastLessons) {
        if (showAllPastLessons) filteredPastLessons else filteredPastLessons.take(4)
    }

    LaunchedEffect(filters) {
        if (selectedFilter !in filters) {
            selectedFilter = filters.first()
        }
    }

    LaunchedEffect(selectedFilter) {
        showAllMoreLessons = false
        showAllPastLessons = false
    }

    LaunchedEffect(Unit) {
        onMainEvent(MainScreenEvent.SetTopBarState(false))
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(true))
    }

    bookmarkPromptLesson?.let { promptLesson ->
        BookmarkLessonBottomSheet(
            lesson = promptLesson,
            onConfirm = {
                onMainEvent(MainScreenEvent.TriggerVibration)
                viewModel.onEvent(DailyLessonEvent.ToggleBookmark(promptLesson.toDomainLesson()))
                bookmarkPromptLesson = null
            },
            onDismiss = { bookmarkPromptLesson = null }
        )
    }

    Scaffold(
        topBar = {
            DailyLessonsTopBar(
                title = stringResource(Res.string.daily_lessons),
                onMenuClick = {
                    onMainEvent(MainScreenEvent.TriggerVibration)
                }
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = bottomPaddingValue,
                    )
                    .dynamicBottomNavigationPadding(),
                contentPadding = PaddingValues(top = 10.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(start = 18.dp)
                    ) {
                        DailyLessonFilterRow(
                            filters = filters,
                            selectedFilter = selectedFilter,
                            onSelectFilter = { selectedFilter = it }
                        )
                    }
                }

                if (state.message != null || state.error != null) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                            LessonStatusBanner(
                                text = state.message ?: state.error.orEmpty(),
                                isError = state.error != null
                            )
                        }
                    }
                }

                if (isDebug) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                            DebugLessonGeneratorCard(
                                isGenerating = state.isGeneratingDebugLesson,
                                onGenerateClick = {
                                    viewModel.onEvent(DailyLessonEvent.GenerateDebugLesson)
                                }
                            )
                        }
                    }
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                        if (todayLesson == null) {
                            EmptyLessonsCard()
                        } else {
                            FeaturedLessonCard(
                                lesson = todayLesson,
                                isPremiumLocked = todayLesson.isLocked(isPremiumMember) && !state.isLessonAdmin,
                                animatedVisibilityScope = animatedVisibilityScope,
                                onClick = { onNavigateToLesson(todayLesson.id, todayLesson.imageUrl) },
                                onBookmarkClick = {
                                    viewModel.onEvent(DailyLessonEvent.ToggleBookmark(todayLesson.toDomainLesson()))
                                },
                                onBookmarkLongPress = {
                                    onMainEvent(MainScreenEvent.TriggerVibration)
                                    bookmarkPromptLesson = todayLesson
                                }
                            )
                        }
                    }
                }

                if (visibleLessons.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                            SectionHeader(
                                title = stringResource(Res.string.daily_lessons_more_lessons),
                                action = if (filteredLessons.size > visibleLessons.size) {
                                    stringResource(Res.string.daily_lessons_see_all)
                                } else if (showAllMoreLessons && filteredLessons.size > 5) {
                                    stringResource(Res.string.daily_lessons_show_less)
                                } else {
                                    null
                                },
                                onActionClick = {
                                    showAllMoreLessons = !showAllMoreLessons
                                }
                            )
                        }
                    }
                }

                items(visibleLessons, key = { "more-${it.id}" }) { lesson ->
                    Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                        WeeklyLessonRow(
                            lesson = lesson,
                            isPremiumLocked = lesson.isLocked(isPremiumMember) && !state.isLessonAdmin,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onClick = { onNavigateToLesson(lesson.id, lesson.imageUrl) },
                            onBookmarkClick = {
                                viewModel.onEvent(DailyLessonEvent.ToggleBookmark(lesson.toDomainLesson()))
                            },
                            onBookmarkLongPress = {
                                onMainEvent(MainScreenEvent.TriggerVibration)
                                bookmarkPromptLesson = lesson
                            }
                        )
                    }
                }

                if (visiblePastLessons.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                            SectionHeader(
                                title = stringResource(Res.string.daily_lessons_past_lessons),
                                action = if (filteredPastLessons.size > visiblePastLessons.size) {
                                    stringResource(Res.string.daily_lessons_see_all)
                                } else if (showAllPastLessons && filteredPastLessons.size > 4) {
                                    stringResource(Res.string.daily_lessons_show_less)
                                } else {
                                    null
                                },
                                onActionClick = {
                                    showAllPastLessons = !showAllPastLessons
                                }
                            )
                        }
                    }
                }

                items(visiblePastLessons, key = { "past-${it.id}" }) { lesson ->
                    Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                        PastLessonRow(
                            lesson = lesson,
                            isPremiumLocked = lesson.isLocked(isPremiumMember) && !state.isLessonAdmin,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onClick = { onNavigateToLesson(lesson.id, lesson.imageUrl) },
                            onBookmarkLongPress = {
                                onMainEvent(MainScreenEvent.TriggerVibration)
                                bookmarkPromptLesson = lesson
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DailyLessonDetailScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    lessonId: String,
    initialImageUrl: String = "",
    bottomPaddingValue: Dp,
    backgroundResource: DrawableResource,
    isPremiumMember: Boolean,
    onMainEvent: (MainScreenEvent) -> Unit,
    onUpgrade: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: DailyLessonsViewModel = koinViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val isDebug = remember { isDebugBuild() }
    val lesson = remember(lessonId, initialImageUrl, state.lessons, isDebug) {
        val generated = when {
            state.lessons.isNotEmpty() -> state.lessons.toLessonUiModels()
            isDebug -> starterDailyLessonUiModels()
            else -> emptyList()
        }
        val loadedLesson = generated.firstOrNull { it.id == lessonId }
            ?: missingDailyLessonUiModel(lessonId)
        if (loadedLesson.imageUrl.isBlank() && initialImageUrl.isNotBlank()) {
            loadedLesson.copy(imageUrl = initialImageUrl)
        } else {
            loadedLesson
        }
    }
    val isLocked = lesson.isLocked(isPremiumMember) && !state.isLessonAdmin
    val lessonDomain = lesson.toDomainLesson()
    val selectedAnswers = state.answersFor(lessonDomain)
    val flowStage = state.stageFor(lessonDomain)
    val lastQuestionIndex = (lesson.questions.size - 1).coerceAtLeast(0)
    val currentQuizIndex = state.currentQuizIndex(lesson.id).coerceIn(0, lastQuestionIndex)
    val currentQuestion = lesson.questions.getOrNull(currentQuizIndex)
    val selectedCurrentAnswer = currentQuestion?.let { question -> selectedAnswers[question.id] }
    val hasSelectedCurrentAnswer = selectedCurrentAnswer.isNullOrBlank().not()
    val isCurrentAnswerCorrect = currentQuestion != null &&
        selectedCurrentAnswer == currentQuestion.correctOptionId
    val isCompleted = lesson.completed || flowStage == LessonFlowStage.Completed
    val isCompleting = state.completingLessonId == lesson.id
    val imageRegenerationStatus = lesson.adminImageRegenerationStatus.trim().lowercase()
    val isImageRegenerationQueued = imageRegenerationStatus == DAILY_LESSON_IMAGE_REGENERATION_QUEUED ||
        lesson.id in state.pendingImageRegenerationLessonIds
    val isImageRegenerationRunning = imageRegenerationStatus == DAILY_LESSON_IMAGE_REGENERATION_RUNNING
    val isImageRegenerationActive = isImageRegenerationQueued || isImageRegenerationRunning
    val imageRegenerationStatusText = when {
        isImageRegenerationRunning -> "Generating a new lesson image. This can take a little bit."
        isImageRegenerationQueued -> "New lesson image is queued. It will update automatically when ready."
        imageRegenerationStatus == DAILY_LESSON_IMAGE_REGENERATION_FAILED -> {
            val message = lesson.adminImageRegenerationErrorMessage.ifBlank {
                "Try again with a simpler image note."
            }
            "Image regeneration failed. $message"
        }
        else -> null
    }
    val imageRegenerationLoadingText = if (isImageRegenerationRunning) {
        "Generating image..."
    } else {
        "Queued..."
    }
    val isRegeneratingContent = state.regeneratingLessonId == lesson.id &&
        state.regeneratingSection == DailyLessonRegenerateSection.Content
    val isRegeneratingImage = (state.regeneratingLessonId == lesson.id &&
        state.regeneratingSection == DailyLessonRegenerateSection.Image) ||
        isImageRegenerationActive
    val isRegeneratingLesson = state.regeneratingLessonId == lesson.id

    LaunchedEffect(Unit) {
        onMainEvent(MainScreenEvent.SetTopBarState(false))
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(false))
    }

    DisposableEffect(Unit) {
        onDispose {
            onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(true))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val detailContentSidePadding = 26.dp
        val startLessonButtonSidePadding = 20.dp

        Image(
            painter = painterResource(backgroundResource),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(24.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
        )

        Scaffold(
            topBar = {
                DailyLessonDetailTopBar(
                    bookmarked = lesson.bookmarked,
                    onNavigateUp = onNavigateUp,
                    onBookmarkClick = {
                        onMainEvent(MainScreenEvent.TriggerVibration)
                        viewModel.onEvent(DailyLessonEvent.ToggleBookmark(lesson.toDomainLesson()))
                    }
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            start = startLessonButtonSidePadding,
                            end = startLessonButtonSidePadding,
                            bottom = 12.dp
                        )
                ) {
                    StartLessonButton(
                        text = when {
                            isLocked -> "Upgrade"
                            isCompleted -> "Completed"
                            isCompleting -> "Saving..."
                            flowStage == LessonFlowStage.Preview -> "Start Lesson"
                            flowStage == LessonFlowStage.Reading -> "Start Quiz"
                            flowStage == LessonFlowStage.Quiz && currentQuestion == null -> "Quiz Unavailable"
                            flowStage == LessonFlowStage.Quiz && !hasSelectedCurrentAnswer -> "Choose Answer"
                            flowStage == LessonFlowStage.Quiz && !isCurrentAnswerCorrect -> "Try Again"
                            flowStage == LessonFlowStage.Quiz && currentQuizIndex < lesson.questions.lastIndex -> "Next Question"
                            else -> "Complete Lesson"
                        },
                        enabled = when {
                            isLocked -> true
                            isCompleted || isCompleting -> false
                            flowStage == LessonFlowStage.Quiz -> currentQuestion != null && hasSelectedCurrentAnswer
                            else -> true
                        },
                        showLoading = isCompleting,
                        onClick = {
                            onMainEvent(MainScreenEvent.TriggerVibration)
                            when {
                                isLocked -> onUpgrade()
                                flowStage == LessonFlowStage.Preview -> {
                                    viewModel.onEvent(DailyLessonEvent.StartLesson(lesson.id))
                                }
                                flowStage == LessonFlowStage.Reading -> {
                                    viewModel.onEvent(DailyLessonEvent.StartQuiz(lesson.id))
                                }
                                flowStage == LessonFlowStage.Quiz && currentQuestion != null && !isCurrentAnswerCorrect -> {
                                    viewModel.onEvent(
                                        DailyLessonEvent.RetryQuizQuestion(
                                            lessonId = lesson.id,
                                            questionId = currentQuestion.id,
                                        )
                                    )
                                }
                                flowStage == LessonFlowStage.Quiz && currentQuestion != null &&
                                    currentQuizIndex < lesson.questions.lastIndex -> {
                                    viewModel.onEvent(
                                        DailyLessonEvent.NextQuizQuestion(
                                            lessonId = lesson.id,
                                            questionCount = lesson.questions.size,
                                        )
                                    )
                                }
                                flowStage == LessonFlowStage.Quiz -> {
                                    viewModel.onEvent(
                                        DailyLessonEvent.CompleteLesson(
                                            lesson.toDomainLesson(selectedAnswers)
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding(),
                        start = detailContentSidePadding,
                        end = detailContentSidePadding
                ),
                contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    LessonHeroImageWithSparkles(
                        lesson = lesson,
                        isPremiumLocked = isLocked,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LessonMetaRow(lesson = lesson)
                        Text(
                            text = lesson.title,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 31.sp
                        )
                        Text(
                            text = lesson.subtitle,
                            color = Color.White.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 23.sp
                        )
                    }
                }

                if (state.message != null || state.error != null) {
                    item {
                        LessonStatusBanner(
                            text = state.message ?: state.error.orEmpty(),
                            isError = state.error != null
                        )
                    }
                }

                if (state.isLessonAdmin && lesson.canShowAdminTools()) {
                    item {
                        AdminLessonRegenerationCard(
                            lesson = lesson,
                            isRegeneratingContent = isRegeneratingContent,
                            isRegeneratingImage = isRegeneratingImage,
                            imageStatusText = imageRegenerationStatusText,
                            imageStatusIsError = imageRegenerationStatus == DAILY_LESSON_IMAGE_REGENERATION_FAILED &&
                                !isImageRegenerationActive,
                            imageLoadingText = imageRegenerationLoadingText,
                            enabled = !isRegeneratingLesson && !isImageRegenerationActive,
                            onRegenerate = { section, instructions ->
                                onMainEvent(MainScreenEvent.TriggerVibration)
                                viewModel.onEvent(
                                    DailyLessonEvent.RegenerateLessonSection(
                                        lessonId = lesson.id,
                                        section = section,
                                        instructions = instructions,
                                    )
                                )
                            }
                        )
                    }
                }

                if (isLocked) {
                    item {
                        LessonIntroLearningCard(lesson = lesson)
                    }
                    item {
                        PremiumLockedLessonCard(onUpgrade = onUpgrade)
                    }
                } else {
                    when (flowStage) {
                        LessonFlowStage.Preview -> {
                            item {
                                LessonIntroLearningCard(lesson = lesson)
                            }
                            item {
                                LessonPreviewPromptCard(lesson = lesson)
                            }
                        }
                        LessonFlowStage.Reading -> {
                            item {
                                LessonMarkdownCard(contentMarkdown = lesson.contentMarkdown)
                            }
                            if (lesson.researchSources.isNotEmpty()) {
                                item {
                                    LessonSourcesCard(sources = lesson.researchSources)
                                }
                            }
                        }
                        LessonFlowStage.Quiz -> {
                            currentQuestion?.let { question ->
                                item {
                                    LessonQuizStepCard(
                                        lesson = lesson,
                                        questionIndex = currentQuizIndex,
                                        question = question,
                                        selectedOptionId = selectedCurrentAnswer,
                                        onSelectAnswer = { optionId ->
                                            viewModel.onEvent(
                                                DailyLessonEvent.SelectAnswer(
                                                    lessonId = lesson.id,
                                                    questionId = question.id,
                                                    optionId = optionId
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        LessonFlowStage.Completed -> {
                            item {
                                LessonCompletionCard(lesson = lesson)
                            }
                            item {
                                LessonMarkdownCard(contentMarkdown = lesson.contentMarkdown)
                            }
                            if (lesson.researchSources.isNotEmpty()) {
                                item {
                                    LessonSourcesCard(sources = lesson.researchSources)
                                }
                            }
                        }
                    }

                    if (lesson.completed && lesson.questions.isNotEmpty()) {
                        item {
                            LessonQuizCard(
                                lesson = lesson,
                                selectedAnswers = selectedAnswers,
                                onSelectAnswer = { questionId, optionId ->
                                    viewModel.onEvent(
                                        DailyLessonEvent.SelectAnswer(
                                            lessonId = lesson.id,
                                            questionId = questionId,
                                            optionId = optionId
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyLessonsTopBar(
    title: String,
    onMenuClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    onMenuClick()
                    scope.launch { DrawerController.send(DrawerCommand.Open) }
                }
            ) {
                org.ballistic.dreamjournalai.shared.dream_main.presentation.components.NotificationPermissionMenuIcon(
                    contentDescription = stringResource(Res.string.menu),
                    tint = Color.White
                )
            }
        },
        actions = {
            Spacer(modifier = Modifier.width(48.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = OriginalXmlColors.DarkBlue.copy(alpha = 0.5f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyLessonDetailTopBar(
    bookmarked: Boolean,
    onNavigateUp: () -> Unit,
    onBookmarkClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.daily_lessons),
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onBookmarkClick) {
                Icon(
                    imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = stringResource(Res.string.daily_lessons_bookmark_content_description),
                    tint = if (bookmarked) Gold else Color.White.copy(alpha = 0.84f)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = OriginalXmlColors.DarkBlue.copy(alpha = 0.5f)
        )
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.LessonHeroImageWithSparkles(
    lesson: DailyLessonUiModel,
    isPremiumLocked: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val imageShape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        LessonHeroSparkles(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .aspectRatio(1f)
                .sharedElement(
                    rememberSharedContentState(key = lessonSharedImageKey(lesson.id)),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        tween(500)
                    }
                )
                .shadow(
                    elevation = 18.dp,
                    shape = imageShape,
                    clip = false
                )
        ) {
            LessonThumbnail(
                imageUrl = lesson.imageUrl,
                imageCacheKey = lessonImageMemoryCacheKey(lesson.id, lesson.imageUrl),
                modifier = Modifier.fillMaxSize(),
                shape = imageShape,
                borderColor = Color.White.copy(alpha = 0.20f)
            )
            if (isPremiumLocked) {
                PremiumLockOverlay(
                    shape = imageShape,
                    modifier = Modifier.matchParentSize(),
                    badgeSize = 64.dp,
                    iconSize = 28.dp
                )
            }
        }
    }
}

@Composable
private fun LessonHeroSparkles(
    modifier: Modifier = Modifier,
) {
    val burstProgress = remember { Animatable(0f) }
    val sparkles = remember { lessonHeroSparkleSpecs() }
    val twinkleTransition = rememberInfiniteTransition(label = "lesson-hero-sparkle-twinkle")
    val twinkle by twinkleTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lesson-hero-sparkle-alpha"
    )

    LaunchedEffect(Unit) {
        burstProgress.snapTo(0f)
        burstProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 920, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val progress = burstProgress.value
        sparkles.forEach { sparkle ->
            val localProgress = ((progress - sparkle.delay) / (1f - sparkle.delay))
                .coerceIn(0f, 1f)
            if (localProgress <= 0f) return@forEach

            val eased = 1f - ((1f - localProgress) * (1f - localProgress))
            val center = Offset(
                x = size.width * (0.5f + ((sparkle.finalX - 0.5f) * eased)),
                y = size.height * (0.5f + ((sparkle.finalY - 0.5f) * eased))
            )
            val alpha = (0.30f + (0.52f * twinkle)).coerceIn(0f, 0.82f)
            val radius = sparkle.radius * (0.55f + (0.45f * eased))
            val ray = radius * 2.2f
            val color = sparkle.color.copy(alpha = alpha)

            drawCircle(
                color = color,
                radius = radius,
                center = center
            )
            drawLine(
                color = color.copy(alpha = alpha * 0.72f),
                start = Offset(center.x - ray, center.y),
                end = Offset(center.x + ray, center.y),
                strokeWidth = (radius * 0.42f).coerceAtLeast(1f),
                cap = StrokeCap.Round
            )
            drawLine(
                color = color.copy(alpha = alpha * 0.72f),
                start = Offset(center.x, center.y - ray),
                end = Offset(center.x, center.y + ray),
                strokeWidth = (radius * 0.42f).coerceAtLeast(1f),
                cap = StrokeCap.Round
            )
        }
    }
}

private data class LessonHeroSparkleSpec(
    val finalX: Float,
    val finalY: Float,
    val radius: Float,
    val delay: Float,
    val color: Color,
)

private fun lessonHeroSparkleSpecs(): List<LessonHeroSparkleSpec> = listOf(
    LessonHeroSparkleSpec(0.08f, 0.18f, 3.2f, 0.00f, Color(0xFFFFB7F1)),
    LessonHeroSparkleSpec(0.92f, 0.16f, 3.0f, 0.01f, Color(0xFFFFE5A8)),
    LessonHeroSparkleSpec(0.06f, 0.43f, 2.6f, 0.02f, Color(0xFFA875FF)),
    LessonHeroSparkleSpec(0.94f, 0.48f, 3.4f, 0.03f, Color(0xFFFF74D6)),
    LessonHeroSparkleSpec(0.14f, 0.78f, 2.7f, 0.04f, Color(0xFFFFCF76)),
    LessonHeroSparkleSpec(0.86f, 0.80f, 2.8f, 0.05f, Color(0xFFC78CFF)),
    LessonHeroSparkleSpec(0.24f, 0.06f, 2.2f, 0.06f, Color(0xFFFFFFFF)),
    LessonHeroSparkleSpec(0.76f, 0.06f, 2.1f, 0.07f, Color(0xFFFFB0E8)),
    LessonHeroSparkleSpec(0.02f, 0.30f, 2.0f, 0.08f, Color(0xFFFFD98A)),
    LessonHeroSparkleSpec(0.98f, 0.64f, 2.2f, 0.09f, Color(0xFFFFFFFF)),
    LessonHeroSparkleSpec(0.32f, 0.94f, 1.9f, 0.10f, Color(0xFFFF8BDD)),
    LessonHeroSparkleSpec(0.68f, 0.94f, 1.9f, 0.11f, Color(0xFFB68AFF)),
    LessonHeroSparkleSpec(0.11f, 0.07f, 1.7f, 0.12f, Color(0xFFFFFFFF)),
    LessonHeroSparkleSpec(0.89f, 0.08f, 1.7f, 0.13f, Color(0xFFFFC4F3)),
    LessonHeroSparkleSpec(0.04f, 0.62f, 1.8f, 0.14f, Color(0xFFFFEFBE)),
    LessonHeroSparkleSpec(0.96f, 0.34f, 1.8f, 0.15f, Color(0xFFC99BFF)),
    LessonHeroSparkleSpec(0.18f, 0.91f, 1.6f, 0.16f, Color(0xFFFF75D0)),
    LessonHeroSparkleSpec(0.82f, 0.92f, 1.6f, 0.17f, Color(0xFFFFFFFF)),
    LessonHeroSparkleSpec(0.01f, 0.16f, 1.4f, 0.18f, Color(0xFFFFC767)),
    LessonHeroSparkleSpec(0.99f, 0.82f, 1.4f, 0.19f, Color(0xFFFFA7E8)),
    LessonHeroSparkleSpec(0.30f, 0.02f, 1.4f, 0.20f, Color(0xFFFFFFFF)),
    LessonHeroSparkleSpec(0.70f, 0.02f, 1.4f, 0.21f, Color(0xFFFFDA91)),
    LessonHeroSparkleSpec(0.12f, 0.55f, 1.3f, 0.22f, Color(0xFFD8B1FF)),
    LessonHeroSparkleSpec(0.88f, 0.58f, 1.3f, 0.23f, Color(0xFFFFF4D3)),
    LessonHeroSparkleSpec(0.40f, 0.98f, 1.2f, 0.24f, Color(0xFFFF8FE1)),
    LessonHeroSparkleSpec(0.60f, 0.98f, 1.2f, 0.25f, Color(0xFFFFFFFF)),
)

@Composable
private fun LessonIntroLearningCard(lesson: DailyLessonUiModel) {
    val learningItems = lesson.whatYoullLearn.take(4).ifEmpty {
        listOf(
            "How this dream concept shows up during sleep.",
            "What current research suggests without overclaiming.",
            "A small reflection you can try in your journal.",
            "How to notice the pattern in future dreams.",
        )
    }
    val iconSet = listOf(
        Icons.Filled.Visibility to Color(0xFFD965FF),
        Icons.AutoMirrored.Filled.ShowChart to Color(0xFFFF6E9F),
        Icons.Filled.NightsStay to Color(0xFFC160FF),
        Icons.Filled.Lightbulb to Color(0xFFFFA06A),
    )

    Surface(
        color = Color(0xFF2A123F).copy(alpha = 0.82f),
        border = BorderStroke(1.dp, Color(0xFFFF70D8).copy(alpha = 0.28f)),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = Color(0xFFFF7BD5),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "What you'll learn",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))
            learningItems.forEachIndexed { index, item ->
                val icon = iconSet.getOrElse(index) { iconSet.last() }
                LessonLearningRow(
                    icon = icon.first,
                    iconTint = icon.second,
                    text = item
                )
                if (index != learningItems.lastIndex) {
                    LessonLearningDivider()
                }
            }

            LessonIntroNote(quote = lesson.quote)
        }
    }
}

@Composable
private fun LessonLearningRow(
    icon: ImageVector,
    iconTint: Color,
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(25.dp)
        )
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.88f),
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun LessonLearningDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.10f))
    )
}

@Composable
private fun LessonIntroNote(quote: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2A1045).copy(alpha = 0.66f))
            .border(
                width = 1.dp,
                color = Color(0xFFFF70D8).copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MagentaGlow,
                modifier = Modifier.size(23.dp)
            )
            Text(
                text = quote.ifBlank { "Awareness is the first step to understanding a dream." },
                color = Color(0xFFE56AFF),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun StartLessonButton(
    text: String,
    enabled: Boolean,
    showLoading: Boolean,
    onClick: () -> Unit,
) {
    val buttonShape = RoundedCornerShape(14.dp)
    val alpha = if (enabled) 1f else 0.58f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(buttonShape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFF5D8F),
                        Color(0xFFFF2EAF),
                        Color(0xFFC622E9)
                    )
                )
            )
            .background(Color.Black.copy(alpha = 1f - alpha))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    color = Color.White.copy(alpha = 0.20f)
                ),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun rememberDailyLessonFilters(lessons: List<DailyLessonUiModel>): List<DailyLessonFilter> {
    val allTitle = stringResource(Res.string.daily_lessons_all)
    val premiumTitle = stringResource(Res.string.daily_lessons_premium)
    val completedTitle = stringResource(Res.string.daily_lessons_completed)
    val bookmarkedTitle = "Bookmarked"
    return remember(lessons, allTitle, premiumTitle, completedTitle, bookmarkedTitle) {
        val categoryFilters = lessons
            .filter { !it.completed }
            .map { it.category }
            .filter { it.isNotBlank() }
            .distinct()
            .take(6)
            .map { category ->
                DailyLessonFilter(
                    title = category.toLessonFilterTitle(),
                    category = category
                )
            }
        listOf(DailyLessonFilter(allTitle)) +
            categoryFilters +
            DailyLessonFilter(premiumTitle, premiumOnly = true) +
            DailyLessonFilter(completedTitle, completedOnly = true) +
            DailyLessonFilter(bookmarkedTitle, bookmarkedOnly = true)
    }
}

@Composable
private fun DailyLessonFilterRow(
    filters: List<DailyLessonFilter>,
    selectedFilter: DailyLessonFilter,
    onSelectFilter: (DailyLessonFilter) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(start = 0.dp, end = 18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            val accent = when {
                filter.bookmarkedOnly -> Gold
                filter.completedOnly -> Color(0xFF7BE99C)
                else -> MagentaGlow
            }
            val container by animateColorAsState(
                targetValue = if (isSelected) accent.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.05f),
                label = "lessonChipContainer"
            )
            val border by animateColorAsState(
                targetValue = if (isSelected) accent.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.14f),
                label = "lessonChipBorder"
            )

            Surface(
                onClick = { onSelectFilter(filter) },
                modifier = Modifier
                    .height(40.dp)
                    .widthIn(max = 178.dp),
                color = container,
                contentColor = Color.White,
                shape = CircleShape,
                border = BorderStroke(1.dp, border)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.title,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun String.toLessonFilterTitle(): String {
    return when {
        contains("creativity", ignoreCase = true) &&
            contains("problem", ignoreCase = true) -> "Creativity"
        else -> this
    }
}

@Composable
private fun BookmarkLessonBottomSheet(
    lesson: DailyLessonUiModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val willRemove = lesson.bookmarked
    ActionBottomSheet(
        title = if (willRemove) "Remove bookmark?" else "Bookmark lesson?",
        message = if (willRemove) {
            "Remove \"${lesson.title}\" from your saved lessons."
        } else {
            "Save \"${lesson.title}\" so you can return to it later."
        },
        buttonText = if (willRemove) "Remove Bookmark" else "Bookmark Lesson",
        buttonContainerColor = Gold,
        buttonContentColor = Color(0xFF241407),
        icon = if (willRemove) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
        iconTint = Gold,
        onClick = onConfirm,
        onClickOutside = onDismiss
    )
}

@Composable
private fun SectionHeader(
    title: String,
    action: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (action != null) {
            Text(
                text = action,
                color = Color.White.copy(alpha = 0.56f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = onActionClick != null) {
                        onActionClick?.invoke()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun LessonStatusBanner(
    text: String,
    isError: Boolean,
) {
    val accent = if (isError) Color(0xFFFF7A9A) else Color(0xFF7BE99C)
    Surface(
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.88f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun DebugLessonGeneratorCard(
    isGenerating: Boolean,
    onGenerateClick: () -> Unit,
) {
    Surface(
        onClick = {
            if (!isGenerating) onGenerateClick()
        },
        color = Color(0xFF17304E).copy(alpha = 0.78f),
        border = BorderStroke(1.dp, Color(0xFF8EC8FF).copy(alpha = 0.32f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    color = Color(0xFF8EC8FF),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(23.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF8EC8FF),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (isGenerating) "Generating debug lesson..." else "Generate test lesson",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Creates a user-only debug article with AI research and artwork.",
                    color = Color.White.copy(alpha = 0.66f),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun AdminLessonRegenerationCard(
    lesson: DailyLessonUiModel,
    isRegeneratingContent: Boolean,
    isRegeneratingImage: Boolean,
    imageStatusText: String?,
    imageStatusIsError: Boolean,
    imageLoadingText: String,
    enabled: Boolean,
    onRegenerate: (DailyLessonRegenerateSection, String) -> Unit,
) {
    var contentInstructions by remember(lesson.id) { mutableStateOf("") }
    var imageInstructions by remember(lesson.id) { mutableStateOf("") }

    Surface(
        color = Color(0xFF30210E).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.48f)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(23.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Admin lesson tools",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Regenerate one live section without changing the lesson topic.",
                        color = Color.White.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 17.sp
                    )
                }
            }

            if (!imageStatusText.isNullOrBlank()) {
                AdminImageRegenerationStatusRow(
                    text = imageStatusText,
                    isError = imageStatusIsError
                )
            }

            AdminInstructionField(
                value = contentInstructions,
                onValueChange = { contentInstructions = it },
                label = "Content notes",
                placeholder = "Example: clean up formatting, make it simpler, remove awkward dashes."
            )
            AdminRegenerateButton(
                text = "Regenerate Content",
                isLoading = isRegeneratingContent,
                enabled = enabled,
                onClick = {
                    onRegenerate(DailyLessonRegenerateSection.Content, contentInstructions.trim())
                }
            )

            AdminInstructionField(
                value = imageInstructions,
                onValueChange = { imageInstructions = it },
                label = "Image notes",
                placeholder = "Example: brighter moonlit scene, more peaceful, less abstract."
            )
            AdminRegenerateButton(
                text = "Regenerate Image",
                isLoading = isRegeneratingImage,
                loadingText = imageLoadingText,
                enabled = enabled,
                onClick = {
                    onRegenerate(DailyLessonRegenerateSection.Image, imageInstructions.trim())
                }
            )
        }
    }
}

@Composable
private fun AdminImageRegenerationStatusRow(
    text: String,
    isError: Boolean,
) {
    val accent = if (isError) Color(0xFFFF7A9A) else Color(0xFF8EC8FF)
    Surface(
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.36f)),
        shape = RoundedCornerShape(13.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isError) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                CircularProgressIndicator(
                    color = accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun AdminInstructionField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.72f)
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.38f)
            )
        },
        minLines = 2,
        maxLines = 4,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Gold.copy(alpha = 0.78f),
            unfocusedBorderColor = Gold.copy(alpha = 0.32f),
            cursorColor = Gold,
            focusedContainerColor = Color.Black.copy(alpha = 0.12f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.10f),
        )
    )
}

@Composable
private fun AdminRegenerateButton(
    text: String,
    isLoading: Boolean,
    loadingText: String = "Regenerating...",
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = Color(0xFF241407),
            disabledContainerColor = Gold.copy(alpha = 0.34f),
            disabledContentColor = Color.White.copy(alpha = 0.72f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF241407),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = if (isLoading) loadingText else text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyLessonsCard() {
    Surface(
        color = Color(0xFF241135).copy(alpha = 0.82f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Lesson is being prepared",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your next researched dream lesson will appear here after the daily generator publishes.",
                color = Color.White.copy(alpha = 0.68f),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.FeaturedLessonCard(
    lesson: DailyLessonUiModel,
    isPremiumLocked: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onBookmarkLongPress: () -> Unit,
) {
    LessonCardContainer(
        color = FeaturedCardColor,
        shape = RoundedCornerShape(22.dp),
        onClick = onClick,
        onLongClick = onBookmarkLongPress
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LessonImageWithPremiumLock(
                lesson = lesson,
                isPremiumLocked = isPremiumLocked,
                size = 132.dp,
                shape = RoundedCornerShape(14.dp),
                animatedVisibilityScope = animatedVisibilityScope
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = stringResource(Res.string.daily_lessons_tonights_lesson),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TrailingLessonAction(
                        isPremiumLocked = isPremiumLocked,
                        bookmarked = lesson.bookmarked,
                        onBookmarkClick = onBookmarkClick
                    )
                }
                Text(
                    text = lesson.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = lesson.subtitle,
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                LessonMetaRow(lesson = lesson)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.WeeklyLessonRow(
    lesson: DailyLessonUiModel,
    isPremiumLocked: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onBookmarkLongPress: () -> Unit,
) {
    LessonCardContainer(
        color = LessonCardColor,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        onLongClick = onBookmarkLongPress
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LessonImageWithPremiumLock(
                lesson = lesson,
                isPremiumLocked = isPremiumLocked,
                size = 94.dp,
                shape = RoundedCornerShape(11.dp),
                animatedVisibilityScope = animatedVisibilityScope
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = lesson.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = lesson.subtitle,
                    color = Color.White.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                LessonMetaRow(lesson = lesson)
            }

            TrailingLessonAction(
                isPremiumLocked = isPremiumLocked,
                bookmarked = lesson.bookmarked,
                onBookmarkClick = onBookmarkClick
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.PastLessonRow(
    lesson: DailyLessonUiModel,
    isPremiumLocked: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    onBookmarkLongPress: () -> Unit,
) {
    LessonCardContainer(
        color = LessonCardColor,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        onLongClick = onBookmarkLongPress
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LessonImageWithPremiumLock(
                lesson = lesson,
                isPremiumLocked = isPremiumLocked,
                size = 94.dp,
                shape = RoundedCornerShape(11.dp),
                animatedVisibilityScope = animatedVisibilityScope
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = lesson.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = lesson.subtitle,
                    color = Color.White.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            CompletedBadge()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LessonCardContainer(
    color: Color,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color)
            .border(1.dp, CardStroke, shape)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    color = Color.White.copy(alpha = 0.16f)
                ),
                onLongClick = onLongClick,
                onClick = onClick
            )
    ) {
        content()
    }
}

@Composable
private fun LessonThumbnail(
    imageUrl: String = "",
    imageCacheKey: String? = null,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp),
    borderColor: Color = Color.White.copy(alpha = 0.12f),
) {
    val platformContext = LocalPlatformContext.current
    val imageRequest = remember(platformContext, imageUrl, imageCacheKey) {
        if (imageUrl.isBlank()) {
            null
        } else {
            ImageRequest.Builder(platformContext)
                .data(imageUrl)
                .crossfade(true)
                .apply {
                    if (!imageCacheKey.isNullOrBlank()) {
                        placeholderMemoryCacheKey(imageCacheKey)
                        memoryCacheKey(imageCacheKey)
                    }
                }
                .build()
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
    ) {
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LessonImagePlaceholder()
        }
    }
}

@Composable
private fun LessonImagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF26103A),
                        Color(0xFF111A3D),
                        Color(0xFF3A1434),
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.NightsStay,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.24f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.LessonImageWithPremiumLock(
    lesson: DailyLessonUiModel,
    isPremiumLocked: Boolean,
    size: Dp,
    shape: RoundedCornerShape,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    Box(
        modifier = Modifier
            .size(size)
            .sharedElement(
                rememberSharedContentState(key = lessonSharedImageKey(lesson.id)),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    tween(500)
                }
            )
    ) {
        LessonThumbnail(
            imageUrl = lesson.imageUrl,
            imageCacheKey = lessonImageMemoryCacheKey(lesson.id, lesson.imageUrl),
            modifier = Modifier.fillMaxSize(),
            shape = shape
        )
        if (isPremiumLocked) {
            PremiumLockOverlay(
                shape = shape,
                badgeSize = (size * 0.38f).coerceIn(38.dp, 54.dp),
                iconSize = (size * 0.19f).coerceIn(18.dp, 25.dp),
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
private fun PremiumLockOverlay(
    shape: RoundedCornerShape,
    badgeSize: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(badgeSize + 16.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Gold.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
        )
        Surface(
            modifier = Modifier.size(badgeSize),
            color = Color(0xFF241407).copy(alpha = 0.72f),
            contentColor = Gold,
            shape = CircleShape,
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.52f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

private fun lessonSharedImageKey(lessonId: String): String = "dailyLesson/image/$lessonId"

private fun lessonImageMemoryCacheKey(lessonId: String, imageUrl: String): String? {
    if (lessonId.isBlank() || imageUrl.isBlank()) return null
    return "${lessonSharedImageKey(lessonId)}/${imageUrl.hashCode()}"
}

@Composable
private fun LessonMetaRow(lesson: DailyLessonUiModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryChip(category = lesson.category)
        Text(
            text = stringResource(Res.string.daily_lessons_min_read, lesson.readMinutes),
            color = Color.White.copy(alpha = 0.48f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Composable
private fun CategoryChip(category: String) {
    val accent = categoryAccent(category)
    val fallbackCategory = stringResource(Res.string.daily_lessons_facts)
    val label = category.ifBlank { fallbackCategory }.toLessonFilterTitle()
    Surface(
        color = accent.copy(alpha = 0.16f),
        contentColor = accent,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.widthIn(max = 154.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TrailingLessonAction(
    isPremiumLocked: Boolean,
    bookmarked: Boolean,
    onBookmarkClick: () -> Unit,
) {
    if (isPremiumLocked) {
        PremiumLockBadge(
            size = 34.dp,
            iconSize = 18.dp
        )
    } else {
        BookmarkIcon(
            bookmarked = bookmarked,
            onBookmarkClick = onBookmarkClick
        )
    }
}

@Composable
private fun PremiumLockBadge(
    size: Dp,
    iconSize: Dp,
) {
    Surface(
        modifier = Modifier.size(size),
        color = Gold.copy(alpha = 0.16f),
        contentColor = Gold,
        shape = CircleShape,
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.44f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun CompletedBadge() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF7BE99C),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(Res.string.daily_lessons_completed),
            color = Color(0xFF90EBA9),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}

@Composable
private fun BookmarkIcon(
    bookmarked: Boolean,
    onBookmarkClick: () -> Unit,
) {
    IconButton(
        onClick = onBookmarkClick,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
            contentDescription = stringResource(Res.string.daily_lessons_bookmark_content_description),
            tint = if (bookmarked) Gold else Gold.copy(alpha = 0.88f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun LessonMarkdownCard(contentMarkdown: String) {
    val richTextState = rememberRichTextState()
    val content = contentMarkdown.ifBlank {
        """
        ## Tonight's Idea

        This lesson is being prepared. Check back soon for the latest researched dream concept.
        """.trimIndent()
    }

    LaunchedEffect(content) {
        richTextState.setMarkdown(content)
    }

    Surface(
        color = Color(0xFF241135).copy(alpha = 0.86f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        RichText(
            state = richTextState,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            color = Color.White.copy(alpha = 0.88f),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun PremiumLockedLessonCard(onUpgrade: () -> Unit) {
    Surface(
        onClick = onUpgrade,
        color = Color(0xFF2A123F).copy(alpha = 0.88f),
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.38f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(28.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Premium lesson",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Unlock the full lesson, quiz, and DreamToken reward.",
                    color = Color.White.copy(alpha = 0.70f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun LessonPreviewPromptCard(lesson: DailyLessonUiModel) {
    Surface(
        color = Color(0xFF20102F).copy(alpha = 0.86f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = Color(0xFF8EC8FF),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Lesson path",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LessonPathPill(text = "${lesson.readMinutes} min")
                LessonPathPill(text = "${lesson.questions.size} questions")
                LessonPathPill(
                    text = if (lesson.dreamTokenAward > 0) {
                        "+${lesson.dreamTokenAward} token"
                    } else {
                        "Free"
                    }
                )
            }
            Text(
                text = "A compact reading followed by three checks to help the idea settle before tonight.",
                color = Color.White.copy(alpha = 0.68f),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun LessonPathPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.78f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LessonQuizStepCard(
    lesson: DailyLessonUiModel,
    questionIndex: Int,
    question: DailyLessonQuestion,
    selectedOptionId: String?,
    onSelectAnswer: (String) -> Unit,
) {
    val questionCount = lesson.questions.size.coerceAtLeast(1)
    val selectedCorrect = selectedOptionId == question.correctOptionId

    Surface(
        color = Color(0xFF27113A).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color(0xFFFF70D8).copy(alpha = 0.24f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MagentaGlow,
                    modifier = Modifier.size(25.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Quick check",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Question ${questionIndex + 1} of $questionCount",
                        color = Color.White.copy(alpha = 0.62f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(questionCount) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                if (index <= questionIndex) {
                                    MagentaGlow.copy(alpha = 0.90f)
                                } else {
                                    Color.White.copy(alpha = 0.12f)
                                }
                            )
                    )
                }
            }

            LessonQuestionBlock(
                index = questionIndex,
                question = question,
                selectedOptionId = selectedOptionId,
                showResult = selectedOptionId != null,
                onSelectAnswer = onSelectAnswer
            )

            if (selectedOptionId != null) {
                Text(
                    text = if (selectedCorrect) {
                        "Correct. Continue when you are ready."
                    } else {
                        "Not quite. Use the explanation, then try this one again."
                    },
                    color = if (selectedCorrect) {
                        Color(0xFF7BE99C)
                    } else {
                        Color(0xFFFFB5D5)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun LessonCompletionCard(lesson: DailyLessonUiModel) {
    Surface(
        color = Color(0xFF1D5130).copy(alpha = 0.70f),
        border = BorderStroke(1.dp, Color(0xFF7BE99C).copy(alpha = 0.34f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF7BE99C),
                modifier = Modifier.size(31.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Lesson completed",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (lesson.dreamTokenAward > 0) {
                        "+${lesson.dreamTokenAward} DreamToken earned"
                    } else {
                        "Saved to your completed lessons"
                    },
                    color = Color.White.copy(alpha = 0.76f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun LessonQuizCard(
    lesson: DailyLessonUiModel,
    selectedAnswers: Map<String, String>,
    onSelectAnswer: (questionId: String, optionId: String) -> Unit,
) {
    Surface(
        color = Color(0xFF27113A).copy(alpha = 0.88f),
        border = BorderStroke(1.dp, Color(0xFFFF70D8).copy(alpha = 0.24f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quick check",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            lesson.questions.forEachIndexed { index, question ->
                LessonQuestionBlock(
                    index = index,
                    question = question,
                    selectedOptionId = selectedAnswers[question.id],
                    showResult = lesson.completed,
                    onSelectAnswer = { optionId -> onSelectAnswer(question.id, optionId) }
                )
            }
        }
    }
}

@Composable
private fun LessonQuestionBlock(
    index: Int,
    question: DailyLessonQuestion,
    selectedOptionId: String?,
    showResult: Boolean,
    onSelectAnswer: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "${index + 1}. ${question.prompt}",
            color = Color.White.copy(alpha = 0.92f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 22.sp
        )
        question.options.forEach { option ->
            val selected = selectedOptionId == option.id
            val correct = question.correctOptionId == option.id
            val borderColor = when {
                showResult && correct -> Color(0xFF7BE99C)
                selected -> MagentaGlow
                else -> Color.White.copy(alpha = 0.12f)
            }
            val fillColor = when {
                showResult && correct -> Color(0xFF1D5130).copy(alpha = 0.42f)
                selected -> MagentaGlow.copy(alpha = 0.18f)
                else -> Color.White.copy(alpha = 0.05f)
            }
            Surface(
                onClick = { if (!showResult) onSelectAnswer(option.id) },
                color = fillColor,
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${option.id.uppercase()}. ${option.text}",
                    color = Color.White.copy(alpha = 0.86f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp)
                )
            }
        }
        if (showResult && question.explanation.isNotBlank()) {
            Text(
                text = question.explanation,
                color = Color.White.copy(alpha = 0.64f),
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun LessonSourcesCard(sources: List<DailyLessonResearchSource>) {
    Surface(
        color = Color(0xFF20102F).copy(alpha = 0.86f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null,
                    tint = Color(0xFF8EC8FF),
                    modifier = Modifier.size(21.dp)
                )
                Text(
                    text = "Research notes",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            sources.take(4).forEach { source ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = source.title.ifBlank { source.url },
                        color = Color.White.copy(alpha = 0.86f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (source.summary.isNotBlank()) {
                        Text(
                            text = source.summary,
                            color = Color.White.copy(alpha = 0.62f),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = source.url,
                        color = Color(0xFF8EC8FF).copy(alpha = 0.80f),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun List<DailyLesson>.toLessonUiModels(): List<DailyLessonUiModel> {
    val sortedLessons = sortedByDescending { it.createdDateIso }
    val tonightLessonId = sortedLessons.firstOrNull()?.id
    val activeLessons = sortedLessons.filter { lesson ->
        !lesson.completed || lesson.id == tonightLessonId
    }
    val completedLessons = sortedLessons.filter { it.completed }
    return activeLessons
        .mapIndexed { index, lesson ->
            lesson.toUiModel(
                section = when (index) {
                    0 -> DailyLessonSection.Today
                    in 1..6 -> DailyLessonSection.ThisWeek
                    else -> DailyLessonSection.Past
                }
            )
        } + completedLessons
            .sortedByDescending { it.lessonActivityMillis() }
            .map { lesson -> lesson.toUiModel(section = DailyLessonSection.Past) }
}

private fun DailyLesson.toUiModel(section: DailyLessonSection): DailyLessonUiModel {
    val accessValue = if (access == DAILY_LESSON_ACCESS_PREMIUM || isPremium) {
        DailyLessonAccess.Premium
    } else {
        DailyLessonAccess.Free
    }
    return DailyLessonUiModel(
        id = id,
        title = title.ifBlank { topic.ifBlank { "Daily Dream Lesson" } },
        subtitle = quickDescription.ifBlank { summary },
        category = category.ifBlank { "Dream Science" },
        readMinutes = minutesToRead.coerceIn(1, DAILY_LESSON_TARGET_READ_MINUTES),
        access = accessValue,
        section = section,
        imageUrl = imageUrl,
        createdDateIso = createdDateIso,
        topic = topic,
        summary = summary,
        contentMarkdown = contentMarkdown,
        whatYoullLearn = whatYoullLearn,
        quote = quote,
        questions = questions,
        researchSources = researchSources,
        selectedAnswers = selectedAnswers,
        dreamTokenAward = dreamTokenAward,
        started = started,
        completed = completed,
        bookmarked = bookmarked,
        completedAtMillis = completedAtMillis,
        updatedAtMillis = updatedAtMillis,
        adminImageRegenerationStatus = adminImageRegenerationStatus,
        adminImageRegenerationJobId = adminImageRegenerationJobId,
        adminImageRegenerationErrorMessage = adminImageRegenerationErrorMessage,
    )
}

private fun missingDailyLessonUiModel(lessonId: String): DailyLessonUiModel {
    return DailyLessonUiModel(
        id = lessonId.ifBlank { "missing-lesson" },
        title = "Lesson unavailable",
        subtitle = "This lesson is not available on this device yet.",
        category = "Dream Science",
        readMinutes = 1,
        access = DailyLessonAccess.Free,
        section = DailyLessonSection.Today,
        createdDateIso = "",
        contentMarkdown = """
            ## Lesson unavailable

            This lesson may still be syncing or may have been removed.
        """.trimIndent(),
    )
}

private fun DailyLessonUiModel.toDomainLesson(
    answers: Map<String, String> = selectedAnswers,
): DailyLesson {
    return DailyLesson(
        id = id,
        access = if (access == DailyLessonAccess.Premium) DAILY_LESSON_ACCESS_PREMIUM else "free",
        isPremium = access == DailyLessonAccess.Premium,
        topic = topic,
        title = title,
        quickDescription = subtitle,
        category = category,
        summary = summary,
        contentMarkdown = contentMarkdown,
        imageUrl = imageUrl,
        minutesToRead = readMinutes,
        dreamTokenAward = dreamTokenAward,
        whatYoullLearn = whatYoullLearn,
        quote = quote,
        questions = questions,
        researchSources = researchSources,
        createdDateIso = createdDateIso,
        completed = completed,
        started = started,
        bookmarked = bookmarked,
        completedAtMillis = completedAtMillis,
        updatedAtMillis = updatedAtMillis,
        selectedAnswers = answers,
        adminImageRegenerationStatus = adminImageRegenerationStatus,
        adminImageRegenerationJobId = adminImageRegenerationJobId,
        adminImageRegenerationErrorMessage = adminImageRegenerationErrorMessage,
    )
}

private fun DailyLesson.lessonActivityMillis(): Long {
    return listOf(completedAtMillis, updatedAtMillis)
        .filter { it > 0 }
        .maxOrNull() ?: createdDateIso.toComparableDateLong()
}

private fun DailyLessonUiModel.lessonActivityMillis(): Long {
    return listOf(completedAtMillis, updatedAtMillis)
        .filter { it > 0 }
        .maxOrNull() ?: createdDateIso.toComparableDateLong()
}

private fun String.toComparableDateLong(): Long {
    return filter { it.isDigit() }.toLongOrNull() ?: 0L
}

private fun DailyLessonUiModel.isLocked(isPremiumMember: Boolean): Boolean {
    return access == DailyLessonAccess.Premium && !isPremiumMember
}

private fun DailyLessonUiModel.canShowAdminTools(): Boolean {
    return id.isNotBlank() &&
        !id.startsWith("debug-") &&
        !id.startsWith("starter-") &&
        id != "missing-lesson"
}

private fun categoryAccent(category: String): Color {
    return when {
        category.contains("lucid", ignoreCase = true) -> Color(0xFFC05DFF)
        category.contains("nightmare", ignoreCase = true) -> Color(0xFFFF5B92)
        category.contains("symbol", ignoreCase = true) -> Color(0xFFA96DFF)
        category.contains("sleep", ignoreCase = true) -> Color(0xFF5FA8FF)
        category.contains("emotion", ignoreCase = true) -> Color(0xFFFF8F70)
        category.contains("creativ", ignoreCase = true) -> Color(0xFFFFC46A)
        else -> Color(0xFF7EA7FF)
    }
}

private const val DAILY_LESSON_TARGET_READ_MINUTES = 5

private fun starterQuestions(): List<DailyLessonQuestion> = listOf(
    DailyLessonQuestion(
        id = "q1",
        prompt = "What makes a dream sign useful?",
        options = listOf(
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("a", "It repeats often enough to notice."),
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("b", "It is always scary."),
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("c", "It has one universal meaning."),
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("d", "It only appears in lucid dreams."),
        ),
        correctOptionId = "a",
        explanation = "Personal repetition makes a sign easier to recognize later.",
    ),
    DailyLessonQuestion(
        id = "q2",
        prompt = "Why keep paragraphs short in dream reflection?",
        options = listOf(
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("a", "Short notes are easier to review on a phone."),
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("b", "Long notes erase dream memory."),
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("c", "Dreams have no details."),
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("d", "Only summaries matter."),
        ),
        correctOptionId = "a",
        explanation = "Readable notes make the habit lighter and easier to continue.",
    ),
    DailyLessonQuestion(
        id = "q3",
        prompt = "What is the best first move after waking from a vivid dream?",
        options = listOf(
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("a", "Pause and capture the strongest details."),
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("b", "Open several apps immediately."),
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("c", "Ignore emotional tone."),
            org.ballistic.dreamjournalai.shared.dream_lessons.domain.model.DailyLessonQuizOption("d", "Wait until the evening."),
        ),
        correctOptionId = "a",
        explanation = "Dream recall is often freshest before the day starts competing for attention.",
    ),
)

private fun starterDailyLessonUiModels(): List<DailyLessonUiModel> = listOf(
    DailyLessonUiModel(
        id = "starter-tonights-lesson",
        title = "Recognize Dream Signs",
        subtitle = "Learn how repeated dream details can become cues for awareness while you sleep.",
        category = "Lucid Dreaming",
        readMinutes = 4,
        access = DailyLessonAccess.Free,
        section = DailyLessonSection.Today,
        createdDateIso = "2026-06-23",
        whatYoullLearn = listOf(
            "What dream signs are and why personal repetition matters.",
            "How to review your journal without forcing meaning.",
            "Why daytime noticing can support nighttime awareness.",
            "A tiny reflection habit you can use tomorrow morning.",
        ),
        quote = "The pattern becomes useful when you can meet it gently.",
        contentMarkdown = """
            ## Start With Repetition

            A dream sign is a detail that shows up often enough to become recognizable: a place, person, feeling, impossible object, or repeated situation.

            The goal is not to decode every symbol. It is to notice what your dreaming mind returns to again and again.

            ## Try This Tomorrow

            - Pick one recent dream.
            - Circle one detail that felt unusual or repeated.
            - Ask: "Would I notice this if it happened again?"

            Over time, that small question can become a doorway into clearer awareness.
        """.trimIndent(),
        questions = starterQuestions(),
        bookmarked = true,
    ),
    DailyLessonUiModel(
        id = "starter-reality-checks",
        title = "Reality Checks 101",
        subtitle = "Simple habits that help trigger lucidity without making sleep feel like homework.",
        category = "Lucid Dreaming",
        readMinutes = 4,
        access = DailyLessonAccess.Free,
        section = DailyLessonSection.ThisWeek,
        createdDateIso = "2026-06-22",
    ),
    DailyLessonUiModel(
        id = "starter-nightmares",
        title = "Why Nightmares Happen",
        subtitle = "Understand fear dreams and how to respond with steadier attention.",
        category = "Nightmares",
        readMinutes = 5,
        access = DailyLessonAccess.Premium,
        section = DailyLessonSection.ThisWeek,
        createdDateIso = "2026-06-21",
    ),
    DailyLessonUiModel(
        id = "starter-rem-memory",
        title = "Dream Facts You Should Know",
        subtitle = "A quick tour through REM sleep, memory, and dream recall.",
        category = "REM & Sleep Science",
        readMinutes = 4,
        access = DailyLessonAccess.Free,
        section = DailyLessonSection.ThisWeek,
        createdDateIso = "2026-06-20",
    ),
    DailyLessonUiModel(
        id = "starter-flying-symbols",
        title = "Symbols of Flying",
        subtitle = "Explore flying dreams as movement, freedom, and perspective.",
        category = "Dream Symbols",
        readMinutes = 5,
        access = DailyLessonAccess.Premium,
        section = DailyLessonSection.ThisWeek,
        createdDateIso = "2026-06-19",
    ),
    DailyLessonUiModel(
        id = "starter-sleep-paralysis",
        title = "Sleep Paralysis Guide",
        subtitle = "What the brain may be doing and how to stay calm.",
        category = "Sleep Paralysis",
        readMinutes = 5,
        access = DailyLessonAccess.Premium,
        section = DailyLessonSection.ThisWeek,
        createdDateIso = "2026-06-18",
    ),
    DailyLessonUiModel(
        id = "starter-nightmare-rewrite",
        title = "Rewrite a Bad Dream",
        subtitle = "A gentle rehearsal method for recurring nightmares.",
        category = "Nightmares",
        readMinutes = 4,
        access = DailyLessonAccess.Premium,
        section = DailyLessonSection.ThisWeek,
        createdDateIso = "2026-06-17",
    ),
    DailyLessonUiModel(
        id = "starter-dream-journal-basics",
        title = "Dream Journal Basics",
        subtitle = "Build the habit of remembering your dreams.",
        category = "Dream Recall",
        readMinutes = 4,
        access = DailyLessonAccess.Free,
        section = DailyLessonSection.Past,
        createdDateIso = "2026-06-16",
        completed = true,
    ),
)
