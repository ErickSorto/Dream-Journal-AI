package org.ballistic.dreamjournalai.feature_dream.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import org.ballistic.dreamjournalai.R.drawable.ic_baseline_home_24

sealed class Screens(
    val route: String,
    val title: String?= null,
    val icon: ImageVector?= null
) {
    object DreamListScreen: Screens(
        route = "dreams_screen",
        title = "Home",
        icon = Icons.Default.Home
    )

    object AddEditDreamScreen: Screens(
        route = "add_edit_dream_screen",
        title = "AddEdit",
        icon = Icons.Default.Add
    )

    object StoreSignInScreen: Screens(
        route = "store_sign_in",
        title = "Shop",
        icon = Icons.Default.Shop
    )

}