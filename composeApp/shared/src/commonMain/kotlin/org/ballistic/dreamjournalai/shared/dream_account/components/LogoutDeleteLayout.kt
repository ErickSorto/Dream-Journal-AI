package org.ballistic.dreamjournalai.shared.dream_account.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.White

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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logout Button - Strong SkyBlue
            Button(
                onClick = {
                    onLogoutClick()
                    onLoginEvent(LoginEvent.SignOut)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue,
                    contentColor = White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Logout",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Delete Button - Strong RedOrange
            Button(
                onClick = {
                    Logger.withTag("LogoutDelete").d { "Delete button clicked, opening confirm dialog" }
                    showDialog.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedOrange,
                    contentColor = White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Account",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Delete Account",
                    fontSize = 16.sp
                )
            }

            if (requiresPassword()) {
                Spacer(modifier = Modifier.height(32.dp))
                PasswordField(
                    isLoginLayout = false,
                    password = userPassword.value,
                    onValueChange = { userPassword.value = it },
                    forgotPassword = {},
                )
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
