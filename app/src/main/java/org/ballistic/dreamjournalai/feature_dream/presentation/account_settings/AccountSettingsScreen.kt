package org.ballistic.dreamjournalai.feature_dream.presentation.account_settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.auth.GoogleAuthProvider
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.user_authentication.presentation.components.PasswordField
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.AuthEvent
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components.ReauthenticateSignInGoogleButton
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components.SignInGoogleButton
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModelState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AccountSettingsScreen(
    authViewModelState: AuthViewModelState,
    paddingValues: PaddingValues,
    authEvent: (AuthEvent) -> Unit = {},
    onNavigateToOnboardingScreen: () -> Unit = {},
) {
    // Add a mutable state for user password input and a flag for Google account users
    val userPassword = remember { mutableStateOf("") }
    val isGoogleAccount = remember { mutableStateOf(false) }

    // Check the user's provider ID to determine if they are using a Google account
    fun checkIfGoogleAccount(): Boolean {
        //email log
        Log.d(
            "AccountSettingsScreen",
            "checkIfGoogleAccount: ${authViewModelState.user?.providerData?.get(0)?.email}"
        )
        Log.d(
            "AccountSettingsScreen",
            "checkIfGoogleAccount: ${authViewModelState.user?.providerData?.get(1)?.email}"
        )
        return authViewModelState.user?.providerData?.get(1)?.providerId == GoogleAuthProvider.PROVIDER_ID
    }

    Box(
        modifier = Modifier
            .padding(paddingValues)
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
                    authEvent(AuthEvent.SignOut)
                    onNavigateToOnboardingScreen()
                },
                modifier = Modifier.fillMaxWidth(.5f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.sky_blue)
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
                    authEvent(
                        AuthEvent.RevokeAccess(
                            password = null,
                            onSuccess = { onNavigateToOnboardingScreen() })
                    )
                },
                modifier = Modifier.fillMaxWidth(.5f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.RedOrange)
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
                    onValueChange = { userPassword.value = it }) {
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}