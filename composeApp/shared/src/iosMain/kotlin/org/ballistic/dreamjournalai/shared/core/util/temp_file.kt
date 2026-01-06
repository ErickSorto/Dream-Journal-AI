package org.ballistic.dreamjournalai.shared.core.util

import dev.gitlive.firebase.storage.File
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual fun createTempFile(bytes: ByteArray): File {
    val tempDir = NSTemporaryDirectory()
    val tempFile = tempDir + "image.jpg"
    val data = bytes.usePinned {
        NSData.create(bytesNoCopy = it.addressOf(0), length = bytes.size.toULong())
    }
    data.writeToFile(tempFile, true)
    return File(NSURL(string = tempFile))
}
