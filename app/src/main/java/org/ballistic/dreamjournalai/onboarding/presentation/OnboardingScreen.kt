package org.ballistic.dreamjournalai.onboarding.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.*
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components.OneTapSignIn
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components.SignInButton
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components.SignInWithGoogle
import org.ballistic.dreamjournalai.onboarding.presentation.viewmodel.WelcomeViewModel
import org.ballistic.dreamjournalai.onboarding.util.OnBoardingPage
import org.ballistic.dreamjournalai.user_authentication.presentation.components.SignUpSignInForgotPasswordLayout
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.AuthEvent
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModelState

@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun OnboardingScreen(
    welcomeViewModel: WelcomeViewModel = hiltViewModel(),
    authViewModelState: AuthViewModelState,
    navigateToDreamJournalScreen: () -> Unit,
    onEvent: (AuthEvent) -> Unit,
    onDataLoaded: () -> Unit
) {
    val isUserExist = authViewModelState.isCurrentUserExist.value
    val loginState = authViewModelState.login.collectAsStateWithLifecycle()
    val signUpState = authViewModelState.signUp.collectAsStateWithLifecycle()
    val emailVerificationState = authViewModelState.emailVerification.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (isUserExist && emailVerificationState.value.verified) {
            navigateToDreamJournalScreen()
        }
    }

    LaunchedEffect(key1 = Unit) {
        delay(1500)
        onDataLoaded()
    }


    val pages = listOf(
        OnBoardingPage.First,
        OnBoardingPage.Second,
        OnBoardingPage.Third,
        OnBoardingPage.Fourth,
    )
    val pagerState = rememberPagerState()

    Box() {
        Image(
            painter = rememberAsyncImagePainter(org.ballistic.dreamjournalai.R.drawable.blue_lighthouse),
            contentDescription = "background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding()
    ) {
        HorizontalPager(
            modifier = Modifier.weight(10f),
            count = 4,
            state = pagerState,
            verticalAlignment = Alignment.Top,

            ) { position ->
             PagerScreen(
                onBoardingPage = pages[position], authViewModelState = authViewModelState,
                pagerState = pagerState,
                onEvent = { onEvent(it) },
            )
        }
        HorizontalPagerIndicator(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(1f),
            pagerState = pagerState
        )
    }
    val scope = rememberCoroutineScope()



    if (loginState.value.login != null && authViewModelState.isEmailVerified.value) {
        welcomeViewModel.saveOnBoardingState(completed = true)
        LaunchedEffect(Unit) {
            navigateToDreamJournalScreen()
        }
    }

//    if (signUpState.value.error != "" || emailVerificationState.value.isLoading) {
//        LaunchedEffect(Unit) {
//            mainScreenViewModel.snackbarHostState.value.showSnackbar(message = authViewModel.signUpErrorMessage.value, actionLabel = "Dismiss")
//        }
//    }
//    if(emailVerificationState.value.sent) {
//        LaunchedEffect(Unit) {
//            mainScreenViewModel.snackbarHostState.value.showSnackbar(message = "Verification email sent", actionLabel = "Dismiss")
//        }
//    }
//
//    if (loginState.value.error.isNotEmpty()) {
//        LaunchedEffect(Unit) {
//            mainScreenViewModel.snackbarHostState.value.showSnackbar(message = authViewModel.loginErrorMessage.value, actionLabel = "Dismiss")
//        }
//    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credentials =
                        authViewModelState.oneTapClient?.getSignInCredentialFromIntent(result.data)
                    val googleIdToken = credentials?.googleIdToken
                    val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
                    scope.launch {
                        onEvent(AuthEvent.SignInWithGoogle(googleCredentials))
                    }
                } catch (it: ApiException) {
                    print(it)
                }
            }
        }

    fun launch(signInResult: BeginSignInResult) {
        val intent = IntentSenderRequest.Builder(signInResult.pendingIntent.intentSender).build()
        launcher.launch(intent)
    }

    OneTapSignIn(launch = {
        launch(it)
    }, authViewModelState = authViewModelState)

    SignInWithGoogle(navigateToHomeScreen = { signedIn ->
        if (signedIn) {
            welcomeViewModel.saveOnBoardingState(completed = true)
            navigateToDreamJournalScreen()
        }
    }, authViewModelState = authViewModelState)
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun PagerScreen(
    onBoardingPage: OnBoardingPage,
    authViewModelState: AuthViewModelState,
    pagerState: PagerState,
    onEvent: (AuthEvent) -> Unit
) {
    if (onBoardingPage == OnBoardingPage.Fourth) {
        //sign in email and password text field
        SignUpPage(pagerState = pagerState, authViewModelState = authViewModelState, onEvent = { onEvent(it) })

    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(0.7f),
                painter = painterResource(id = onBoardingPage.image),
                contentDescription = "Pager Image"
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = onBoardingPage.title,
                fontSize = MaterialTheme.typography.h4.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .padding(top = 20.dp),
                text = onBoardingPage.description,
                fontSize = MaterialTheme.typography.subtitle1.fontSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SignUpPage(
    pagerState: PagerState,
    authViewModelState: AuthViewModelState,
    onEvent : (AuthEvent) -> Unit = {}
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SignInButton(
            onClick = {
                onEvent(AuthEvent.OneTapSignIn)
            },
            modifier = Modifier.fillMaxWidth(),
            pagerState = pagerState,
        )

        SignUpSignInForgotPasswordLayout(
            authViewModelState = authViewModelState, pagerState = pagerState
        ) {
            onEvent(it)
        }
    }
}