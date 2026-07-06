package org.ballistic.dreamjournalai.shared.core.analytics

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics

interface AppAnalytics {
    fun track(eventName: String, params: Map<String, Any?> = emptyMap())
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String)
}

class FirebaseAppAnalytics : AppAnalytics {
    private val logger = Logger.withTag("AppAnalytics")

    init {
        runCatching {
            Firebase.analytics.setDefaultEventParameters(
                mapOf(AnalyticsParam.SchemaVersion to SchemaVersion)
            )
        }.onFailure { error ->
            logger.w(error) { "Unable to set Firebase Analytics default parameters." }
        }
    }

    override fun track(eventName: String, params: Map<String, Any?>) {
        val cleanName = eventName.sanitizeAnalyticsKey(maxLength = MaxEventNameLength)
        val cleanParams = params.sanitizedAnalyticsParams()
        logger.d { "track $cleanName $cleanParams" }

        runCatching {
            Firebase.analytics.logEvent(cleanName, cleanParams)
        }.onFailure { error ->
            logger.w(error) { "Firebase Analytics event failed: $cleanName" }
        }
    }

    override fun setUserId(userId: String?) {
        runCatching {
            Firebase.analytics.setUserId(userId?.take(MaxUserIdLength))
        }.onFailure { error ->
            logger.w(error) { "Unable to set Firebase Analytics user id." }
        }
    }

    override fun setUserProperty(name: String, value: String) {
        val cleanName = name.sanitizeAnalyticsKey(maxLength = MaxParamNameLength)
        val cleanValue = value.take(MaxUserPropertyValueLength)

        runCatching {
            Firebase.analytics.setUserProperty(cleanName, cleanValue)
        }.onFailure { error ->
            logger.w(error) { "Unable to set Firebase Analytics user property: $cleanName" }
        }
    }

    private fun Map<String, Any?>.sanitizedAnalyticsParams(): Map<String, Any> {
        return entries
            .asSequence()
            .filter { it.value != null }
            .mapNotNull { (key, value) ->
                val cleanKey = key.sanitizeAnalyticsKey(maxLength = MaxParamNameLength)
                val cleanValue = value?.sanitizeAnalyticsValue() ?: return@mapNotNull null
                cleanKey to cleanValue
            }
            .distinctBy { it.first }
            .take(MaxEventParamCount)
            .toMap()
    }

    private fun Any.sanitizeAnalyticsValue(): Any? {
        return when (this) {
            is Boolean -> this
            is Double -> this
            is Float -> toDouble()
            is Int -> this
            is Long -> this
            is Short -> toInt()
            is String -> take(MaxStringParamLength)
            else -> toString().take(MaxStringParamLength)
        }
    }

    private fun String.sanitizeAnalyticsKey(maxLength: Int): String {
        val normalized = lowercase()
            .replace(InvalidAnalyticsKeyChars, "_")
            .trim('_')
            .ifBlank { "event" }
        val prefixed = if (normalized.firstOrNull()?.isLetter() == true) {
            normalized
        } else {
            "dn_$normalized"
        }
        val safePrefix = if (
            prefixed.startsWith("firebase_") ||
            prefixed.startsWith("google_") ||
            prefixed.startsWith("ga_")
        ) {
            "dn_$prefixed"
        } else {
            prefixed
        }
        return safePrefix.take(maxLength).trimEnd('_').ifBlank { "event" }
    }

    private companion object {
        const val SchemaVersion = "2026_06_v1"
        const val MaxEventNameLength = 40
        const val MaxParamNameLength = 40
        const val MaxEventParamCount = 25
        const val MaxStringParamLength = 100
        const val MaxUserIdLength = 256
        const val MaxUserPropertyValueLength = 36
        val InvalidAnalyticsKeyChars = Regex("[^a-zA-Z0-9_]")
    }
}

object AnalyticsParam {
    const val AccountType = "account_type"
    const val Action = "action"
    const val Area = "area"
    const val DurationMs = "duration_ms"
    const val EntryPoint = "entry_point"
    const val Error = "error"
    const val Field = "field"
    const val HasPremium = "has_premium"
    const val HasTrial = "has_trial"
    const val InitialPage = "initial_page"
    const val IsAnonymous = "is_anonymous"
    const val Label = "label"
    const val OfferingId = "offering_id"
    const val PackageId = "package_id"
    const val Page = "page"
    const val Placement = "placement"
    const val Plan = "plan"
    const val ProductId = "product_id"
    const val Result = "result"
    const val SchemaVersion = "schema_version"
    const val Source = "source"
    const val Step = "step"
    const val StepIndex = "step_index"
    const val TokenBalance = "token_balance"
    const val TokenCount = "token_count"
    const val Value = "value"
    const val Variant = "variant"
}

object AnalyticsUserProperty {
    const val AccountType = "account_type"
    const val PremiumStatus = "premium_status"
}

fun Enum<*>.analyticsValue(): String {
    return name
        .replace(Regex("([a-z])([A-Z])"), "$1_$2")
        .replace(Regex("([A-Z])([A-Z][a-z])"), "$1_$2")
        .lowercase()
}

fun countBucket(count: Int): String {
    return when {
        count <= 0 -> "0"
        count == 1 -> "1"
        count <= 3 -> "2_3"
        count <= 7 -> "4_7"
        count <= 14 -> "8_14"
        count <= 30 -> "15_30"
        else -> "31_plus"
    }
}

fun durationBucket(durationMs: Long): String {
    val seconds = (durationMs / 1_000).coerceAtLeast(0)
    return when {
        seconds < 60 -> "under_1m"
        seconds < 180 -> "1_3m"
        seconds < 300 -> "3_5m"
        seconds < 600 -> "5_10m"
        seconds < 1_200 -> "10_20m"
        else -> "20m_plus"
    }
}
