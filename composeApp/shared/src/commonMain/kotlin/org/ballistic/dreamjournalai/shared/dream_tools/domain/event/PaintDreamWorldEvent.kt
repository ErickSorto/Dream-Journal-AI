package org.ballistic.dreamjournalai.shared.dream_tools.domain.event

import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.ImageStyle
import org.ballistic.dreamjournalai.shared.dream_tools.domain.model.DreamWorldPainting

sealed interface PaintDreamWorldEvent {
    data class GeneratePainting(val cost: Int, val style: String) : PaintDreamWorldEvent
    data class DeletePainting(val painting: DreamWorldPainting) : PaintDreamWorldEvent
    data class SetPaintingToDelete(val painting: DreamWorldPainting?) : PaintDreamWorldEvent
    data class ToggleDeleteConfirmation(val show: Boolean) : PaintDreamWorldEvent
    data class SelectPainting(val painting: DreamWorldPainting?) : PaintDreamWorldEvent
    data class ToggleImageGenerationPopUp(val isVisible: Boolean) : PaintDreamWorldEvent
    data class OnImageStyleChanged(val style: ImageStyle) : PaintDreamWorldEvent
    data object AnimationFinished : PaintDreamWorldEvent
    data object TriggerVibration : PaintDreamWorldEvent
}
