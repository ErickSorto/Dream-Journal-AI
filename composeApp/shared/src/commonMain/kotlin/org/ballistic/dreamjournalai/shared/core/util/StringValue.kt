package org.ballistic.dreamjournalai.shared.core.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A sealed class to represent a string that can be either a dynamic value
 * or a reference to a string resource. This helps in keeping ViewModels
 * free from Android Context or platform-specific resource handling.
 */
sealed class StringValue {

    /**
     * Represents a string that is determined at runtime.
     */
    data class DynamicString(val value: String) : StringValue()

    /**
     * Represents a string that comes from Compose Multiplatform string resources.
     * @param resId The [StringResource].
     * @param args Optional arguments for formatted strings.
     */
    class Resource(val resId: StringResource, vararg val args: Any) : StringValue()

    /**
     * Represents an empty string.
     */
    object Empty : StringValue()

    /**
     * Resolves the [StringValue] to a [String] within a Composable context.
     */
    @Composable
    fun asString(): String {
        return when (this) {
            is Empty -> ""
            is DynamicString -> value
            is Resource -> stringResource(resId, *args)
        }
    }
}
