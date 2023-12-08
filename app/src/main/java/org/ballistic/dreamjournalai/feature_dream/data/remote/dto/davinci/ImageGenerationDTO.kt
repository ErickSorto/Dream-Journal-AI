package org.ballistic.dreamjournalai.feature_dream.data.remote.dto.davinci

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class ImageGenerationDTO(
    val created: Int,
    @SerializedName("data")
    val dataList: List<Data>
)