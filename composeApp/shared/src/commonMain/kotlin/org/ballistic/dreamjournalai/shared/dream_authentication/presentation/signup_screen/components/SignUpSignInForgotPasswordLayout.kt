package org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.core.ComposableData
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModelState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource


@Composable
fun SignupLoginTabLayout(loginViewModelState: LoginViewModelState, onLayoutChange: (LoginEvent) -> Unit) {
    if (!loginViewModelState.isForgotPasswordLayout) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            LoginOrSignupTab(
                text = stringResource(Res.string.login),
                isLoginLayout = loginViewModelState.isLoginLayout,
                isSignUpLayout = loginViewModelState.isSignUpLayout,
                isClicked = {
                    onLayoutChange(LoginEvent.ShowLoginLayout)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            )
            LoginOrSignupTab(
                text = stringResource(Res.string.signup),
                isLoginLayout = loginViewModelState.isLoginLayout,
                isSignUpLayout = loginViewModelState.isSignUpLayout,
                isClicked = {
                    onLayoutChange(LoginEvent.ShowSignUpLayout)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun ForgotPasswordLayout(
    loginViewModelState: LoginViewModelState,
    authEvent: (LoginEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (loginViewModelState.error is StringValue.DynamicString && (loginViewModelState.error as StringValue.DynamicString).value.isNotEmpty()) {
        LaunchedEffect(loginViewModelState.error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    (loginViewModelState.error as StringValue.DynamicString).value
                )
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            modifier = Modifier,
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
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors(
                containerColor = OriginalXmlColors.LighterYellow
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = stringResource(Res.string.reset_password),
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
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors(
                containerColor = OriginalXmlColors.SkyBlue
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = stringResource(Res.string.back_to_login),
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}


@Composable
fun SignupLayout(
    signupViewModelState: SignupViewModelState,
    isLoginLayout: Boolean,
    onSignupEvent: (SignupEvent) -> Unit,
    onLoginEvent: (LoginEvent) -> Unit = {},
    shouldAnimate: Boolean = true,
    staggerMillis: Long = 220L,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val isEnabled = signupViewModelState.signUpEmail.isNotBlank() &&
        signupViewModelState.signUpPassword.isNotBlank()
    val emailVisible = remember { mutableStateOf(false) }
    val passwordVisible = remember { mutableStateOf(false) }
    val buttonVisible = remember { mutableStateOf(false) }

    LaunchedEffect(shouldAnimate, staggerMillis) {
        if (shouldAnimate) {
            emailVisible.value = false
            passwordVisible.value = false
            buttonVisible.value = false
            emailVisible.value = true
            delay(staggerMillis)
            passwordVisible.value = true
            delay(staggerMillis)
            buttonVisible.value = true
        } else {
            emailVisible.value = true
            passwordVisible.value = true
            buttonVisible.value = true
        }
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            modifier = Modifier,
            email = signupViewModelState.signUpEmail,
            onValueChange = {
                onSignupEvent(SignupEvent.EnteredSignUpEmail(it))
            },
            isVisible = emailVisible,
            animate = shouldAnimate
        )

        PasswordField(
            modifier = Modifier,
            password = signupViewModelState.signUpPassword,
            onValueChange = { newValue ->
                onSignupEvent(SignupEvent.EnteredSignUpPassword(newValue))
            },
            forgotPassword = { onLoginEvent(LoginEvent.ShowForgotPasswordLayout) },
            isLoginLayout = isLoginLayout,
            isVisible = passwordVisible,
            animate = shouldAnimate,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (shouldAnimate) {
            AnimatedVisibility(
                visible = buttonVisible.value,
                enter = slideInHorizontally(animationSpec = tween(300), initialOffsetX = { 1000 }),
                exit = fadeOut(animationSpec = tween(220))
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp),
                    enabled = isEnabled,
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
                        containerColor = OriginalXmlColors.SkyBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.sign_up),
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                }
            }
        } else {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp),
                enabled = isEnabled,
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
                    containerColor = OriginalXmlColors.SkyBlue
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = stringResource(Res.string.sign_up),
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }
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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (loginViewModelState.error is StringValue.DynamicString && (loginViewModelState.error as StringValue.DynamicString).value.isNotEmpty()) {
        LaunchedEffect(loginViewModelState.error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    (loginViewModelState.error as StringValue.DynamicString).value
                )
            }
        }
    }

    val enterDuration = 300
    val staggeredDelay = staggerMillis

    val emailString = stringResource(Res.string.email)
    val passwordString = stringResource(Res.string.password)
    val loginButtonString = stringResource(Res.string.login)


    // Local default visibility states when not using external stagger
    val localStates = remember {
        listOf(
            ComposableData(key = emailString, visible = mutableStateOf(false)),
            ComposableData(key = passwordString, visible = mutableStateOf(false)),
            ComposableData(key = loginButtonString, visible = mutableStateOf(false)),
        )
    }

    // Decide which states to use
    val emailState = emailVisible ?: localStates.first { it.key == emailString }.visible
    val passwordState = passwordVisible ?: localStates.first { it.key == passwordString }.visible
    val loginButtonState = loginButtonVisible ?: localStates.first { it.key == loginButtonString }.visible

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
            modifier = Modifier.fillMaxWidth(),
            loginViewModelState = loginViewModelState,
            onLoginEvent = onLoginEvent,
            isVisible = loginButtonState,
            animate = shouldAnimate,
            enterDurationMillis = enterDuration,
            exitWithFade = preferFadeExit,
        )
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
    val loginString = stringResource(Res.string.login)
    val signupString = stringResource(Res.string.signup)
    TextButton(
        modifier = modifier
            .background(
                if (isLoginLayout && text == loginString || isSignUpLayout && text == signupString) {
                    OriginalXmlColors.LightBlack.copy(alpha = 0.7f)
                } else {
                    Color.White.copy(alpha = 0.06f)
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
            color = if (isLoginLayout && text == loginString || isSignUpLayout && text == signupString) {
                Color.White
            } else {
                Color.White.copy(alpha = 0.5f)
            },
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
