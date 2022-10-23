package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.smarttoolfactory.animatedlist.AnimatedInfiniteLazyRow
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.animatedlist.AnimatedInfiniteLazyRow
import com.smarttoolfactory.animatedlist.model.AnimationProgress
import com.smarttoolfactory.cropper.model.CropAspectRatio
import com.smarttoolfactory.cropper.model.aspectRatios
import com.smarttoolfactory.cropper.widget.AspectRatioSelectionCard



@Composable
fun InfoPage(
    viewModel: AddEditDreamViewModel = hiltViewModel()
) {
    Box(modifier = Modifier
        .fillMaxSize()
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
        val initialSelectedIndex = Dream.dreamBackgroundColors.indexOf(viewModel.dreamBackgroundColor.value)
        var currentIndex by remember { mutableStateOf(initialSelectedIndex) }


        AnimatedInfiniteLazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            Dream.dreamBackgroundColors,
            1000,
        ) {



        }
        


    }
}

