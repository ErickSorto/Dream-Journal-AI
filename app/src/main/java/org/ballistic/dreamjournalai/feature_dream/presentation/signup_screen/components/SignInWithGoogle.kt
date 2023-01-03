package org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.domain.repository.SignInWithGoogleResponse
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.AuthViewModel

@Composable
fun SignInWithGoogle(
    viewModel: AuthViewModel = hiltViewModel(),
    navigateToHomeScreen: (signedIn: Boolean) -> Unit
) {
    when(val signInWithGoogleResponse = viewModel.signInWithGoogleResponse) {
        is Resource.Loading -> ProgressBar()
        is Resource.Success -> LaunchedEffect(viewModel.hasSignedIn) {
            navigateToHomeScreen(viewModel.hasSignedIn)
        }
        is Resource.Error -> LaunchedEffect(Unit) {
            print(signInWithGoogleResponse.message)
        }
    }
}
