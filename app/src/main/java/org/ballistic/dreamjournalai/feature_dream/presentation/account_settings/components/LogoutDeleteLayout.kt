package org.ballistic.dreamjournalai.feature_dream.presentation.account_settings.components

import android.util.Log
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.GoogleAuthProvider
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components.PasswordField
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.LoginViewModelState

@Composable
fun LogoutDeleteLayout(
    loginViewModelState: LoginViewModelState,
    onLoginEvent: (LoginEvent) -> Unit = {},
) {
// Add a mutable state for user password input and a flag for Google account users
    val userPassword = remember { mutableStateOf("") }

    // Check the user's provider ID to determine if they are using a Google account
    fun checkIfGoogleAccount(): Boolean {
        loginViewModelState.user?.providerData?.let { providerData ->
            for (info in providerData) {
                Log.d("AccountSettingsScreen", "checkIfGoogleAccount: ${info.email}")
                if (info.providerId == GoogleAuthProvider.PROVIDER_ID) {
                    return true
                }
            }
        }
        return false
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
                    onLoginEvent(LoginEvent.SignOut)
                },
                modifier = Modifier.fillMaxWidth(.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.sky_blue)
                )
            ) {
                Text(
                    text = "Logout",
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))



            Button(
                onClick = {
                    // Call RevokeAccess with the user password for email/password users or null for Google users
                    onLoginEvent(
                        LoginEvent.RevokeAccess(
                            password = null
                            )
                    )
                },
                modifier = Modifier.fillMaxWidth(.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.RedOrange)
                )
            ) {
                Text(
                    text = "Delete Account",
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            // Show a password input field if the user is not signed in with a Google account
            if (!checkIfGoogleAccount()) {
                PasswordField(
                    isLoginLayout = false,
                    password = userPassword.value,
                    onValueChange = { userPassword.value = it },
                    forgotPassword = {},
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}