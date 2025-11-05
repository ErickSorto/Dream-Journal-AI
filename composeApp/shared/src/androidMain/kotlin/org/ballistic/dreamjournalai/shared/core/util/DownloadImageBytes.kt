package org.ballistic.dreamjournalai.shared.core.util

import co.touchlab.kermit.Logger
import java.net.URL

private val netLogger = Logger.withTag("DJAI/Reads/Network")

actual suspend fun downloadImageBytes(urlString: String): ByteArray {
    netLogger.d { "Log.d(\"DJAI/Reads/Network\"){ downloadImageBytes(url=$urlString) – starting }" }
    val bytes = URL(urlString).readBytes()
    netLogger.d { "Log.d(\"DJAI/Reads/Network\"){ downloadImageBytes(url=$urlString) – completed, size=${bytes.size} bytes }" }
    return bytes
}