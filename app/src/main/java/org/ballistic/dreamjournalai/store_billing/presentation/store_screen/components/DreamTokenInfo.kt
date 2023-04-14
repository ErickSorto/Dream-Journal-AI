package org.ballistic.dreamjournalai.store_billing.presentation.store_screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import org.ballistic.dreamjournalai.R


@Composable
fun DreamTokenInfo(modifier: Modifier) {

    Column(
        modifier = modifier
            .padding(top = 32.dp)
            .background(
                colorResource(id = R.color.white).copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        SubInfoMonthlyYearlyPrice()
        SubInfoFeatures()
    }
}

@Composable
fun SubInfoMonthlyYearlyPrice() {
    val isYearly = remember {
        mutableStateOf(true)
    }
    Column(
        modifier = Modifier
            .padding(paddingValues = PaddingValues(8.dp, 16.dp, 8.dp, 8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubscriptionTabText(
                text = "Monthly",
                modifier = Modifier.weight(1f),
                isYearly.value,
                isClicked = { isYearly.value = false })


            SubscriptionTabText(
                text = "Annual",
                modifier = Modifier.weight(1f),
                isYearly.value,
                isClicked = { isYearly.value = true })
        }


        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\$3.33/month",
                modifier = Modifier.padding(8.dp),
                fontSize = 20.sp,
                fontWeight = Bold,
                color = Color.Black
            )
            Text(
                text = "(39.99/year)",
                modifier = Modifier.padding(8.dp),
                fontSize = 20.sp,
                fontWeight = Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun SavingsLabel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = colorResource(id = R.color.Yellow).copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Text(
            text = "Save 45%",
            modifier = Modifier.padding(8.dp),
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = Bold,
            maxLines = 1
        )
    }
}

@Composable
fun SubInfoFeatures() {
    Column(
        modifier = Modifier
            .padding(paddingValues = PaddingValues(0.dp, 8.dp, 0.dp, 0.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.Yellow).copy(alpha = 0.8f),
                        colorResource(id = R.color.Yellow).copy(alpha = 1f)
                    )
                ),
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp)
            )
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CheckmarkAndText(
            text = "1200 Dream Tokens",
            subText = "(100 monthly)",
            modifier = Modifier
        )
        CheckmarkAndText(
            text = "Access dictionary",
            subText = "(Coming Soon)",
            modifier = Modifier
        )
        CheckmarkAndText(
            text = "Access AI recorder",
            subText = "(Coming Soon)",
            modifier = Modifier
        )
        CheckmarkAndText(
            text = "No Ads",
            subText = "",
            modifier = Modifier
        )
    }
}

@Composable
fun SubscriptionTabText(
    text: String,
    modifier: Modifier = Modifier,
    isYearly: Boolean,
    isClicked: () -> Unit
) {
    TextButton(
        modifier = modifier
            .background(
                if (isYearly && text == "Annual" || !isYearly && text == "Monthly") {
                    Color.White.copy(alpha = 0.3f)
                } else {
                    Color.White.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(16.dp)
            ),
        onClick = { isClicked() },
    )
    {
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = Bold,
            maxLines = 1,
            modifier = Modifier.padding(4.dp, 8.dp, 8.dp, 8.dp)
        )

        if (text == "Annual") {
            SavingsLabel()
        }
    }
}

@Composable
fun CheckmarkAndText(
    text: String,
    subText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(R.drawable.baseline_check_24),
            contentDescription = "checkmark",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(48.dp)
                .padding(8.dp, 8.dp, 0.dp, 8.dp)
        )
        Text(text = text, fontSize = 16.sp, color = Color.White, fontWeight = Bold)
        Text(text = subText, fontSize = 12.sp, color = Color.White, fontWeight = Bold)
    }
}

@Composable
fun CustomButtonLayout(
    buy500IsClicked: () -> Unit,
    buy100IsClicked: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp, 8.dp, 8.dp),
    ) {
        val (monthly, annual, tag) = createRefs()

        DreamToken500ButtonBuy(
            modifier = Modifier.constrainAs(annual) {
                bottom.linkTo(monthly.top)
            },
            buy500IsClicked = {
                buy500IsClicked()
            })

        DreamToken100ButtonBuy(
            modifier = Modifier.constrainAs(monthly) {
                bottom.linkTo(parent.bottom)
            },
            buy100IsClicked = {
                buy100IsClicked()
            }
        )
        MostPopularBanner(modifier = Modifier.constrainAs(tag) {

        })
    }
}

@Composable
fun DreamToken500ButtonBuy(
    modifier: Modifier,
    buy500IsClicked: () -> Unit = {}
) {
    Button(
        onClick = { buy500IsClicked() },
        modifier = modifier
            .padding(8.dp, 0.dp, 8.dp, 8.dp)
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.Yellow).copy(alpha = 0.8f),
            contentColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "500 DreamTokens",
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
                Text(
                    text = "\$19.99",
                    fontSize = 20.sp,
                    fontWeight = Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(8.dp, 8.dp, 8.dp, 0.dp)
                )
                Text(
                    text = "Save 45%",
                    fontSize = 12.sp,
                    fontWeight = Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(8.dp, 0.dp, 8.dp, 8.dp)
                )
            }
        }
    }
}

@Composable
fun DreamToken100ButtonBuy(
    modifier: Modifier,
    buy100IsClicked: () -> Unit
) {
    Button(
        onClick = { buy100IsClicked() },
        modifier = modifier
            .padding(8.dp, 8.dp, 8.dp, 8.dp)
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.sky_blue).copy(alpha = 0.8f),
            contentColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "100 DreamTokens",
                fontSize = 20.sp,
                fontWeight = Bold,
                maxLines = 1,
                modifier = Modifier
                    .padding(8.dp, 8.dp, 8.dp, 8.dp)
                    .align(Alignment.CenterStart)
            )

            Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                Text(
                    text = "\$4.99",
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
fun MostPopularBanner(modifier: Modifier) {
    //offset by item height/ 2
    Box(
        modifier = modifier
            .padding(8.dp, 8.dp, 8.dp, 0.dp)
            .offset(y = (-20).dp)
            .fillMaxWidth(.5f)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colorResource(id = R.color.RedOrange).copy(alpha = 0.9f),
                        colorResource(id = R.color.RedOrange),
                    )
                ),
            )
    ) {
        Text(
            text = "Most Popular",
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.Center),
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = Bold,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}