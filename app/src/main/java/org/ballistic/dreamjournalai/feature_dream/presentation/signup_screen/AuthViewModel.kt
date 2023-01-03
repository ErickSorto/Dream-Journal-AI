package org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.feature_dream.domain.repository.OneTapSignInResponse
import org.ballistic.dreamjournalai.feature_dream.domain.repository.SignInWithGoogleResponse

import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    val oneTapClient: SignInClient
): ViewModel() {
    val isUserAuthenticated get() = repo.isUserAuthenticatedInFirebase
    var hasSignedIn = false

    var oneTapSignInResponse by mutableStateOf<Resource<OneTapSignInResponse>>(Resource.Success())
        private set
    var signInWithGoogleResponse by mutableStateOf<Resource<SignInWithGoogleResponse>>(Resource.Success())
        private set

    fun oneTapSignIn() = viewModelScope.launch {
        oneTapSignInResponse = Resource.Loading()
        oneTapSignInResponse = Resource.Success(repo.oneTapSignInWithGoogle())
    }

    fun signInWithGoogle(googleCredential: AuthCredential) = viewModelScope.launch {
        oneTapSignInResponse = Resource.Loading()
        signInWithGoogleResponse = Resource.Success(repo.firebaseSignInWithGoogle(googleCredential))
        hasSignedIn = true
    }

    fun onEvent(event: AuthEvent){
        when(event){
            is AuthEvent.SignedIn -> {
                hasSignedIn = event.value
            }
            is AuthEvent.SignedOut -> {
                hasSignedIn = event.value
            }
        }
    }

    data class AuthState(
        val hasSignedIn: Boolean = false
    )
}