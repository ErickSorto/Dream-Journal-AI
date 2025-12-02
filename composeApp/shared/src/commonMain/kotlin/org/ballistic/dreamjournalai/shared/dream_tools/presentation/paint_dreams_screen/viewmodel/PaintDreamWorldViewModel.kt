package org.ballistic.dreamjournalai.shared.dream_tools.presentation.paint_dreams_screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.core.util.formatLocalDate
import org.ballistic.dreamjournalai.shared.dream_add_edit.data.AIResult
import org.ballistic.dreamjournalai.shared.dream_add_edit.data.AITextType
import org.ballistic.dreamjournalai.shared.dream_add_edit.data.DreamAIService
import org.ballistic.dreamjournalai.shared.dream_add_edit.domain.ImageStyle
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.util.OrderType
import org.ballistic.dreamjournalai.shared.dream_tools.domain.DreamWorldPaintingRepository
import org.ballistic.dreamjournalai.shared.dream_tools.domain.event.PaintDreamWorldEvent
import org.ballistic.dreamjournalai.shared.dream_tools.domain.model.DreamWorldPainting
import kotlin.random.Random
import kotlin.time.ExperimentalTime

class PaintDreamWorldViewModel(
    private val dreamUseCases: DreamUseCases,
    private val authRepository: AuthRepository,
    private val dreamWorldPaintingRepository: DreamWorldPaintingRepository,
    private val aiService: DreamAIService,
    private val vibratorUtil: VibratorUtil
) : ViewModel() {

    private val _state = MutableStateFlow(PaintDreamWorldScreenState())
    val state: StateFlow<PaintDreamWorldScreenState> = _state.asStateFlow()

    private val logger = Logger.withTag("PaintDreamWorldVM")

    init {
        loadPaintings()
        observeTokensAndStatus()
    }

    fun onEvent(event: PaintDreamWorldEvent) {
        when (event) {
            is PaintDreamWorldEvent.GeneratePainting -> generatePainting(event.cost, event.style)
            is PaintDreamWorldEvent.DeletePainting -> deletePainting(event.painting)
            is PaintDreamWorldEvent.SelectPainting -> _state.update { it.copy(selectedPainting = event.painting) }
            is PaintDreamWorldEvent.ToggleDeleteConfirmation -> _state.update { it.copy(isDeleteDialogVisible = event.show) }
            is PaintDreamWorldEvent.SetPaintingToDelete -> _state.update { it.copy(paintingToDelete = event.painting) }
            is PaintDreamWorldEvent.ToggleImageGenerationPopUp -> {
                if (event.isVisible) {
                    viewModelScope.launch {
                        vibratorUtil.triggerVibration()
                    }
                }
                _state.update { it.copy(isImageGenerationPopUpVisible = event.isVisible) }
            }
            is PaintDreamWorldEvent.OnImageStyleChanged -> _state.update { it.copy(imageStyle = event.style) }
            PaintDreamWorldEvent.AnimationFinished -> _state.update { it.copy(isAnimationPlayed = true) }
            PaintDreamWorldEvent.TriggerVibration -> {
                viewModelScope.launch {
                    vibratorUtil.triggerVibration()
                }
            }
        }
    }

    private fun loadPaintings() {
        dreamWorldPaintingRepository.getPaintings()
            .onEach { paintings ->
                _state.update { 
                    val sorted = paintings.sortedByDescending { p -> p.timestamp }
                    it.copy(paintings = sorted, selectedPainting = if (it.selectedPainting == null) sorted.firstOrNull() else it.selectedPainting)
                }
            }
            .catch { e ->
                logger.e(e) { "Error loading paintings" }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTokensAndStatus() {
        // Listen to tokens
        viewModelScope.launch {
            authRepository.addDreamTokensFlowListener().collect { resource ->
                if (resource is Resource.Success) {
                    _state.update { it.copy(dreamTokens = resource.data?.toInt() ?: 0) }
                }
            }
        }
        
        // Listen to hasGeneratedDreamWorld status
        viewModelScope.launch {
            authRepository.hasGeneratedDreamWorld.collect { hasGenerated ->
                _state.update { it.copy(hasGeneratedDreamWorld = hasGenerated) }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun generatePainting(cost: Int, style: String) {
        logger.d { "generatePainting called. Cost: $cost, Style: $style" }
        if (_state.value.dreamTokens < cost) {
            logger.w { "Not enough tokens. Have: ${_state.value.dreamTokens}, Need: $cost" }
             _state.update { it.copy(error = "Not enough tokens ($cost required)") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isImageGenerationPopUpVisible = false) }
            
            try {
                val allDreams = dreamUseCases.getDreams(OrderType.Date).first()
                
                if (allDreams.size < 3) {
                     _state.update { it.copy(isLoading = false, error = "Need at least 3 dreams to generate a world") }
                     return@launch
                }
                
                val dreamsToProcess = if (allDreams.size > 15) {
                    allDreams.shuffled().take(15)
                } else {
                    allDreams
                }

                val content = dreamsToProcess.joinToString("\n\n") { dream ->
                    val transcription = if (dream.audioTranscription.isNotBlank()) "\nTranscription: ${dream.audioTranscription}" else ""
                    "Date: ${dream.date}\nContent: ${dream.content}$transcription"
                }

                val summaryResult = aiService.generateText(AITextType.DREAM_WORLD_SUMMARY, content, cost = 0)
                if (summaryResult is AIResult.Error) {
                    _state.update { it.copy(isLoading = false, error = "Failed to summarize: ${summaryResult.message}") }
                    return@launch
                }
                
                val summary = (summaryResult as AIResult.Success).data
                
                // Generate Image
                // Using gpt-image-1 as requested
                // Style is now passed directly (it contains the worldPromptAffix from ImageStyle)
                val imageResult = aiService.generateImageFromDetails(summary, cost = cost, style = style, model = "gpt-image-1")
                
                if (imageResult is AIResult.Error) {
                     _state.update { it.copy(isLoading = false, error = "Failed to paint: ${imageResult.message}") }
                     return@launch
                }
                
                val imageUrl = (imageResult as AIResult.Success).data
                
                // Success - Consume tokens & Save
                if (cost > 0) {
                    authRepository.consumeDreamTokens(cost)
                }
                
                // Mark as generated if it wasn't already
                if (!_state.value.hasGeneratedDreamWorld) {
                    authRepository.setHasGeneratedDreamWorld(true)
                }
                
                val now = kotlin.time.Clock.System.now()
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                
                // Generate a random ID if not provided by repo (assuming client-side ID gen is ok or placeholder)
                // Since we need to select it immediately, we generate a unique ID here.
                val id = now.toEpochMilliseconds().toString() + Random.nextInt()

                val painting = DreamWorldPainting(
                    id = id,
                    imageUrl = imageUrl,
                    description = summary,
                    timestamp = now.toEpochMilliseconds(),
                    date = formatLocalDate(today)
                )

                dreamWorldPaintingRepository.savePainting(painting)
                vibratorUtil.triggerVibration()
                
                _state.update { it.copy(isLoading = false, selectedPainting = painting) }
                
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun deletePainting(painting: DreamWorldPainting) {
        viewModelScope.launch {
            dreamWorldPaintingRepository.deletePainting(painting.id)
            if (_state.value.selectedPainting?.id == painting.id) {
                _state.update { it.copy(selectedPainting = null) }
            }
        }
    }
}

data class PaintDreamWorldScreenState(
    val paintings: List<DreamWorldPainting> = emptyList(),
    val selectedPainting: DreamWorldPainting? = null,
    val paintingToDelete: DreamWorldPainting? = null,
    val isLoading: Boolean = false,
    val dreamTokens: Int = 0,
    val hasGeneratedDreamWorld: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val isImageGenerationPopUpVisible: Boolean = false,
    val isAnimationPlayed: Boolean = false,
    val imageStyle: ImageStyle = ImageStyle.LET_AI_CHOOSE,
    val error: String? = null
)
