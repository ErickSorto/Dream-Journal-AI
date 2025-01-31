package org.ballistic.dreamjournalai.shared.dream_add_edit.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.ballistic.dreamjournalai.shared.dream_symbols.presentation.viewmodel.DictionaryWord

sealed class AddEditDreamEvent {
    data class ChangeDreamBackgroundImage(val dreamBackGroundImage: Int) : AddEditDreamEvent()
    data class ChangeLucidity(val lucidity: Int) : AddEditDreamEvent()
    data class ChangeVividness(val vividness: Int) : AddEditDreamEvent()
    data class ChangeRecurrence(val boolean: Boolean) : AddEditDreamEvent()
    data class ChangeNightmare(val boolean: Boolean) : AddEditDreamEvent()
    data class ChangeIsLucid(val boolean: Boolean) : AddEditDreamEvent()
    data class ChangeMood(val mood: Int) : AddEditDreamEvent()
    data class ChangeFalseAwakening(val boolean: Boolean) : AddEditDreamEvent()
    data class ChangeTimeOfDay(val timeOfDay: String) : AddEditDreamEvent()
    data class ChangeFavorite(val boolean: Boolean) : AddEditDreamEvent()

    data class ClickGenerateAIResponse(
        val value: String,
        val cost: Int
    ) : AddEditDreamEvent()
    data class AdAIResponseToggle(val value: Boolean) : AddEditDreamEvent()

    data class ClickGenerateAIImage(
        val value: String,
        val cost: Int
    ) : AddEditDreamEvent()
    data class AdAIImageToggle(val value: Boolean) : AddEditDreamEvent()

    data class ClickGenerateAIAdvice(
        val value: String,
        val cost: Int
    ) : AddEditDreamEvent()
    data class AdAIAdviceToggle(val value: Boolean) : AddEditDreamEvent()

    data class ClickGenerateFromQuestion(
        val value: String,
        val cost: Int
    ) : AddEditDreamEvent()
    data class AdQuestionToggle(val value: Boolean) : AddEditDreamEvent()

    data class ClickGenerateMood(
        val value: String,
        val cost: Int
    ) : AddEditDreamEvent()
    data class AdMoodToggle(val value: Boolean) : AddEditDreamEvent()

    data class ClickGenerateStory(
        val value: String,
        val cost: Int
    ) : AddEditDreamEvent()
    data class AdStoryToggle(val value: Boolean) : AddEditDreamEvent()

    data class ClickGenerateFromDescription(val value: Boolean) : AddEditDreamEvent()
    data class ChangeDetailsOfDream(val value: String) : AddEditDreamEvent()
    data class ChangeDreamWakeTime(val value: LocalTime) : AddEditDreamEvent()
    data class ChangeDreamSleepTime(val value: LocalTime) : AddEditDreamEvent()
    data class ChangeDreamDate(val value: LocalDate) : AddEditDreamEvent()
    data class ChangeQuestionOfDream(val value: String) : AddEditDreamEvent()
    data class SaveDream(val onSaveSuccess: () -> Unit) : AddEditDreamEvent()
    data object DeleteDream : AddEditDreamEvent()

    data class ClickWord(val word: DictionaryWord) : AddEditDreamEvent()

    data object GetUnlockedWords : AddEditDreamEvent()

    data object LoadWords : AddEditDreamEvent()

    data object ContentHasChanged : AddEditDreamEvent()

    data object FilterDreamWordInDictionary : AddEditDreamEvent()

    data class ClickBuyWord(val dictionaryWord: DictionaryWord, val isAd: Boolean) :
        AddEditDreamEvent()

    data class ToggleDreamImageGenerationPopUpState(val value: Boolean) : AddEditDreamEvent()
    data class ToggleDreamInterpretationPopUpState(val value: Boolean) : AddEditDreamEvent()
    data class ToggleDreamAdvicePopUpState(val value: Boolean) : AddEditDreamEvent()

    data class ToggleDreamQuestionPopUpState(val value: Boolean) : AddEditDreamEvent()

    data class ToggleDreamStoryPopUpState(val value: Boolean) : AddEditDreamEvent()

    data class ToggleDreamMoodPopUpState(val value: Boolean) : AddEditDreamEvent()

    data class ToggleDialogState(val value: Boolean) : AddEditDreamEvent()

    data class ToggleBottomSheetState(val value: Boolean) : AddEditDreamEvent()

    data class ToggleSleepTimePickerDialog(val show: Boolean) : AddEditDreamEvent()
    data class ToggleWakeTimePickerDialog(val show: Boolean) : AddEditDreamEvent()
    data class ToggleCalendarDialog(val show: Boolean) : AddEditDreamEvent()

    data class ToggleDreamHasChanged(val value: Boolean) : AddEditDreamEvent()
    data object FlagDreamContent : AddEditDreamEvent()

    data object GetDreamTokens : AddEditDreamEvent()

    data object OnCleared : AddEditDreamEvent()

    data object TriggerVibration : AddEditDreamEvent()
}