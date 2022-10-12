package org.ballistic.dreamjournalai.feature_dream.presentation.add_edit_dream

import androidx.compose.ui.focus.FocusState

sealed class AddEditDreamEvent(

) {
    data class EnteredTitle(val value: String) : AddEditDreamEvent()
    data class ChangeTitleFocus(val focusState: FocusState) : AddEditDreamEvent()
    data class EnteredContent(val value: String) : AddEditDreamEvent()
    data class ChangeContentFocus(val focusState: FocusState) : AddEditDreamEvent()
    data class ChangeColor(val color: Int) : AddEditDreamEvent()
    object SaveDream : AddEditDreamEvent()
}