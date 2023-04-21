package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.ArcRotationAnimation
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.InterpretCustomButton
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.PaintCustomButton
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components.TypewriterText
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.viewmodel.AddEditDreamViewModel

@OptIn(ExperimentalPagerApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AIInterpreterPage(
    viewModel: AddEditDreamViewModel,
    infiniteTransition: InfiniteTransition,
    pagerState: PagerState,
) {
    val responseState = viewModel.dreamUiState.value.dreamAIExplanation

    TypewriterText(
        text = responseState.response.trim(),
        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 16.dp)
    )
    if (responseState.response == "" && !responseState.isLoading) {
        InterpretCustomButton(
            isFavorite = viewModel.dreamUiState.value.dreamInfo.dreamIsFavorite,
            state = pagerState,
            size = 120.dp,
            fontSize = 24.sp
        )
    }
    if (responseState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp, 0.dp, 16.dp, 16.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            ArcRotationAnimation(
                infiniteTransition = infiniteTransition,
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AIPainterPage(
    viewModel: AddEditDreamViewModel,
    painter: Painter,
    infiniteTransition: InfiniteTransition,
    pagerState: PagerState,
) {
    val imageState = viewModel.dreamUiState.value.dreamAIImage

    AnimatedVisibility(
        visible = viewModel.dreamUiState.value.dreamAIImage.image != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Image(
            painter = painter,
            contentDescription = "AI Generated Image",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp, 16.dp, 16.dp, 16.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
    }
    if (imageState.image == null && !imageState.isLoading) {
        PaintCustomButton(
            isLucid = viewModel.dreamUiState.value.dreamInfo.dreamIsLucid,
            state = pagerState,
            size = 120.dp,
            fontSize = 24.sp
        )
    }
    if (imageState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp, 0.dp, 16.dp, 16.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            ArcRotationAnimation(
                infiniteTransition = infiniteTransition,
            )
        }
    }
}