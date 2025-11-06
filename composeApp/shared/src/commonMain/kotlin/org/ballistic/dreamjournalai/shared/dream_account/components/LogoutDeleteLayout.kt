package org.ballistic.dreamjournalai.shared.dream_account.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.PasswordField
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.RedOrange
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.SkyBlue

@Composable
fun LogoutDeleteLayout(
    onLoginEvent: (LoginEvent) -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val userPassword = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }

    fun requiresPassword(): Boolean {
        val providers = Firebase.auth.currentUser?.providerData
        val needs = providers?.any { it.providerId == "password" } == true
        Logger.withTag("LogoutDelete").d { "requiresPassword=$needs providers=${providers?.map { it.providerId }}" }
        return needs
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = {
                    onLogoutClick()
                    onLoginEvent(LoginEvent.SignOut)
                },
                modifier = Modifier.fillMaxWidth(.5f),
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
            ) {
                Text(text = "Logout", fontSize = 15.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    Logger.withTag("LogoutDelete").d { "Delete button clicked, opening confirm dialog" }
                    showDialog.value = true
                },
                modifier = Modifier.fillMaxWidth(.5f),
                colors = ButtonDefaults.buttonColors(containerColor = RedOrange)
            ) {
                Text(text = "Delete Account", fontSize = 15.sp, color = Color.White)
            }

            if (requiresPassword()) {
                PasswordField(
                    isLoginLayout = false,
                    password = userPassword.value,
                    onValueChange = { userPassword.value = it },
                    forgotPassword = {},
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        ConfirmDeleteAccountDialog(
            visible = showDialog.value,
            requirePassword = requiresPassword(),
            password = userPassword.value,
            onPasswordChange = { userPassword.value = it },
            onDismiss = {
                Logger.withTag("LogoutDelete").d { "Confirm dialog dismissed" }
                showDialog.value = false
            },
            onConfirm = {
                val needsPass = requiresPassword()
                Logger.withTag("LogoutDelete").d { "Confirm delete clicked, requiresPassword=$needsPass passwordLength=${userPassword.value.length}" }
                showDialog.value = false
                onLoginEvent(
                    LoginEvent.RevokeAccess(
                        password = if (needsPass) userPassword.value else null
                    )
                )
            },
        )
    }
}