package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.input.TextFieldLineLimits
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreInterceptKeyBeforeSoftKeyboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import org.ballistic.dreamjournalai.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransparentHintTextField(
    text: String,
    hint: String,
    modifier: Modifier = Modifier,
    isHintVisible: Boolean = true,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle = TextStyle(),
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    Box(modifier = modifier)
    {
        BasicTextField2(
            value = text,
            onValueChange = onValueChange,
            textStyle = textStyle,
            lineLimits = if (singleLine) TextFieldLineLimits.SingleLine else TextFieldLineLimits.Default,
            scrollState = rememberScrollState(),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth(),
            cursorBrush = Brush.verticalGradient(
                colors = listOf(
                    colorResource(id = R.color.white),
                    colorResource(id = R.color.white)
                )
            )
        )

        if (isHintVisible) {
            Text(text = hint, style = textStyle, color = colorResource(id = R.color.white))
        }
    }
}/**
 * Provides a callback when a text field has focus and the back button is pressed.
 *
 * This is currently useful to work around this bug: https://issuetracker.google.com/issues/312895384
 *
 * https://stackoverflow.com/a/77043957/2191796
 */
fun Modifier.onKeyboardDismiss(handleOnBackPressed: () -> Unit): Modifier =
    @OptIn(ExperimentalComposeUiApi::class)
    this.onPreInterceptKeyBeforeSoftKeyboard {
        if (it.key.keyCode == 17179869184) {
            handleOnBackPressed.invoke()
        }
        true
    }

