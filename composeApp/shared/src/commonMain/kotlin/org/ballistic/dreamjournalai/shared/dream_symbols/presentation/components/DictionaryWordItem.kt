package org.ballistic.dreamjournalai.shared.dream_symbols.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.baseline_lock_24
import dreamjournalai.composeapp.shared.generated.resources.dream_token_content_description_text
import dreamjournalai.composeapp.shared.generated.resources.unlock
import dreamjournalai.composeapp.shared.generated.resources.view_word
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.viewmodel.DictionaryWord
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun DictionaryWordItem(
    modifier: Modifier = Modifier,
    wordItem: DictionaryWord,
    onWordClick: () -> Unit = {},
    playAnimation: Boolean = true  // Added to control the animation
) {
    // Scale animation
    val animatedProgress = remember { Animatable(initialValue = 0.8f) }
    if (playAnimation) {
        LaunchedEffect(key1 = wordItem) {
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

    // Rest of your composable code remains the same
    Box(
        modifier = animatedModifier
            .padding(16.dp, 8.dp, 16.dp, 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (wordItem.isUnlocked) OriginalXmlColors.LightBlack.copy(alpha = 0.8f) else OriginalXmlColors.LightBlack.copy(alpha = 0.2f)
            )
            .fillMaxWidth()
            .clickable {
                onWordClick()
            }
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = wordItem.word,
                style = if (wordItem.word.length > 20) {
                    MaterialTheme.typography.titleSmall
                } else {
                    MaterialTheme.typography.titleMedium
                },
                color = if (wordItem.isUnlocked) OriginalXmlColors.BrighterWhite else OriginalXmlColors.BrighterWhite.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (wordItem.isUnlocked) {
                Text(
                    text = stringResource(Res.string.view_word),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OriginalXmlColors.BrighterWhite,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            } else {

                Button(
                    onClick = { onWordClick() },
                    modifier = Modifier
                        .padding(8.dp, 8.dp, 8.dp, 8.dp)
                        .shadow(4.dp, RoundedCornerShape(10.dp)), // Add shadow with rounded corners
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OriginalXmlColors.RedOrange,
                    ),
                    contentPadding = PaddingValues(start = 14.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
                    elevation = ButtonDefaults.buttonElevation(5.dp),
                    shape = RoundedCornerShape(10.dp) // Rounded corners for a softer look
                ) {
                    Text(
                        text = stringResource(Res.string.unlock),
                        style = MaterialTheme.typography.labelLarge,
                        color = OriginalXmlColors.BrighterWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    DictionaryCostLabel()
                }


            }
        }
    }
}

@Composable
fun DictionaryCostLabel() {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .background(color = Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.baseline_lock_24),
            tint = OriginalXmlColors.BrighterWhite,
            contentDescription = stringResource(Res.string.dream_token_content_description_text),
            modifier = Modifier
                .size(35.dp)
                .padding(8.dp, 4.dp, 0.dp, 4.dp),
        )
    }
}
