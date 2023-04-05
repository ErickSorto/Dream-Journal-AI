package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.identity.BeginSignInResult
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components.ProgressBar
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.AuthEvent
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModelState

@Composable
fun OneTapSignIn(
    authViewModelState: AuthViewModelState,
    launch: (result: BeginSignInResult) -> Unit
) {
    when (val oneTapSignInResponse = authViewModelState.oneTapSignInResponse.value) {
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


