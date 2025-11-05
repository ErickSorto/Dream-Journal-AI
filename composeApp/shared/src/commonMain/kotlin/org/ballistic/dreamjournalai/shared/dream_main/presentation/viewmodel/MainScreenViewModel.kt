package org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.background_during_day
import dreamjournalai.composeapp.shared.generated.resources.blue_lighthouse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.core.util.StoreLinkOpener
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.jetbrains.compose.resources.DrawableResource
import kotlin.time.ExperimentalTime

class MainScreenViewModel(
    private val repo: AuthRepository,
    private val vibratorUtil: VibratorUtil,
    private val storeLinkOpener: StoreLinkOpener
) : ViewModel() {
    private val _mainScreenViewModelState = MutableStateFlow(MainScreenViewModelState())
    val mainScreenViewModelState: StateFlow<MainScreenViewModelState> = _mainScreenViewModelState.asStateFlow()

    init {
        Logger.d("MainScreenViewModel") { "MainScreenViewModel initialized" }
        updateBackgroundPeriodically()
        _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
            backgroundResource = getBackgroundResource()
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun getBackgroundResource(): DrawableResource {
        val currentTime =  kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val currentHour = currentTime.hour

        return if (currentHour in 20..23 || currentHour in 0..5) {
            Res.drawable.blue_lighthouse
        } else {
            Res.drawable.blue_lighthouse
        }
    }

    private fun updateBackgroundPeriodically() {
        viewModelScope.launch {
            while (isActive) {
                val newResource = getBackgroundResource()
                if (newResource != _mainScreenViewModelState.value.backgroundResource) {
                    _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                        backgroundResource = newResource
                    )
                }
                delay(60000) // Check every minute
            }
        }
    }

    fun onEvent (event: MainScreenEvent) = viewModelScope.launch {
        Logger.d("MainScreenViewModel") { "onEvent: $event" }
        when (event) {
            is MainScreenEvent.SetBottomBarVisibilityState -> {
                viewModelScope.launch {
                    _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                        scaffoldState = _mainScreenViewModelState.value.scaffoldState.copy(
                            bottomBarState = event.state
                        )
                    )
                }
            }
            is MainScreenEvent.SetSearchingState -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    scaffoldState = _mainScreenViewModelState.value.scaffoldState.copy(
                        isUserSearching = event.state
                    )
                )
            }
            is MainScreenEvent.SetTopBarState -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    scaffoldState = _mainScreenViewModelState.value.scaffoldState.copy(
                        topBarState = event.state
                    )
                )
            }
            is MainScreenEvent.SetFloatingActionButtonState -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    scaffoldState = _mainScreenViewModelState.value.scaffoldState.copy(
                        floatingActionButtonState = event.state
                    )
                )
            }
            is MainScreenEvent.SetDrawerState -> {
                viewModelScope.launch {
                    _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                        isDrawerEnabled = event.state
                    )
                }
            }
            is MainScreenEvent.SearchDreams -> {
                // store plain string in state; composables should handle text input and emit events
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    searchedText = event.query
                )
            }
            is MainScreenEvent.ConsumeDreamTokens -> {
                val result = repo.consumeDreamTokens(event.tokensToConsume)
                if (result is Resource.Error) {
                    result.message?.let { msg ->
                        viewModelScope.launch {
                            SnackbarController.sendEvent(
                                org.ballistic.dreamjournalai.shared.SnackbarEvent(
                                    message = msg,
                                    action = org.ballistic.dreamjournalai.shared.SnackbarAction("Dismiss") { }
                                )
                            )
                        }
                    }
                }
            }
            is MainScreenEvent.ShowSnackBar -> {
                viewModelScope.launch {
                    SnackbarController.sendEvent(
                        org.ballistic.dreamjournalai.shared.SnackbarEvent(
                            message = event.message,
                            action = org.ballistic.dreamjournalai.shared.SnackbarAction("Dismiss") { }
                        )
                    )
                }
            }
            is MainScreenEvent.ToggleDrawerState -> {
                // ViewModel does not own a DrawerState (Compose UI object). Instead, store intent as a boolean.
                Logger.d("MainScreenViewModel") { "ToggleDrawerState -> ${event.drawerValue}" }
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    isDrawerOpen = (event.drawerValue != DrawerValue.Closed)
                )
            }
            is MainScreenEvent.UserInteracted -> {
                repo.recordUserInteraction()
            }
            is MainScreenEvent.GetAuthState -> {
                repo.getAuthState(viewModelScope)
            }

            is MainScreenEvent.SetBottomBarEnabledState -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    isBottomBarEnabledState = event.state
                )
            }
            is MainScreenEvent.UpdatePaddingValues -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    paddingValues = event.paddingValues
                )
            }

            is MainScreenEvent.SetDreamRecentlySaved -> {
                _mainScreenViewModelState.update{
                    it.copy(
                        isDreamRecentlySaved = event.state
                    )
                }
            }
            is MainScreenEvent.TriggerVibration -> {
                viewModelScope.launch { vibratorUtil.triggerVibration() }
            }
            is MainScreenEvent.OpenStoreLink -> {
                storeLinkOpener.openStoreLink()
            }
        }
    }
}

data class MainScreenViewModelState(
    val scaffoldState: ScaffoldState = ScaffoldState(),
    val isDrawerEnabled : Boolean = true,
    val isBottomBarEnabledState : Boolean = true,
    // ViewModel no longer holds a Compose DrawerState; instead expose a simple boolean intent
    val isDrawerOpen: Boolean = false,
    // Keep search text as a plain String for stability; composables handle input
    val searchedText: String = "",
    val dreamTokens: Int = 0,
    val backgroundResource: DrawableResource = Res.drawable.background_during_day,
    val paddingValues: PaddingValues = PaddingValues(0.dp),
    val isDreamRecentlySaved: Boolean = false
)
data class ScaffoldState (
    val bottomBarState: Boolean = true,
    val topBarState: Boolean = true,
    val floatingActionButtonState: Boolean = true,
    val isUserSearching : Boolean = false,
)