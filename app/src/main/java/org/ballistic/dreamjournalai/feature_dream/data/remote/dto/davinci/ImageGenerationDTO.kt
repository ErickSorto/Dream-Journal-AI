package org.ballistic.dreamjournalai.feature_dream.data.remote.dto.davinci

import com.google.gson.annotations.SerializedName

data class ImageGenerationDTO(
    val created: Int,
    @SerializedName("data")
    val dataList: List<Data>
)