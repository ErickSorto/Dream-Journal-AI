package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel

import androidx.compose.runtime.MutableState
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
import org.ballistic.dreamjournalai.feature_dream.presentation.dream_list_screen.state.DreamsState
import org.ballistic.dreamjournalai.user_authentication.domain.repository.*
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.AuthEvent

import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    oneTapClient: SignInClient
) : ViewModel() {
    private val _state = MutableStateFlow(AuthViewModelState(
        repo = repo,
        oneTapClient = oneTapClient,
    ))
    val state: StateFlow<AuthViewModelState> = _state.asStateFlow()


     fun onEvent(event: AuthEvent) = viewModelScope.launch {
        when (event) {
            is AuthEvent.OneTapSignIn -> {
                oneTapSignIn()
            }
            is AuthEvent.SignInWithGoogle -> {
                signInWithGoogle(event.googleCredential)
            }
            is AuthEvent.LoginWithEmailAndPassword -> {
                loginWithEmailAndPassword(event.email, event.password)
            }
            is AuthEvent.SignUpWithEmailAndPassword -> {
                signUpWithEmailAndPassword(event.email, event.password)
            }
            AuthEvent.SendEmailVerification -> {
                sendEmailVerification()
            }
            is AuthEvent.SendPasswordResetEmail -> {
                sendPasswordResetEmail(event.email)
            }
            AuthEvent.ReloadUser -> {
                reloadUser()
            }
            AuthEvent.SignOut -> {
                signOut()
            }
            AuthEvent.RevokeAccess -> {
                revokeAccess()
            }
        }
    }

    private fun oneTapSignIn() = viewModelScope.launch {
        _state.value.oneTapSignInResponse.value = Resource.Loading()
        val response = repo.oneTapSignInWithGoogle()
        _state.value.oneTapSignInResponse.value = response
    }

    suspend fun signInWithGoogle(googleCredential: AuthCredential) = repo.firebaseSignInWithGoogle(
        googleCredential
    ).onEach { result ->
        when (result) {
            is Resource.Success -> {
                _state.value = _state.value.copy(
                    login = MutableStateFlow(LoginState(login = result.data)),
                    signInWithGoogleResponse = MutableStateFlow(Resource.Success(result.data))
                )
            }
            is Resource.Error -> {
                _state.value = _state.value.copy(
                    login = MutableStateFlow(LoginState(error = result.message ?: "Error")),
                    signInWithGoogleResponse = MutableStateFlow(Resource.Error(result.message ?: "Error"))
                )
            }
            is Resource.Loading -> {
                _state.value = _state.value.copy(
                    login = MutableStateFlow(LoginState(isLoading = true)),
                    signInWithGoogleResponse = MutableStateFlow(Resource.Loading())
                )
            }
        }
    }.launchIn(viewModelScope)

    suspend fun loginWithEmailAndPassword(email: String, password: String) =
        repo.firebaseSignInWithEmailAndPassword(email, password).onEach { result ->
            if (_state.value.loginEmail.value.isEmpty() || _state.value.loginPassword.value.isEmpty()) {
                _state.value.loginErrorMessage.value = "Email or password is empty"
            } //incorrect format
            else if (!_state.value.loginEmail.value.contains("@") || !_state.value.loginEmail.value.contains(".")) {
                _state.value.loginErrorMessage.value = "Email format is incorrect"
            } //incorrect password
            else {
                _state.value.loginErrorMessage.value = "Wrong email or password"
            }

            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(login = MutableStateFlow(LoginState(isLoading = true)))
                }
                is Resource.Success -> {
                    _state.value = _state.value.copy(login = MutableStateFlow(LoginState(login = result.data)))
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(login = MutableStateFlow(LoginState(error = result.message ?: "Error")))
                }
            }
        }.launchIn(viewModelScope)

    private suspend fun signUpWithEmailAndPassword(email: String, password: String) =
        viewModelScope.launch {
            if (_state.value.signUpEmail.value.isEmpty() || _state.value.signUpPassword.value.isEmpty()) {
                _state.value.signUpErrorMessage.value = "Email or password is empty"
            } //incorrect format
            else if (!_state.value.signUpEmail.value.contains("@") || !_state.value.signUpEmail.value.contains(".")) {
                _state.value.signUpErrorMessage.value = "Email format is incorrect"
            } else {
                _state.value.signUpErrorMessage.value = ""
            }
            repo.firebaseSignUpWithEmailAndPassword(email, password).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(signUp = MutableStateFlow(SignUpState(signUp = result.data)))
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(signUp = MutableStateFlow(SignUpState(error = result.message ?: "Error")))
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(signUp = MutableStateFlow(SignUpState(isLoading = true)))
                    }
                }
            }.launchIn(viewModelScope)
        }

    private suspend fun sendEmailVerification() = viewModelScope.launch {
        repo.sendEmailVerification().onEach {
            when (it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(emailVerification = MutableStateFlow(VerifyEmailState(sent = true)))
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(emailVerification = MutableStateFlow(VerifyEmailState(error = it.message ?: "Error")))
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(emailVerification = MutableStateFlow(VerifyEmailState(isLoading = true)))
                }
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun sendPasswordResetEmail(email: String) = viewModelScope.launch {
        _state.value = _state.value.copy(sendPasswordResetEmailResponse = mutableStateOf(Resource.Loading()))
        _state.value = _state.value.copy(sendPasswordResetEmailResponse = mutableStateOf(repo.sendPasswordResetEmail(email)))
    }

    private suspend fun reloadUser() = viewModelScope.launch {
        _state.value = _state.value.copy(reloadUserResponse = mutableStateOf(Resource.Loading()))
        _state.value = _state.value.copy(reloadUserResponse = mutableStateOf(Resource.Success(repo.reloadFirebaseUser())))
    }

    private fun signOut() = repo.signOut()

    private suspend fun revokeAccess() = viewModelScope.launch {
        _state.value = _state.value.copy(revokeAccessResponse = mutableStateOf(Resource.Loading()))
        _state.value = _state.value.copy(revokeAccessResponse = mutableStateOf(Resource.Success(repo.revokeAccess())))
    }
}

data class AuthViewModelState(
    val repo: AuthRepository,
    val loginEmail: MutableState<String> = mutableStateOf(""),
    val loginPassword: MutableState<String> = mutableStateOf(""),
    val signUpEmail: MutableState<String> = mutableStateOf(""),
    val signUpPassword: MutableState<String> = mutableStateOf(""),
    val forgotPasswordEmail: MutableState<String> = mutableStateOf(""),
    val isEmailVerified: MutableState<Boolean> = mutableStateOf(repo.currentUser?.isEmailVerified ?: false),
    val isCurrentUserExist: MutableState<Boolean> = mutableStateOf(repo.currentUser != null),
    val oneTapClient: SignInClient? = null,
    val isLoginLayout: MutableState<Boolean> = mutableStateOf(true),
    val isSignUpLayout: MutableState<Boolean> = mutableStateOf(false),
    val isForgotPasswordLayout: MutableState<Boolean> = mutableStateOf(false),
    val oneTapSignInResponse: MutableState<OneTapSignInResponse> = mutableStateOf(Resource.Success()),
    val signInWithGoogleResponse: MutableStateFlow<Resource<AuthResult>> = MutableStateFlow(Resource.Loading()),
    val signInResponse: MutableState<Resource<SignInResponse>> = mutableStateOf(Resource.Success()),
    val signUpResponse: MutableState<Resource<SignUpResponse>> = mutableStateOf(Resource.Success()),
    val sendEmailVerificationResponse: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Success()),
    val sendPasswordResetEmailResponse: MutableState<SendPasswordResetEmailResponse> = mutableStateOf(Resource.Success()),
    val revokeAccessResponse: MutableState<Resource<RevokeAccessResponse>> = mutableStateOf(Resource.Success()),
    val reloadUserResponse: MutableState<Resource<ReloadUserResponse>> = mutableStateOf(Resource.Success()),
    val loginErrorMessage: MutableState<String> = mutableStateOf(""),
    val signUpErrorMessage: MutableState<String> = mutableStateOf(""),
    val login: StateFlow<LoginState> = MutableStateFlow(LoginState()),
    val signUp: StateFlow<SignUpState> = MutableStateFlow(SignUpState()),
    val emailVerification: StateFlow<VerifyEmailState> = MutableStateFlow(VerifyEmailState())
)
data class VerifyEmailState(
    val verified : Boolean = false,
    val sent: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = ""
)
data class LoginState(
    val login: AuthResult? = null,
    val isLoading: Boolean = false,
    val error: String = ""
)

data class SignUpState(
    val signUp: AuthResult? = null,
    val isLoading: Boolean = false,
    val error: String = ""
)