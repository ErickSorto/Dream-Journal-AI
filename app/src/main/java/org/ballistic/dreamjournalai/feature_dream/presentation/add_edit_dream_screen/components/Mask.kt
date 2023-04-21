package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components

import android.graphics.PathDashPathEffect
import android.graphics.PathEffect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp

import androidx.compose.ui.unit.dp

@Composable
fun PaintAnimatedImage(
    painter: Painter?,
    modifier: Modifier = Modifier,
    contentDescription: String?,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    fadeInDuration: Int = 1000,
    paddingTop: Dp = 16.dp,
    paddingBottom: Dp = 16.dp,
    paddingHorizontal: Dp = 16.dp
) {
    AnimatedVisibility(
        visible = painter != null,
        enter = fadeIn(animationSpec = tween(durationMillis = fadeInDuration)),
        exit = fadeOut(animationSpec = tween(durationMillis = fadeInDuration)),
    ) {
        Image(
            painter = painter!!,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            contentScale = contentScale,
            alignment = alignment
        )
    }
}
