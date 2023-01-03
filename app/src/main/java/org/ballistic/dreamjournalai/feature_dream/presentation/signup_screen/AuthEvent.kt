package org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen

sealed class AuthEvent{
    data class SignedIn(val value: Boolean) : AuthEvent()
    data class SignedOut(val value: Boolean) : AuthEvent()
}
