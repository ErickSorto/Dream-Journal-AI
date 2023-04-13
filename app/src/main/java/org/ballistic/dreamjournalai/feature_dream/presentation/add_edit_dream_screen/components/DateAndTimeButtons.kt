package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.viewmodel.AddEditDreamViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateAndTimeButtonsLayout(
    viewModel: AddEditDreamViewModel = hiltViewModel(),
) {
    Row(
        modifier = Modifier
            .padding(0.dp, 8.dp, 0.dp, 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.3f)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateButton(viewModel = viewModel,
            modifier = Modifier
                .weight(1f)
                .background(Color.Transparent),
            onClick = { viewModel.calendarState.show() })
        Spacer(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(Color.Black.copy(alpha = 0.8f))
        )
        SleepTimeButton(viewModel = viewModel,
            modifier = Modifier.weight(1f),
            onClick = { viewModel.sleepTimePickerState.show() })
        Spacer(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(Color.Black.copy(alpha = 0.8f))
        )
        WakeTimeButton(viewModel = viewModel,
            modifier = Modifier.weight(1f),
            onClick = { viewModel.wakeTimePickerState.show() })
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateButton(
    viewModel: AddEditDreamViewModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(0.dp, 4.dp, 0.dp, 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Transparent),
        colors = buttonColors(
            containerColor = Color.Transparent,
        )
    ) {
        Column(
            modifier = Modifier.background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Date", fontSize = 12.sp, color = Color.Black)
            Text(
                text = viewModel.dreamUiState.value.dreamInfo.dreamDate,
                fontSize = 10.sp,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SleepTimeButton(
    viewModel: AddEditDreamViewModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(0.dp, 4.dp, 0.dp, 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent),
        colors = buttonColors(
            containerColor = Color.Transparent,
        )
    ) {
        Column(
            modifier = Modifier.background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "Sleep Time", fontSize = 12.sp, color = Color.Black)
            Text(
                text =  viewModel.dreamUiState.value.dreamInfo.dreamSleepTime,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WakeTimeButton(
    viewModel: AddEditDreamViewModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(0.dp, 4.dp, 0.dp, 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent),
        colors = buttonColors(
            containerColor = Color.Transparent,
        )
    ) {

        Column(
            modifier = Modifier.background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Wake Time", fontSize = 12.sp, color = Color.Black)
            Text(
                text = viewModel.dreamUiState.value.dreamInfo.dreamWakeTime,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}