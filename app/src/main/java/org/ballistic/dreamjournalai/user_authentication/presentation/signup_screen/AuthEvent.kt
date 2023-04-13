package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen

import com.google.firebase.auth.AuthCredential

sealed class AuthEvent {
    object OneTapSignIn : AuthEvent()
    data class SignInWithGoogle(val googleCredential: AuthCredential) : AuthEvent()
    data class LoginWithEmailAndPassword(val email: String, val password: String) : AuthEvent()
    data class SignUpWithEmailAndPassword(val email: String, val password: String) : AuthEvent()
    object SendEmailVerification : AuthEvent()
    data class SendPasswordResetEmail(val email: String) : AuthEvent()
    object ReloadUser : AuthEvent()
    object SignOut : AuthEvent()
    object RevokeAccess : AuthEvent()
    data class EnteredLoginEmail(val email: String) : AuthEvent()
    data class EnteredLoginPassword(val password: String) : AuthEvent()
    data class EnteredSignUpEmail(val email: String) : AuthEvent()
    data class EnteredSignUpPassword(val password: String) : AuthEvent()
    data class EnteredForgotPasswordEmail(val email: String) : AuthEvent()
}