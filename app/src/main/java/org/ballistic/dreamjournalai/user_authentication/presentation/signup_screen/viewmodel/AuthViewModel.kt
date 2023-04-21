package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.core.Resource
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
            is AuthEvent.EnteredLoginEmail -> {
                _state.value = _state.value.copy(
                    loginEmail = event.email
                )
            }
            is AuthEvent.EnteredLoginPassword -> {
                _state.value = _state.value.copy(
                    loginPassword = event.password
                )
            }
            is AuthEvent.EnteredSignUpEmail -> {
                _state.value = _state.value.copy(
                    signUpEmail = event.email
                )
            }
            is AuthEvent.EnteredSignUpPassword -> {
                _state.value = _state.value.copy(
                    signUpPassword = event.password
                )
            }
            is AuthEvent.EnteredForgotPasswordEmail -> {
                _state.value = _state.value.copy(
                    forgotPasswordEmail = event.email
                )
            }
            is AuthEvent.ReloadUser -> {
                reloadUser()
            }
            is AuthEvent.SignOut -> {
                signOut()
            }
            is AuthEvent.RevokeAccess -> {
                revokeAccess(event.password, onSuccess = event.onSuccess)
            }
        }
    }

    private fun oneTapSignIn() = viewModelScope.launch {
        _state.value.oneTapSignInResponse.value = Resource.Loading()
        val response = repo.oneTapSignInWithGoogle()
        _state.value.oneTapSignInResponse.value = response
    }

    private suspend fun signInWithGoogle(googleCredential: AuthCredential) = repo.firebaseSignInWithGoogle(
        googleCredential
    ).onEach { result ->
        when (result) {
            is Resource.Success -> {
                _state.value = _state.value.copy(
                    login = MutableStateFlow(LoginState(isLoggedIn = true)),
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

    private suspend fun loginWithEmailAndPassword(email: String, password: String) =
        repo.firebaseSignInWithEmailAndPassword(email, password).onEach { result ->
            if (_state.value.loginEmail.isEmpty() || _state.value.loginPassword.isEmpty()) {
                _state.value.loginErrorMessage.value = "Email or password is empty"
            } //incorrect format
            else if (!_state.value.loginEmail.contains("@") || !_state.value.loginEmail.contains(".")) {
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
                    _state.value = _state.value.copy(login = MutableStateFlow(LoginState(isLoggedIn = true)))
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(login = MutableStateFlow(LoginState(error = result.message ?: "Error")))
                }
            }
        }.launchIn(viewModelScope)

    private suspend fun signUpWithEmailAndPassword(email: String, password: String) =
        viewModelScope.launch {
            if (_state.value.signUpEmail.isEmpty() || _state.value.signUpPassword.isEmpty()) {
                _state.value.signUpErrorMessage.value = "Email or password is empty"
            } //incorrect format
            else if (!_state.value.signUpEmail.contains("@") || !_state.value.signUpEmail.contains(".")) {
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

    private fun signOut() = viewModelScope.launch {
        repo.signOut()
    }

    private suspend fun revokeAccess(password: String?, onSuccess: () -> Unit) = viewModelScope.launch {

        repo.revokeAccess(
            password,
        ).onEach {
            when (it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(revokeAccess = MutableStateFlow(RevokeAccessState(isRevoked = true)))
                    onSuccess()
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(revokeAccess = MutableStateFlow(RevokeAccessState(error = it.message ?: "Error")))
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(revokeAccess = MutableStateFlow(RevokeAccessState(isLoading = true)))
                }
            }
        }.launchIn(viewModelScope)
    }
}

data class AuthViewModelState(
    val repo: AuthRepository,
    val user: FirebaseUser? = repo.currentUser,
    val loginEmail: String = "",
    val loginPassword: String = "",
    val signUpEmail: String = "",
    val signUpPassword: String = "",
    val forgotPasswordEmail: String = "",
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
    val reloadUserResponse: MutableState<Resource<ReloadUserResponse>> = mutableStateOf(Resource.Success()),
    val loginErrorMessage: MutableState<String> = mutableStateOf(""),
    val signUpErrorMessage: MutableState<String> = mutableStateOf(""),
    val login: StateFlow<LoginState> = MutableStateFlow(LoginState()),
    val signUp: StateFlow<SignUpState> = MutableStateFlow(SignUpState()),
    val emailVerification: StateFlow<VerifyEmailState> = MutableStateFlow(VerifyEmailState()),
    val revokeAccess: StateFlow<RevokeAccessState> = MutableStateFlow(RevokeAccessState()),
    val isUserExist: StateFlow<Boolean> = repo.isUserExist,
    val emailVerified: StateFlow<Boolean> = repo.emailVerified,
    val isLoggedIn: StateFlow<Boolean> = repo.isLoggedIn,
)
data class VerifyEmailState(
    val verified : Boolean = false,
    val sent: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = ""
)

data class RevokeAccessState(
    val isRevoked: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = ""
)
data class LoginState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = ""
)

data class SignUpState(
    val signUp: AuthResult? = null,
    val isLoading: Boolean = false,
    val error: String = ""
)