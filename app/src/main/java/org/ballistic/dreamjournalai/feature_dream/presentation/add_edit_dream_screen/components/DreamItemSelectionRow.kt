package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.smarttoolfactory.animatedlist.AnimatedInfiniteLazyRow
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream.Companion.dreamBackgroundImages
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.AddEditDreamEvent

@Composable
fun DreamImageSelectionRow(
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit = {},
    dreamBackgroundImage: MutableState<Int>
) {

    val initialSelectedItem = dreamBackgroundImages.indexOf(dreamBackgroundImage.value)

    AnimatedInfiniteLazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        items = dreamBackgroundImages,
        initialFirstVisibleIndex = initialSelectedItem - 3,
        selectorIndex = initialSelectedItem - 1,
        visibleItemCount = 6,
        spaceBetweenItems = 8.dp,
        itemScaleRange = 1,
        showPartialItem = true,
        activeColor = Color.Cyan,
        inactiveColor = Color.Gray,
        itemContent = { animationProgress, _, imageResId, width ->
            animationProgress.scale

           //  selectedItem = animationProgress.itemIndex
           // dreamBackgroundImage.value = selectedItem
            Box(
                modifier = Modifier
                    .width(width)
                    .height(width)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .shadow(width, CircleShape)
                    .clickable {
                        val imageIndex = dreamBackgroundImages.indexOf(imageResId)
                        onAddEditDreamEvent(AddEditDreamEvent.ChangeDreamBackgroundImage(imageIndex))
                        dreamBackgroundImage.value = imageIndex
                    }
            ){
                Image(
                    painter = rememberAsyncImagePainter(imageResId),
                    contentDescription = "Color",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}