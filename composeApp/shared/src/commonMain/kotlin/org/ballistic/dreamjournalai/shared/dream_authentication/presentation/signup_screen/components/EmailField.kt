package org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.email
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.onKeyboardDismiss
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EmailField(
    email: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    isVisible: MutableState<Boolean> = mutableStateOf(false),
    animate: Boolean = true,
    enterDurationMillis: Int = 300,
    exitWithFade: Boolean = false,
) {
    val focusManager = LocalFocusManager.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    if (!animate) {
        OutlinedTextField(
            value = email,
            onValueChange = { newValue -> onValueChange(newValue) },
            label = { Text(text = stringResource(Res.string.email), color = Color.White) },
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .padding(bottom = 8.dp)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        scope.launch {
                            delay(180)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                }
                .onKeyboardDismiss { focusManager.clearFocus() },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LightBlack.copy(alpha = 0.62f),
                unfocusedContainerColor = LightBlack.copy(alpha = 0.54f),
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                cursorColor = Color.White,
                errorCursorColor = Color.Red,
                focusedBorderColor = Color.White.copy(alpha = 0.34f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                disabledBorderColor = Color.Black,
                errorBorderColor = Color.Red,
                focusedLabelColor = Color.White.copy(alpha = 0.92f),
                unfocusedLabelColor = Color.White.copy(alpha = 0.72f),
                disabledLabelColor = Color.White.copy(alpha = 0.5f),
                errorLabelColor = Color.Red,
            )
        )
    } else {
        AnimatedVisibility(
            visible = isVisible.value,
            enter = slideInHorizontally(animationSpec = tween(enterDurationMillis), initialOffsetX = { 1000 }),
            exit = if (exitWithFade) fadeOut(animationSpec = tween(220)) else slideOutHorizontally(animationSpec = tween(enterDurationMillis), targetOffsetX = { -1000 })
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { newValue ->
                    onValueChange(newValue)
                },
                label = {
                    Text(
                        text = stringResource(Res.string.email),
                        color = Color.White
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 16.sp,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp)
                    .padding(bottom = 8.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            scope.launch {
                                delay(180)
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    }
                    .onKeyboardDismiss {
                        focusManager.clearFocus()
                    },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LightBlack.copy(alpha = 0.62f),
                    unfocusedContainerColor = LightBlack.copy(alpha = 0.54f),
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    cursorColor = Color.White,
                    errorCursorColor = Color.Red,
                    focusedBorderColor = Color.White.copy(alpha = 0.34f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                    disabledBorderColor = Color.Black,
                    errorBorderColor = Color.Red,
                    focusedLabelColor = Color.White.copy(alpha = 0.92f),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.72f),
                    disabledLabelColor = Color.White.copy(alpha = 0.5f),
                    errorLabelColor = Color.Red,
                )
            )
        }
    }
}
