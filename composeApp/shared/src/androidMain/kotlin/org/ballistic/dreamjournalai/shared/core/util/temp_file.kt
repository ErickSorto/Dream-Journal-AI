package org.ballistic.dreamjournalai.shared.core.util

import android.net.Uri
import dev.gitlive.firebase.storage.File
import java.io.File as JavaFile

actual fun createTempFile(bytes: ByteArray): File {
    val tempFile = JavaFile.createTempFile("image", ".jpg")
    tempFile.writeBytes(bytes)
    return File(Uri.fromFile(tempFile))
}
