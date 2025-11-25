package org.ballistic.dreamjournalai.shared.core.util

expect suspend fun readFileBytes(path: String): ByteArray
