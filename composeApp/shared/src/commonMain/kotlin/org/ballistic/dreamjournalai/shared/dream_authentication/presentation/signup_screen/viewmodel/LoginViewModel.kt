package org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.auth
import dreamjournalai.composeapp.shared.generated.resources.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.ReloadUserResponse
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.SendPasswordResetEmailResponse
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.SignInResponse
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent

class LoginViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginViewModelState())
    val state: StateFlow<LoginViewModelState> = _state.asStateFlow()

    private var authStateJob: Job? = null

    init {
        // Initialize some basic state with the current user (if any)
        val user = Firebase.auth.currentUser
        _state.update {
            it.copy(
                isLoggedIn     = user != null,
                isEmailVerified = user?.isEmailVerified == true,
                isUserAnonymous = user?.isAnonymous == true,
                isUserExist     = repo.isUserExist.value
            )
        }
    }

    /**
     * Start observing auth changes. If already started, do nothing.
     */
    private fun beginAuthStateListener() {
        if (authStateJob != null) {
            // Already listening, no need to start again
            return
        }

        authStateJob = viewModelScope.launch {
            Firebase.auth.authStateChanged.collect { user ->
                _state.update { currentState ->
                    currentState.copy(
                        isLoggedIn     = user != null,
                        isEmailVerified = user?.isEmailVerified == true,
                        isUserAnonymous = user?.isAnonymous == true,
                        isUserExist     = repo.isUserExist.value
                    )
                }
            }
        }
    }

    /**
     * Optional function to stop observing auth changes if needed.
     */
    private fun stopAuthStateListener() {
        authStateJob?.cancel()
        authStateJob = null
    }


    fun onEvent(event: LoginEvent) = viewModelScope.launch {
        Logger.withTag("LoginVM").d { "onEvent received: ${event::class.simpleName}" }

         when (event) {
            is LoginEvent.SignInWithGoogle -> {
                signInWithGoogle(event.googleCredential)
            }

            is LoginEvent.SignInWithApple -> {
                signInWithApple(event.appleCredential)
            }

            is LoginEvent.LoginWithEmailAndPassword -> {
                loginWithEmailAndPassword(event.email, event.password)
            }

            is LoginEvent.SendPasswordResetEmail -> {
                sendPasswordResetEmail(event.email)
            }

            is LoginEvent.ResendEmailVerification -> {
                resendEmailVerification()
            }

            is LoginEvent.EnteredLoginEmail -> {
                _state.update { it.copy(loginEmail = event.email) }
            }

            is LoginEvent.EnteredLoginPassword -> {
                _state.update { it.copy(loginPassword = event.password) }
            }

            is LoginEvent.EnteredForgotPasswordEmail -> {
                _state.update { it.copy(forgotPasswordEmail = event.email) }
            }

            is LoginEvent.ReloadUser -> {
                reloadUser()
            }

            is LoginEvent.SignOut -> {
                signOut()
            }

            is LoginEvent.RevokeAccess -> {
                Logger.withTag("LoginVM").d { "RevokeAccess event passwordProvided=${event.password?.isNotBlank() == true}" }
                revokeAccess(event.password)
            }

            is LoginEvent.ToggleLoading -> {
                _state.update { it.copy(isLoading = event.isLoading) }
            }
            is LoginEvent.BeginAuthStateListener -> {
                beginAuthStateListener()
            }

            is LoginEvent.ReauthAndDelete -> {
                reauthWithGoogleAndDelete(event.googleCredential)
            }

            // handle layout control events by updating plain boolean flags
            is LoginEvent.ShowLoginLayout -> {
                _state.update { it.copy(isLoginLayout = true, isSignUpLayout = false, isForgotPasswordLayout = false) }
            }
            is LoginEvent.ShowSignUpLayout -> {
                _state.update { it.copy(isLoginLayout = false, isSignUpLayout = true, isForgotPasswordLayout = false) }
            }
            is LoginEvent.ShowForgotPasswordLayout -> {
                _state.update { it.copy(isLoginLayout = false, isSignUpLayout = false, isForgotPasswordLayout = true) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAuthStateListener()
    }

    private fun <T> handleResource(
        resourceFlow: Flow<Resource<T>>,
        transform: suspend (T) -> LoginViewModelState,
        errorTransform: (StringValue) -> LoginViewModelState, // Changed to StringValue
        loadingTransform: () -> LoginViewModelState
    ) = resourceFlow.onEach { resource ->
        when (resource) {
            is Resource.Loading -> _state.value = loadingTransform()
            is Resource.Success -> _state.value = resource.data?.let { transform(it) }!!
            is Resource.Error -> _state.value = errorTransform(StringValue.DynamicString(resource.message ?: "Error")) // Wrapped error message
        }
    }.launchIn(viewModelScope)



    private suspend fun signInWithGoogle(googleCredential: AuthCredential) {
        Logger.d { "signInWithGoogle() called in LoginViewModel with credential=$googleCredential" }
         handleResource(
             resourceFlow = repo.firebaseSignInWithGoogle(googleCredential),
             transform = {
                Logger.d { "LoginViewModel: firebaseSignInWithGoogle emitted success result, starting transfer/updates" }
                 try {
                     if (it.second != null && it.second != "") {
                        Logger.d { "Transferring dreams from anon=${it.second} to new user=${it.first.user?.uid}" }
                         repo.transferDreamsFromAnonymousToPermanent(
                             it.first.user?.uid ?: "", it.second ?: ""
                         )
                     }
                 } catch (_: Exception) {
                    Logger.e("LoginViewModel") { "transferDreamsFromAnonymousToPermanent failed" }
                 }

                 try {
                    Logger.d { "LoginViewModel: reloading firebase user" }
                     repo.reloadFirebaseUser()
                 } catch (_: Exception) {
                    Logger.e("LoginViewModel") { "reloadFirebaseUser failed" }
                 }

                 val refreshedUser = Firebase.auth.currentUser ?: it.first.user
                 Logger.d { "LoginViewModel: refreshedUser=${refreshedUser?.uid}, isAnonymous=${refreshedUser?.isAnonymous}" }
                 _state.update { current ->
                     current.copy(
                         user = refreshedUser?.let { u ->
                             UserUi(
                                 uid = u.uid,
                                 displayName = u.displayName,
                                 email = u.email,
                                 isAnonymous = u.isAnonymous,
                                 photoUrl = null
                             )
                         },
                         isEmailVerified = refreshedUser?.isEmailVerified ?: false,
                         isLoggedIn = true,
                         isUserAnonymous = refreshedUser?.isAnonymous ?: false,
                         isUserExist = refreshedUser != null,
                         isLoading = false,
                         signInWithGoogleResponse = SignInUiState.Success(
                             SignInResultUi(
                                 userId = refreshedUser?.uid,
                                 previousAnonymousId = it.second
                             )
                         )
                     )
                 }

                 beginAuthStateListener()

                 // Return the updated state to the handler
                 _state.value
              },
             errorTransform = { error ->
                Logger.e("LoginViewModel") { "signInWithGoogle error: $error" } // Use StringValue's toString for logging
                 viewModelScope.launch {
                     SnackbarController.sendEvent(
                         SnackbarEvent(
                             error,
                             SnackbarAction(StringValue.Resource(Res.string.dismiss)) { } // Use StringValue.Resource
                         )
                     )
                 }
                 _state.value.copy(
                     isLoading = false,
                     error = error, // Store StringValue
                     signInWithGoogleResponse = SignInUiState.Error(error)
                 )
             },
             loadingTransform = {
                Logger.d { "signInWithGoogle loadingTransform: setting loading=true" }
                 _state.value.copy(
                     isLoading = true,
                     signInWithGoogleResponse = SignInUiState.Loading
                 )
             }
         )
     }

    private suspend fun signInWithApple(appleCredential: AuthCredential) {
        Logger.d { "signInWithApple() called in LoginViewModel with credential=$appleCredential" }
        handleResource(
            resourceFlow = repo.firebaseSignInWithApple(appleCredential),
            transform = {
                Logger.d { "LoginViewModel: firebaseSignInWithApple emitted success result, starting transfer/updates" }
                try {
                    if (it.second != null && it.second != "") {
                        repo.transferDreamsFromAnonymousToPermanent(
                            it.first.user?.uid ?: "", it.second ?: ""
                        )
                    }
                } catch (_: Exception) {
                    Logger.e("LoginViewModel") { "Apple transferDreamsFromAnonymousToPermanent failed" }
                }

                try {
                    repo.reloadFirebaseUser()
                } catch (_: Exception) {
                    Logger.e("LoginViewModel") { "Apple reloadFirebaseUser failed" }
                }

                val refreshedUser = Firebase.auth.currentUser ?: it.first.user
                _state.update { current ->
                    current.copy(
                        user = refreshedUser?.let { u ->
                            UserUi(
                                uid = u.uid,
                                displayName = u.displayName,
                                email = u.email,
                                isAnonymous = u.isAnonymous,
                                photoUrl = null
                            )
                        },
                        isEmailVerified = refreshedUser?.isEmailVerified ?: false,
                        isLoggedIn = true,
                        isUserAnonymous = refreshedUser?.isAnonymous ?: false,
                        isUserExist = refreshedUser != null,
                        isLoading = false,
                        signInWithGoogleResponse = SignInUiState.Success(
                            SignInResultUi(
                                userId = refreshedUser?.uid,
                                previousAnonymousId = it.second
                            )
                        )
                    )
                }

                beginAuthStateListener()

                _state.value
            },
            errorTransform = { error ->
                Logger.e("LoginViewModel") { "signInWithApple error: $error" }
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            error,
                            SnackbarAction(StringValue.Resource(Res.string.dismiss)) { }
                        )
                    )
                }
                _state.value.copy(
                    isLoading = false,
                    error = error,
                    signInWithGoogleResponse = SignInUiState.Error(error)
                )
            },
            loadingTransform = {
                _state.value.copy(
                    isLoading = true,
                    signInWithGoogleResponse = SignInUiState.Loading
                )
            }
        )
    }


    private suspend fun loginWithEmailAndPassword(email: String, password: String) {
        if (!checkLoginFields()) return
        handleResource(
            resourceFlow = repo.firebaseSignInWithEmailAndPassword(email, password),
            transform = {
                _state.value.copy(
                    isLoading = false,
                    isLoggedIn = it.user != null,
                    isUserExist = it.user != null,
                    isEmailVerified = it.user?.isEmailVerified == true
                )
            },
            errorTransform = { error ->
                val normalizedError = normalizeAuthError(error)
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            normalizedError,
                            SnackbarAction(StringValue.Resource(Res.string.dismiss)) { } // Use StringValue.Resource
                        )
                    )
                }
                _state.value.copy(
                    isLoading = false,
                    error = normalizedError // Store StringValue
                )
            },
            loadingTransform = { _state.value.copy(isLoading = true) }
        )
    }

    private suspend fun checkLoginFields(): Boolean {
        return when {
            _state.value.loginEmail.isEmpty() || _state.value.loginPassword.isEmpty() -> {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.Resource(Res.string.email_password_empty),
                        action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {})
                    )
                )
                false
            }

            !_state.value.loginEmail.contains("@") || !_state.value.loginEmail.contains(".") -> {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.Resource(Res.string.email_incorrect_format),
                        action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {})
                    )
                )
                false
            }

            _state.value.loginPassword.length < 6 -> {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.Resource(Res.string.password_too_short),
                        action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {})
                    )
                )
                false
            }

            else -> true
        }
    }

    private fun normalizeAuthError(error: StringValue): StringValue {
        val raw = (error as? StringValue.DynamicString)?.value?.lowercase() ?: return error

        return when {
            "invalid credential" in raw ||
                "wrong-password" in raw ||
                "wrong password" in raw ||
                "invalid login credentials" in raw ||
                "user-not-found" in raw -> {
                StringValue.DynamicString("Incorrect email or password")
            }

            "email not verified" in raw || "verify your account" in raw -> {
                StringValue.Resource(Res.string.email_not_verified_login)
            }

            "invalid-email" in raw || "badly formatted" in raw -> {
                StringValue.Resource(Res.string.email_incorrect_format)
            }

            "password should be at least 6 characters" in raw -> {
                StringValue.Resource(Res.string.password_too_short)
            }

            else -> error
        }
    }

    private fun sendPasswordResetEmail(email: String) = viewModelScope.launch {
        _state.value = _state.value.copy(sendPasswordResetEmailResponse = Resource.Loading())
        _state.value = _state.value.copy(
            sendPasswordResetEmailResponse = repo.sendPasswordResetEmail(email)
        )
    }

    private fun reloadUser() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, reloadUserResponse = Resource.Loading()) }
        val response = repo.reloadFirebaseUser()
        val refreshedUser = Firebase.auth.currentUser
        val verified = refreshedUser?.isEmailVerified == true
        _state.update {
            it.copy(
                reloadUserResponse = response,
                isLoggedIn = refreshedUser != null,
                isEmailVerified = verified,
                isUserAnonymous = refreshedUser?.isAnonymous == true,
                isUserExist = refreshedUser != null,
                isLoading = false
            )
        }

        val message = when {
            response is Resource.Error -> StringValue.DynamicString(response.message ?: "Unable to check verification right now.")
            verified -> StringValue.Resource(Res.string.email_verified_snackbar)
            else -> StringValue.Resource(Res.string.email_verification_still_pending)
        }
        SnackbarController.sendEvent(
            SnackbarEvent(
                message = message,
                action = SnackbarAction(StringValue.Resource(Res.string.dismiss)) { }
            )
        )
    }

    private fun resendEmailVerification() = viewModelScope.launch {
        repo.sendEmailVerification().onEach { resource ->
            when (resource) {
                is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = StringValue.Resource(Res.string.email_verification_resent_snackbar),
                            action = SnackbarAction(StringValue.Resource(Res.string.dismiss)) { },
                            duration = SnackbarDuration.Indefinite
                        )
                    )
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = StringValue.DynamicString(resource.message ?: "Unable to resend verification email."),
                            action = SnackbarAction(StringValue.Resource(Res.string.dismiss)) { }
                        )
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun signOut() = viewModelScope.launch {
        repo.signOut()
        _state.value = _state.value.copy(
            isLoggedIn = false,
            isEmailVerified = false,
            isUserAnonymous = false,
            isLoading = false,
            signInWithGoogleResponse = SignInUiState.Idle
        )
    }

    private suspend fun revokeAccess(password: String?) {
        Logger.withTag("LoginVM").d { "revokeAccess start passwordProvided=${password?.isNotBlank() == true}" }
        handleResource(
            resourceFlow = repo.revokeAccess(password),
            transform = {
                Logger.withTag("LoginVM").d { "revokeAccess success -> isRevoked=true, isLoggedIn=false" }
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = StringValue.Resource(Res.string.account_deleted_successfully), // Use StringValue
                            action = null
                        )
                    )
                }
                _state.value.copy(
                    revokeAccess = RevokeAccessState(isRevoked = true),
                    isLoggedIn = false,
                )
            },
            errorTransform = { error ->
                Logger.withTag("LoginVM").e { "revokeAccess error: $error" } // Use StringValue's toString for logging
                viewModelScope.launch {
                    val lower = if (error is StringValue.DynamicString) error.value.lowercase() else ""
                    val needsRecent = listOf(
                        "requires-recent-login",
                        "recent login",
                        "requires recent",
                        "recently",
                        "reauth",
                        "sensitive and requires"
                    ).any { lower.contains(it) }
                    val msg = if (needsRecent)
                        StringValue.Resource(Res.string.re_auth_error) // Use StringValue
                    else error // Already StringValue
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = msg,
                            action = SnackbarAction(StringValue.Resource(Res.string.dismiss)) { } // Use StringValue.Resource
                        )
                    )
                }
                _state.value.copy(
                    revokeAccess = RevokeAccessState(error = error) // Store StringValue
                )
            },
            loadingTransform = {
                Logger.withTag("LoginVM").d { "revokeAccess loading" }
                _state.value.copy(
                    revokeAccess = RevokeAccessState(isLoading = true)
                )
            }
        )
    }

    private suspend fun reauthWithGoogleAndDelete(googleCredential: AuthCredential) {
        Logger.withTag("LoginVM").d { "reauthWithGoogleAndDelete start" }
        try {
            // Reauthenticate current user with provided Google credential
            val user = Firebase.auth.currentUser
            if (user == null) {
                Logger.withTag("LoginVM").e { "reauthWithGoogleAndDelete: no current user" }
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.Resource(Res.string.no_authenticated_user), // Use StringValue
                        action = null
                    )
                )
                return
            }
            Logger.withTag("LoginVM").d { "reauthenticating with Google credential" }
            user.reauthenticate(googleCredential)
            Logger.withTag("LoginVM").d { "reauthenticate success, proceeding to delete" }
            revokeAccess(null)
        } catch (e: Exception) {
            Logger.withTag("LoginVM").e { "reauthWithGoogleAndDelete error: ${e.message}" }
            SnackbarController.sendEvent(
                SnackbarEvent(
                    message = StringValue.DynamicString(e.message ?: "Reauthentication failed"), // Use StringValue
                    action = SnackbarAction(StringValue.Resource(Res.string.dismiss)) { } // Use StringValue.Resource
                )
            )
        }
    }
}

data class LoginViewModelState(
     val loginEmail: String = "",
     val loginPassword: String = "",
     val forgotPasswordEmail: String = "",
     val signInWithGoogleResponse: SignInUiState = SignInUiState.Idle,
    // Layout flags replaced with stable booleans
    val isLoginLayout: Boolean = true,
    val isSignUpLayout: Boolean = false,
    val isForgotPasswordLayout: Boolean = false,
    val signInResponse: Resource<SignInResponse> = Resource.Success(),
    val sendPasswordResetEmailResponse: SendPasswordResetEmailResponse = Resource.Success(),
    val reloadUserResponse: ReloadUserResponse = Resource.Success(),
    val revokeAccess: RevokeAccessState = RevokeAccessState(),
    val user: UserUi? = null,
    val isUserExist: Boolean = false,
    val isEmailVerified: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: StringValue = StringValue.Empty,
    val isUserAnonymous: Boolean = false,
 )

data class RevokeAccessState(
     val isRevoked: Boolean = false,
     val isLoading: Boolean = false,
     val error: StringValue = StringValue.Empty
 )

@Immutable
data class UserUi(
    val uid: String?,
    val displayName: String? = null,
    val email: String? = null,
    val isAnonymous: Boolean = false,
    val photoUrl: String? = null,
)

@Immutable
data class SignInResultUi(
    val userId: String?,
    val previousAnonymousId: String?
)

// Concrete, non-generic UI state for the sign-in-with-Google flow.
// Using a concrete sealed type avoids Compose runtime stability checks that happen for generic type parameters.
@Immutable
sealed class SignInUiState {
    @Immutable
    object Idle : SignInUiState()

    @Immutable
    object Loading : SignInUiState()

    @Immutable
    data class Success(val result: SignInResultUi) : SignInUiState()

    @Immutable
    data class Error(val message: StringValue) : SignInUiState()
}
