package org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.ic_google_logo
import dreamjournalai.composeapp.shared.generated.resources.sign_in_with_google
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val AuthProviderButtonColor = Color(0xFF020109)

@Composable
fun SignInGoogleButton(
    modifier: Modifier,
    isVisible: Boolean,
    isEnabled: Boolean,
    label: String? = null,
    showGoogleLogo: Boolean = true,
    iconRes: DrawableResource? = null,
    onClick: () -> Unit,
) {
    val providerIcon = iconRes ?: if (showGoogleLogo) Res.drawable.ic_google_logo else null

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(initialOffsetX = { 1000 }),
            exit = slideOutHorizontally { -1000 }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AuthProviderButtonColor)
                    .border(
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        enabled = isEnabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onClick
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (providerIcon != null) {
                        Image(
                            painter = painterResource(
                                providerIcon
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = label ?: stringResource(Res.string.sign_in_with_google),
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
