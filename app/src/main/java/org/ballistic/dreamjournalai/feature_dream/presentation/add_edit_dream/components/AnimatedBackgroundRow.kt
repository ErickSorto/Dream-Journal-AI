package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.components

import android.graphics.drawable.Drawable
import android.media.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.smarttoolfactory.animatedlist.AnimatedInfiniteLazyRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smarttoolfactory.animatedlist.AnimatedInfiniteLazyRow
import com.smarttoolfactory.animatedlist.model.AnimationProgress
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamEvent
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.AddEditDreamViewModel


@Composable
internal fun AnimatedAspectRatioSelection(
    modifier: Modifier = Modifier,
    initialSelectedIndex: Int = 2,
    viewModel: AddEditDreamViewModel = hiltViewModel(),
) {

    var currentIndex by remember { mutableStateOf(initialSelectedIndex) }

    AnimatedInfiniteLazyRow(
        modifier = modifier.padding(horizontal = 10.dp),
        items = Dream.dreamBackgroundColors,
        inactiveItemPercent = 80,
        initialFirstVisibleIndex = initialSelectedIndex - 2
    ) { animationProgress: AnimationProgress, index: Int, height: Dp, width: Dp ->

        val scale = animationProgress.scale
        val color = animationProgress.color
        val selectedLocalIndex = animationProgress.itemIndex


    }
}