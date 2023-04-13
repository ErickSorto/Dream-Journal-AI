package org.ballistic.dreamjournalai.user_authentication.presentation.components

import androidx.compose.animation.AnimatedVisibility
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.core.Constants.LOGIN
import org.ballistic.dreamjournalai.core.Constants.SIGNUP
import org.ballistic.dreamjournalai.user_authentication.presentation.forgot_password.components.ForgotPassword
import org.ballistic.dreamjournalai.user_authentication.presentation.sign_in.components.LogIn
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.AuthEvent
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModelState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SignUpSignInForgotPasswordLayout(
    authViewModelState: AuthViewModelState,
    pagerState: PagerState,
    authEvent: (AuthEvent) -> Unit,
    navigateToHomeScreen: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .background(Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        if (!authViewModelState.isForgotPasswordLayout.value) {
            Row {
                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    visible = pagerState.currentPage == 3
                ) {
                    LoginOrSignupTab(
                        text = "Login",
                        isLoginLayout = authViewModelState.isLoginLayout.value,
                        isSignUpLayout = authViewModelState.isSignUpLayout.value,
                        isClicked = {
                            authViewModelState.isLoginLayout.value = true
                            authViewModelState.isSignUpLayout.value = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp, bottom = 8.dp)
                    )
                }

                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    visible = pagerState.currentPage == 3
                ) {
                    LoginOrSignupTab(
                        text = "Signup",
                        isLoginLayout = authViewModelState.isLoginLayout.value,
                        isSignUpLayout = authViewModelState.isSignUpLayout.value,
                        isClicked = {
                            authViewModelState.isLoginLayout.value = false
                            authViewModelState.isSignUpLayout.value = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp, bottom = 8.dp),
                    )
                }


            }
        }
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = pagerState.currentPage == 3
        ) {
            when {
                authViewModelState.isLoginLayout.value -> {
                    LoginLayout(
                        authViewModelState = authViewModelState,
                        authEvent = {
                            authEvent(it)
                        },
                        pagerState = pagerState,
                        navigateToHomeScreen = {
                            navigateToHomeScreen()
                        }
                    )
                }
                authViewModelState.isForgotPasswordLayout.value -> {
                    ForgotPasswordLayout(
                        authViewModelState = authViewModelState,
                        pagerState = pagerState,
                    ) {
                        authEvent(it)
                    }
                }
                else -> {
                    SignupLayout(
                        pagerState = pagerState,
                        authViewModelState = authViewModelState,
                        authEvent = {
                            authEvent(it)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ForgotPasswordLayout(
    authViewModelState: AuthViewModelState,
    pagerState: PagerState,
    authEvent: (AuthEvent) -> Unit
) {


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            email = authViewModelState.forgotPasswordEmail,
            pagerState = pagerState,
            onValueChange = {
                authEvent(AuthEvent.EnteredForgotPasswordEmail(it))
            },
        )

        Button(
            onClick = {
                authEvent(AuthEvent.SendPasswordResetEmail(authViewModelState.forgotPasswordEmail))
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
                authViewModelState.isForgotPasswordLayout.value = false
                authViewModelState.isLoginLayout.value = true
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
            authViewModelState.isLoginLayout.value = true
            authViewModelState.isForgotPasswordLayout.value = false
        },
        showResetPasswordMessage = { /*TODO*/ },
        showErrorMessage = { /*TODO*/ },
        authViewModelState = authViewModelState
    )
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalPagerApi::class)
@Composable
fun SignupLayout(
    authViewModelState: AuthViewModelState,
    authEvent: (AuthEvent) -> Unit,
    pagerState: PagerState,
) {
    val keyboard = LocalSoftwareKeyboardController.current


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            email = authViewModelState.signUpEmail,
            pagerState = pagerState,
            onValueChange = {
                authEvent(AuthEvent.EnteredSignUpEmail(it))
            },
        )

        PasswordField(
            password = authViewModelState.signUpPassword,
            onValueChange = { newValue ->
                authEvent(AuthEvent.EnteredSignUpPassword(newValue))
            },
            forgotPassword = { authViewModelState.isForgotPasswordLayout.value = true },
            isLoginLayout = authViewModelState.isLoginLayout.value,
        )

        Button(
            modifier = Modifier.fillMaxWidth(.5f),
            onClick = {
                keyboard?.hide()
                authEvent(AuthEvent.SignUpWithEmailAndPassword(authViewModelState.signUpEmail, authViewModelState.signUpPassword))
                authEvent(AuthEvent.SendEmailVerification)
            },
            colors = buttonColors(
                backgroundColor = colorResource(id = R.color.sky_blue)
            )
        ) {
            Text(
                text = SIGNUP,
                fontSize = 15.sp
            )
        }
    }

//    SignUp(
//        sendEmailVerification = { },
//        showVerifyEmailMessage = { /*TODO*/ })

}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalPagerApi::class)
@Composable
fun LoginLayout(
    authViewModelState: AuthViewModelState,
    pagerState: PagerState,
    authEvent: (AuthEvent) -> Unit,
    navigateToHomeScreen: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            email = authViewModelState.loginEmail,
            pagerState = pagerState,
            onValueChange = {
                authEvent(AuthEvent.EnteredLoginEmail(it))
            },
        )

        PasswordField(
            password = authViewModelState.loginPassword,
            onValueChange = { newValue ->
                authEvent(AuthEvent.EnteredLoginPassword(newValue))
            },
            forgotPassword = {
                authViewModelState.isForgotPasswordLayout.value = true
                authViewModelState.isLoginLayout.value = false
            },
            isLoginLayout = authViewModelState.isLoginLayout.value,
        )

        Button(
            modifier = Modifier
                .fillMaxWidth(.5f),
            onClick = {
                keyboard?.hide()
                authEvent(AuthEvent.LoginWithEmailAndPassword(authViewModelState.loginEmail, authViewModelState.loginPassword))
            },
            colors = buttonColors(
                backgroundColor = colorResource(id = R.color.lighter_yellow)
            )
        ) {
            Text(
                text = LOGIN,
                fontSize = 15.sp
            )
        }
    }

    LogIn(showErrorMessage = { /*TODO*/ }, authViewModelState = authViewModelState)
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