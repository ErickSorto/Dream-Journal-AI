package org.ballistic.dreamjournalai.onboarding.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.navigation.Screens
import org.ballistic.dreamjournalai.onboarding.data.DataStoreRepository
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    private val _isLoading: MutableState<Boolean> = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    //if completed startDestination = DreamListScreen.route else Welcome.route
    private val _startDestination: MutableState<String> = mutableStateOf(Screens.Welcome.route)

    val startDestination: State<String> = _startDestination

    init {
        viewModelScope.launch {
            repository.readOnBoardingState().collectLatest { completed ->
                if (completed) {
                    _startDestination.value = Screens.DreamListScreen.route
                } else {
                    _startDestination.value = Screens.Welcome.route
                }
                _isLoading.value = false
            }
        }
    }
}