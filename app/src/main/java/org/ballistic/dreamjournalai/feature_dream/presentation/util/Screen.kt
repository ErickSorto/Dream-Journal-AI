package org.ballistic.dreamjournalai.feature_dream.presentation.util

sealed class Screen(val route: String) {
    object DreamsScreen : Screen("dreams_screen")
    object AddEditDreamScreen : Screen("add_edit_dream_screen")
}