package org.ballistic.dreamjournalai.shared.dream_account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel.MainScreenViewModelState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.RedOrange

@Composable
fun AccountSettingsScreen(
    loginViewModelState: LoginViewModelState,
    signupViewModelState: SignupViewModelState,
    mainScreenViewModelState: MainScreenViewModelState,
    onLoginEvent: (LoginEvent) -> Unit = {},
    onSignupEvent: (SignupEvent) -> Unit = {},
    navigateToOnboardingScreen: () -> Unit = {},
    navigateToDreamJournalScreen: () -> Unit = {},
    onMainEvent: (org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent) -> Unit = {}
) {
    val isLoading = loginViewModelState.isLoading
    val isUserAnonymous = loginViewModelState.isUserAnonymous
    val isEmailVerified = loginViewModelState.isEmailVerified
    val isUserLoggedIn = loginViewModelState.isLoggedIn
    val animationDisplay = remember { mutableStateOf(false) }

    // Navigate to Home only when transitioning from not-logged-in to logged-in+verified
    val loggedInAndVerified = isUserLoggedIn && isEmailVerified && !isUserAnonymous
    var prevLoggedInAndVerified by remember { mutableStateOf(loggedInAndVerified) }
    LaunchedEffect(loggedInAndVerified) {
        if (!prevLoggedInAndVerified && loggedInAndVerified) {
            navigateToDreamJournalScreen()
        }
        prevLoggedInAndVerified = loggedInAndVerified
    }

    // Local SnackbarHostState owned by the composable
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Observe global snackbar events and show them here
    ObserveAsEvents(
        flow = SnackbarController.events,
        key1 = snackbarHostState
    ) { event ->
        // show snackbar on UI scope
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = event.message
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                event.action?.action?.invoke()
            }
        }
    }

    Scaffold(
        topBar = {
            DreamAccountSettingsScreenTopBar(
                mainScreenViewModelState = mainScreenViewModelState,
                onOpenDrawer = { onMainEvent(org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent.ToggleDrawerState(androidx.compose.material3.DrawerValue.Open)) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 64.dp)
    ) {
        if (loginViewModelState.isEmailVerified &&
            loginViewModelState.isLoggedIn &&
            !loginViewModelState.isUserAnonymous
        ) {
            LogoutDeleteLayout(
                loginViewModelState = loginViewModelState,
                onLoginEvent = onLoginEvent
            )
        } else {
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

                if (
                    animationDisplay.value
                ) {
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


// `MyGoogleSignInButton` is implemented per-platform in androidMain/iosMain (see `GoogleSignInCompose.*`).
// Keep the SignInGoogleButton UI helper here — platform actuals call it.
