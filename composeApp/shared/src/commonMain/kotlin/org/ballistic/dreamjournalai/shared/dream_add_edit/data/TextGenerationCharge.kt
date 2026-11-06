package org.ballistic.dreamjournalai.shared.dream_add_edit.data

/** Charge only when generation delivered usable text. Failed/empty retries are free. */
internal suspend fun AIResult<String>.chargeForGeneratedText(
    cost: Int,
    charge: suspend (Int) -> Unit
): AIResult<String> {
    if (this is AIResult.Error) return this
    val text = (this as AIResult.Success).data
    if (text.isBlank()) return AIResult.Error("No text was generated. Please try again.")
    if (cost > 0) charge(cost)
    return this
}
