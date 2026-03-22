package org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import dreamjournalai.composeapp.shared.generated.resources.ic_google_logo
import dreamjournalai.composeapp.shared.generated.resources.sign_in_with_google
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SignInGoogleButton(
    modifier: Modifier,
    isVisible: Boolean,
    isEnabled: Boolean,
    label: String? = null,
    onClick: () -> Unit,
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(initialOffsetX = { 1000 }),
            exit = slideOutHorizontally { -1000 }
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    disabledContainerColor = Color.Black.copy(alpha = 0.1f),
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                enabled = isEnabled,
                onClick = onClick
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            Res.drawable.ic_google_logo
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = label ?: stringResource(Res.string.sign_in_with_google),
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
