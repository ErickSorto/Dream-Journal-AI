package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.user_authentication.domain.repository.*

import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    val oneTapClient: SignInClient
) : ViewModel() {
    val loginEmail = mutableStateOf("")
    val loginPassword = mutableStateOf("")
    val signUpEmail = mutableStateOf("")
    val signUpPassword = mutableStateOf("")
    val forgotPasswordEmail = mutableStateOf("")

    val isCurrentUserExist = repo.isCurrentUserExist()
    val isEmailVerified get() = repo.currentUser?.isEmailVerified ?: false

    private val _login = MutableStateFlow(SignInState())
    val signIn: StateFlow<SignInState> = _login

    private val _signUp = MutableStateFlow(SignUpState())
    val signUp: StateFlow<SignUpState> = _signUp

    private val _emailVerification = MutableStateFlow(VerifyEmailState())
    val emailVerification: StateFlow<VerifyEmailState> = _emailVerification


    val isLoginLayout = mutableStateOf(true)
    val isSignUpLayout = mutableStateOf(false)
    val isForgotPasswordLayout = mutableStateOf(false)


    var oneTapSignInResponse by mutableStateOf<Resource<OneTapSignInResponse>>(Resource.Success())
        private set
    var signInWithGoogleResponse by mutableStateOf<Flow<Resource<AuthResult>>>(flow {})
        private set

    var signInResponse by mutableStateOf<Resource<SignInResponse>>(Resource.Success())
        private set
    var signUpResponse by mutableStateOf<SignUpResponse>(Resource.Success())
        private set
    var sendEmailVerificationResponse by mutableStateOf<Resource<Boolean>>(Resource.Success())
        private set

    var sendPasswordResetEmailResponse by mutableStateOf<SendPasswordResetEmailResponse>(Resource.Success())
        private set

    var revokeAccessResponse by mutableStateOf<Resource<RevokeAccessResponse>>(Resource.Success())
        private set

    var reloadUserResponse by mutableStateOf<Resource<ReloadUserResponse>>(Resource.Success())
        private set

    val loginErrorMessage = mutableStateOf("")
    val signUpErrorMessage = mutableStateOf("")


    fun oneTapSignIn() = viewModelScope.launch {
        oneTapSignInResponse = Resource.Loading()
        oneTapSignInResponse = Resource.Success(repo.oneTapSignInWithGoogle())
    }

    suspend fun signInWithGoogle(googleCredential: AuthCredential) = repo.firebaseSignInWithGoogle(
        googleCredential
    ).onEach { result ->
        when (result) {
            is Resource.Success -> {
                _login.emit(SignInState(login = result.data))
            }
            is Resource.Error -> {
                _login.emit(SignInState(error = result.message ?: "Error"))
            }
            is Resource.Loading -> {
                _login.emit(SignInState(isLoading = true))
            }
        }
    }.launchIn(viewModelScope)


    suspend fun loginWithEmailAndPassword(email: String, password: String) =
        repo.firebaseSignInWithEmailAndPassword(email, password).onEach { result ->
            if (loginEmail.value.isEmpty() || loginPassword.value.isEmpty()) {
                loginErrorMessage.value = "Email or password is empty"
            } //incorrect format
            else if (!loginEmail.value.contains("@") || !loginEmail.value.contains(".")) {
                loginErrorMessage.value = "Email format is incorrect"
            } //incorrect password
            else {
                loginErrorMessage.value = "Wrong email or password"
            }

            when (result) {
                is Resource.Loading -> {
                    _login.emit(SignInState(isLoading = true))
                }
                is Resource.Success -> {
                    _login.emit(SignInState(login = result.data))
                }
                is Resource.Error -> {
                    _login.emit(SignInState(error = result.message ?: "Error"))
                }
            }
        }.launchIn(viewModelScope)

    suspend fun signUpWithEmailAndPassword(email: String, password: String) =
        viewModelScope.launch {
            if (signUpEmail.value.isEmpty() || signUpPassword.value.isEmpty()) {
                signUpErrorMessage.value = "Email or password is empty"
            } //incorrect format
            else if (!signUpEmail.value.contains("@") || !signUpEmail.value.contains(".")) {
                signUpErrorMessage.value = "Email format is incorrect"
            }
            else {
                signUpErrorMessage.value = ""
            }
            repo.firebaseSignUpWithEmailAndPassword(email, password).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _signUp.emit(SignUpState(signUp = result.data))
                    }
                    is Resource.Error -> {
                        _signUp.emit(SignUpState(error = result.message ?: "Error"))
                    }
                    is Resource.Loading -> {
                        _signUp.emit(SignUpState(isLoading = true))
                    }
                }
            }.launchIn(viewModelScope)
        }

    fun sendEmailVerification() = viewModelScope.launch {
        repo.sendEmailVerification().onEach {
            when (it) {
                is Resource.Success -> {
                    _emailVerification.emit(VerifyEmailState(sent = true))
                }
                is Resource.Error -> {
                    _emailVerification.emit(VerifyEmailState(error = it.message ?: "Error"))
                }
                is Resource.Loading -> {
                    _emailVerification.emit(VerifyEmailState(isLoading = true))
                }
            }
        }.launchIn(viewModelScope)
    }

    fun sendPasswordResetEmail(email: String) = viewModelScope.launch {
        sendPasswordResetEmailResponse = Resource.Loading()
        sendPasswordResetEmailResponse = repo.sendPasswordResetEmail(email)
    }

    fun reloadUser() = viewModelScope.launch {
        reloadUserResponse = Resource.Loading()
        reloadUserResponse = Resource.Success(repo.reloadFirebaseUser())
    }

    fun signOut() = repo.signOut()

    fun revokeAccess() = viewModelScope.launch {
        revokeAccessResponse = Resource.Loading()
        revokeAccessResponse = Resource.Success(repo.revokeAccess())
    }
}


data class VerifyEmailState(
    val verified : Boolean = false,
    val sent: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = ""
)
data class SignInState(
    val login: AuthResult? = null,
    val isLoading: Boolean = false,
    val error: String = ""
)

data class SignUpState(
    val signUp: AuthResult? = null,
    val isLoading: Boolean = false,
    val error: String = ""
)