package org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.SkyBlue
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LighterYellow
import org.ballistic.dreamjournalai.shared.core.ComposableData
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModelState


@Composable
fun SignupLoginTabLayout(loginViewModelState: LoginViewModelState, onLayoutChange: (LoginEvent) -> Unit) {
    if (!loginViewModelState.isForgotPasswordLayout) {
        Row {
            LoginOrSignupTab(
                text = "Login",
                isLoginLayout = loginViewModelState.isLoginLayout,
                isSignUpLayout = loginViewModelState.isSignUpLayout,
                isClicked = {
                    onLayoutChange(LoginEvent.ShowLoginLayout)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp, bottom = 8.dp, start = 16.dp)
            )
            LoginOrSignupTab(
                text = "Signup",
                isLoginLayout = loginViewModelState.isLoginLayout,
                isSignUpLayout = loginViewModelState.isSignUpLayout,
                isClicked = {
                    onLayoutChange(LoginEvent.ShowSignUpLayout)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, bottom = 8.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun ForgotPasswordLayout(
    loginViewModelState: LoginViewModelState,
    authEvent: (LoginEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            modifier = Modifier.padding(horizontal = 16.dp),
            email = loginViewModelState.forgotPasswordEmail,
            onValueChange = {
                authEvent(LoginEvent.EnteredForgotPasswordEmail(it))
            },
            isVisible = remember {
                mutableStateOf(true)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                authEvent(LoginEvent.SendPasswordResetEmail(loginViewModelState.forgotPasswordEmail))
            },
            modifier = Modifier
                .fillMaxWidth(.5f)
                .height(40.dp),
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors(
                containerColor = LighterYellow
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = "Reset Password",
                fontSize = 15.sp,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                authEvent(LoginEvent.ShowLoginLayout)
            },
            modifier = Modifier
                .fillMaxWidth(.5f)
                .height(40.dp),
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors(
                containerColor = SkyBlue
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = "Back to Login",
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }

    ForgotPassword(
        navigateBack = {
            authEvent(LoginEvent.ShowLoginLayout)
        },
        showResetPasswordMessage = { /*TODO*/ },
        showErrorMessage = { /*TODO*/ },
        loginViewModelState = loginViewModelState
    )
}


@Composable
fun SignupLayout(
    signupViewModelState: SignupViewModelState,
    isLoginLayout: Boolean,
    onSignupEvent: (SignupEvent) -> Unit,
    onLoginEvent: (LoginEvent) -> Unit = {}
) {
    val keyboard = LocalSoftwareKeyboardController.current


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            modifier = Modifier.padding(horizontal = 16.dp),
            email = signupViewModelState.signUpEmail,
            onValueChange = {
                onSignupEvent(SignupEvent.EnteredSignUpEmail(it))
            },
            isVisible = remember {
                mutableStateOf(true)
            }
        )

        PasswordField(
            modifier = Modifier.padding(horizontal = 16.dp),
            password = signupViewModelState.signUpPassword,
            onValueChange = { newValue ->
                onSignupEvent(SignupEvent.EnteredSignUpPassword(newValue))
            },
            forgotPassword = { onLoginEvent(LoginEvent.ShowForgotPasswordLayout) },
            isLoginLayout = isLoginLayout,
            isVisible = remember {
                mutableStateOf(true)
            },
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth(.5f)
                .height(40.dp),
            onClick = {
                keyboard?.hide()
                onSignupEvent(
                    SignupEvent.SignUpWithEmailAndPassword(
                        signupViewModelState.signUpEmail,
                        signupViewModelState.signUpPassword
                    )
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors(
                containerColor = SkyBlue
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = "Sign Up",
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}


@Composable
fun LoginLayout(
    loginViewModelState: LoginViewModelState,
    onLoginEvent: (LoginEvent) -> Unit,
    onAnimationComplete: () -> Unit = {},
    shouldAnimate: Boolean = true,
    staggerMillis: Long = 220L,
    // External stagger control (optional)
    emailVisible: MutableState<Boolean>? = null,
    passwordVisible: MutableState<Boolean>? = null,
    loginButtonVisible: MutableState<Boolean>? = null,
    useExternalStagger: Boolean = false,
    preferFadeExit: Boolean = false,
) {

    val enterDuration = 300
    val staggeredDelay = staggerMillis

    // Local default visibility states when not using external stagger
    val localStates = remember {
        listOf(
            ComposableData(key = "Email", visible = mutableStateOf(false)),
            ComposableData(key = "Password", visible = mutableStateOf(false)),
            ComposableData(key = "LoginButton", visible = mutableStateOf(false)),
        )
    }

    // Decide which states to use
    val emailState = emailVisible ?: localStates.first { it.key == "Email" }.visible
    val passwordState = passwordVisible ?: localStates.first { it.key == "Password" }.visible
    val loginButtonState = loginButtonVisible ?: localStates.first { it.key == "LoginButton" }.visible

    // Only run internal stagger when not using external one
    LaunchedEffect(shouldAnimate, staggerMillis, useExternalStagger) {
        if (!useExternalStagger) {
            if (shouldAnimate) {
                localStates.forEachIndexed { index, composableData ->
                    if (index == 0) {
                        composableData.visible.value = true
                    } else {
                        delay(staggeredDelay)
                        composableData.visible.value = true
                    }
                }
            } else {
                localStates.forEach { it.visible.value = true }
            }
            onAnimationComplete()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmailField(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp)
                .focusable(),
            email = loginViewModelState.loginEmail,
            onValueChange = { onLoginEvent(LoginEvent.EnteredLoginEmail(it)) },
            isVisible = emailState,
            animate = shouldAnimate,
            enterDurationMillis = enterDuration,
            exitWithFade = preferFadeExit,
        )

        PasswordField(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp)
                .focusable(),
            password = loginViewModelState.loginPassword,
            onValueChange = { newValue -> onLoginEvent(LoginEvent.EnteredLoginPassword(newValue)) },
            forgotPassword = { onLoginEvent(LoginEvent.ShowForgotPasswordLayout) },
            isLoginLayout = loginViewModelState.isLoginLayout,
            isVisible = passwordState,
            animate = shouldAnimate,
            enterDurationMillis = enterDuration,
            exitWithFade = preferFadeExit,
        )

        Spacer(modifier = Modifier.height(8.dp))

        LoginButton(
            modifier = Modifier.fillMaxWidth(.5f),
            loginViewModelState = loginViewModelState,
            onLoginEvent = onLoginEvent,
            isVisible = loginButtonState,
            animate = shouldAnimate,
            enterDurationMillis = enterDuration,
            exitWithFade = preferFadeExit,
        )
    }
    if (!useExternalStagger) {
        LogIn(showErrorMessage = { /*TODO*/ }, loginViewModelState = loginViewModelState)
    }
}

@Composable
fun LoginOrSignupTab(
    text: String,
    modifier: Modifier = Modifier,
    isLoginLayout: Boolean,
    isSignUpLayout: Boolean,
    isClicked: () -> Unit,
) {
    TextButton(
        modifier = modifier
            .background(
                if (isLoginLayout && text == "Login" || isSignUpLayout && text == "Signup") {
                    LightBlack.copy(alpha = 0.7f)
                } else {
                   LightBlack.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(8.dp)
            ),
        onClick = { isClicked() },
        shape = RoundedCornerShape(8.dp)
    )
    {
        Text(
            text = text,
            fontSize = 16.sp,
            color = if (isLoginLayout && text == "Login" || isSignUpLayout && text == "Signup") {
                Color.White
            } else {
                Color.White.copy(alpha = 0.5f)
            },
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 8.dp)
        )
    }
}
