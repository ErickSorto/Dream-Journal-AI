package org.ballistic.dreamjournalai.user_authentication.presentation.sign_up.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components.ProgressBar
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModelState


//@Composable
//fun SendEmailVerification(
//    authViewModelState: AuthViewModelState
//) {
//    when(val sendEmailVerificationResponse = authViewModelState.sendEmailVerificationResponse) {
//        is Resource.Loading -> ProgressBar()
//        is Resource.Success -> Unit
//        is Resource.Error -> sendEmailVerificationResponse.apply {
//            LaunchedEffect(Unit) {
//                print(message)
//            }
//        }
//    }
//}