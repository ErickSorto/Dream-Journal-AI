package org.ballistic.dreamjournalai.shared.core.util

import android.content.Context
import co.touchlab.kermit.Logger
import org.ballistic.dreamjournalai.shared.DreamJournalAIApp
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

actual suspend fun saveBytesToImageFile(bytes: ByteArray): String? {
    return try {
        val context: Context = DreamJournalAIApp.applicationContext()
        val fileName = "dream_image_${UUID.randomUUID()}.png"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { it.write(bytes) }
        file.absolutePath
    } catch (e: Exception) {
        Logger.e("saveBytesToImageFile", e)
        null
    }
}
