package org.ballistic.dreamjournalai.feature_dream.domain.repository

import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.firebase.auth.AuthCredential
import org.ballistic.dreamjournalai.core.Resource


typealias OneTapSignInResponse = Resource<BeginSignInResult>
typealias SignInWithGoogleResponse = Resource<Boolean>

interface AuthRepository {
    val isUserAuthenticatedInFirebase: Boolean

    suspend fun oneTapSignInWithGoogle(): OneTapSignInResponse

    suspend fun firebaseSignInWithGoogle(googleCredential: AuthCredential): SignInWithGoogleResponse
}