package org.ballistic.dreamjournalai.shared.core.util

import dev.gitlive.firebase.storage.File

expect fun createTempFile(bytes: ByteArray): File
