package org.ballistic.dreamjournalai.shared.dream_authentication

import cocoapods.GoogleSignIn.GIDSignIn
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual object GoogleAuthProvider {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun provideGoogleAuth(context: Any?): Result = suspendCoroutine { continuation ->
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (rootViewController != null) {
            GIDSignIn.sharedInstance.signInWithPresentingViewController(rootViewController) { result, error ->
                when {
                    result != null -> {
                        val account = Account(
                            idToken = result.user.idToken?.tokenString.orEmpty(),
                            accessTokenOrNonce = result.user.accessToken.tokenString
                        )
                        continuation.resume(Result.Success(account))
                    }
                    error != null && error.code.toInt() != -5 -> {
                        continuation.resume(Result.Error(error.localizedDescription))
                    }
                    else -> continuation.resume(Result.Cancelled)
                }
            }
        } else {
            continuation.resume(Result.Cancelled)
        }
    }
}
