package org.ballistic.dreamjournalai.user_authentication.presentation.forgot_password.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.user_authentication.presentation.components.ProgressBar
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel


@Composable
fun ForgotPassword(
    viewModel: AuthViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    showResetPasswordMessage: () -> Unit,
    showErrorMessage: (errorMessage: String?) -> Unit
) {
    when(val sendPasswordResetEmailResponse = viewModel.sendPasswordResetEmailResponse) {
        is Resource.Loading -> ProgressBar()
        is Resource.Success -> {
            val isPasswordResetEmailSent = sendPasswordResetEmailResponse.data
            LaunchedEffect(isPasswordResetEmailSent) {
                if (isPasswordResetEmailSent == true) {
                    navigateBack()
                    showResetPasswordMessage()
                }
            }
        }
        is Resource.Error -> {
            sendPasswordResetEmailResponse.apply {
                LaunchedEffect(Unit) {
                    print("Error")
                    showErrorMessage(message)
                }
            }
        }
    }
}

