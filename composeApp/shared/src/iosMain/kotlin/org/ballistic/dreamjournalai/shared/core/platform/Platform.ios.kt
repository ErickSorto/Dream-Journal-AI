package org.ballistic.dreamjournalai.shared.core.platform

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

actual fun getPlatformName(): String = "iOS"

@OptIn(ExperimentalNativeApi::class)
actual fun isDebugBuild(): Boolean = Platform.isDebugBinary
