package org.ballistic.dreamjournalai.shared.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.ui.graphics.vector.ImageVector
import dreamjournalai.composeapp.shared.generated.resources.DreamIntellegentRealityChecker
import dreamjournalai.composeapp.shared.generated.resources.DreamInterpreterMassTool
import dreamjournalai.composeapp.shared.generated.resources.DreamPredictorTool
import dreamjournalai.composeapp.shared.generated.resources.DreamRandomPicker
import dreamjournalai.composeapp.shared.generated.resources.DreamStatisticalAnalysis
import dreamjournalai.composeapp.shared.generated.resources.DreamWorldTool
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.account_settings
import dreamjournalai.composeapp.shared.generated.resources.dream_tools
import dreamjournalai.composeapp.shared.generated.resources.export_dreams
import dreamjournalai.composeapp.shared.generated.resources.favorites
import dreamjournalai.composeapp.shared.generated.resources.my_dreams
import dreamjournalai.composeapp.shared.generated.resources.nightmares
import dreamjournalai.composeapp.shared.generated.resources.rate_this_app
import dreamjournalai.composeapp.shared.generated.resources.statistics
import dreamjournalai.composeapp.shared.generated.resources.store
import dreamjournalai.composeapp.shared.generated.resources.symbols
import dreamjournalai.composeapp.shared.generated.resources.mass_dream_interpreter_title
import dreamjournalai.composeapp.shared.generated.resources.mass_dream_interpreter_description
import dreamjournalai.composeapp.shared.generated.resources.dream_world_painter_title
import dreamjournalai.composeapp.shared.generated.resources.dream_world_painter_description
import dreamjournalai.composeapp.shared.generated.resources.random_dream_picker_title
import dreamjournalai.composeapp.shared.generated.resources.random_dream_picker_description
import dreamjournalai.composeapp.shared.generated.resources.analyze_statistics_title
import dreamjournalai.composeapp.shared.generated.resources.analyze_statistics_description
import dreamjournalai.composeapp.shared.generated.resources.dynamic_lucid_checker_title
import dreamjournalai.composeapp.shared.generated.resources.dynamic_lucid_checker_description
import dreamjournalai.composeapp.shared.generated.resources.dream_journal_reminder_title
import dreamjournalai.composeapp.shared.generated.resources.dream_journal_reminder_description
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed class Route{
    @Serializable
    data object DreamJournalScreen : Route()

    @Serializable
    data class AddEditDreamScreen(val dreamID: String?, val backgroundID: Int) : Route()

    @Serializable
    data object MainScreen : Route()

    @Serializable
    data object OnboardingScreen : Route()

    @Serializable
    data object StoreScreen : Route()

    @Serializable
    data object Favorites : Route()

    @Serializable
    data object AccountSettings : Route()

    @Serializable
    data object Tools : Route()

    @Serializable
    data object AnalyzeMultipleDreams : Route()

    @Serializable
    data object PaintDreamWorldDetails : Route()

    @Serializable
    data object DynamicLucidChecker : Route()

    @Serializable
    data object PaintDreamWorld : Route()

    @Serializable
    data object Statistics : Route()

    @Serializable
    data object NotificationSettings : Route()

    @Serializable
    data object Nightmares : Route()

    @Serializable
    data object Symbol : Route()

    @Serializable
    data object DreamToolGraphScreen : Route()

    @Serializable
    data class FullScreenImageScreen(val imageID: String) : Route()

    @Serializable
    data object RateMyApp : Route()
    
    @Serializable
    data object ExportDreams : Route()

    @Serializable
    data object AboutMeScreen : Route()
}


/**
 * A sealed class representing each "tool route"
 * in a type-safe, serializable manner for KMM & Compose Navigation.
 */
@Serializable
sealed class ToolRoute(
    val image: String
) {
    @Serializable
    data class AnalyzeMultipleDreamsDetails(
        val imageID: String
    ) : ToolRoute(imageID)

    @Serializable
    data class RandomDreamPicker(
        val imageID: String
    ) : ToolRoute(imageID)

    @Serializable
    data class PaintDreamWorld(
        val imageID: String
    ) : ToolRoute(imageID)

    @Serializable
    data class DreamJournalReminder(
        val imageID: String
    ) : ToolRoute(imageID)

    @Serializable
    data class DynamicLucidChecker(
        val imageID: String
    ) : ToolRoute(imageID)

    @Serializable
    data class Statistics(
        val imageID: String
    ) : ToolRoute(imageID)
    
    @Serializable
    data class FullScreenImage(
        val imageURL: String
    ) : ToolRoute(imageURL)
}

enum class DreamTools(
    val title: StringResource,
    val description: StringResource,
    val route: ToolRoute,
    val enabled: Boolean
) {
    AnalyzeDreams(
        title = Res.string.mass_dream_interpreter_title,
        description = Res.string.mass_dream_interpreter_description,
        route = ToolRoute.AnalyzeMultipleDreamsDetails(
            imageID = DreamDrawable.MASS_INTERPRETATION_TOOL.name
        ),
        enabled = true
    ),
    DREAM_WORLD(
        title = Res.string.dream_world_painter_title,
        description = Res.string.dream_world_painter_description,
        route = ToolRoute.PaintDreamWorld(
            imageID = DreamDrawable.DREAM_WORLD_PAINTER_TOOL.name
        ),
        enabled = true
    ),
    RandomDreamPicker(
        title = Res.string.random_dream_picker_title,
        description = Res.string.random_dream_picker_description,
        route = ToolRoute.RandomDreamPicker(
            imageID = DreamDrawable.DICE_TOOL.name
        ),
        enabled = true
    ),
    AnalyzeStatistics(
        title = Res.string.analyze_statistics_title,
        description = Res.string.analyze_statistics_description,
        route = ToolRoute.Statistics(
            imageID = DreamDrawable.DREAM_STATISTIC_ANALYZER_TOOL.name
        ),
        enabled = false
    ),
    DynamicLucidChecker(
        title = Res.string.dynamic_lucid_checker_title,
        description = Res.string.dynamic_lucid_checker_description,
        route = ToolRoute.DynamicLucidChecker(
            imageID = DreamDrawable.REALITY_CHECK_REMINDER_TOOL.name
        ),
        enabled = false
    ),
    DreamJournalReminder(
        title = Res.string.dream_journal_reminder_title,
        description = Res.string.dream_journal_reminder_description,
        route = ToolRoute.DreamJournalReminder(
            imageID = DreamDrawable.DREAM_JOURNAL_REMINDER_TOOL.name
        ),
        enabled = false
    ),
   ;
}

@Serializable
enum class DreamDrawable {
    MASS_INTERPRETATION_TOOL,
    DICE_TOOL,
    DREAM_STATISTIC_ANALYZER_TOOL,
    REALITY_CHECK_REMINDER_TOOL,
    DREAM_JOURNAL_REMINDER_TOOL,
    DREAM_WORLD_PAINTER_TOOL
}

enum class DrawerNavigation(val title: StringResource?, val icon: ImageVector, val route: Route){
    DreamJournalScreen(Res.string.my_dreams, Icons.Filled.Book, Route.DreamJournalScreen),
    StoreScreen(Res.string.store, Icons.Default.Shop, Route.StoreScreen),
    Favorites(Res.string.favorites, Icons.Default.Favorite, Route.Favorites),
    AccountSettings(Res.string.account_settings, Icons.Default.Settings, Route.AccountSettings),
    Statistics(Res.string.statistics, Icons.Default.BarChart, Route.Statistics),
  //  NotificationSettings("Notification Settings", Icons.Default.Notifications, Route.NotificationSettings),
    Nightmares(Res.string.nightmares, Icons.Default.ErrorOutline, Route.Nightmares),
    Symbol(Res.string.symbols, Icons.AutoMirrored.Filled.List, Route.Symbol),
    RateMyApp(Res.string.rate_this_app, Icons.Default.Favorite, Route.RateMyApp),
    ExportDreams(Res.string.export_dreams, Icons.Default.Upload, Route.ExportDreams),
    DreamToolGraphScreen(Res.string.dream_tools, Icons.Default.Build, Route.DreamToolGraphScreen),
}

enum class BottomNavigationRoutes(val title: StringResource?, val icon: ImageVector, val route: Route){
    DreamJournalScreen(Res.string.my_dreams, Icons.Filled.Book, Route.DreamJournalScreen),
    StoreScreen(Res.string.store, Icons.Default.Shop, Route.StoreScreen)
}


fun DreamDrawable.toDrawableResource(): DrawableResource {
    return when (this) {
        DreamDrawable.MASS_INTERPRETATION_TOOL -> Res.drawable.DreamInterpreterMassTool
        DreamDrawable.DICE_TOOL                -> Res.drawable.DreamRandomPicker
        DreamDrawable.DREAM_STATISTIC_ANALYZER_TOOL -> Res.drawable.DreamStatisticalAnalysis
        DreamDrawable.REALITY_CHECK_REMINDER_TOOL    -> Res.drawable.DreamIntellegentRealityChecker
        DreamDrawable.DREAM_JOURNAL_REMINDER_TOOL    -> Res.drawable.DreamPredictorTool
        DreamDrawable.DREAM_WORLD_PAINTER_TOOL       -> Res.drawable.DreamWorldTool
    }
}
