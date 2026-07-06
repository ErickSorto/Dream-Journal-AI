package org.ballistic.dreamjournalai.shared.dream_account

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ballistic.dreamjournalai.shared.dream_authentication.Account

@Composable
expect fun MyGoogleSignInButton(
    modifier: Modifier = Modifier,
    onGotToken: (Account) -> Unit,
    onError: (String) -> Unit = {},
    isLoading: Boolean,
    isVisible: Boolean = true,
    label: String = "Google",
)

@Composable
expect fun MyAppleSignInButton(
    modifier: Modifier = Modifier,
    onGotToken: (Account) -> Unit,
    onError: (String) -> Unit = {},
    isLoading: Boolean,
    isVisible: Boolean = true,
    label: String = "Apple",
)
