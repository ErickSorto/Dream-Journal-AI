package org.ballistic.dreamjournalai.shared.dream_statistics

sealed class StatisticEvent {
    data object LoadDreams : StatisticEvent()
    data object LoadDictionary : StatisticEvent()
    data object LoadStatistics : StatisticEvent()
    data object GetDreamTokens : StatisticEvent()
}