package org.ballistic.dreamjournalai.shared.dream_account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import org.ballistic.dreamjournalai.shared.core.components.TypewriterText
import org.ballistic.dreamjournalai.shared.dream_account.components.DreamAccountSettingsScreenTopBar
import org.ballistic.dreamjournalai.shared.dream_account.components.LogoutDeleteLayout
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.ObserveLoginState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.ObserverLogoutDeleteState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModelState
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page.OnboardingAuthCard


@Composable
fun AccountSettingsScreen(
    loginViewModelState: LoginViewModelState,
    signupViewModelState: SignupViewModelState,
    onLoginEvent: (LoginEvent) -> Unit = {},
    onSignupEvent: (SignupEvent) -> Unit = {},
    onMainEvent: (MainScreenEvent) -> Unit = {},
    navigateToOnboardingScreen: () -> Unit = {},
    navigateToDreamJournalScreen: () -> Unit = {}
) {
    val isLoading = loginViewModelState.isLoading
    val isUserAnonymous = loginViewModelState.isUserAnonymous
    val isEmailVerified = loginViewModelState.isEmailVerified
    val isUserLoggedIn = loginViewModelState.isLoggedIn
    var logoutInProgress by remember { mutableStateOf(false) }

    // Synchronous detection of logout transition (persisted across branches)
    var prevLoggedIn by remember { mutableStateOf(isUserLoggedIn) }
    val justLoggedOut = prevLoggedIn && !isUserLoggedIn
    SideEffect { prevLoggedIn = isUserLoggedIn }
    LaunchedEffect(justLoggedOut) {
        if (justLoggedOut) navigateToOnboardingScreen()
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

    LaunchedEffect(isUserAnonymous) {
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(!isUserAnonymous))
        onMainEvent(MainScreenEvent.SetTopBarState(!isUserAnonymous))
    }

    Scaffold(
        topBar = {
            if (!isUserAnonymous) {
                DreamAccountSettingsScreenTopBar(
                )
            }
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.widthIn(max = 520.dp)
                    ) {
                        OnboardingAuthCard(
                            enteredName = "",
                            loginViewModelState = loginViewModelState,
                            signupViewModelState = signupViewModelState,
                            isLoading = isLoading,
                            onLoginEvent = onLoginEvent,
                            onSignupEvent = onSignupEvent,
                            onBackClick = null,
                            eyebrowText = if (isUserAnonymous) "Guest account" else "Account setup",
                            titleOverride = if (isUserAnonymous) {
                                "Save your guest progress."
                            } else {
                                "Create your account."
                            },
                            subtitleOverride = if (isUserAnonymous) {
                                "Connect an account to keep your dreams, sync across devices, and make this guest journal permanent."
                            } else {
                                "Create or connect an account to sync your dreams and keep everything safe."
                            },
                            showGuestButton = !isUserAnonymous,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isUserAnonymous) {
                            GuestAccountDismissBubble(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 12.dp, end = 12.dp),
                                onClick = {
                                    onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(true))
                                    onMainEvent(MainScreenEvent.SetTopBarState(true))
                                    navigateToDreamJournalScreen()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestAccountDismissBubble(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color(0xFF5A3C92).copy(alpha = 0.34f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.22f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = "X",
            style = TextStyle(
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
