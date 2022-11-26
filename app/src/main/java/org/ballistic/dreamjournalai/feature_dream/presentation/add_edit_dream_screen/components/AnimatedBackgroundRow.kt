package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components


//@Composable
//internal fun AnimatedAspectRatioSelection(
//    modifier: Modifier = Modifier,
//    initialSelectedIndex: Int = 2,
//    //dreamViewMo,
//) {
//
//    var currentIndex by remember { mutableStateOf(initialSelectedIndex) }
//
//    AnimatedInfiniteLazyRow(
//        modifier = modifier.padding(horizontal = 10.dp),
//        items = Dream.dreamBackgroundColors,
//        inactiveItemPercent = 80,
//        initialFirstVisibleIndex = initialSelectedIndex - 2
//    ) { animationProgress: AnimationProgress, index: Int,item:  , width: Dp ->
//
//        val scale = animationProgress.scale
//        val color = animationProgress.color
//        val selectedLocalIndex = animationProgress.itemIndex
//
//        Box(modifier = Modifier
//            .graphicsLayer {
//                scaleX = scale
//                scaleY = scale
//            }
//            .width(width),
//
//        )
//
//        if (currentIndex != selectedLocalIndex) {
//            currentIndex = selectedLocalIndex
//            onAspectRatioChange(aspectRatios[selectedLocalIndex])
//        }
//
//
//    }
//}