package org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LighterYellow
import org.ballistic.dreamjournalai.shared.core.Constants

@Composable
fun LoginButton(
    modifier: Modifier,
    onLoginEvent: (LoginEvent) -> Unit,
    loginViewModelState: LoginViewModelState,
    isVisible: MutableState<Boolean>,
    animate: Boolean = true,
    enterDurationMillis: Int = 300,
    exitWithFade: Boolean = false,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    if (!animate) {
        Button(
            modifier = modifier
                .fillMaxWidth(.5f)
                .height(40.dp),
            shape = RoundedCornerShape(12.dp),
            onClick = {
                keyboard?.hide()
                onLoginEvent(
                    LoginEvent.LoginWithEmailAndPassword(
                        loginViewModelState.loginEmail,
                        loginViewModelState.loginPassword
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = LighterYellow
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = Constants.LOGIN,
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    } else {
        AnimatedVisibility(
            visible = isVisible.value,
            enter = slideInHorizontally(animationSpec = tween(enterDurationMillis), initialOffsetX = { 1000 }),
            exit = if (exitWithFade) fadeOut(animationSpec = tween(220)) else slideOutHorizontally(animationSpec = tween(enterDurationMillis)) { -1000 }
        ) {
            Button(
                modifier = modifier
                    .fillMaxWidth(.5f)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    keyboard?.hide()
                    onLoginEvent(
                        LoginEvent.LoginWithEmailAndPassword(
                            loginViewModelState.loginEmail,
                            loginViewModelState.loginPassword
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LighterYellow
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = Constants.LOGIN,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }
        }
    }
}
