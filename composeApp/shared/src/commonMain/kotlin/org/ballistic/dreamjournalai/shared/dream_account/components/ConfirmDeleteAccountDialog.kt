package org.ballistic.dreamjournalai.shared.dream_account.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dreamjournalai.composeapp.shared.generated.resources.*
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.PasswordField
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConfirmDeleteAccountDialog(
    visible: Boolean,
    expectedPhrase: String = "DeleteDreams",
    requirePassword: Boolean,
    password: String,
    onPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!visible) return

    val typed = remember { mutableStateOf("") }
    // Log dialog shown
    Logger.withTag("DeleteDialog").d { "shown requirePassword=$requirePassword pwdLen=${password.length}" }

    val normalizedTyped = typed.value.trim()
    val sanitizedTyped = normalizedTyped.replace("\"", "")
    val phraseMatches = sanitizedTyped.equals(expectedPhrase, ignoreCase = true)
    val canConfirm = phraseMatches && (!requirePassword || password.isNotBlank())

    AlertDialog(
        onDismissRequest = {
            Logger.withTag("DeleteDialog").d { "dismissed" }
            onDismiss()
        },
        title = {
            Text(
                text = stringResource(Res.string.delete_account_title),
                color = OriginalXmlColors.BrighterWhite,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(Res.string.delete_account_description),
                    color = OriginalXmlColors.BrighterWhite,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.delete_account_prompt, expectedPhrase),
                    color = Color(0xFFB0BEC5),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = typed.value,
                    onValueChange = { typed.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = expectedPhrase, color = Color.LightGray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OriginalXmlColors.BrighterWhite,
                        unfocusedTextColor = OriginalXmlColors.BrighterWhite,
                        focusedBorderColor = if (phraseMatches || typed.value.isBlank()) Color.White.copy(alpha = 0.6f) else OriginalXmlColors.RedOrange,
                        unfocusedBorderColor = if (phraseMatches || typed.value.isBlank()) Color.White.copy(alpha = 0.3f) else OriginalXmlColors.RedOrange.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    )
                )
                if (!phraseMatches && typed.value.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(Res.string.delete_account_phrase_mismatch),
                        color = OriginalXmlColors.RedOrange,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // Password input if required
                if (requirePassword) {
                    Spacer(Modifier.height(12.dp))
                    PasswordField(
                        isLoginLayout = false,
                        password = password,
                        onValueChange = onPasswordChange,
                        forgotPassword = {},
                    )
                    if (password.isBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(Res.string.password_required),
                            color = OriginalXmlColors.RedOrange,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    Logger.withTag("DeleteDialog").d { "confirm clicked canConfirm=$canConfirm phraseMatches=$phraseMatches requirePassword=$requirePassword pwdLen=${password.length}" }
                    onConfirm()
                },
                enabled = canConfirm
            ) {
                Text(
                    stringResource(Res.string.confirm_delete),
                    color = if (canConfirm) OriginalXmlColors.RedOrange else Color.Gray,
                    fontWeight = if (canConfirm) FontWeight.Bold else FontWeight.Normal
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel), color = Color(0xFFE1F5FE))
            }
        },
        shape = MaterialTheme.shapes.medium,
        containerColor = Color(0xFF2C2C2C),
    )
}
