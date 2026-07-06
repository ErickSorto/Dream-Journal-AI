package org.ballistic.dreamjournalai.shared.core.platform

import android.content.pm.ApplicationInfo
import org.ballistic.dreamjournalai.shared.DreamJournalAIApp

actual fun getPlatformName(): String = "Android"

actual fun isDebugBuild(): Boolean {
    val appInfo = DreamJournalAIApp.applicationContext().applicationInfo
    return appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}
