package org.ballistic.dreamjournalai.shared.dream_main.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.navigation.BottomNavigationRoutes
import org.ballistic.dreamjournalai.shared.navigation.Route
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White

@Composable
fun BottomNavigation(
    navController: NavController,
    isNavigationEnabled: Boolean,
    onMainEvent: (MainScreenEvent) -> Unit,
    modifier: Modifier
) {
    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = Color.Black,
        modifier = modifier
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val infiniteTransition = rememberInfiniteTransition(label = "")
        BottomNavigationRoutes.entries.forEachIndexed { index, item ->
            val isSelected by remember(currentRoute) {
                mutableStateOf(
                    derivedStateOf { currentRoute == item.route::class.qualifiedName }
                )
            }

            // Setup for on-click and selection-based scaling
            val targetScale = if (isSelected.value) 1.25f else 1.1f
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

            NavigationBarItem(
                icon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                val finalScale = if (isSelected.value) floatingScale * scale.value else scale.value
                                scaleX = finalScale
                                scaleY = finalScale
                                rotationZ = if (isSelected.value) rotation else 0f
                            }
                        ) {
                            // Gold highlight icon (bottom layer)
                            if (isSelected.value) {
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
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer(alpha = 0.99f)
                                    .drawWithCache {
                                        onDrawWithContent {
                                            drawContent()
                                            if (isSelected.value) {
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
                                tint = if (isSelected.value) Color.Unspecified else Color.LightGray
                            )
                        }

                        AnimatedVisibility(
                            visible = isSelected.value,
                            modifier = Modifier.padding(start = 4.dp),
                        ) {
                            Text(
                                text = item.title!!,
                                color = White,
                                style = typography.titleSmall,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                },
                selected = isSelected.value,
                onClick = {
                    onMainEvent(MainScreenEvent.TriggerVibration)
                    if (currentRoute != item.route.toString()) {
                        navController.navigate(item.route) {
                            // Ensure we pop to a real destination ID, not a label
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = LightBlack.copy(alpha = 0.2f),
                ),
                alwaysShowLabel = true,
                enabled = isNavigationEnabled
            )
        }
    }
}
