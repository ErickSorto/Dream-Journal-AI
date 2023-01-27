package org.ballistic.dreamjournalai.feature_dream.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

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


    object SignInScreen: Screens(
        route = "sign_in",
        title = "SignIn",
        icon = Icons.Default.Shop
    )

    object StoreScreen: Screens(
        route = "dream_store",
        title = "Store",
        icon = Icons.Default.Shop
    )

    object Welcome : Screens(route = "welcome_screen", title = "Welcome", icon = null)

}