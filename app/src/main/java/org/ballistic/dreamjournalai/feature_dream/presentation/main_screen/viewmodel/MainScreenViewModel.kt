package org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainScreenViewModel @Inject constructor(
) : ViewModel() {
    val scaffoldState = mutableStateOf(ScaffoldState())

    fun setBottomBarState(state: Boolean) {
        scaffoldState.value = scaffoldState.value.copy(bottomBarState = state)
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
    val floatingActionButtonState: Boolean = true
)