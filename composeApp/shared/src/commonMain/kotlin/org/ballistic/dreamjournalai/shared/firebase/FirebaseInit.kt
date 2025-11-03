package org.ballistic.dreamjournalai.shared.firebase

/**
 * Platform-specific Firebase initialization.
 * On Android we initialize with an Android Context; on iOS the AppDelegate configures Firebase,
 * so the iOS actual is a no-op to avoid double-configuration.
 */
expect fun initFirebaseIfRequired(context: Any?)

