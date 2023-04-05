package org.ballistic.dreamjournalai.user_authentication.presentation.forgot_password.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.user_authentication.presentation.components.ProgressBar
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModelState


@Composable
fun ForgotPassword(
    authViewModelState: AuthViewModelState,
    navigateBack: () -> Unit,
    showResetPasswordMessage: () -> Unit,
    showErrorMessage: (errorMessage: String?) -> Unit
) {
    HandleSendPasswordResetEmailResponse(
        authViewModelState.sendPasswordResetEmailResponse.value,
        navigateBack,
        showResetPasswordMessage,
        showErrorMessage
    )
}

@Composable
private fun HandleSendPasswordResetEmailResponse(
    sendPasswordResetEmailResponse: Resource<Boolean>,
    navigateBack: () -> Unit,
    showResetPasswordMessage: () -> Unit,
    showErrorMessage: (errorMessage: String?) -> Unit
) {
    when (sendPasswordResetEmailResponse) {
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
            LaunchedEffect(Unit) {
                println("Error")
                showErrorMessage(sendPasswordResetEmailResponse.message)
            }
        }
    }
}

