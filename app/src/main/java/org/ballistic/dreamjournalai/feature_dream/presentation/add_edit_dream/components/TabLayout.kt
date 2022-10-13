package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream.components

import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable




@Composable
@UiComposable
fun TabLayout(
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf(0) }
    val titles = listOf("TAB 1", "TAB 2", "TAB 3")

    val indicator = @Composable { tabPositions: List<TabPosition> ->
        TabRowDefaults.Indicator( // custom indicator
            Modifier.tabIndicatorOffset(tabPositions[state])
        )
    }
    //create tab layout with 3 tabs, one for dream title and content, one for dream information and one for dream artificial intelligence
    TabRow(
        selectedTabIndex = 0,
        indicator = indicator
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = state == index,
                onClick = { state = index }
            )
        }
    }

}