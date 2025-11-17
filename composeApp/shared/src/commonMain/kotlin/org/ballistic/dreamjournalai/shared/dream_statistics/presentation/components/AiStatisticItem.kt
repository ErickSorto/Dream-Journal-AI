package org.ballistic.dreamjournalai.shared.dream_statistics.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AiStatisticItem(
    title: String,
    value: Int,
    icon: Painter,
    color: Color,
    modifier: Modifier = Modifier,
    animationDelay: Long
) {
    var animatedValue by remember { mutableStateOf(0) }
    var textIsVisible by remember { mutableStateOf(false) }
    var numberIsVisible by remember { mutableStateOf(false) }
    val iconScale = remember { Animatable(0f) }

    LaunchedEffect(key1 = Unit) {
        // Initial delay for staggering effect
        delay(animationDelay)

        // 1. Icon pops up
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.5f,
                stiffness = 200f
            )
        )

        // 2. Text appears and types out
        textIsVisible = true
        delay(350) // Wait for typewriter animation to complete

        // 3. Number appears and counts up
        numberIsVisible = true
        Animatable(0f).animateTo(value.toFloat(), tween(500)) {
            animatedValue = this.value.toInt()
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = title,
            modifier = Modifier
                .size(28.dp)
                .scale(iconScale.value), // Apply scale only to the Icon
            tint = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (textIsVisible) {
            TypewriterText(
                text = title,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (numberIsVisible) {
            Text(
                text = animatedValue.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun TypewriterText(
    text: String,
    textStyle: androidx.compose.ui.text.TextStyle
) {
    var animatedText by remember { mutableStateOf("") }
    val textLength = text.length
    val animationDuration = 300 // in milliseconds

    LaunchedEffect(key1 = text) {
        for (i in 0..textLength) {
            animatedText = text.substring(0, i)
            delay((animationDuration / textLength).toLong())
        }
    }

    Text(
        text = animatedText,
        style = textStyle
    )
}