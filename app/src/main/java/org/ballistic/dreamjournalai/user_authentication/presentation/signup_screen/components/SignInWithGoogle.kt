package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.AuthResult
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModelState

@Composable
fun SignInWithGoogle(
    authViewModelState: AuthViewModelState,
    navigateToHomeScreen: (signedIn: Boolean) -> Unit
) {
    val signInWithGoogleResponse by authViewModelState.signInWithGoogleResponse.collectAsStateWithLifecycle()

    when (signInWithGoogleResponse) {
        is Resource.Loading<*> -> null
        is Resource.Success<*> -> (signInWithGoogleResponse as? Resource.Success<AuthResult>)?.data?.let { authResult ->
            if (authResult.user != null) {
                LaunchedEffect(authResult.user) {
                    navigateToHomeScreen(true)
                }
            }
        }
        is Resource.Error<*> -> LaunchedEffect(Unit) {
            print(signInWithGoogleResponse.message)
        }
    }
}
