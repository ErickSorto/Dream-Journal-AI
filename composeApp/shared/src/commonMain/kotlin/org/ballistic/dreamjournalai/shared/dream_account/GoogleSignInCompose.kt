package org.ballistic.dreamjournalai.shared.dream_account

import androidx.compose.runtime.Composable
import org.ballistic.dreamjournalai.shared.dream_authentication.Account

expect @Composable fun MyGoogleSignInButton(
    onGotToken: (Account) -> Unit,
    onError: (String) -> Unit = {},
    isLoading: Boolean
)
