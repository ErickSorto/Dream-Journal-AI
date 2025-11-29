package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.ballistic.dreamjournalai.shared.core.components.DreamTokenLayout
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.ImageStyle
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White
import kotlin.math.absoluteValue

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun ImageGenerationPopUp(
    dreamTokens: Int,
    imageStyle: ImageStyle,
    onDreamTokenClick: (amount: Int, style: String) -> Unit,
    onAdClick: () -> Unit,
    onClickOutside: () -> Unit,
    onImageStyleChange: (ImageStyle) -> Unit,
    modifier: Modifier = Modifier,
    isWorldPainting: Boolean = false,
    fixedCost: Int? = null
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("High Quality", "Low Quality")
    
    // Use fixedCost if provided, else standard calculation logic
    val amount = fixedCost ?: if (isWorldPainting) 5 else if (selectedIndex == 0) 2 else 1

    val imageStyles = ImageStyle.entries
    val pagerState = rememberPagerState(
        initialPage = imageStyle.ordinal,
        pageCount = { imageStyles.size }
    )
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(pagerState.currentPage) {
        onImageStyleChange(imageStyles[pagerState.currentPage])
    }
    
    // Ensure pager stays in sync if external state changes
    LaunchedEffect(imageStyle) {
        if (pagerState.currentPage != imageStyle.ordinal) {
            pagerState.animateScrollToPage(imageStyle.ordinal)
        }
    }

    val scrimBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.6f to Color.Transparent,
            1.0f to Color.Black.copy(alpha = .95f)
        )
    )

    ModalBottomSheet(
        modifier = modifier.fillMaxWidth(),
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        onDismissRequest = {
            onClickOutside()
        },
        content = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(
                        LightBlack
                    )
                    .verticalScroll(rememberScrollState())
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (isWorldPainting) "Paint World" else "Dream Painter",
                        style = MaterialTheme.typography.headlineSmall,
                        color = White,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DreamTokenLayout(
                        totalDreamTokens = dreamTokens
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (!isWorldPainting) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        options.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = options.size
                                ),
                                onClick = { selectedIndex = index },
                                selected = index == selectedIndex,
                                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = OriginalXmlColors.SkyBlue.copy(alpha = 0.8f),
                                    activeContentColor = White,
                                    inactiveContainerColor = Color.DarkGray.copy(alpha = 0.5f),
                                    inactiveContentColor = White.copy(alpha = 0.7f),
                                    activeBorderColor = OriginalXmlColors.SkyBlue
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Text(
                    text = "Choose Image Style",
                    style = MaterialTheme.typography.titleMedium,
                    color = White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                     contentPadding = PaddingValues(horizontal = 26.dp),
                     pageSpacing = (-12).dp
                ) { page ->
                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 10000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .graphicsLayer {
                                val pageOffset =
                                    ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

                                val horizontalScale = lerp(
                                    start = 0.85f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleX = horizontalScale
                                scaleY = horizontalScale

                                alpha = lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .border(
                                    width = 2.dp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clip(RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val imageUrl = if (isWorldPainting) imageStyles[page].worldPaintingImage else imageStyles[page].image
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = imageStyles[page].displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(scrimBrush)
                            )
                            Text(
                                text = imageStyles[page].displayName,
                                color = White,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                AdTokenLayout(
                    isAdButtonVisible = amount <= 1 && fixedCost != 0, // Don't show ad if free
                    onAdClick = {
                        onAdClick()
                    },
                    onDreamTokenClick = {
                        val styleToSend = if (isWorldPainting) imageStyle.worldPromptAffix else imageStyle.promptAffix
                        onDreamTokenClick(amount, styleToSend)
                    },
                    amount = amount,
                    customText = if (fixedCost == 0) "Paint First Dream Free \uD83C\uDF19" else null
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        containerColor = LightBlack
    )
}

fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}