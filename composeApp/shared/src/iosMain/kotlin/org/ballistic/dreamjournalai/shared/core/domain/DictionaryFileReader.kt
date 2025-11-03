package org.ballistic.dreamjournalai.shared.core.domain

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSBundle
import platform.Foundation.NSLog
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.UIKit.NSDataAsset

// actual implementation for iOS/tvOS/watchOS that reads a CSV from the app bundle.
// Make sure the CSV (for example `dream_dictionary.csv`) is added to the iOS target's
// "Copy Bundle Resources" in Xcode so it is available at runtime or provided as a Data asset in Assets.xcassets.
actual class DictionaryFileReader {
    actual fun readDictionaryWordsFromCsv(fileName: String): List<String> {
        val name = fileName.substringBeforeLast('.', fileName)
        val ext = fileName.substringAfterLast('.', "")
        val resourceExt = ext.ifEmpty { "csv" }

        // Helper to decide if parsed content looks usable
        fun isUsable(data: NSData, parsed: List<String>): Boolean {
            val bytes = data.length.toLong()
            // Heuristics: must be at least some bytes and at least one non-empty line with commas
            val hasComma = parsed.any { it.contains(',') }
            val nonEmptyLines = parsed.count { it.isNotBlank() }
            return (bytes > 256 && nonEmptyLines >= 1 && hasComma) || nonEmptyLines >= 3
        }

        // Try file via pathForResource first, but only accept if content looks usable
        val path = NSBundle.mainBundle.pathForResource(name, resourceExt)
        if (path != null) {
            val data = NSData.dataWithContentsOfFile(path)
            if (data != null) {
                val parsed = parseCsvData(data)
                if (isUsable(data, parsed)) {
                    NSLog("[DictionaryFileReader] using bundle file at path: %s (bytes=%ld, lines=%d)", path, data.length.toLong(), parsed.size)
                    return parsed
                } else {
                    NSLog("[DictionaryFileReader] bundle file at %s seems insufficient (bytes=%ld, lines=%d). Will keep searching...", path, data.length.toLong(), parsed.size)
                }
            } else {
                NSLog("[DictionaryFileReader] pathForResource returned data=null for %s.%s", name, resourceExt)
            }
        } else {
            NSLog("[DictionaryFileReader] pathForResource returned null for %s.%s", name, resourceExt)
        }

        // Try as an Asset Catalog Data asset in the main bundle (multiple plausible names)
        val assetCandidateNames = listOf(
            name,
            "$name.$resourceExt",
            "dream_dictionary",
            "dream_dictionary.csv",
            "Data",
            "data"
        )
        for (assetName in assetCandidateNames) {
            try {
                val asset = NSDataAsset(name = assetName, bundle = NSBundle.mainBundle)
                if (asset != null) {
                    val bytes = asset.data
                    val parsed = parseCsvData(bytes)
                    if (isUsable(bytes, parsed)) {
                        NSLog("[DictionaryFileReader] using NSDataAsset('%s') (bytes=%ld, lines=%d)", assetName, bytes.length.toLong(), parsed.size)
                        return parsed
                    } else {
                        NSLog("[DictionaryFileReader] NSDataAsset('%s') seems insufficient (bytes=%ld, lines=%d).", assetName, bytes.length.toLong(), parsed.size)
                    }
                } else {
                    NSLog("[DictionaryFileReader] NSDataAsset('%s') returned null", assetName)
                }
            } catch (t: Throwable) {
                NSLog("[DictionaryFileReader] error loading NSDataAsset('%s'): %s", assetName, t.toString())
            }
        }

        // Fallback: search common resource paths
        val resourcePath = NSBundle.mainBundle.resourcePath
        if (resourcePath != null) {
            val candidates = listOf(
                "$resourcePath/$name.$resourceExt",
                "$resourcePath/Resources/$name.$resourceExt",
                "$resourcePath/raw/$name.$resourceExt",
                "$resourcePath/Raw/$name.$resourceExt",
                "$resourcePath/Assets/$name.$resourceExt",
                "$resourcePath/$fileName"
            )
            for (candidate in candidates) {
                val data = NSData.dataWithContentsOfFile(candidate)
                if (data != null) {
                    val parsed = parseCsvData(data)
                    if (isUsable(data, parsed)) {
                        NSLog("[DictionaryFileReader] using candidate file: %s (bytes=%ld, lines=%d)", candidate, data.length.toLong(), parsed.size)
                        return parsed
                    } else {
                        NSLog("[DictionaryFileReader] candidate file insufficient: %s (bytes=%ld, lines=%d)", candidate, data.length.toLong(), parsed.size)
                    }
                }
            }
        } else {
            NSLog("[DictionaryFileReader] mainBundle.resourcePath is null")
        }

        // Last resort: enumerate bundle, pick the first usable match
        try {
            val fm = NSFileManager.defaultManager
            val bundlePath = NSBundle.mainBundle.bundlePath
            val enumerator = fm.enumeratorAtPath(bundlePath)
            if (enumerator != null) {
                var entry: Any? = enumerator.nextObject()
                while (entry != null) {
                    val entryStr = entry.toString()
                    if (entryStr.endsWith(".$resourceExt") && entryStr.contains(name)) {
                        val full = "$bundlePath/$entryStr"
                        val data = NSData.dataWithContentsOfFile(full)
                        if (data != null) {
                            val parsed = parseCsvData(data)
                            if (isUsable(data, parsed)) {
                                NSLog("[DictionaryFileReader] using enumerated file: %s (bytes=%ld, lines=%d)", full, data.length.toLong(), parsed.size)
                                return parsed
                            } else {
                                NSLog("[DictionaryFileReader] enumerated file insufficient: %s (bytes=%ld, lines=%d)", full, data.length.toLong(), parsed.size)
                            }
                        }
                    }
                    entry = enumerator.nextObject()
                }
            }
        } catch (e: Throwable) {
            NSLog("[DictionaryFileReader] error while scanning bundle: %s", e.toString())
        }

        NSLog("[DictionaryFileReader] could not find a usable %s.%s in bundle (file or asset)", name, resourceExt)
        return emptyList()
    }

    @OptIn(BetaInteropApi::class)
    private fun parseCsvData(data: NSData): List<String> {
        val nsString = NSString.create(data, NSUTF8StringEncoding) ?: return emptyList()
        val text = nsString.toString()
        return text
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
    }
}
