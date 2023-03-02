package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamViewModel


//@OptIn(ExperimentalPagerApi::class, DelicateCoroutinesApi::class)
//@Composable
//fun GenerateButton(viewModel: AddEditDreamViewModel, state: PagerState) {
//    val scope = rememberCoroutineScope()
//    val dreamUiState = viewModel.dreamUiState
//    if (dreamUiState.value.dreamContent.isNotBlank() && dreamUiState.value.dreamContent.length > 10) {
//        Box(
//            contentAlignment = Alignment.BottomCenter,
//            modifier = Modifier.background(Color.Transparent)
//        ) {
//            Button(
//                onClick = {
//                    scope.launch {
//                        delay(100)
//                        state.animateScrollToPage(1)
//                    }
//                    GlobalScope.launch {
//                        viewModel.onEvent(AddEditDreamEvent.ClickGenerateAIResponse(viewModel.dreamUiState.value.dreamContent))
//                        viewModel.onEvent(AddEditDreamEvent.ClickGenerateDetails(viewModel.dreamUiState.value.dreamContent))
//                        delay(3000)
//                        viewModel.onEvent(AddEditDreamEvent.ClickGenerateAIImage(viewModel.dreamUiState.value.dreamAIImage.image.toString()))
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 16.dp),
//                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.7f)),
//                shape = RoundedCornerShape(10.dp)
//            ) {
//                Text(
//                    text = "Generate AI Response",
//                    modifier = Modifier
//                        .padding(16.dp),
//                    color = Color.Black,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            //vector asset button for generating AI response
//            IconButton(onClick = { /*TODO*/ }) {
//
//            }
//        }
//    }
//}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun GenerateButtonsLayout(
    viewModel: AddEditDreamViewModel,
    state: PagerState
) {
    Row(
        modifier = Modifier
            .padding(0.dp, 16.dp, 0.dp, 0.dp)
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.2f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PaintCustomButton(
            isLucid = viewModel.dreamUiState.value.dreamInfo.dreamIsLucid,
            viewModel = viewModel,
            state = state
        )
        InterpretCustomButton(
            isFavorite = viewModel.dreamUiState.value.dreamInfo.dreamIsFavorite,
            viewModel = viewModel,
            state = state
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun PaintCustomButton(
    isLucid: Boolean,
    viewModel: AddEditDreamViewModel = hiltViewModel(),
    state: PagerState
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.IconButton(
            onClick = {
                scope.launch {
                    delay(100)
                    state.animateScrollToPage(1)
                }
                viewModel.imageGenerationPopUpState.value = true
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = rememberAsyncImagePainter(R.drawable.paint_vector),
                contentDescription = "Paint",
                modifier = Modifier
                    .size(40.dp)
                    .rotate(45f),
                tint = if (isLucid) colorResource(R.color.sky_blue) else Color.Black
            )

        }
        Text(text = "Paint Dream", fontSize = 16.sp)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun InterpretCustomButton(
    isFavorite: Boolean,
    viewModel: AddEditDreamViewModel = hiltViewModel(),
    state: PagerState
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.IconButton(
            onClick = {
                scope.launch {
                    delay(100)
                    state.animateScrollToPage(1)
                }
                viewModel.dreamInterpretationPopUpState.value = true
            },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                painter = rememberAsyncImagePainter(R.drawable.interpret_vector),
                contentDescription = "Interpret",
                modifier = Modifier
                    .size(40.dp)
                    .rotate(45f),
                tint = if (isFavorite) colorResource(R.color.Yellow) else Color.Black
            )
        }
        Text(text = "Interpret Dream", fontSize = 16.sp)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AdTokenLayout(
    viewModel: AddEditDreamViewModel = hiltViewModel(),
    onAdClick: () -> Unit = {},
    onDreamTokenClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WatchAdButton(onClick = { onAdClick() })
        Spacer(modifier = Modifier.height(8.dp))
        DreamTokenGenerateButton(onClick = { onDreamTokenClick() })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WatchAdButton(
    onClick: () -> Unit = {}
) {
    Button(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.sky_blue)),
        shape = RoundedCornerShape(10.dp)
    ) {

        Icon(
            painter = rememberAsyncImagePainter(R.drawable.baseline_smart_display_24),
            contentDescription = "Watch Ad",
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp),
            tint = Color.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Watch Ad",
            modifier = Modifier
                .padding(16.dp),
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DreamTokenGenerateButton(
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.Yellow)),
        shape = RoundedCornerShape(10.dp)
    ) {


        Image(
            painter = rememberAsyncImagePainter(R.drawable.dream_token),
            contentDescription = "DreamToken",
            modifier = Modifier
                .padding(16.dp, 16.dp, 0.dp, 16.dp)
                .size(40.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Dream Token (1)",
            modifier = Modifier
                .padding(0.dp, 16.dp, 16.dp, 16.dp),
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}