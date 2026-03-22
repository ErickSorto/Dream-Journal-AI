package org.ballistic.dreamjournalai.shared.dream_store.presentation.anonymous_store_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.dream_token
import dreamjournalai.composeapp.shared.generated.resources.dream_token_content_description_text
import dreamjournalai.composeapp.shared.generated.resources.login_button
import dreamjournalai.composeapp.shared.generated.resources.store_anonymous_hero
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AnonymousStoreScreen(
    paddingValues: PaddingValues,
    navigateToAccountScreen: () -> Unit,
) {
    val showCardShell = remember { mutableStateOf(false) }
    val showCardContent = remember { mutableStateOf(false) }
    val showBenefitOne = remember { mutableStateOf(false) }
    val showBenefitTwo = remember { mutableStateOf(false) }
    val showButton = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(70)
        showCardShell.value = true
        delay(260)
        showCardContent.value = true
        delay(220)
        showBenefitOne.value = true
        delay(140)
        showBenefitTwo.value = true
        delay(220)
        showButton.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(paddingValues)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = showCardShell.value,
                enter = fadeIn(animationSpec = tween(480)) + scaleIn(
                    initialScale = 0.985f,
                    animationSpec = tween(560, easing = FastOutSlowInEasing)
                ) + slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(),
                    color = Color(0xFF120F2A).copy(alpha = 0.62f),
                    shape = RoundedCornerShape(30.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.14f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 18.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedVisibility(
                            visible = showCardContent.value,
                            enter = fadeIn(animationSpec = tween(420)) + slideInVertically(
                                initialOffsetY = { it / 12 },
                                animationSpec = tween(520, easing = FastOutSlowInEasing)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(212.dp)
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0x44FFD6A6),
                                                    Color(0x308AB2FF),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.10f),
                                            shape = RoundedCornerShape(28.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(Res.drawable.store_anonymous_hero),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(194.dp)
                                            .padding(horizontal = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                listOf(
                                                    Color(0x26FFD1A8),
                                                    Color(0x18FFFFFF)
                                                )
                                            ),
                                            shape = RoundedCornerShape(999.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(999.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Dream store",
                                        style = TextStyle(
                                            color = Color(0xFFFFDFC2),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Sign in to unlock the full dream store.",
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        lineHeight = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Keep your dream tokens safe, sync purchases across devices, and open premium dream tools without losing your progress.",
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        color = Color.White.copy(alpha = 0.80f),
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        AnimatedVisibility(
                            visible = showBenefitOne.value,
                            enter = fadeIn(animationSpec = tween(320)) + slideInVertically(
                                initialOffsetY = { it / 8 },
                                animationSpec = tween(420, easing = FastOutSlowInEasing)
                            )
                        ) {
                            StoreBenefitRow(
                                title = "Keep every token",
                                body = "Your purchases and unlocks stay attached to your account."
                            )
                        }

                        Spacer(modifier = Modifier.height(if (showBenefitOne.value) 10.dp else 0.dp))

                        AnimatedVisibility(
                            visible = showBenefitTwo.value,
                            enter = fadeIn(animationSpec = tween(320)) + slideInVertically(
                                initialOffsetY = { it / 8 },
                                animationSpec = tween(420, easing = FastOutSlowInEasing)
                            )
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                StoreBenefitRow(
                                    title = "Unlock premium tools",
                                    body = "Interpretations, art, and dream symbols stay ready whenever you need them."
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showButton.value,
                        enter = fadeIn(animationSpec = tween(320)) + scaleIn(
                            initialScale = 0f,
                            transformOrigin = TransformOrigin.Center,
                            animationSpec = tween(520, easing = FastOutSlowInEasing)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFE97A2C),
                                            Color(0xFFFFB54D)
                                        )
                                    )
                                )
                                .clickable(onClick = navigateToAccountScreen)
                                .padding(vertical = 16.dp, horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.login_button),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreBenefitRow(
    title: String,
    body: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0x55FFD6A6),
                            Color(0x338BAEFF),
                            Color(0x18FFFFFF)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.dream_token),
                contentDescription = stringResource(Res.string.dream_token_content_description_text),
                modifier = Modifier
                    .size(30.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = body,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )
        }
    }
}
