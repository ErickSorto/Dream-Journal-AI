package org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainScreenViewModel @Inject constructor(
) : ViewModel() {
    val scaffoldState = mutableStateOf(ScaffoldState())
    val snackbarHostState = mutableStateOf(SnackbarHostState())



    fun setBottomBarState(state: Boolean) {
        scaffoldState.value = scaffoldState.value.copy(bottomBarState = state)
    }
    fun setSearchingState(state: Boolean) {
        scaffoldState.value = scaffoldState.value.copy(isUserSearching = state)
    }
    fun getSearchingState(): Boolean {
        return scaffoldState.value.isUserSearching
    }

    fun setTopBarState(state: Boolean) {
        scaffoldState.value = scaffoldState.value.copy(topBarState = state)
    }

    fun setFloatingActionButtonState(state: Boolean) {
        scaffoldState.value = scaffoldState.value.copy(floatingActionButtonState = state)
    }

    fun getBottomBarState(): Boolean {
        return scaffoldState.value.bottomBarState
    }

    fun getTopBarState(): Boolean {
        return scaffoldState.value.topBarState
    }

    fun getFloatingActionButtonState(): Boolean {
        return scaffoldState.value.floatingActionButtonState
    }

}

data class ScaffoldState (
    val bottomBarState: Boolean = true,
    val topBarState: Boolean = true,
    val floatingActionButtonState: Boolean = true,
    val isUserSearching : Boolean = false
)