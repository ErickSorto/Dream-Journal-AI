package org.ballistic.dreamjournalai.user_authentication.presentation.sign_in.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components.ProgressBar
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModelState

@Composable
fun LogIn(
    authViewModelState: AuthViewModelState,
    showErrorMessage: (errorMessage: String?) -> Unit,
) {
    when(val signInResponse = authViewModelState.signInResponse.value) {
        is Resource.Loading -> ProgressBar()
        is Resource.Success -> Unit
        is Resource.Error -> signInResponse.apply {
            LaunchedEffect(Unit) {
                print("SignIn: $message")
                showErrorMessage(message)
            }
        }
    }
}