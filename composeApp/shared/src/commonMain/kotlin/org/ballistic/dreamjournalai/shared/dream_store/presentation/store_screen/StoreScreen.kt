package org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.check_mark
import dreamjournalai.composeapp.shared.generated.resources.dream_token_benefit_content_description
import dreamjournalai.composeapp.shared.generated.resources.get_more_dream_tokens
import org.ballistic.dreamjournalai.shared.core.components.DreamTokenLayout
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_store.domain.DreamTokenBenefit
import org.ballistic.dreamjournalai.shared.dream_store.domain.StoreEvent
import org.ballistic.dreamjournalai.shared.dream_store.presentation.anonymous_store_screen.AnonymousStoreScreen
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.components.CustomButtonLayout
import org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.viewmodel.StoreScreenViewModelState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.StringResource

@Composable
fun StoreScreen(
    storeScreenViewModelState: StoreScreenViewModelState,
    bottomPaddingValue: Dp,
    onMainEvent: (MainScreenEvent) -> Unit = {},
    onStoreEvent: (StoreEvent) -> Unit = {},
    navigateToAccountScreen: () -> Unit = {},
) {
    val isAnonymous = storeScreenViewModelState.isUserAnonymous
    val tokenTotal = storeScreenViewModelState.dreamTokens

    if (isAnonymous) {
        onMainEvent(MainScreenEvent.SetTopBarState(true))
        AnonymousStoreScreen(
            paddingValues = PaddingValues(bottom = 68.dp),
            navigateToAccountScreen = navigateToAccountScreen
        )
    } else {
        onMainEvent(MainScreenEvent.SetTopBarState(false))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPaddingValue)
                .dynamicBottomNavigationPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFE90F2F),
                                Color(0xFFF7AB5A)
                            ), // OrangeRed to Tomato gradient
                            startX = 0f,
                            endX = 1000f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.get_more_dream_tokens),
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.size(36.dp))
            CustomButtonLayout(
                storeScreenViewModelState = storeScreenViewModelState,
                buy100IsClicked = {
                    onStoreEvent(StoreEvent.ToggleLoading(true))
                    onStoreEvent(StoreEvent.Buy100DreamTokens)
                },
                buy500IsClicked = {
                    onStoreEvent(StoreEvent.ToggleLoading(true))
                    onStoreEvent(StoreEvent.Buy500DreamTokens)
                },
            )

            DreamBenefitInfoLayout(tokenTotal)
        }
    }
}

@Composable
fun DreamBenefitInfoLayout(
    totalDreamTokens: Int = 0
) {
    //val orientation = LocalContext.current.resources.configuration.orientation
    val pageAspectWidth = .9f

       // if (orientation == 1) .9f else .25f

    //one page and a little bit of the next page is visible
    val oneAndABitPerViewport = object : PageSize {
        override fun Density.calculateMainAxisPageSize(
            availableSpace: Int,
            pageSpacing: Int
        ): Int {
            return (availableSpace * pageAspectWidth).toInt()
        }
    }
    val pagerState = rememberPagerState(pageCount = {
        5
    })

    HorizontalPager(
        state = pagerState,
        pageSize = oneAndABitPerViewport,
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
    ) { page ->
        when (page) {
            0 -> DreamTokenBenefitItem(DreamTokenBenefit.DreamTokenSlideBenefit, totalDreamTokens)
            1 -> DreamTokenBenefitItem(DreamTokenBenefit.DreamPainting)
            2 -> DreamTokenBenefitItem(DreamTokenBenefit.DreamDictionary)
            3 -> DreamTokenBenefitItem(DreamTokenBenefit.DreamInterpretation)
            4 -> DreamTokenBenefitItem(DreamTokenBenefit.DreamAdFree)
        }
    }
}

@Composable
fun DreamTokenBenefitItem(
    dreamTokenBenefit: DreamTokenBenefit,
    totalDreamTokens: Int = 0
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 12f)
                .clip(RoundedCornerShape(8.dp))
                .background(OriginalXmlColors.LightBlack.copy(alpha = 0.8f))
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = painterResource(dreamTokenBenefit.image),
                contentDescription = stringResource(Res.string.dream_token_benefit_content_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = stringResource(dreamTokenBenefit.title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            if (dreamTokenBenefit.description != null) {
                Text(
                    text = stringResource(dreamTokenBenefit.description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                dreamTokenBenefit.benefit1?.let { CheckAndBenefit(it) }
                dreamTokenBenefit.benefit2?.let { CheckAndBenefit(it) }
                dreamTokenBenefit.benefit3?.let { CheckAndBenefit(it) }
                dreamTokenBenefit.benefit4?.let { CheckAndBenefit(it) }
            }
        }
        if (dreamTokenBenefit == DreamTokenBenefit.DreamTokenSlideBenefit) {
            DreamTokenLayout(
                totalDreamTokens = totalDreamTokens,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            )
        }
    }

}

@Composable
fun CheckAndBenefit(benefit: StringResource) {
    Row(
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(Res.drawable.check_mark),
            contentDescription = stringResource(Res.string.check_mark),
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.FillBounds
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(benefit),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
    }
}