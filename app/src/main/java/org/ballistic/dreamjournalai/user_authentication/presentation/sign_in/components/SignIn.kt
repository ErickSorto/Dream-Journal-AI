package org.ballistic.dreamjournalai.user_authentication.presentation.sign_in.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components.ProgressBar
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModelState

@Composable
fun SignIn(
    authViewModelState: AuthViewModelState,
    showErrorMessage: (errorMessage: String?) -> Unit,
) {
    HandleSignInResponse(
        authViewModelState.signInResponse.value,
        showErrorMessage
    )
}

@Composable
private fun HandleSignInResponse(
    signInResponse: Resource<Boolean>,
    showErrorMessage: (errorMessage: String?) -> Unit
) {
    when (signInResponse) {
        is Resource.Loading -> ProgressBar()
        is Resource.Success -> Unit
        is Resource.Error -> {
            LaunchedEffect(Unit) {
                println("SignIn: ${signInResponse.message}")
                showErrorMessage(signInResponse.message)
            }
        }
    }
}