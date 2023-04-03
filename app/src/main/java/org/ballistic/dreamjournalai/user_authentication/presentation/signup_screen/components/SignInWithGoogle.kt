package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.collect
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components.ProgressBar
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel

@Composable
fun SignInWithGoogle(
    viewModel: AuthViewModel = hiltViewModel(),
    navigateToHomeScreen: (signedIn: Boolean) -> Unit
) {
    val signInWithGoogleResponse by viewModel.signInWithGoogleResponse.collectAsState(initial = Resource.Loading())

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
