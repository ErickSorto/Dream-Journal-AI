package org.ballistic.dreamjournalai.user_authentication.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import org.ballistic.dreamjournalai.core.Constants.EMAIL_LABEL
import org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.AuthEvent

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
@Composable
fun EmailField(
    email: String,
    pagerState: PagerState,
    onValueChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = email,
        onValueChange = { newValue ->
            onValueChange(newValue)
        },
        label = {
            Text(
                text = EMAIL_LABEL
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .focusRequester(focusRequester),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.White.copy(alpha = 0.1f),
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black,
            disabledLabelColor = Color.Black,
            disabledBorderColor = Color.Black,
            textColor = Color.Black,
            backgroundColor = Color.White.copy(alpha = 0.3f),
            leadingIconColor = Color.Black,
            trailingIconColor = Color.Black,
            errorLabelColor = Color.Red,
            errorBorderColor = Color.Red,
            errorCursorColor = Color.Red
        )
    )

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 3) {
            focusRequester.requestFocus()
        }
    }
}
