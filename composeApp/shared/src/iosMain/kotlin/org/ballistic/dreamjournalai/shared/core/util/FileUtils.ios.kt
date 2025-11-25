package org.ballistic.dreamjournalai.shared.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.getBytes

@OptIn(ExperimentalForeignApi::class)
actual suspend fun readFileBytes(path: String): ByteArray {
    val data = NSData.dataWithContentsOfFile(path) ?: return ByteArray(0)
    return ByteArray(data.length.toInt()).apply {
        usePinned { pinned ->
            data.getBytes(pinned.addressOf(0), data.length)
        }
    }
}
