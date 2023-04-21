package org.ballistic.dreamjournalai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null
) {
    object DreamJournalScreen : Screens(
        route = "dreams_screen",
        title = "My Dream Journal",
        icon = Icons.Default.Book
    )

    object AddEditDreamScreen : Screens(
        route = "add_edit_dream_screen",
        title = "AddEdit",
        icon = Icons.Default.Add
    )

    object SignInScreen : Screens(
        route = "sign_in",
        title = "SignIn",
        icon = Icons.Default.Person
    )

    object StoreScreen : Screens(
        route = "dream_store",
        title = "Store",
        icon = Icons.Default.Shop
    )

    object Favorites : Screens(
        route = "favorites",
        title = "Favorites",
        icon = Icons.Default.Favorite
    )

    object AccountSettings : Screens(
        route = "account_settings",
        title = "Account Settings",
        icon = Icons.Default.Settings
    )

    object AboutMe : Screens(
        route = "about_me",
        title = "About Me",
        icon = Icons.Default.Info
    )

    object Tools : Screens(
        route = "tools",
        title = "Tools",
        icon = Icons.Default.Build
    )

    object Statistics : Screens(
        route = "statistics",
        title = "Statistics",
        icon = Icons.Default.BarChart
    )

    object NotificationSettings : Screens(
        route = "notification_settings",
        title = "Notification Settings",
        icon = Icons.Default.Notifications
    )

    object Nightmares : Screens(
        route = "nightmares",
        title = "Nightmares",
        icon = Icons.Default.ErrorOutline
    )

    object Dictionary : Screens(
        route = "dictionary",
        title = "Dictionary",
        icon = Icons.Default.List
    )

    object DreamSettings : Screens(
        "dream_settings",
        "Dream Settings",
        Icons.Default.Bedtime
    )

    object OnboardingScreen : Screens(route = "welcome_screen", title = "Welcome", icon = null)
    object MainScreen : Screens(route = "main_screen", title = "Main", icon = null)
}