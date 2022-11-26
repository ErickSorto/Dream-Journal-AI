package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.components

import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.insets.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.pages.AIPage
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.pages.DescriptionPage
import org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.pages.InfoPage


@OptIn(ExperimentalPagerApi::class)
@Composable
@UiComposable
fun TabLayout(
    dreamColor: Int
) {


    val pages = listOf("Description", "AI", "Info")
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    //create tab layout with 3 tabs, one for dream title and content, one for dream information and one for dream artificial intelligence
    TabRow(
        modifier = Modifier,
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator( // custom indicator
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = Color.Black,
            )
        },
        contentColor = Color.Black,
        containerColor = Color.White.copy(alpha = 0.4f),
    ) {
        pages.forEachIndexed { index, page ->
            Tab(
                text = { Text(page, style = typography.titleSmall) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }


    }
    HorizontalPager(
        count = pages.size,
        state = pagerState,
    ) { page ->
        when (page) {
            0 -> {
                DescriptionPage(pagerState)
            }
            1 -> {
                AIPage()
            }
            2 -> {
                InfoPage()
            }
        }
    }
}