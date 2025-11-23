package org.ballistic.dreamjournalai.shared.dream_account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.gitlive.firebase.auth.GoogleAuthProvider as FirebaseGoogleAuthProvider
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.ObserveAsEvents
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.core.components.TypewriterText
import org.ballistic.dreamjournalai.shared.dream_account.components.DreamAccountSettingsScreenTopBar
import org.ballistic.dreamjournalai.shared.dream_account.components.LogoutDeleteLayout
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.AnonymousButton
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.ObserveLoginState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.ObserverLogoutDeleteState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.SignupLoginLayout
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModelState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.RedOrange

@Composable
fun AccountSettingsScreen(
    loginViewModelState: LoginViewModelState,
    signupViewModelState: SignupViewModelState,
    onLoginEvent: (LoginEvent) -> Unit = {},
    onSignupEvent: (SignupEvent) -> Unit = {},
    navigateToOnboardingScreen: () -> Unit = {},
    navigateToDreamJournalScreen: () -> Unit = {}
) {
    val isLoading = loginViewModelState.isLoading
    val isUserAnonymous = loginViewModelState.isUserAnonymous
    val isEmailVerified = loginViewModelState.isEmailVerified
    val isUserLoggedIn = loginViewModelState.isLoggedIn
    val animationDisplay = remember { mutableStateOf(false) }
    var logoutInProgress by remember { mutableStateOf(false) }

    // Synchronous detection of logout transition (persisted across branches)
    var prevLoggedIn by remember { mutableStateOf(isUserLoggedIn) }
    val justLoggedOut = prevLoggedIn && !isUserLoggedIn
    SideEffect { prevLoggedIn = isUserLoggedIn }
    LaunchedEffect(justLoggedOut) {
        if (justLoggedOut) navigateToOnboardingScreen()
    }

    // Navigate immediately if user clicks logout
    LaunchedEffect(logoutInProgress) {
        if (logoutInProgress) navigateToOnboardingScreen()
    }

    // Navigate to Home only when transitioning from not-logged-in to logged-in+verified
    val loggedInAndVerified = isUserLoggedIn && isEmailVerified && !isUserAnonymous
    var prevLoggedInAndVerified by remember { mutableStateOf(loggedInAndVerified) }
    LaunchedEffect(loggedInAndVerified) {
        if (!prevLoggedInAndVerified && loggedInAndVerified) {
            navigateToDreamJournalScreen()
        }
        prevLoggedInAndVerified = loggedInAndVerified
    }

    Scaffold(
        topBar = {
            DreamAccountSettingsScreenTopBar(
            )
        },
        snackbarHost = {
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .navigationBarsPadding()
    ) {
        if (loginViewModelState.isEmailVerified &&
            loginViewModelState.isLoggedIn &&
            !loginViewModelState.isUserAnonymous
        ) {
            LogoutDeleteLayout(
                onLoginEvent = onLoginEvent,
                onLogoutClick = { logoutInProgress = true }
            )
        } else {
            // If we just logged out OR logout is in progress, suppress the entire sign-in UI
            if (!logoutInProgress && !justLoggedOut) {
                ObserveLoginState(
                    isLoggedIn = isUserLoggedIn,
                    isEmailVerified = isEmailVerified,
                    isUserAnonymous = isUserAnonymous,
                    isUserAnonymousAlready = true,
                    navigateToDreamJournalScreen = navigateToDreamJournalScreen,
                )

                ObserverLogoutDeleteState(
                    isLoggedIn = isUserLoggedIn,
                    isAnonymous = isUserAnonymous,
                    navigateToLoginScreen = navigateToOnboardingScreen
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(it)
                        .navigationBarsPadding()
                        .fillMaxSize()
                        .padding(),
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .background(
                                color = LightBlack.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                    ) {
                        Text(
                            text = "Log in to save your dreams! \n" +
                                    "You are currently using a guest account.",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = Color.White,
                        )
                    }

                    SignupLoginLayout(
                        loginViewModelState = loginViewModelState,
                        signupViewModelState = signupViewModelState,
                        onLoginEvent = { onLoginEvent(it) },
                        onSignupEvent = { onSignupEvent(it) },
                        onAnimationComplete = {
                            animationDisplay.value = true
                        },
                    )

                    // Add padding around the Google sign-in button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MyGoogleSignInButton(
                            { account ->
                                val googleCredential = FirebaseGoogleAuthProvider.credential(
                                    idToken = account.idToken,
                                    accessToken = account.accessTokenOrNonce
                                )
                                onLoginEvent(LoginEvent.SignInWithGoogle(googleCredential))
                            },
                            {
                                onLoginEvent(LoginEvent.ToggleLoading(false))
                                println("Google sign-in error: $it")
                            },
                            isLoading
                        )
                    }

                    if (!isUserAnonymous) {
                        AnonymousButton(
                            modifier = Modifier
                                .padding(bottom = 8.dp, top = 8.dp, start = 16.dp, end = 16.dp),
                            isVisible = true,
                            onClick = {
                                onLoginEvent(LoginEvent.ToggleLoading(true))
                                onSignupEvent(SignupEvent.AnonymousSignIn)
                            },
                            isEnabled = !isLoading
                        )
                    }

                    if (animationDisplay.value) {
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .background(
                                    color = RedOrange.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            TypewriterText(
                                text = "Warning: Guest accounts are deleted after 30 days of inactivity.",
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
