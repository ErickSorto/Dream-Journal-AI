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
}
