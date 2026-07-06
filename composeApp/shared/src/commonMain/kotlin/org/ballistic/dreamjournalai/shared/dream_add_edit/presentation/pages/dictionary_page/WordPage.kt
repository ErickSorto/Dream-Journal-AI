package org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.pages.dictionary_page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.symbols_empty_hero
import org.ballistic.dreamjournalai.shared.core.components.PremiumIllustratedEmptyState
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.AddEditDreamEvent
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.components.ArcRotationAnimation
import org.ballistic.dreamjournalai.shared.dream_add_edit.presentation.viewmodel.AddEditDreamState
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.components.BuySymbolBottomSheet
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.components.DictionaryWordDrawer
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.components.DictionaryWordItem


@Composable
fun WordPage(
    addEditDreamState: AddEditDreamState,
    onAddEditDreamEvent: (AddEditDreamEvent) -> Unit
) {
    val dreamTokens = addEditDreamState.dreamTokens
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val processedWords = addEditDreamState.dreamFilteredDictionaryWords.map { wordItem ->
        wordItem.copy(
            isUnlocked = wordItem.isUnlocked || addEditDreamState.unlockedWords.contains(wordItem.word),
            cost = if (addEditDreamState.unlockedWords.contains(wordItem.word)) 0 else wordItem.cost
        )
    }

    if (addEditDreamState.dreamFilteredDictionaryWords.isEmpty() && !addEditDreamState.isDreamFilterLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PremiumIllustratedEmptyState(
                image = Res.drawable.symbols_empty_hero,
                eyebrow = "Dream symbols",
                title = "Write a little more to discover symbols.",
                body = "Once your dream has a few more details, symbols, themes, and hidden meanings will start showing up here for you to explore.",
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
        return
    }

    if (addEditDreamState.isDreamFilterLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .padding(16.dp, 16.dp, 16.dp, 16.dp),
            contentAlignment = Alignment.Center
        ) {
            ArcRotationAnimation()
        }
    } else {
        if (!addEditDreamState.isClickedWordUnlocked && addEditDreamState.bottomSheetState) {
            BuySymbolBottomSheet(
                title = addEditDreamState.clickedWord.word,
                onAdClick = {
                    scope.launch {
                        onAddEditDreamEvent(
                            AddEditDreamEvent.ClickBuyWord(
                                dictionaryWord = addEditDreamState.clickedWord,
                                isAd = true,
                            )
                        )
                    }
                },
                onDreamTokenClick = {
                    onAddEditDreamEvent(
                        AddEditDreamEvent.ClickBuyWord(
                            dictionaryWord = addEditDreamState.clickedWord,
                            isAd = false,
                        )
                    )
                },
                onClickOutside = {
                    onAddEditDreamEvent(AddEditDreamEvent.ToggleBottomSheetState(false))
                },
                token = dreamTokens,
                amount = addEditDreamState.clickedWord.cost
            )
        } else if (addEditDreamState.isClickedWordUnlocked && addEditDreamState.bottomSheetState) {
            DictionaryWordDrawer(
                title = addEditDreamState.clickedWord.word,
                definition = addEditDreamState.clickedWord.definition,
                onClickOutside = {
                    onAddEditDreamEvent(AddEditDreamEvent.ToggleBottomSheetState(false))
                },
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp, top = 8.dp)
        ) {
            items(processedWords) { wordItem ->
                DictionaryWordItem(
                    wordItem = wordItem,
                    onWordClick = { onAddEditDreamEvent(AddEditDreamEvent.ClickWord(wordItem)) }
                )
            }
        }
    }
}
