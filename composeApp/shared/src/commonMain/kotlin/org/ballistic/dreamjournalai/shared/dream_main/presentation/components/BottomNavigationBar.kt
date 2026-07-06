package org.ballistic.dreamjournalai.shared.dream_main.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.navigation.BottomNavigationRoutes
import org.ballistic.dreamjournalai.shared.navigation.Route
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavigation(
    selectedRoute: BottomNavigationRoutes?,
    isNavigationEnabled: Boolean,
    onMainEvent: (MainScreenEvent) -> Unit,
    onNavigate: (Route) -> Unit,
    modifier: Modifier
) {
    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = Color.Black,
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BottomNavigationRoutes.entries.forEachIndexed { index, item ->
                BottomNavigationItem(
                    item = item,
                    isSelected = item == selectedRoute,
                    isNavigationEnabled = isNavigationEnabled,
                    infiniteTransition = infiniteTransition,
                    onMainEvent = onMainEvent,
                    onNavigate = onNavigate,
                    modifier = Modifier.weight(1f)
                )

                if (index == 1) {
                    Spacer(modifier = Modifier.width(64.dp))
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationItem(
    item: BottomNavigationRoutes,
    isSelected: Boolean,
    isNavigationEnabled: Boolean,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    onMainEvent: (MainScreenEvent) -> Unit,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
            // Setup for on-click and selection-based scaling
            val targetScale = if (isSelected) 1.25f else 1.1f
            val scale = remember { Animatable(targetScale) }

            // Continuous floating effect setup
            val floatingScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = ""
            )
            val rotation by infiniteTransition.animateFloat(
                initialValue = -2f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = ""
            )

            // Apply immediate scale change on selection
            LaunchedEffect(isSelected) {
                scale.animateTo(
                    targetValue = targetScale,
                    animationSpec = tween(durationMillis = 400)
                )
            }

    Surface(
        onClick = {
            onMainEvent(MainScreenEvent.TriggerVibration)
            if (!isSelected) {
                onNavigate(item.route)
            }
        },
        enabled = isNavigationEnabled,
        color = if (isSelected) LightBlack.copy(alpha = 0.20f) else Color.Transparent,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .height(62.dp)
            .padding(horizontal = 2.dp),
    ) {
                    Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 7.dp, bottom = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                val finalScale = if (isSelected) floatingScale * scale.value else scale.value
                                scaleX = finalScale
                                scaleY = finalScale
                                rotationZ = if (isSelected) rotation else 0f
                            }
                        ) {
                            // Gold highlight icon (bottom layer)
                            if (isSelected) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null, // decorative
                                    modifier = Modifier
                                        .offset(y = 1.5.dp, x = (-.8).dp)
                                        .size(24.dp),
                                    tint = Color(0xFFFAB4A6) // Soft Peachy-Pink Highlight
                                )
                            }

                            // Main icon (top layer)
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title?.let { stringResource(it) }, // Resolve StringResource here
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer(alpha = 0.99f)
                                    .drawWithCache {
                                        onDrawWithContent {
                                            drawContent()
                                            if (isSelected) {
                                                drawRect(
                                                    brush = Brush.verticalGradient(
                                                        listOf(
                                                            Color(0xFFF48FB1), // Softer Pink
                                                            Color(0xFFF06292)  // Soft Pink
                                                        )
                                                    ),
                                                    blendMode = BlendMode.SrcIn
                                                )
                                            }
                                        }
                                    },
                                tint = if (isSelected) Color.Unspecified else Color.LightGray
                            )
                        }

                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(animationSpec = tween(180)) + expandVertically(
                                animationSpec = tween(220),
                                expandFrom = Alignment.Top
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(160),
                                shrinkTowards = Alignment.Top
                            ) + fadeOut(animationSpec = tween(120)),
                            modifier = Modifier.padding(top = 3.dp),
                        ) {
                            item.title?.let {
                                Text(
                                    text = stringResource(it), // Resolve StringResource here
                                    color = White,
                                    style = typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        lineHeight = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
    }
}
