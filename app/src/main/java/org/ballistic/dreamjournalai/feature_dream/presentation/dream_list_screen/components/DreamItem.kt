package org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream.Companion.dreamBackgroundImages

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DreamItem(
    dream: Dream,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    playAnimation: Boolean = false,
    onDeleteClick: () -> Unit
) {
    val animatedProgress = remember { Animatable(initialValue = 0.8f) }
    if (playAnimation) {
        LaunchedEffect(key1 = dream) {
            animatedProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        }
    }

    val animatedModifier = if (playAnimation) {
        modifier
            .graphicsLayer(
                scaleX = animatedProgress.value,
                scaleY = animatedProgress.value
            )
    } else {
        modifier
    }
    val imageResId = if (dream.backgroundImage in dreamBackgroundImages) {
        dream.backgroundImage
    } else {
        R.drawable.background_during_day
    }

    Box(
        modifier = animatedModifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(colorResource(id = R.color.dark_blue).copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp, 16.dp, 8.dp, 16.dp)
                    .size(60.dp)
                    .shadow(16.dp, CircleShape, true, Color.Black.copy(alpha = 0.8f))
                    .clip(CircleShape)
                    .background(Color.Transparent)

            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageResId),
                    contentDescription = "Color",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp, 12.dp, 0.dp, 12.dp)

            ) {
                Text(
                    text = dream.title,
                    style = typography.titleSmall,
                    color = colorResource(id = R.color.brighter_white),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = dream.content,
                    style = typography.bodySmall,
                    color = colorResource(id = R.color.white),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                modifier = Modifier
                    .padding(0.dp, 8.dp, 8.dp, 8.dp)
                    .fillMaxHeight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (dream.isFavorite) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_star_24),
                        contentDescription = "Favorite",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(bottom = 4.dp),
                        alignment = Alignment.Center
                    )
                }

                if (dream.isLucid) {
                    Image(
                        painter = painterResource(id = R.drawable.lighthouse_vector),
                        contentDescription = "Lucid",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(bottom = 4.dp),
                        alignment = Alignment.TopCenter

                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.baseline_delete_24),
                    contentDescription = "Delete",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            onDeleteClick()
                        },
                    alignment = Alignment.BottomCenter,
                )
            }
        }
    }
}
