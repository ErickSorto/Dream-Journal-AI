package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.core.ComposableData
import org.ballistic.dreamjournalai.core.Constants.SIGNUP
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.SignupViewModelState


@Composable
fun SignupLoginTabLayout(loginViewModelState: LoginViewModelState) {
    if (!loginViewModelState.isForgotPasswordLayout.value) {
        Row {
            LoginOrSignupTab(
                text = "Login",
                isLoginLayout = loginViewModelState.isLoginLayout.value,
                isSignUpLayout = loginViewModelState.isSignUpLayout.value,
                isClicked = {
                    loginViewModelState.isLoginLayout.value = true
                    loginViewModelState.isSignUpLayout.value = false
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp, bottom = 8.dp, start = 16.dp)
            )
            LoginOrSignupTab(
                text = "Signup",
                isLoginLayout = loginViewModelState.isLoginLayout.value,
                isSignUpLayout = loginViewModelState.isSignUpLayout.value,
                isClicked = {
                    loginViewModelState.isLoginLayout.value = false
                    loginViewModelState.isSignUpLayout.value = true
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
        )

        Button(
            onClick = {
                authEvent(LoginEvent.SendPasswordResetEmail(loginViewModelState.forgotPasswordEmail))
            },
            modifier = Modifier.fillMaxWidth(.5f),
            colors = buttonColors(
                backgroundColor = colorResource(id = R.color.lighter_yellow)
            )
        ) {
            Text(
                text = "Reset Password",
                fontSize = 15.sp
            )
        }

        Button(
            onClick = {
                loginViewModelState.isForgotPasswordLayout.value = false
                loginViewModelState.isLoginLayout.value = true
            },
            modifier = Modifier.fillMaxWidth(.5f),
            colors = buttonColors(
                backgroundColor = colorResource(id = R.color.sky_blue)
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
            loginViewModelState.isLoginLayout.value = true
            loginViewModelState.isForgotPasswordLayout.value = false
        },
        showResetPasswordMessage = { /*TODO*/ },
        showErrorMessage = { /*TODO*/ },
        loginViewModelState = loginViewModelState
    )
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignupLayout(
    signupViewModelState: SignupViewModelState,
    onSignupEvent: (SignupEvent) -> Unit,
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
            forgotPassword = { signupViewModelState.isForgotPasswordLayout.value = true },
            isLoginLayout = signupViewModelState.isLoginLayout.value,
            isVisible = remember {
                mutableStateOf(true)
            },
        )

        Button(
            modifier = Modifier.fillMaxWidth(.5f),
            onClick = {
                keyboard?.hide()
                onSignupEvent(
                    SignupEvent.SignUpWithEmailAndPassword(
                        signupViewModelState.signUpEmail,
                        signupViewModelState.signUpPassword
                    )
                )
            },
            colors = buttonColors(
                backgroundColor = colorResource(id = R.color.sky_blue)
            ),
        ) {
            Text(
                text = SIGNUP,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun LoginLayout(
    loginViewModelState: LoginViewModelState,
    onLoginEvent: (LoginEvent) -> Unit,
    onSignupEvent: (SignupEvent) -> Unit,
    onAnimationComplete : () -> Unit = {}
) {
    val isUserAnonymous = loginViewModelState.isUserAnonymous
    val staggeredDelay = 100L
    val composablesData = listOf(
        ComposableData(key = "Email", visible = remember { mutableStateOf(false) }),
        ComposableData(key = "Password", visible = remember { mutableStateOf(false) }),
        ComposableData(key = "LoginButton", visible = remember { mutableStateOf(false) }),
        ComposableData(key = "SignInGoogleButton", visible = remember { mutableStateOf(false) }),
        ComposableData(key = "AnonymousButton", visible = remember { mutableStateOf(false) }),
    )

    LaunchedEffect(key1 = true) {
        for (composableData in composablesData) {
            delay(staggeredDelay)
            composableData.visible.value = true
        }
        onAnimationComplete()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmailField(
            modifier = Modifier.padding(horizontal = 16.dp),
            email = loginViewModelState.loginEmail,
            onValueChange = {
                onLoginEvent(LoginEvent.EnteredLoginEmail(it))
            },
            isVisible = composablesData.first { it.key == "Email" }.visible
        )

        PasswordField(
            modifier = Modifier.padding(horizontal = 16.dp),
            password = loginViewModelState.loginPassword,
            onValueChange = { newValue ->
                onLoginEvent(LoginEvent.EnteredLoginPassword(newValue))
            },
            forgotPassword = {
                loginViewModelState.isForgotPasswordLayout.value = true
                loginViewModelState.isLoginLayout.value = false
            },
            isLoginLayout = loginViewModelState.isLoginLayout.value,
            isVisible = composablesData.first { it.key == "Password" }.visible
        )

        LoginButton(
            modifier = Modifier.fillMaxWidth(.5f),
            loginViewModelState = loginViewModelState,
            onLoginEvent = onLoginEvent,
            isVisible = composablesData.first { it.key == "LoginButton" }.visible,
        )

        SignInGoogleButton(
            onClick = {
                onLoginEvent(LoginEvent.OneTapSignIn)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 32.dp, start = 16.dp, end = 16.dp),
            isVisible = composablesData.first { it.key == "SignInGoogleButton" }.visible
        )

        if (!isUserAnonymous) {
            AnonymousButton(
                modifier = Modifier
                    .padding(bottom = 8.dp, top = 8.dp, start = 16.dp, end = 16.dp),
                isVisible = composablesData.first { it.key == "AnonymousButton" }.visible,
                onClick = {
                    onSignupEvent(SignupEvent.AnonymousSignIn)
                }
            )
        }
    }
    LogIn(showErrorMessage = { /*TODO*/ }, loginViewModelState = loginViewModelState)
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
                    Color.White.copy(alpha = 0.4f)
                } else {
                    Color.White.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(8.dp)
            ),
        onClick = { isClicked() },
    )
    {
        androidx.compose.material3.Text(
            text = text,
            fontSize = 16.sp,
            color = if (isLoginLayout && text == "Login" || isSignUpLayout && text == "Signup") {
                Color.Black
            } else {
                Color.Black.copy(alpha = 0.5f)
            },
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 8.dp)
        )
    }
}