package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen

sealed class AuthEvent{
    data class SignedIn(val value: Boolean) : AuthEvent()
    data class SignedOut(val value: Boolean) : AuthEvent()
}
