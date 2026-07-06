package org.ballistic.dreamjournalai.shared.dream_authentication

import cocoapods.GoogleSignIn.GIDSignIn
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationAppleIDRequest
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.CoreCrypto.CC_SHA256
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.darwin.NSObject
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val GoogleSignInCancelledCode = -5
private const val AppleSignInCancelledCode = 1001
private const val Sha256DigestSize = 32
private const val AppleNonceSize = 32
private const val AppleNonceCharset = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._"

actual object GoogleAuthProvider {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun provideGoogleAuth(context: Any?): Result = suspendCoroutine { continuation ->
        val rootViewController = UIApplication.sharedApplication.activeRootViewController()
        if (rootViewController != null) {
            GIDSignIn.sharedInstance.signInWithPresentingViewController(rootViewController) { result, error ->
                when {
                    result != null -> {
                        val idToken = result.user.idToken?.tokenString.orEmpty()
                        if (idToken.isBlank()) {
                            continuation.resume(Result.Error("Google sign-in did not return an ID token. Check the iOS server client ID."))
                            return@signInWithPresentingViewController
                        }
                        val account = Account(
                            idToken = idToken,
                            accessTokenOrNonce = result.user.accessToken.tokenString
                        )
                        continuation.resume(Result.Success(account))
                    }
                    error != null && error.code.toInt() != GoogleSignInCancelledCode -> {
                        continuation.resume(Result.Error(error.localizedDescription))
                    }
                    else -> continuation.resume(Result.Cancelled)
                }
            }
        } else {
            continuation.resume(Result.Error("Unable to find a presenting view controller for Google sign-in."))
        }
    }
}

actual object AppleAuthProvider {
    actual suspend fun provideAppleAuth(context: Any?): Result = suspendCoroutine { continuation ->
        val rootViewController = UIApplication.sharedApplication.activeRootViewController()
        val presentationAnchor = UIApplication.sharedApplication.activeKeyWindow()
        if (rootViewController == null || presentationAnchor == null) {
            continuation.resume(Result.Error("Unable to find a presenting window for Sign in with Apple."))
            return@suspendCoroutine
        }

        val rawNonce = secureRandomNonce()
        val request = ASAuthorizationAppleIDProvider().createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
            nonce = rawNonce.sha256Hex()
        }
        val controller = ASAuthorizationController(authorizationRequests = listOf(request))
        val delegate = AppleSignInDelegate(
            rawNonce = rawNonce,
            presentationAnchor = presentationAnchor,
            onComplete = { result ->
                AppleSignInDelegateHolder.current = null
                continuation.resume(result)
            }
        )
        AppleSignInDelegateHolder.current = delegate
        controller.delegate = delegate
        controller.presentationContextProvider = delegate
        controller.performRequests()
    }
}

actual object PlatformAuthCapabilities {
    actual val supportsAppleSignIn: Boolean = true
}

private object AppleSignInDelegateHolder {
    var current: AppleSignInDelegate? = null
}

private class AppleSignInDelegate(
    private val rawNonce: String,
    private val presentationAnchor: ASPresentationAnchor,
    private val onComplete: (Result) -> Unit,
) : NSObject(), ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {

    override fun presentationAnchorForAuthorizationController(
        controller: ASAuthorizationController
    ): ASPresentationAnchor = presentationAnchor

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization
    ) {
        val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        val identityToken = credential?.identityToken?.utf8String()
        if (identityToken.isNullOrBlank()) {
            onComplete(Result.Error("Sign in with Apple did not return an identity token."))
            return
        }

        onComplete(
            Result.Success(
                Account(
                    idToken = identityToken,
                    accessTokenOrNonce = rawNonce
                )
            )
        )
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError
    ) {
        if (didCompleteWithError.code.toInt() == AppleSignInCancelledCode) {
            onComplete(Result.Cancelled)
        } else {
            onComplete(Result.Error(didCompleteWithError.localizedDescription))
        }
    }
}

private fun UIApplication.activeRootViewController(): UIViewController? {
    val activeWindow = connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { window -> window.isKeyWindow() }
    return activeWindow?.rootViewController?.topMostPresentedViewController()
        ?: keyWindow?.rootViewController?.topMostPresentedViewController()
}

private fun UIApplication.activeKeyWindow(): UIWindow? =
    connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { window -> window.isKeyWindow() }
        ?: keyWindow

private tailrec fun UIViewController.topMostPresentedViewController(): UIViewController =
    presentedViewController?.topMostPresentedViewController() ?: this

@OptIn(ExperimentalForeignApi::class)
private fun secureRandomNonce(): String {
    val randomBytes = ByteArray(AppleNonceSize)
    val status = randomBytes.usePinned { pinned ->
        SecRandomCopyBytes(kSecRandomDefault, randomBytes.size.convert(), pinned.addressOf(0))
    }
    check(status == errSecSuccess) { "Unable to create Sign in with Apple nonce." }

    return buildString(randomBytes.size) {
        randomBytes.forEach { byte ->
            val index = byte.toUByte().toInt() % AppleNonceCharset.length
            append(AppleNonceCharset[index])
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun String.sha256Hex(): String {
    val input = encodeToByteArray()
    val digest = ByteArray(Sha256DigestSize)
    input.usePinned { inputPinned ->
        digest.usePinned { digestPinned ->
            CC_SHA256(
                inputPinned.addressOf(0),
                input.size.convert(),
                digestPinned.addressOf(0).reinterpret()
            )
        }
    }
    return digest.joinToString(separator = "") { byte ->
        byte.toUByte().toString(16).padStart(2, '0')
    }
}

private fun NSData.utf8String(): String? =
    NSString.create(data = this, encoding = NSUTF8StringEncoding)?.toString()
