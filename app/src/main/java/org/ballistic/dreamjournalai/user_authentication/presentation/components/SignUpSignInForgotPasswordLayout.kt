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
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.core.Constants.LOGIN
import org.ballistic.dreamjournalai.core.Constants.SIGNUP
import org.ballistic.dreamjournalai.user_authentication.presentation.forgot_password.components.ForgotPassword
import org.ballistic.dreamjournalai.user_authentication.presentation.sign_up.components.SignUp
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel.MainScreenViewModel
import org.ballistic.dreamjournalai.user_authentication.presentation.sign_in.components.SignIn

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SignUpSignInForgotPasswordLayout(
    mainScreenViewModel: MainScreenViewModel= hiltViewModel(),
    viewModel: AuthViewModel,
    pagerState: PagerState
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .background(Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        if (!viewModel.isForgotPasswordLayout.value) {
            Row {
                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    visible = pagerState.currentPage == 3
                ) {
                    LoginOrSignupTab(
                        text = "Login",
                        isLoginLayout = viewModel.isLoginLayout.value,
                        isSignUpLayout = viewModel.isSignUpLayout.value,
                        isClicked = {
                            viewModel.isLoginLayout.value = true
                            viewModel.isSignUpLayout.value = false
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
                        isLoginLayout = viewModel.isLoginLayout.value,
                        isSignUpLayout = viewModel.isSignUpLayout.value,
                        isClicked = {
                            viewModel.isLoginLayout.value = false
                            viewModel.isSignUpLayout.value = true
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
                viewModel.isLoginLayout.value -> {
                    LoginLayout(
                        viewModel = viewModel,
                        login = { email, password ->
                            scope.launch {
                                viewModel.loginWithEmailAndPassword(email, password)
                            }
                        },
                        pagerState = pagerState
                    )
                }
                viewModel.isForgotPasswordLayout.value -> {
                    ForgotPasswordLayout(
                        viewModel = viewModel,
                        sendPasswordResetEmail = { viewModel.sendPasswordResetEmail(it) },
                        pagerState = pagerState,
                    )
                }
                else -> {
                    SignupLayout(viewModel = viewModel, signup = { email, password ->
                        scope.launch {
                            viewModel.signUpWithEmailAndPassword(email, password)
                        }
                    }, pagerState = pagerState)
                }
            }
        }
    }

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ForgotPasswordLayout(
    viewModel: AuthViewModel,
    sendPasswordResetEmail: (email: String) -> Unit,
    pagerState: PagerState,
) {


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            email = viewModel.forgotPasswordEmail,
            pagerState = pagerState,
        )

        Button(
            onClick = {
                sendPasswordResetEmail(viewModel.forgotPasswordEmail.value)
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
                viewModel.isForgotPasswordLayout.value = false
                viewModel.isLoginLayout.value = true
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
            viewModel.isLoginLayout.value = true
            viewModel.isForgotPasswordLayout.value = false
        },
        showResetPasswordMessage = { /*TODO*/ },
        showErrorMessage = { /*TODO*/ },
    )
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalPagerApi::class)
@Composable
fun SignupLayout(
    viewModel: AuthViewModel,
    signup: (email: String, password: String) -> Unit,
    pagerState: PagerState,
) {
    val keyboard = LocalSoftwareKeyboardController.current


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            email = viewModel.signUpEmail,
            pagerState = pagerState,
        )

        PasswordField(
            password = viewModel.signUpPassword,
            onPasswordValueChange = { newValue ->

            },
            forgotPassword = { viewModel.isForgotPasswordLayout.value = true },
            viewModel = viewModel
        )

        Button(
            modifier = Modifier.fillMaxWidth(.5f),
            onClick = {
                keyboard?.hide()
                signup(viewModel.signUpEmail.value, viewModel.signUpPassword.value)
                viewModel.sendEmailVerification()
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

    SignUp(
        sendEmailVerification = {  },
        showVerifyEmailMessage = { /*TODO*/ })

}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalPagerApi::class)
@Composable
fun LoginLayout(
    viewModel: AuthViewModel,
    login: (email: String, password: String) -> Unit,
    pagerState: PagerState,
) {
    val keyboard = LocalSoftwareKeyboardController.current


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(
            email = viewModel.loginEmail,
            pagerState = pagerState,
        )

        PasswordField(
            password = viewModel.loginPassword,
            onPasswordValueChange = { newValue ->

            },
            forgotPassword = {
                viewModel.isForgotPasswordLayout.value = true
                viewModel.isLoginLayout.value = false
            },
            viewModel = viewModel
        )

        Button(
            modifier = Modifier
                .fillMaxWidth(.5f),
            onClick = {
                keyboard?.hide()
                login(viewModel.loginEmail.value, viewModel.loginPassword.value)
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

    SignIn(showErrorMessage = { /*TODO*/ })

}

@Composable
fun LoginOrSignupTab(
    text: String,
    modifier: Modifier = Modifier,
    isLoginLayout: Boolean,
    isSignUpLayout: Boolean,
    isClicked: () -> Unit
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