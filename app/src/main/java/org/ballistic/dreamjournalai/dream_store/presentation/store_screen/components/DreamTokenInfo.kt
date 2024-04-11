package org.ballistic.dreamjournalai.dream_store.presentation.store_screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import org.ballistic.dreamjournalai.R
import org.ballistic.dreamjournalai.dream_store.presentation.store_screen.StoreScreenViewModelState


@Composable
fun CustomButtonLayout(
    storeScreenViewModelState: StoreScreenViewModelState,
    buy500IsClicked: () -> Unit,
    buy100IsClicked: () -> Unit
) {
    val lastClickTime = remember { mutableLongStateOf(0L) }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp, 8.dp, 8.dp),
    ) {
        val (monthly, annual, tag) = createRefs()

        DreamToken500ButtonBuy(
            storeScreenViewModelState = storeScreenViewModelState,
            modifier = Modifier.constrainAs(annual) {
                bottom.linkTo(monthly.top)
            },
            buy500IsClicked = singleClick(lastClickTime) { buy500IsClicked() }
        )

        DreamToken100ButtonBuy(
            storeScreenViewModelState = storeScreenViewModelState,
            modifier = Modifier.constrainAs(monthly) {
                bottom.linkTo(parent.bottom)
            },
            buy100IsClicked = singleClick(lastClickTime) { buy100IsClicked() }
        )

        MostPopularBanner(modifier = Modifier
            .constrainAs(tag) {}
            .offset(y = (-10).dp))
    }
}

@Composable
fun DreamToken500ButtonBuy(
    storeScreenViewModelState: StoreScreenViewModelState,
    modifier: Modifier,
    buy500IsClicked: () -> Unit = {}
) {
    Button(
        onClick = { buy500IsClicked() },
        modifier = modifier
            .padding(8.dp, 0.dp, 8.dp, 8.dp)
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(0.dp, 8.dp, 8.dp, 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.light_black).copy(alpha = 0.8f),
            contentColor = Color.White
        ),
        enabled = !storeScreenViewModelState.isBillingClientLoading
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "500 Dream Tokens",
                fontSize = 20.sp,
                fontWeight = Bold,
                maxLines = 1,
                modifier = Modifier
                    .padding(8.dp, 8.dp, 8.dp, 8.dp)
                    .align(Alignment.CenterStart)
            )

            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display the original price with a strikethrough
                Text(
                    text = "\$14.99",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    textDecoration = TextDecoration.LineThrough, // Apply strikethrough
                    modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 0.dp)
                )
                // Display the new price below
                Text(
                    text = "\$4.99", // New price
                    fontSize = 20.sp,
                    fontWeight = Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 8.dp)
                )
            }
        }
    }
}

@Composable
fun DreamToken100ButtonBuy(
    storeScreenViewModelState: StoreScreenViewModelState,
    modifier: Modifier,
    buy100IsClicked: () -> Unit
) {
    Button(
        onClick = { buy100IsClicked() },
        modifier = modifier
            .padding(8.dp, 8.dp, 8.dp, 8.dp)
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.light_black).copy(alpha = 0.8f),
            contentColor = Color.White
        ),
        enabled = !storeScreenViewModelState.isBillingClientLoading
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "100 Dream Tokens",
                fontSize = 20.sp,
                fontWeight = Bold,
                maxLines = 1,
                modifier = Modifier
                    .padding(8.dp, 8.dp, 8.dp, 8.dp)
                    .align(Alignment.CenterStart)
            )

            Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                Text(
                    text = "\$2.99",
                    fontSize = 20.sp,
                    fontWeight = Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(8.dp, 8.dp, 8.dp, 8.dp)
                )
            }
        }
    }
}

@Composable
fun MostPopularBanner(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // Slowing down the shimmer to make it less frequent
    val shinePosition by infiniteTransition.animateFloat(
        initialValue = 0f, // Adjusted to start from the beginning
        targetValue = 1f, // Adjusted to move fully across
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing), // Slower duration
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    // Defining a more vibrant red-orange gradient background
    val backgroundBrush = Brush.horizontalGradient(
        colors = listOf(
            colorResource(id = R.color.RedOrange).copy(alpha = 0.9f),
            colorResource(id = R.color.RedOrange).copy(alpha = 1f)
        )
    )

    // Adjusting the shimmer effect to be more focused and visible
    val shimmerBrush = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.3f), // Adjusted alpha for visibility
            Color.Transparent
        ),
        startX = lerp(fraction = shinePosition - 0.1f) * scale,
        endX = lerp(fraction = shinePosition) * scale
    )

    Box(
        modifier = modifier
            .padding(8.dp, 8.dp, 8.dp, 0.dp)
            .offset(y = (-15).dp)
            .fillMaxWidth(.5f)
            .background(brush = backgroundBrush)
            .background(brush = shimmerBrush)
    ) {
        Text(
            text = "Best Value (Save 66%)",
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.Center),
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = Bold,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

// Helper function for linear interpolation
private fun lerp(fraction: Float): Float {
    return (1 - fraction) * -1000f + fraction * 1000f
}

@Composable
fun singleClick(
    lastClickTimeState: MutableState<Long>,
    onClick: () -> Unit
): () -> Unit {
    return {
        val now = System.currentTimeMillis()
        if (now - lastClickTimeState.value >= 300) {
            onClick()
            lastClickTimeState.value = now
        }
    }
}