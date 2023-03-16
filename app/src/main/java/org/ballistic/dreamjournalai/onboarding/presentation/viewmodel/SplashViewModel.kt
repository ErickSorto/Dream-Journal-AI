package org.ballistic.dreamjournalai.onboarding.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.navigation.Screens
import org.ballistic.dreamjournalai.onboarding.presentation.data.DataStoreRepository
import org.ballistic.dreamjournalai.user_authentication.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: DataStoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    val state_ = mutableStateOf(SplashState())
    val state: State<SplashState> = state_

    //if completed startDestination = DreamListScreen.route else Welcome.route

    init {
        viewModelScope.launch {
            repository.readOnBoardingState().collectLatest { completed ->
                if (completed) {
                    state_.value = state.value.copy(startDestination = Screens.DreamJournalScreen.route)
                } else {
                    state_.value = state.value.copy(startDestination = Screens.OnboardingScreen.route)
                }
                state_.value = state.value.copy(isLoading = false)
            }
        }
    }

    data class SplashState(
        val isLoading: Boolean = true,
        val startDestination: String = Screens.DreamJournalScreen.route
    )
}