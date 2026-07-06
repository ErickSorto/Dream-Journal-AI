package org.ballistic.dreamjournalai.shared.dream_main.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

private val NotificationPermissionAlertRed = Color(0xFFE9435A)
private val NotificationPermissionAlertBorder = Color(0xFFFFD6DE)

@Composable
fun NotificationPermissionMenuIcon(
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

@Composable
fun NotificationPermissionAlertBadge(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(NotificationPermissionAlertRed)
            .border(
                width = 1.dp,
                color = NotificationPermissionAlertBorder.copy(alpha = 0.92f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = min(this.size.width, this.size.height) * 0.13f
            drawLine(
                color = Color.White.copy(alpha = 0.96f),
                start = Offset(x = this.size.width * 0.50f, y = this.size.height * 0.27f),
                end = Offset(x = this.size.width * 0.50f, y = this.size.height * 0.58f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.96f),
                radius = strokeWidth * 0.62f,
                center = Offset(x = this.size.width * 0.50f, y = this.size.height * 0.74f)
            )
        }
    }
}
