package org.ballistic.dreamjournalai.shared.core.platform

import androidx.compose.runtime.Composable
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream

@Composable
expect fun rememberDreamExporter(): DreamExporter

interface DreamExporter {
    fun exportToPdf(dreams: List<Dream>, fileName: String, onResult: (Boolean) -> Unit)
    fun exportToTxt(content: String, fileName: String, onResult: (Boolean) -> Unit)
}
