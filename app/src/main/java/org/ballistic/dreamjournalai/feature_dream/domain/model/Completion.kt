package org.ballistic.dreamjournalai.feature_dream.domain.model

import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.davinci.Choice
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.davinci.Usage

data class Completion(
    val choices: List<Choice>,
    val created: Int,
    val id: String,
    val model: String,
    val `object`: String,
    val usage: Usage
)
