package org.ballistic.dreamjournalai.user_authentication.presentation.signup_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import org.ballistic.dreamjournalai.R

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AnonymousButton(
    modifier: Modifier,
    pagerState: PagerState,
    onClick: () -> Unit,
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
            visible = pagerState.currentPage == 3
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
                        id = R.drawable.anonymous_icon
                    ),
                    contentDescription = null
                )
                Text(
                    text = "Browse Anonymously",
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 18.sp
                )
            }
        }
    }
}