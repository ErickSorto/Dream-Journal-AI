package org.ballistic.dreamjournalai.user_authentication.presentation.sign_up.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components.ProgressBar
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel


@Composable
fun SendEmailVerification(
    viewModel: AuthViewModel = hiltViewModel()
) {
    when(val sendEmailVerificationResponse = viewModel.sendEmailVerificationResponse) {
        is Resource.Loading -> ProgressBar()
        is Resource.Success -> Unit
        is Resource.Error -> sendEmailVerificationResponse.apply {
            LaunchedEffect(Unit) {
                print(message)
            }
        }
    }
}