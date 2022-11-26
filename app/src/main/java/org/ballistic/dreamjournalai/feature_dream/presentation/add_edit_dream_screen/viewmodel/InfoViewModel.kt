package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream_screen.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.ballistic.dreamjournalai.feature_dream.domain.use_case.GetOpenAITextResponse
import javax.inject.Inject

class InfoViewModel @Inject constructor(
    val getOpenAITextResponse: GetOpenAITextResponse,
    savedStateHandle: SavedStateHandle

): ViewModel(){




}