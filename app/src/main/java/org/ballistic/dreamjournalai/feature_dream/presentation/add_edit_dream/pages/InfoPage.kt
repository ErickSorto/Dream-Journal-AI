package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPage(
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp) , horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier
            .background(Color.Transparent)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dream.dreamBackgroundColors.forEach { image ->
                    Box(
                        modifier = Modifier
                            .size(if (viewModel.dreamBackgroundColor.value == image) 60.dp else 50.dp)
                            .shadow(15.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .border(
                                if (viewModel.dreamBackgroundColor.value == image) 5.dp else 0.dp,
                                Color.Black.copy(alpha = 0.3f),
                                CircleShape
                            )
                            .clickable {
                                viewModel.onEvent(AddEditDreamEvent.ChangeColorBackground(image))
                            }
                    ){
                        Image(painter = painterResource(id = image), contentDescription = "Color", contentScale = ContentScale.Crop)
                    }
                }

            }


    }
        //slider for vividness
        Text(text = "Vividness: " + viewModel.dreamVividness.value.rating , modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally)
            .padding(16.dp))

        Slider(
            value = viewModel.dreamVividness.value.rating.toFloat(),
            onValueChange = {
                viewModel.onEvent(AddEditDreamEvent.ChangeVividness(it.toInt()))
            },
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            thumb = {
                Icon(
                    imageVector = Icons.Filled.ShieldMoon,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = Color.Red
                )
            }
        )
        //slider for lucidity
        Text(text = "Lucidity: " + viewModel.dreamLucidity.value.rating , modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally)
            .padding(16.dp))

        Slider(
            value = viewModel.dreamLucidity.value.rating.toFloat(),
            onValueChange = {
                viewModel.onEvent(AddEditDreamEvent.ChangeLucidity(it.toInt()))
            },
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            thumb = {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = Color.Red
                )
            }
        )


//        val initialSelectedIndex = Dream.dreamBackgroundColors.indexOf(viewModel.dreamBackgroundColor.value)
//        var currentIndex by remember { mutableStateOf(initialSelectedIndex) }
//
//
//        AnimatedInfiniteLazyRow(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp),
//            Dream.dreamBackgroundColors,
//            1000,
//        ) {
//
//
//
//        }



    }
}

