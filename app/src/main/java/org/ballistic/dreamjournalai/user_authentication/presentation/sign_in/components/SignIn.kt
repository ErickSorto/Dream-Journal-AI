package org.ballistic.dreamjournalai.user_authentication.presentation.sign_in.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.ListenableWorker.Result.Success
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components.ProgressBar
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel

@Composable
fun SignIn(
    viewModel: AuthViewModel = hiltViewModel(),
    showErrorMessage: (errorMessage: String?) -> Unit,
) {
    when(val signInResponse = viewModel.signInResponse) {
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