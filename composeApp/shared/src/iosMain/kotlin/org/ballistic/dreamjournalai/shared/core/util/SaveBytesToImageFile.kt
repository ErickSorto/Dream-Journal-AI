package org.ballistic.dreamjournalai.shared.core.util

import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual suspend fun saveBytesToImageFile(bytes: ByteArray): String? {
    return try {
        val fileName = "dream_image_${NSUUID.UUID().UUIDString}.png"
        val documentsPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String
        val filePath = "$documentsPath/$fileName"

        val data = bytes.usePinned {
            NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong())
        }

        data.writeToFile(filePath, true)
        filePath
    } catch (e: Exception) {
        Logger.e("saveBytesToImageFile", e)
        null
    }
}
