package org.ballistic.dreamjournalai.feature_dream.presentation.main_screen.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ballistic.dreamjournalai.navigation.Screens
import org.ballistic.dreamjournalai.user_authentication.domain.repository.AuthRepository
import javax.inject.Inject


@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {
    val scaffoldState = mutableStateOf(ScaffoldState())
    val snackbarHostState = mutableStateOf(SnackbarHostState())
    val startDestination = mutableStateOf(Screens.DreamJournalScreen.route)


    init {
        getAuthState()
    }

    fun getAuthState() = repo.getAuthState(viewModelScope)



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