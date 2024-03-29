package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.auth.api.identity.BeginSignInResult
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.LoginViewModelState

@Composable
fun OneTapSignIn(
    loginViewModelState: LoginViewModelState,
    launch: (result: BeginSignInResult) -> Unit
) {
    when (val oneTapSignInResponse = loginViewModelState.oneTapSignInResponse.value) {
        is Resource.Loading -> ProgressBar()
        is Resource.Success -> oneTapSignInResponse.data?.let { result ->
            LaunchedEffect(result) {
                launch(result)
            }
        }
        is Resource.Error -> oneTapSignInResponse.apply {
            LaunchedEffect(Unit) {
                print(message)
            }
        }
    }
}


