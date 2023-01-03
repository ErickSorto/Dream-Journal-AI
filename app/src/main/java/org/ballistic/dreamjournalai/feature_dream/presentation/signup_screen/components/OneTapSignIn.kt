package org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.identity.BeginSignInResult
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.AuthViewModel

@Composable
fun OneTapSignIn(
    viewModel: AuthViewModel = hiltViewModel(),
    launch: (result: BeginSignInResult) -> Unit
) {
    when (val oneTapSignInResponse = viewModel.oneTapSignInResponse) {
        is Resource.Loading -> ProgressBar()
        is Resource.Success -> oneTapSignInResponse.data?.data?.let {
            LaunchedEffect(it) {
                launch(it)
            }
        }
        is Resource.Error -> LaunchedEffect(Unit) {
            print(oneTapSignInResponse)
        }
    }
}


