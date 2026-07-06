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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import dreamjournalai.composeapp.shared.generated.resources.*
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.dream_account.MyAppleSignInButton
import org.jetbrains.compose.resources.DrawableResource
import org.ballistic.dreamjournalai.shared.dream_account.MyGoogleSignInButton
import org.ballistic.dreamjournalai.shared.dream_authentication.PlatformAuthCapabilities
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.AnonymousButton
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.SignupLoginLayout
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModelState
import org.jetbrains.compose.resources.stringResource

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
    heroDrawableOverride: DrawableResource? = null,
    playEntryAnimation: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val authContentVisible = remember(playEntryAnimation) { mutableStateOf(!playEntryAnimation) }
    val socialButtonsVisible = remember(playEntryAnimation) { mutableStateOf(!playEntryAnimation) }
    val shouldAnimateFields = remember(playEntryAnimation) { mutableStateOf(false) }
    val authHeroEntered = remember(playEntryAnimation) { mutableStateOf(!playEntryAnimation) }
    val hasPlayedEntryAnimation = remember(playEntryAnimation) { mutableStateOf(!playEntryAnimation) }
    val snackbarHostState = remember { SnackbarHostState() }
    var googleProviderError by remember { mutableStateOf<String?>(null) }
    val layoutMode = when {
        loginViewModelState.isLoginLayout -> "login"
        loginViewModelState.isSignUpLayout -> "signup"
        loginViewModelState.isForgotPasswordLayout -> "forgot"
        else -> "login"
    }
    val signedInEmailUser =
        loginViewModelState.isLoggedIn && !loginViewModelState.isUserAnonymous
    val showEmailVerificationGate =
        !loginViewModelState.isEmailVerified &&
            (signedInEmailUser || signupViewModelState.verificationEmailSent)
    val supportsSocialButtons = layoutMode != "forgot" && !showEmailVerificationGate
    val showAppleSignIn = PlatformAuthCapabilities.supportsAppleSignIn
    val displayEyebrow = if (showEmailVerificationGate) {
        stringResource(Res.string.email_verification_eyebrow)
    } else {
        eyebrowText
    }
    val authTitle = if (showEmailVerificationGate) {
        stringResource(Res.string.email_verification_title)
    } else titleOverride ?: if (enteredName.isBlank()) {
        "Save your dream path."
    } else {
        "${enteredName.trim()}, save your dream path."
    }
    val authSubtitle = if (showEmailVerificationGate) {
        stringResource(Res.string.email_verification_subtitle)
    } else subtitleOverride
        ?: "Create your account to keep your 7-night plan, sync your journal, and continue where you left off."
    val titleFontSize = if (authTitle.length > 28) 21.sp else 24.sp
    val titleLineHeight = if (authTitle.length > 28) 28.sp else 32.sp

    LaunchedEffect(playEntryAnimation, supportsSocialButtons) {
        if (!playEntryAnimation) {
            authContentVisible.value = true
            socialButtonsVisible.value = supportsSocialButtons
            shouldAnimateFields.value = false
            authHeroEntered.value = true
            hasPlayedEntryAnimation.value = true
        }
    }

    LaunchedEffect(authHeroEntered.value) {
        if (!playEntryAnimation) return@LaunchedEffect
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

    LaunchedEffect(Unit) {
        if (!playEntryAnimation) return@LaunchedEffect
        kotlinx.coroutines.delay(620)
        if (!hasPlayedEntryAnimation.value) {
            authHeroEntered.value = true
        }
    }

    LaunchedEffect(layoutMode, hasPlayedEntryAnimation.value) {
        if (hasPlayedEntryAnimation.value) {
            authContentVisible.value = true
            shouldAnimateFields.value = false
            socialButtonsVisible.value = supportsSocialButtons
        }
    }

    AuthSnackbarHandler(snackbarHostState = snackbarHostState)

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
            text = displayEyebrow,
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
            drawableRes = heroDrawableOverride ?: Res.drawable.dream_onboarding_design,
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
                    if (showEmailVerificationGate) {
                        EmailVerificationGate(
                            isLoading = isLoading,
                            initialResendCooldownSeconds = if (signupViewModelState.verificationEmailSent) {
                                60
                            } else {
                                0
                            },
                            onCheckVerification = { onLoginEvent(LoginEvent.ReloadUser) },
                            onResendEmail = { onLoginEvent(LoginEvent.ResendEmailVerification) }
                        )
                    } else {
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
        }

        if (supportsSocialButtons) {
            OnboardingAuthDivider(text = if (showGuestButton) "Or continue with" else "Or connect with")
            val socialButtonsAreVisible = socialButtonsVisible.value && supportsSocialButtons

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MyGoogleSignInButton(
                        modifier = if (showAppleSignIn) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                        onGotToken = { account ->
                            googleProviderError = null
                            val googleCredential =
                                dev.gitlive.firebase.auth.GoogleAuthProvider.credential(
                                    idToken = account.idToken,
                                    accessToken = account.accessTokenOrNonce
                                )
                            onLoginEvent(LoginEvent.SignInWithGoogle(googleCredential))
                        },
                        onError = {
                            onLoginEvent(LoginEvent.ToggleLoading(false))
                            googleProviderError = it
                            println("Google sign-in error: $it")
                        },
                        isLoading = isLoading,
                        isVisible = socialButtonsAreVisible,
                        label = "Google"
                    )

                    if (showAppleSignIn) {
                        MyAppleSignInButton(
                            modifier = Modifier.weight(1f),
                            onGotToken = { account ->
                                googleProviderError = null
                                val appleCredential =
                                    dev.gitlive.firebase.auth.OAuthProvider.credential(
                                        providerId = "apple.com",
                                        idToken = account.idToken,
                                        rawNonce = account.accessTokenOrNonce
                                    )
                                onLoginEvent(LoginEvent.SignInWithApple(appleCredential))
                            },
                            onError = {
                                onLoginEvent(LoginEvent.ToggleLoading(false))
                                googleProviderError = it
                                println("Apple sign-in error: $it")
                            },
                            isLoading = isLoading,
                            isVisible = socialButtonsAreVisible,
                            label = "Apple"
                        )
                    }
                }

                if (showGuestButton) {
                    AnonymousButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSignupEvent(SignupEvent.AnonymousSignIn)
                        },
                        isEnabled = !isLoading,
                        isVisible = socialButtonsAreVisible,
                        label = "Guest"
                    )
                }
            }
        }

        AnimatedVisibility(visible = googleProviderError != null) {
            Text(
                text = googleProviderError.orEmpty(),
                color = Color(0xFFFFB4AB),
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )
    }
}

@Composable
private fun EmailVerificationGate(
    isLoading: Boolean,
    initialResendCooldownSeconds: Int,
    onCheckVerification: () -> Unit,
    onResendEmail: () -> Unit,
) {
    var resendCooldownSeconds by remember { mutableStateOf(initialResendCooldownSeconds) }

    LaunchedEffect(resendCooldownSeconds) {
        if (resendCooldownSeconds > 0) {
            kotlinx.coroutines.delay(1_000)
            resendCooldownSeconds -= 1
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFC66D),
                            Color(0xFFFF8BB7),
                            Color(0xFF8FCBFF)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.34f),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = null,
                tint = Color(0xFF24184A),
                modifier = Modifier.size(34.dp)
            )
        }

        Text(
            text = stringResource(Res.string.email_verification_body),
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        Button(
            onClick = onCheckVerification,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFC66D),
                contentColor = Color(0xFF1A1034),
                disabledContainerColor = Color(0xFFFFC66D).copy(alpha = 0.46f),
                disabledContentColor = Color(0xFF1A1034).copy(alpha = 0.58f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
        ) {
            Text(
                text = if (isLoading) {
                    stringResource(Res.string.email_verification_checking)
                } else {
                    stringResource(Res.string.email_verification_check_cta)
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val canResend = !isLoading && resendCooldownSeconds == 0
        Text(
            text = if (resendCooldownSeconds > 0) {
                stringResource(Res.string.email_verification_resend_cooldown, resendCooldownSeconds)
            } else {
                stringResource(Res.string.email_verification_resend_cta)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = canResend) {
                    resendCooldownSeconds = 60
                    onResendEmail()
                }
                .padding(vertical = 8.dp),
            style = TextStyle(
                color = if (canResend) {
                    Color(0xFF9DD7FF)
                } else {
                    Color.White.copy(alpha = 0.52f)
                },
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun AuthSnackbarHandler(
    snackbarHostState: SnackbarHostState
) {
    var eventToShow by remember { mutableStateOf<SnackbarEvent?>(null) }

    LaunchedEffect(Unit) {
        SnackbarController.events.collect { event ->
            eventToShow = event
        }
    }

    val currentEvent = eventToShow
    if (currentEvent != null) {
        val message = currentEvent.message.asString()
        val actionLabel = currentEvent.action?.name?.asString()

        LaunchedEffect(currentEvent, message, actionLabel) {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = currentEvent.duration
            )
            if (result == SnackbarResult.ActionPerformed) {
                currentEvent.action?.action?.invoke()
            }
            eventToShow = null
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
