package org.ballistic.dreamjournalai.shared.dream_account

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.dream_authentication.GoogleAuthProvider
import org.ballistic.dreamjournalai.shared.dream_authentication.Account
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.SignInGoogleButton
import co.touchlab.kermit.Logger
import android.widget.Toast

@Composable
actual fun MyGoogleSignInButton(
    onGotToken: (Account) -> Unit,
    onError: (String) -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    SignInGoogleButton(
        modifier = Modifier.fillMaxWidth(),
        isVisible = true,
        isEnabled = !isLoading,
        onClick = {
            Logger.d { "[DJAI/MyGoogleBtn] clicked - launching provider" }
            scope.launch {
                try {
                    val result = GoogleAuthProvider.provideGoogleAuth(context)
                    Logger.d { "[DJAI/MyGoogleBtn] provider returned result=$result" }
                    when (result) {
                        is org.ballistic.dreamjournalai.shared.dream_authentication.Result.Success -> {
                            val account = result.account
                            if (!account.isEmpty) {
                                Logger.d { "[DJAI/MyGoogleBtn] got account idToken=${account.idToken.take(12)}..." }
                                onGotToken(account)
                            } else {
                                Logger.w { "[DJAI/MyGoogleBtn] account tokens empty" }
                                Toast.makeText(context, "Google sign-in returned empty tokens", Toast.LENGTH_SHORT).show()
                                onError("Empty token")
                            }
                        }
                        is org.ballistic.dreamjournalai.shared.dream_authentication.Result.Error -> {
                            Logger.e("DJAI/MyGoogleBtn") { "provider error: ${result.message}" }
                            Toast.makeText(context, "Google sign-in error: ${result.message}", Toast.LENGTH_SHORT).show()
                            onError(result.message)
                        }
                        is org.ballistic.dreamjournalai.shared.dream_authentication.Result.Cancelled -> {
                            Logger.i { "[DJAI/MyGoogleBtn] provider returned Cancelled" }
                            Toast.makeText(context, "Google sign-in cancelled or no account available", Toast.LENGTH_SHORT).show()
                            onError("Cancelled by user or no credentials available")
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("DJAI/MyGoogleBtn") { "Unexpected error from provider: ${e.message}" }
                    Toast.makeText(context, "Google sign-in unexpected error: ${e.message}", Toast.LENGTH_SHORT).show()
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    )
}
