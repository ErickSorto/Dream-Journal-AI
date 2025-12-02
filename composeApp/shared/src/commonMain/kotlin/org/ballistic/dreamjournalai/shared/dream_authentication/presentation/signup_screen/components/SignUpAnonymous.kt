package org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.anonymous_icon
import dreamjournalai.composeapp.shared.generated.resources.guest_account
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AnonymousButton(
    modifier: Modifier,
    isVisible: Boolean,
    onClick: () -> Unit,
    isEnabled : Boolean,
    enterDurationMillis: Int = 300,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(animationSpec = tween(enterDurationMillis), initialOffsetX = { 1000 }),
            exit = slideOutHorizontally(animationSpec = tween(enterDurationMillis)) { -1000 }
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    contentColor = OriginalXmlColors.White,
                    disabledContainerColor = Color.Black.copy(alpha = 0.1f),
                    disabledContentColor = OriginalXmlColors.White.copy(alpha = 0.3f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                onClick = onClick,
                enabled = isEnabled
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(
                            Res.drawable.anonymous_icon
                        ),
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.CenterStart),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(Res.string.guest_account),
                        fontSize = 16.sp,
                        color = OriginalXmlColors.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
