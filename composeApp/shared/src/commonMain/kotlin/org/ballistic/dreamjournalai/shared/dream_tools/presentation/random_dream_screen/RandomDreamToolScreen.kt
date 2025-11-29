package org.ballistic.dreamjournalai.shared.dream_tools.presentation.random_dream_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.baseline_casino_24
import kotlinx.coroutines.delay
import org.ballistic.dreamjournalai.shared.BottomNavigationController
import org.ballistic.dreamjournalai.shared.BottomNavigationEvent
import org.ballistic.dreamjournalai.shared.core.components.TypewriterText
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.core.util.BackHandler
import org.ballistic.dreamjournalai.shared.dream_tools.domain.DreamTools
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.RandomToolEvent
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.DreamToolButton
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.DreamToolScreenWithNavigateUpTopBar
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalSharedTransitionApi::class, InternalResourceApi::class)
@Composable
fun SharedTransitionScope.RandomDreamToolScreen(
    randomDreamToolScreenState: RandomDreamToolScreenState,
    imageID: DrawableResource,
    imagePath: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomPadding: Dp,
    onEvent: (RandomToolEvent) -> Unit,
    onNavigateToDream: (dreamID: String?, backgroundID: Int) -> Unit,
    navigateUp: () -> Unit
) {

    LaunchedEffect(Unit) {
        BottomNavigationController.sendEvent(BottomNavigationEvent.SetVisibility(true))
        onEvent(RandomToolEvent.GetDreams)
    }

    var isAnimationFinished by remember { mutableStateOf(false) }
    var isGlowVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isAnimationFinished) {
        if (isAnimationFinished) {
            delay(1000) // Wait for button expansion
            isGlowVisible = true
        }
    }

    BackHandler(true) {
        navigateUp()
    }

    // In your composable function
    LaunchedEffect(key1 = randomDreamToolScreenState) {
        snapshotFlow { randomDreamToolScreenState.randomDream }
            .collect { randomDream ->
                randomDream?.let { dream ->
                    onNavigateToDream(dream.id, dream.backgroundImage)
                }
            }
    }

    Scaffold(
        topBar = {
            DreamToolScreenWithNavigateUpTopBar(
                title = "Random Dream",
                navigateUp = navigateUp,
                onEvent = { onEvent(RandomToolEvent.TriggerVibration) }
            )
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .dynamicBottomNavigationPadding()
                .padding(top = innerPadding.calculateTopPadding(), bottom = bottomPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Image(
                    painter = painterResource(imageID),
                    contentDescription = "Random Dream",
                    modifier = Modifier
                        .aspectRatio(16 / 9f)
                        .fillMaxWidth()
                        .sharedElement(
                            rememberSharedContentState(key = "image/$imagePath"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(500)
                            }
                        )
                        .clip(RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)),
                    contentScale = ContentScale.Crop,
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = LightBlack.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                        )
                        .animateContentSize()
                ) {
                    Text(
                        text = DreamTools.RandomDreamPicker.title,
                        modifier = Modifier
                            .fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 0.dp),
                        color = Color.White,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TypewriterText(
                        text = "Reading a random dream is important because it sharpens your ability to " +
                                "recall dreams and reveals underlying patterns, crucial for insightful " +
                                "dream analysis and mastering lucid dreaming.",
                        modifier = Modifier
                            .fillMaxWidth().padding(16.dp, 0.dp, 16.dp, 0.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodyMedium,
                        delay = 550,
                        onAnimationComplete = { isAnimationFinished = true }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val buttonWidth by animateFloatAsState(
                        targetValue = if (isAnimationFinished) 1f else 0f,
                        animationSpec = tween(1000)
                    )

                    if (isAnimationFinished) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DreamToolButton(
                            text = "Random Dream",
                            icon = Res.drawable.baseline_casino_24,
                            onClick = { onEvent(RandomToolEvent.GetRandomDream) },
                            modifier = Modifier.fillMaxWidth(),
                            buttonModifier = Modifier.fillMaxWidth(buttonWidth).padding(horizontal = 16.dp),
                            isGlowVisible = isGlowVisible
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
