package org.ballistic.dreamjournalai.user_authentication.presentation.sign_up.components


//@Composable
//@ExperimentalComposeUiApi
//fun SignUpContent(
//    padding: PaddingValues,
//    signUp: (email: String, password: String) -> Unit,
//    navigateBack: () -> Unit
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
//
//        )
//        SmallSpacer()
//        Button(
//            onClick = {
//                keyboard?.hide()
//                signUp(email.text, password.text)
//            }
//        ) {
//            Text(
//                text = SIGN_UP,
//                fontSize = 15.sp
//            )
//        }
//        Text(
//            modifier = Modifier.clickable {
//                navigateBack()
//            },
//            text = ALREADY_USER,
//            fontSize = 15.sp
//        )
//    }
//}