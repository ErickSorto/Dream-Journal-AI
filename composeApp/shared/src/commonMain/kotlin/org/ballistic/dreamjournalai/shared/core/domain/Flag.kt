package org.ballistic.dreamjournalai.shared.core.domain


import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class Flag @OptIn(ExperimentalTime::class) constructor(
    val dreamId: String? = null,            // Nullable dream ID
    val imageAddress: String,               // URL of the associated image
    val timestamp: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
    val status: String = "pending"          // Flag status: pending, reviewed, resolved
) {
    // For dev.gitlive firebase, you might keep a no-arg constructor if desired
    @OptIn(ExperimentalTime::class)
    constructor() : this(null, "", kotlin.time.Clock.System.now().toEpochMilliseconds(), "pending")
}