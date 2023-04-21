package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign


@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    animationDuration: Int = 3000
) {
    val typedText = remember(text) { mutableStateOf(AnnotatedString("")) }
    val animatedIndex = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(text) {
        //val delayPerChar = animationDuration / text.length
        animatedIndex.animateTo(
            targetValue = text.length.toFloat(),
            animationSpec = tween(animationDuration),
            block = {
                typedText.value = AnnotatedString(text.substring(0, animatedIndex.value.toInt()))
            }
        )
    }
    Column(modifier = Modifier.verticalScroll( rememberScrollState())) {
        Text(
            text = typedText.value,
            modifier = modifier,
            textAlign = textAlign,
            style = style
        )
    }
}