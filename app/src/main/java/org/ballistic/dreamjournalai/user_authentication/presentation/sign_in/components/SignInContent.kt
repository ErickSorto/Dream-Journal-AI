package org.ballistic.dreamjournalai.user_authentication.presentation.sign_in.components

//@Composable
//@ExperimentalComposeUiApi
//fun SignInContent(
//    padding: PaddingValues,
//    signIn: (email: String, password: String) -> Unit,
//    navigateToForgotPasswordScreen: () -> Unit,
//    navigateToSignUpScreen: () -> Unit
//) {
//    var email by rememberSaveable(
//        stateSaver = TextFieldValue.Saver
//    ) { mutableStateOf(TextFieldValue(NO_VALUE)) }
//    var password by rememberSaveable(
//        stateSaver = TextFieldValue.Saver
//    ) { mutableStateOf(TextFieldValue(NO_VALUE)) }
//    val keyboard = LocalSoftwareKeyboardController.current
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(padding),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        EmailField(
//            email = email,
//            onEmailValueChange = { newValue ->
//                email = newValue
//            }
//        )
//        SmallSpacer()
//        PasswordField(
//            password = password,
//            onPasswordValueChange = { newValue ->
//                password = newValue
//            },
//            forgotPassword = {
//                navigateToForgotPasswordScreen()
//            }
//        )
//        SmallSpacer()
//        Button(
//            onClick = {
//                keyboard?.hide()
//                signIn(email.text, password.text)
//            }
//        ) {
//            Text(
//                text = SIGN_IN,
//                fontSize = 15.sp
//            )
//        }
//        Row {
//            Text(
//                modifier = Modifier.clickable {
//                    navigateToForgotPasswordScreen()
//                },
//                text = FORGOT_PASSWORD,
//                fontSize = 15.sp
//            )
//            Text(
//                modifier = Modifier.padding(start = 4.dp, end = 4.dp),
//                text = VERTICAL_DIVIDER,
//                fontSize = 15.sp,
//                fontWeight = FontWeight.Bold
//            )
//            Text(
//                modifier = Modifier.clickable {
//                    navigateToSignUpScreen()
//                },
//                text = NO_ACCOUNT,
//                fontSize = 15.sp
//            )
//        }
//    }
//}