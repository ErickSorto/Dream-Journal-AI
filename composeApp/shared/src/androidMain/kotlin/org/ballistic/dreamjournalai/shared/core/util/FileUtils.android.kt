package org.ballistic.dreamjournalai.shared.core.util

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun readFileBytes(path: String): ByteArray = withContext(Dispatchers.IO) {
    File(path).readBytes()
}
