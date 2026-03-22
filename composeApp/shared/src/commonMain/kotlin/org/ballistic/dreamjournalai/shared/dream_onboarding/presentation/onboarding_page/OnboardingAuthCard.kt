package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.onboarding_page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ballistic.dreamjournalai.shared.dream_account.MyGoogleSignInButton
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.AnonymousButton
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.SignupLoginLayout
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModelState

private const val AuthStaggerMs: Long = 180
private const val AuthEnterDurationMs: Int = 300
private const val AuthSocialDelayMs: Long = 560

@Composable
fun OnboardingAuthCard(
    enteredName: String,
    loginViewModelState: LoginViewModelState,
    signupViewModelState: SignupViewModelState,
    isLoading: Boolean,
    onLoginEvent: (LoginEvent) -> Unit,
    onSignupEvent: (SignupEvent) -> Unit,
    onBackClick: (() -> Unit)?,
    eyebrowText: String = "Account step",
    titleOverride: String? = null,
    subtitleOverride: String? = null,
    backText: String = "Back to dream path",
    showGuestButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val authContentVisible = remember { mutableStateOf(false) }
    val socialButtonsVisible = remember { mutableStateOf(false) }
    val shouldAnimateFields = remember { mutableStateOf(false) }
    val authHeroEntered = remember { mutableStateOf(false) }
    val hasPlayedEntryAnimation = remember { mutableStateOf(false) }
    val layoutMode = when {
        loginViewModelState.isLoginLayout -> "login"
        loginViewModelState.isSignUpLayout -> "signup"
        loginViewModelState.isForgotPasswordLayout -> "forgot"
        else -> "login"
    }
    val supportsSocialButtons = layoutMode != "forgot"
    val authTitle = titleOverride ?: if (enteredName.isBlank()) {
        "Save your dream path."
    } else {
        "${enteredName.trim()}, save your dream path."
    }
    val authSubtitle = subtitleOverride
        ?: "Create your account to keep your 7-night plan, sync your journal, and continue where you left off."
    val titleFontSize = if (authTitle.length > 28) 21.sp else 24.sp
    val titleLineHeight = if (authTitle.length > 28) 28.sp else 32.sp

    LaunchedEffect(authHeroEntered.value) {
        if (authHeroEntered.value && !hasPlayedEntryAnimation.value) {
            authContentVisible.value = false
            socialButtonsVisible.value = false
            shouldAnimateFields.value = false
            kotlinx.coroutines.delay(120)
            authContentVisible.value = true
            shouldAnimateFields.value = true
            kotlinx.coroutines.delay(AuthSocialDelayMs)
            socialButtonsVisible.value = supportsSocialButtons
            hasPlayedEntryAnimation.value = true
        }
    }

    LaunchedEffect(layoutMode, hasPlayedEntryAnimation.value) {
        if (hasPlayedEntryAnimation.value) {
            authContentVisible.value = true
            shouldAnimateFields.value = false
            socialButtonsVisible.value = supportsSocialButtons
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(34.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF412C74).copy(alpha = 0.92f),
                        Color(0xFF24184A).copy(alpha = 0.86f),
                        Color(0xFF120F2A).copy(alpha = 0.82f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(34.dp)
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = eyebrowText,
            style = TextStyle(
                color = Color(0xFFFFE0C9),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Text(
            text = authTitle,
            modifier = Modifier.fillMaxWidth(0.98f),
            style = TextStyle(
                color = Color.White,
                fontSize = titleFontSize,
                lineHeight = titleLineHeight,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = authSubtitle,
            modifier = Modifier.fillMaxWidth(0.98f),
            style = TextStyle(
                color = Color.White.copy(alpha = 0.76f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        OnboardingPageHeroImage(
            visible = true,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .align(Alignment.CenterHorizontally)
        ) { entered ->
            authHeroEntered.value = entered
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.07f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = authContentVisible.value,
                    enter = slideInHorizontally(
                        animationSpec = tween(AuthEnterDurationMs + 140, easing = FastOutSlowInEasing),
                        initialOffsetX = { 800 }
                    ),
                    exit = fadeOut(animationSpec = tween(220))
                ) {
                    SignupLoginLayout(
                        loginViewModelState = loginViewModelState,
                        signupViewModelState = signupViewModelState,
                        onLoginEvent = onLoginEvent,
                        onSignupEvent = onSignupEvent,
                        loginShouldAnimate = shouldAnimateFields.value && layoutMode == "login",
                        signupShouldAnimate = shouldAnimateFields.value && layoutMode == "signup"
                    )
                }
            }
        }

        OnboardingAuthDivider(text = if (showGuestButton) "Or continue with" else "Or connect with")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MyGoogleSignInButton(
                modifier = if (showGuestButton) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                onGotToken = { account ->
                    val googleCredential =
                        dev.gitlive.firebase.auth.GoogleAuthProvider.credential(
                            idToken = account.idToken,
                            accessToken = account.accessTokenOrNonce
                        )
                    onLoginEvent(LoginEvent.SignInWithGoogle(googleCredential))
                },
                onError = {
                    onLoginEvent(LoginEvent.ToggleLoading(false))
                    println("Google sign-in error: $it")
                },
                isLoading = isLoading,
                isVisible = socialButtonsVisible.value,
                label = "Google"
            )

            if (showGuestButton) {
                AnonymousButton(
                    modifier = Modifier.weight(1f),
                    isVisible = socialButtonsVisible.value,
                    onClick = {
                        onSignupEvent(SignupEvent.AnonymousSignIn)
                    },
                    isEnabled = !isLoading,
                    label = "Guest"
                )
            }
        }

        if (onBackClick != null) {
            Text(
                text = backText,
                modifier = Modifier
                    .graphicsLayer(alpha = 0.92f)
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onBackClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = TextStyle(
                    color = Color(0xFFFFDFC6),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun OnboardingAuthDivider(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer(alpha = 0.8f)
                .background(Color.White.copy(alpha = 0.14f))
                .height(1.dp)
        )
        Text(
            text = text,
            style = TextStyle(
                color = Color.White.copy(alpha = 0.66f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer(alpha = 0.8f)
                .background(Color.White.copy(alpha = 0.14f))
                .height(1.dp)
        )
    }
}
