package org.ballistic.dreamjournalai.feature_dream.presentation.signup_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.core.Constants.SIGN_IN_WITH_GOOGLE

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SignInButton(
    modifier: Modifier,
    pagerState: PagerState,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 40.dp)
            .fillMaxHeight(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = pagerState.currentPage == 2
        ) {
            Button(
                modifier = Modifier.padding(bottom = 16.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(R.color.LightGreen)
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
                    text = SIGN_IN_WITH_GOOGLE,
                    modifier = Modifier.padding(6.dp),
                    fontSize = 18.sp
                )
            }
        }
    }
}