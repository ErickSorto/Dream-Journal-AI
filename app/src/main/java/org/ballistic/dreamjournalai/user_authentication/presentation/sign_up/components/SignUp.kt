package org.ballistic.dreamjournalai.user_authentication.presentation.sign_up.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components.ProgressBar

import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.viewmodel.AuthViewModel


//@Composable
//fun SignUp(
//    viewModel: AuthViewModel = hiltViewModel(),
//    sendEmailVerification: () -> Unit,
//    showVerifyEmailMessage: () -> Unit
//) {
//    when (val signUpResponse = viewModel.signUpResponse) {
//        is Resource.Loading -> ProgressBar()
//        is Resource.Success -> {
//            val isUserSignedUp = signUpResponse.data
//            LaunchedEffect(isUserSignedUp) {
//                if (isUserSignedUp == true) {
//                    sendEmailVerification()
//                    showVerifyEmailMessage()
//                }
//            }
//        }
//        is Resource.Error -> signUpResponse.apply {
//            LaunchedEffect(Unit) {
//                print(message)
//            }
//        }
//
//    }
//}