package org.ballistic.dreamjournalai.feature_dream.data.dto

data class Model(
    val id: String,
    val `object`: String,
    val owned_by: String,
    val permission: List<String>
)