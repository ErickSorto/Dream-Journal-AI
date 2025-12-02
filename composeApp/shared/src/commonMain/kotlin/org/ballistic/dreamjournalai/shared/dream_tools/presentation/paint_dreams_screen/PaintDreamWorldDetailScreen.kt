package org.ballistic.dreamjournalai.shared.dream_tools.presentation.paint_dreams_screen

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
import androidx.compose.foundation.layout.fillMaxHeight
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
import dreamjournalai.composeapp.shared.generated.resources.baseline_brush_24
import dreamjournalai.composeapp.shared.generated.resources.dream_visualization_tool
import dreamjournalai.composeapp.shared.generated.resources.dream_world_detail_description
import dreamjournalai.composeapp.shared.generated.resources.paint_dream_world_button
import dreamjournalai.composeapp.shared.generated.resources.paint_dream_world_screen_title
import kotlinx.coroutines.delay
import org.ballistic.dreamjournalai.shared.BottomNavigationController
import org.ballistic.dreamjournalai.shared.BottomNavigationEvent
import org.ballistic.dreamjournalai.shared.core.components.TypewriterText
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.PaintDreamWorldEvent
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.DreamToolButton
import org.ballistic.dreamjournalai.shared.dream_tools.presentation.components.DreamToolScreenWithNavigateUpTopBar
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PaintDreamWorldDetailScreen(
    imageID: DrawableResource,
    imagePath: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomPadding: Dp,
    onNavigate: () -> Unit,
    onEvent: (PaintDreamWorldEvent) -> Unit,
    navigateUp: () -> Unit
) {
    LaunchedEffect(Unit) {
        BottomNavigationController.sendEvent(BottomNavigationEvent.SetVisibility(true))
    }

    var isAnimationFinished by remember { mutableStateOf(false) }
    var isGlowVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isAnimationFinished) {
        if (isAnimationFinished) {
            delay(1000) // Wait for button expansion
            isGlowVisible = true
        }
    }

    Scaffold(
        topBar = {
            DreamToolScreenWithNavigateUpTopBar(
                title = stringResource(Res.string.paint_dream_world_screen_title),
                navigateUp = navigateUp,
                onEvent = { onEvent(PaintDreamWorldEvent.TriggerVibration) }
            )
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .dynamicBottomNavigationPadding()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = bottomPadding
                )
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dream Visualization Section
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Image(
                    painter = painterResource(imageID),
                    contentDescription = stringResource(Res.string.dream_visualization_tool),
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
                        text = stringResource(org.ballistic.dreamjournalai.shared.navigation.DreamTools.DREAM_WORLD.title),
                        modifier = Modifier
                            .fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 0.dp),
                        color = Color.White,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TypewriterText(
                        text = stringResource(Res.string.dream_world_detail_description),
                        modifier = Modifier
                            .fillMaxWidth().padding(16.dp, 0.dp, 16.dp, 0.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodyMedium,
                        delay = 500,
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
                            text = stringResource(Res.string.paint_dream_world_button),
                            icon = Res.drawable.baseline_brush_24,
                            onClick = {
                                onEvent(PaintDreamWorldEvent.TriggerVibration)
                                onNavigate()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            buttonModifier = Modifier
                                .fillMaxWidth(buttonWidth).padding(horizontal = 16.dp),
                            isGlowVisible = isGlowVisible
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}