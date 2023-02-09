package org.ballistic.dreamjournalai.onboarding.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.onboarding.data.DataStoreRepository
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {
    val state_ = mutableStateOf(SplashState())
    val state: State<SplashState> = state_


    //if completed startDestination = DreamListScreen.route else Welcome.route

    init {
        viewModelScope.launch {
            repository.readOnBoardingState().collectLatest { completed ->
                if (completed) {
                    state_.value = state.value.copy(startDestination = Screens.DreamListScreen.route)

                } else {
                    state_.value = state.value.copy(startDestination = Screens.Welcome.route)
                }
                state_.value = state.value.copy(isLoading = false)
            }
        }
    }


    data class SplashState(
        val isLoading: Boolean = true,
        val startDestination: String = Screens.DreamListScreen.route
    )
}