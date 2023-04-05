package org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.MainScreenEvent
import org.ballistic.dreamjournalai.user_authentication.domain.repository.AuthRepository
import javax.inject.Inject


@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {
    private val _mainScreenViewModelState = MutableStateFlow(MainScreenViewModelState())
    val mainScreenViewModelState: StateFlow<MainScreenViewModelState> = _mainScreenViewModelState.asStateFlow()


    init {
        getAuthState()
    }

    fun getAuthState() = repo.getAuthState(viewModelScope)

    fun onEvent (event: MainScreenEvent) = viewModelScope.launch {
        when (event) {
            is MainScreenEvent.SetBottomBarState -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    scaffoldState = _mainScreenViewModelState.value.scaffoldState.copy(
                        bottomBarState = event.state
                    )
                )
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
        }
    }



//    fun setBottomBarState(state: Boolean) {
//        scaffoldState.value = scaffoldState.value.copy(bottomBarState = state)
//    }
//    fun setSearchingState(state: Boolean) {
//        scaffoldState.value = scaffoldState.value.copy(isUserSearching = state)
//    }
//    fun getSearchingState(): Boolean {
//        return scaffoldState.value.isUserSearching
//    }
//
//    fun setTopBarState(state: Boolean) {
//        scaffoldState.value = scaffoldState.value.copy(topBarState = state)
//    }
//
//    fun setFloatingActionButtonState(state: Boolean) {
//        scaffoldState.value = scaffoldState.value.copy(floatingActionButtonState = state)
//    }
//
//    fun getBottomBarState(): Boolean {
//        return scaffoldState.value.bottomBarState
//    }
//
//    fun getTopBarState(): Boolean {
//        return scaffoldState.value.topBarState
//    }
//
//    fun getFloatingActionButtonState(): Boolean {
//        return scaffoldState.value.floatingActionButtonState
//    }
}


data class MainScreenViewModelState(
    val scaffoldState: ScaffoldState = ScaffoldState(),
)
data class ScaffoldState (
    val bottomBarState: Boolean = true,
    val topBarState: Boolean = true,
    val floatingActionButtonState: Boolean = true,
    val isUserSearching : Boolean = false,
    val snackBarHostState: MutableState<SnackbarHostState> = mutableStateOf(SnackbarHostState()),
)