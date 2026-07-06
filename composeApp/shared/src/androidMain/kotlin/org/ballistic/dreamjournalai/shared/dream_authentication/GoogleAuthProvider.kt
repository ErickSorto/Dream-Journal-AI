package org.ballistic.dreamjournalai.shared.dream_authentication

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import co.touchlab.kermit.Logger
import org.ballistic.dreamjournalai.shared.R

actual object GoogleAuthProvider {
    actual suspend fun provideGoogleAuth(context: Any?): Result {
        val ctx = context as? Context
        if (ctx == null) {
            Logger.e { "[DJAI/GoogleAuthProv] provideGoogleAuth called with null context" }
            return Result.Error("Context is null")
        }

        val clientId = try {
            ctx.getString(R.string.default_web_client_id)
        } catch (t: Throwable) {
            Logger.e(t) { "[DJAI/GoogleAuthProv] Failed to read default_web_client_id resource" }
            "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com"
        }

        Logger.d { "[DJAI/GoogleAuthProv] launching CredentialManager.getCredential with clientId=$clientId" }
        val googleIdOption = GetGoogleIdOption
            .Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(ctx)

        return try {
            val result = credentialManager.getCredential(request = request, context = ctx)
            val credObj = result.credential
            // Log credential class and details for debugging
            Logger.d { "[DJAI/GoogleAuthProv] CredentialManager returned credential class=${credObj::class.java.name}" }
            if (credObj is CustomCredential) {
                try {
                    // credObj.data is a Bundle — avoid unsafe conversions
                    val dataPreview = credObj.data.keySet().joinToString(",") { key -> "$key=${credObj.data.get(key)}" }.take(200)
                    Logger.d { "[DJAI/GoogleAuthProv] CustomCredential type=${credObj.type}, dataPreview=$dataPreview" }
                } catch (t: Throwable) {
                    Logger.d { "[DJAI/GoogleAuthProv] CustomCredential data logging failed: ${t.message}" }
                }
            }

            val credential = result.credential
            val isGoogleCreds = credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL

            if (isGoogleCreds) {
                val tokenResult = GoogleIdTokenCredential.createFrom(credential.data)
                Logger.d { "[DJAI/GoogleAuthProv] tokenResult.idToken length=${tokenResult.idToken.length}, id=${tokenResult.id.take(12)}" }
                val account = Account(idToken = tokenResult.idToken, accessTokenOrNonce = tokenResult.id)
                Result.Success(account)
            } else {
                Logger.i { "[DJAI/GoogleAuthProv] credential was not GoogleIdTokenCredential; returning Cancelled" }
                Result.Cancelled
            }
        } catch (ex: GetCredentialCancellationException) {
            Logger.i { "[DJAI/GoogleAuthProv] Google credential flow was closed: ${ex.message}" }
            Result.Cancelled
        } catch (ex: NoCredentialException) {
            Logger.w { "[DJAI/GoogleAuthProv] No Google credential available: ${ex.message}" }
            Result.Error("No Google account found. Add a Google account or try email login.")
        } catch (ex: GetCredentialException) {
            Logger.e { "[DJAI/GoogleAuthProv] CredentialManager failed: ${ex.type} ${ex.message}" }
            Result.Error("Google sign-in could not start. Please try again.")
        } catch (ex: Exception) {
            Logger.e { "[DJAI/GoogleAuthProv] provideGoogleAuth failed: ${ex.message}" }
            Result.Error(ex.message ?: ex.localizedMessage.orEmpty())
        }
    }
}

actual object AppleAuthProvider {
    actual suspend fun provideAppleAuth(context: Any?): Result {
        return Result.Error("Sign in with Apple is only available on iOS.")
    }
}

actual object PlatformAuthCapabilities {
    actual val supportsAppleSignIn: Boolean = false
}
