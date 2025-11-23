package org.ballistic.dreamjournalai.shared.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPageWithInfo
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIMarkupTextPrintFormatter
import platform.UIKit.UIPrintPageRenderer
import platform.UIKit.UIWindow

@Composable
actual fun rememberDreamExporter(): DreamExporter {
    val scope = rememberCoroutineScope()
    return remember(scope) {
        object : DreamExporter {
            override fun exportToPdf(dreams: List<Dream>, fileName: String, onResult: (Boolean) -> Unit) {
                // Perform on Main thread to ensure UIKit safety and correct layout
                scope.launch(Dispatchers.Main) {
                    // 2. Format content as HTML
                    val htmlContent = formatDreamsAsHtml(dreams)

                    // 3. Create PDF data using Custom UIPrintPageRenderer
                    val pdfData = createPdfFromHtml(htmlContent)

                    if (pdfData != null) {
                        // Ensure filename has .pdf extension
                        val finalFileName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"

                        // Save to temp file and share the URL
                        val fileUrl = saveToTempFile(pdfData, finalFileName)
                        if (fileUrl != null) {
                            shareFile(fileUrl, onResult)
                        } else {
                            onResult(false)
                        }
                    } else {
                        onResult(false)
                    }
                }
            }

            override fun exportToTxt(content: String, fileName: String, onResult: (Boolean) -> Unit) {
                scope.launch(Dispatchers.Main) {
                    val nsString = content as platform.Foundation.NSString
                    val data = nsString.dataUsingEncoding(platform.Foundation.NSUTF8StringEncoding)

                    if (data != null) {
                        val finalFileName = if (fileName.endsWith(".txt", ignoreCase = true)) fileName else "$fileName.txt"
                        val fileUrl = saveToTempFile(data, finalFileName)
                        if (fileUrl != null) {
                            shareFile(fileUrl, onResult)
                        } else {
                            onResult(false)
                        }
                    } else {
                        onResult(false)
                    }
                }
            }
        }
    }
}

// Custom Renderer to explicitly define paper and printable rects without KVC
class CustomPrintPageRenderer(
    private val paperRectValue: CValue<CGRect>,
    private val printableRectValue: CValue<CGRect>
) : UIPrintPageRenderer() {
    override fun paperRect(): CValue<CGRect> = paperRectValue
    override fun printableRect(): CValue<CGRect> = printableRectValue
}

@OptIn(ExperimentalForeignApi::class)
private fun createPdfFromHtml(html: String): NSData? {
    val formatter = UIMarkupTextPrintFormatter(markupText = html)

    // A4 paper size: 595.2 x 841.8 points
    val paperRect = CGRectMake(0.0, 0.0, 595.2, 841.8)
    // Margins: 40 points (approx 0.55 inch) -> Printable area
    // Using 40.0 to match Android implementation and ensure content is near top
    val margin = 40.0
    val printableRect = CGRectMake(margin, margin, 595.2 - (margin * 2), 841.8 - (margin * 2))

    // Use our custom renderer
    val renderer = CustomPrintPageRenderer(paperRect, printableRect)
    renderer.addPrintFormatter(formatter, startingAtPageAtIndex = 0)

    val pdfData = NSMutableData()
    UIGraphicsBeginPDFContextToData(pdfData, paperRect, null)

    val numberOfPages = renderer.numberOfPages()
    
    if (numberOfPages > 0) {
        for (i in 0 until numberOfPages) {
            // Explicitly begin page with the paper size info to ensure consistency
            UIGraphicsBeginPDFPageWithInfo(paperRect, null)
            renderer.drawPageAtIndex(i, inRect = printableRect)
        }
    }

    UIGraphicsEndPDFContext()
    return if (pdfData.length.toInt() > 0) pdfData else null
}

private fun saveToTempFile(data: NSData, fileName: String): NSURL? {
    val tempDir = NSTemporaryDirectory()
    val path = "$tempDir$fileName"
    val url = NSURL.fileURLWithPath(path)
    return if (data.writeToURL(url, true)) url else null
}

private fun shareFile(url: NSURL, onResult: (Boolean) -> Unit) {
    val activityViewController = UIActivityViewController(
        activityItems = listOf(url),
        applicationActivities = null
    ).apply {
        completionWithItemsHandler = { _, success, _, _ ->
            onResult(success)
        }
    }

    val window = UIApplication.sharedApplication.windows.first() as? UIWindow
    window?.rootViewController?.presentViewController(activityViewController, animated = true, completion = null)
}

private fun formatDreamsAsHtml(dreams: List<Dream>): String {
    val builder = StringBuilder()
    builder.append("<html><head><style>")
    // Reset all basic margins
    builder.append("body { font-family: Helvetica, Arial, sans-serif; font-size: 12pt; margin: 0; padding: 0; }")
    builder.append("h1 { font-size: 18pt; color: #333; margin-top: 0; padding-top: 0; }")
    builder.append(".date { font-size: 10pt; color: #666; margin-bottom: 10px; }")
    builder.append(".content { margin-bottom: 30px; line-height: 1.5; }")
    builder.append("</style></head><body>")

    dreams.forEachIndexed { index, dream ->
        // Use page-break-before for every item except the very first one.
        // This prevents a blank first page and ensures subsequent items start at the top of new pages.
        val style = if (index > 0) "style='page-break-before: always;'" else ""
        
        builder.append("<div $style>") 
        builder.append("<h1>${dream.title}</h1>")
        builder.append("<div class='date'>Date: ${dream.date}</div>")
        
        // Sanitize content
        val safeContent = dream.content
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br/>")
            
        builder.append("<div class='content'>$safeContent</div>")
        builder.append("</div>")
    }

    builder.append("</body></html>")
    return builder.toString()
}
