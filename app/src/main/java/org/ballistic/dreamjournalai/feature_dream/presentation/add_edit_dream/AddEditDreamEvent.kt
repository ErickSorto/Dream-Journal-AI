package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream

import androidx.compose.ui.focus.FocusState

sealed class AddEditDreamEvent{
    data class EnteredTitle(val value: String) : AddEditDreamEvent()
    data class ChangeTitleFocus(val focusState: FocusState) : AddEditDreamEvent()
    data class EnteredContent(val value: String) : AddEditDreamEvent()
    data class ChangeContentFocus(val focusState: FocusState) : AddEditDreamEvent()
    data class ChangeColorBackground(val colorBackGroundImage: Int) : AddEditDreamEvent()
    data class ChangeLucidity(val lucidity: Int) : AddEditDreamEvent()
    data class ChangeVividness(val vividness: Int) : AddEditDreamEvent()
    data class ChangeRecurrence(val boolean: Boolean) : AddEditDreamEvent()
    data class ChangeNightmare(val boolean: Boolean) : AddEditDreamEvent()
    data class ChangeIsLucid(val boolean: Boolean) : AddEditDreamEvent()
    data class ChangeRealism(val realism: Int) : AddEditDreamEvent()
    data class ChangeMood(val mood: Int) : AddEditDreamEvent()
    data class ChangeFalseAwakening(val boolean: Boolean) : AddEditDreamEvent()
    data class ChangeTimeOfDay(val timeOfDay: String) : AddEditDreamEvent()
    data class ChangeFavorite(val boolean: Boolean) : AddEditDreamEvent()
    data class AIResponse(val response: String) : AddEditDreamEvent()

    object SaveDream : AddEditDreamEvent()
}