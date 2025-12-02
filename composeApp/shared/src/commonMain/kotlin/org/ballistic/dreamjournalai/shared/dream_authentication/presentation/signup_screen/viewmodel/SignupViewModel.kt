package org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.SignUpResponse
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent

class SignupViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignupViewModelState())
    val state: StateFlow<SignupViewModelState> = _state.asStateFlow()

    fun onEvent(event: SignupEvent) = viewModelScope.launch {
        when (event) {
            is SignupEvent.SignUpWithEmailAndPassword -> {
                signUpWithEmailAndPassword(event.email, event.password)
            }

            is SignupEvent.AnonymousSignIn -> {
                anonymousSignIn()
            }

            is SignupEvent.EnteredSignUpEmail -> {
                _state.value = _state.value.copy(
                    signUpEmail = event.email
                )
            }

            is SignupEvent.EnteredSignUpPassword -> {
                _state.value = _state.value.copy(
                    signUpPassword = event.password
                )
            }
        }
    }

    private fun <T> handleResource(
        resourceFlow: Flow<Resource<T>>,
        transform: (T) -> Job,
        errorTransform: (StringValue) -> SignupViewModelState, // Changed to StringValue
        loadingTransform: () -> SignupViewModelState
    ) = resourceFlow.onEach { resource ->
        when (resource) {
            is Resource.Loading -> _state.value = loadingTransform()
            is Resource.Success -> resource.data?.let { transform(it) }
            is Resource.Error -> _state.value = errorTransform(StringValue.DynamicString(resource.message ?: "Error")) // Wrapped error message
        }
    }.launchIn(viewModelScope)

    private suspend fun signUpWithEmailAndPassword(email: String, password: String) {
        if (!checkSignUpFields()) return
        handleResource(
            resourceFlow = repo.firebaseSignUpWithEmailAndPassword(email, password),
            transform = { result ->
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = StringValue.DynamicString(result), // Wrapped result
                            action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {}) // Use StringValue.Resource
                        )
                    )
                }
            },
            errorTransform = { error ->
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = error, // Already StringValue
                            action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {}) // Use StringValue.Resource
                        )
                    )
                }
                _state.value.copy(
                    error = error,
                )
            },
            loadingTransform = { _state.value.copy(isLoading = true) }
        )
    }

    private suspend fun anonymousSignIn() {
        handleResource(
            resourceFlow = repo.anonymousSignIn(),
            transform = { authResult ->
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            isUserAnonymous = authResult.user?.isAnonymous == true,
                            isLoggedIn = authResult.user != null,
                            isUserExist = authResult.user != null,
                        )
                    }
                }
            },
            errorTransform = { error ->
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = error, // Already StringValue
                            action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {}) // Use StringValue.Resource
                        )
                    )
                }
                _state.value.copy(error = error)
            },
            loadingTransform = { _state.value.copy(isLoading = true) }
        )
    }

    private suspend fun checkSignUpFields(): Boolean {
        // currentSnackbarData dismissal is UI-specific; instead emit a snackbar event on invalid fields
        return when {
            _state.value.signUpEmail.isEmpty() || _state.value.signUpPassword.isEmpty() -> {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.Resource(Res.string.email_password_empty), // Use StringValue
                        action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {}) // Use StringValue.Resource
                    )
                )
                false
            }

            !_state.value.signUpEmail.contains("@") || !_state.value.signUpEmail.contains(".") -> {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.Resource(Res.string.email_incorrect_format), // Use StringValue
                        action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {}) // Use StringValue.Resource
                    )
                )
                false
            }

            _state.value.signUpPassword.length < 6 -> {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = StringValue.Resource(Res.string.password_too_short), // Use StringValue
                        action = SnackbarAction(StringValue.Resource(Res.string.dismiss), {}) // Use StringValue.Resource
                    )
                )
                false
            }

            else -> {
                true
            }
        }
    }
}


data class SignupViewModelState(
    val loginEmail: String = "",
    val loginPassword: String = "",
    val signUpEmail: String = "",
    val signUpPassword: String = "",
    val forgotPasswordEmail: String = "",
    // layout flags removed to keep state stable; use LoginViewModel to control UI layout
    val signUpResponse: Resource<SignUpResponse> = Resource.Success(),
    val sendEmailVerificationResponse: Resource<Boolean> = Resource.Success(),
    val emailVerification: VerifyEmailState = VerifyEmailState(),
    val revokeAccess: RevokeAccessState = RevokeAccessState(),
    val isUserExist: Boolean = false,
    val isEmailVerified: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: StringValue = StringValue.Empty,
    val isUserAnonymous: Boolean = false,
)

data class VerifyEmailState(
    val verified: Boolean = false,
    val sent: Boolean = false,
    val isLoading: Boolean = false,
    val error: StringValue = StringValue.Empty
)
