package org.ballistic.dreamjournalai.shared.core.platform

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
actual fun rememberDreamExporter(): DreamExporter {
    val context = LocalContext.current
    var onResultCallback by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        onResultCallback?.invoke(true)
        onResultCallback = null
    }

    return remember {
        object : DreamExporter {
            override fun exportToPdf(dreams: List<Dream>, fileName: String, onResult: (Boolean) -> Unit) {
                onResultCallback = onResult
                sharePdf(context, dreams, fileName, launcher, onResult)
            }

            override fun exportToTxt(content: String, fileName: String, onResult: (Boolean) -> Unit) {
                onResultCallback = onResult
                shareTxt(context, content, fileName, launcher, onResult)
            }
        }
    }
}

private fun sharePdf(
    context: Context,
    dreams: List<Dream>,
    fileName: String,
    launcher: ActivityResultLauncher<Intent>,
    onResult: (Boolean) -> Unit
) {
    val file = File(context.cacheDir, fileName)

    try {
        val document = PdfDocument()
        dreams.forEachIndexed { index, dream ->
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 1).create() // A4 size
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val titlePaint = TextPaint().apply {
                textSize = 24f
                isFakeBoldText = true
            }
            val datePaint = TextPaint().apply {
                textSize = 12f
            }
            val contentPaint = TextPaint().apply {
                textSize = 12f
            }
            var y = 60f
            val contentWidth = (canvas.width - 80).toInt()

            canvas.drawText(dream.title, 40f, y, titlePaint)
            y += titlePaint.fontSpacing * 1.5f
            canvas.drawText(dream.date, 40f, y, datePaint)
            y += datePaint.fontSpacing * 2f

            val fullContent = buildString {
                append(dream.content)
                if (dream.audioTranscription.isNotBlank()) {
                    if (isNotEmpty()) append("\n\n")
                    append("Transcript:\n")
                    append(dream.audioTranscription)
                }
            }

            val contentLayout = StaticLayout.Builder.obtain(
                fullContent, 0, fullContent.length, contentPaint, contentWidth
            ).build()
            canvas.save()
            canvas.translate(40f, y)
            contentLayout.draw(canvas)
            canvas.restore()

            document.finishPage(page)
        }

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

    } catch (e: IOException) {
        e.printStackTrace()
        onResult(false)
        return
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Share Dream")
    launcher.launch(chooser)
}

private fun shareTxt(
    context: Context,
    content: String,
    fileName: String,
    launcher: ActivityResultLauncher<Intent>,
    onResult: (Boolean) -> Unit
) {
    val file = File(context.cacheDir, fileName)
    try {
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray())
        }
    } catch (e: IOException) {
        e.printStackTrace()
        onResult(false)
        return
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Share Dream")
    launcher.launch(chooser)
}
