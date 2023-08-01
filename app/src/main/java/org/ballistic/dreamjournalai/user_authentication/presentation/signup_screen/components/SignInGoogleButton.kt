package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.core.Constants.SIGN_IN_WITH_GOOGLE

@Composable
fun SignInGoogleButton(
    modifier: Modifier,
    isVisible: MutableState<Boolean>,
    onClick: () -> Unit,
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = isVisible.value,
            enter = slideInHorizontally(initialOffsetX = { 1000 }),
            exit = slideOutHorizontally { -1000 }
        ) {
            Button(
                modifier = Modifier.padding().fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(R.color.sky_blue)
                ),
                onClick = onClick
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.ic_google_logo
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = SIGN_IN_WITH_GOOGLE,
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 18.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ReauthenticateSignInGoogleButton(
    modifier: Modifier,
    onClick: () -> Unit,
    enabled: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp, 8.dp, 16.dp, 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = enabled
        ) {
            Button(
                modifier = Modifier.padding(),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(R.color.sky_blue)
                ),
                onClick = onClick
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.ic_google_logo
                    ),
                    contentDescription = null
                )
                Text(
                    text = "Reauthenticate",
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 18.sp
                )
            }
        }
    }
}