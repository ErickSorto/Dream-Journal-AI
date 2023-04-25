package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ballistic.dreamjournalai.R

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DreamInterpretationPopUp(
    onAdClick: () -> Unit,
    onDreamTokenClick: () -> Unit,
    onClickOutside: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {
            onClickOutside()
        },
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp, 8.dp, 16.dp, 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //Dream Painter
                    Text(
                        text = "Dream Interpreter",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colorResource(id = R.color.black),
                        modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 16.dp)
                    )
                    AdTokenLayout(
                        onAdClick = onAdClick,
                        onDreamTokenClick = onDreamTokenClick,
                        amount = 1
                    )
                }
            }
        }
    )
}

