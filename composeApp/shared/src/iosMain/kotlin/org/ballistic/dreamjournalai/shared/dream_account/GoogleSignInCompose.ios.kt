package org.ballistic.dreamjournalai.shared.dream_account

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.rememberCoroutineScope
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.ic_apple_logo
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.dream_authentication.AppleAuthProvider
import org.ballistic.dreamjournalai.shared.dream_authentication.GoogleAuthProvider
import org.ballistic.dreamjournalai.shared.dream_authentication.Account
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.SignInGoogleButton

@Composable
actual fun MyGoogleSignInButton(
    modifier: Modifier,
    onGotToken: (Account) -> Unit,
    onError: (String) -> Unit,
    isLoading: Boolean,
    isVisible: Boolean,
    label: String,
) {
    val scope = rememberCoroutineScope()

    SignInGoogleButton(
        modifier = modifier.fillMaxWidth(),
        isVisible = isVisible,
        isEnabled = !isLoading,
        label = label,
        onClick = {
            scope.launch {
                try {
                    val result = GoogleAuthProvider.provideGoogleAuth(null)
                    when (result) {
                        is org.ballistic.dreamjournalai.shared.dream_authentication.Result.Success -> {
                            val account = result.account
                            if (!account.isEmpty) onGotToken(account) else onError("Empty token")
                        }
                        is org.ballistic.dreamjournalai.shared.dream_authentication.Result.Error -> onError(result.message)
                        is org.ballistic.dreamjournalai.shared.dream_authentication.Result.Cancelled -> Unit
                    }
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    )
}

@Composable
actual fun MyAppleSignInButton(
    modifier: Modifier,
    onGotToken: (Account) -> Unit,
    onError: (String) -> Unit,
    isLoading: Boolean,
    isVisible: Boolean,
    label: String,
) {
    val scope = rememberCoroutineScope()

    SignInGoogleButton(
        modifier = modifier.fillMaxWidth(),
        isVisible = isVisible,
        isEnabled = !isLoading,
        label = label,
        showGoogleLogo = false,
        iconRes = Res.drawable.ic_apple_logo,
        onClick = {
            scope.launch {
                try {
                    val result = AppleAuthProvider.provideAppleAuth(null)
                    when (result) {
                        is org.ballistic.dreamjournalai.shared.dream_authentication.Result.Success -> {
                            val account = result.account
                            if (!account.isEmpty) onGotToken(account) else onError("Empty token")
                        }
                        is org.ballistic.dreamjournalai.shared.dream_authentication.Result.Error -> onError(result.message)
                        is org.ballistic.dreamjournalai.shared.dream_authentication.Result.Cancelled -> Unit
                    }
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    )
}
