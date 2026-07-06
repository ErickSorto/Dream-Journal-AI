package org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DreamEmotionRadar(
    val joy: Int = 0,
    val trust: Int = 0,
    val fear: Int = 0,
    val surprise: Int = 0,
    val sadness: Int = 0,
    val disgust: Int = 0,
    val anger: Int = 0,
    val anticipation: Int = 0,
) {
    fun valueFor(axis: EmotionRadarAxis): Int {
        return when (axis) {
            EmotionRadarAxis.Joy -> joy
            EmotionRadarAxis.Trust -> trust
            EmotionRadarAxis.Fear -> fear
            EmotionRadarAxis.Surprise -> surprise
            EmotionRadarAxis.Sadness -> sadness
            EmotionRadarAxis.Disgust -> disgust
            EmotionRadarAxis.Anger -> anger
            EmotionRadarAxis.Anticipation -> anticipation
        }.coerceIn(0, MaxValue)
    }

    fun withValue(axis: EmotionRadarAxis, value: Int): DreamEmotionRadar {
        val safeValue = value.coerceIn(0, MaxValue)
        return when (axis) {
            EmotionRadarAxis.Joy -> copy(joy = safeValue)
            EmotionRadarAxis.Trust -> copy(trust = safeValue)
            EmotionRadarAxis.Fear -> copy(fear = safeValue)
            EmotionRadarAxis.Surprise -> copy(surprise = safeValue)
            EmotionRadarAxis.Sadness -> copy(sadness = safeValue)
            EmotionRadarAxis.Disgust -> copy(disgust = safeValue)
            EmotionRadarAxis.Anger -> copy(anger = safeValue)
            EmotionRadarAxis.Anticipation -> copy(anticipation = safeValue)
        }
    }

    fun isEmpty(): Boolean = EmotionRadarAxis.entries.all { valueFor(it) == 0 }

    fun dominantAxes(limit: Int = 3): List<EmotionRadarAxis> {
        return EmotionRadarAxis.entries
            .filter { valueFor(it) > 0 }
            .sortedByDescending { valueFor(it) }
            .take(limit)
    }

    fun overallMoodRating(): Int {
        if (isEmpty()) return 0
        val positive = joy + trust + anticipation + surprise
        val difficult = fear + sadness + disgust + anger
        val score = 3f + ((positive - difficult).toFloat() / (MaxValue * 4f)) * 2f
        return score.toInt().coerceIn(1, MaxValue)
    }

    companion object {
        const val MaxValue = 5

        fun average(radars: List<DreamEmotionRadar>): DreamEmotionRadar {
            val nonEmpty = radars.filter { !it.isEmpty() }
            if (nonEmpty.isEmpty()) return DreamEmotionRadar()

            fun avg(selector: (DreamEmotionRadar) -> Int): Int {
                return (nonEmpty.sumOf(selector).toFloat() / nonEmpty.size)
                    .toInt()
                    .coerceIn(0, MaxValue)
            }

            return DreamEmotionRadar(
                joy = avg { it.joy },
                trust = avg { it.trust },
                fear = avg { it.fear },
                surprise = avg { it.surprise },
                sadness = avg { it.sadness },
                disgust = avg { it.disgust },
                anger = avg { it.anger },
                anticipation = avg { it.anticipation },
            )
        }
    }
}

enum class EmotionRadarAxis(val label: String) {
    Joy("Joy"),
    Trust("Love"),
    Fear("Fear"),
    Surprise("Surprise"),
    Sadness("Sadness"),
    Disgust("Disgust"),
    Anger("Anger"),
    Anticipation("Interest"),
}
