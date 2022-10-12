package org.ballistic.dreamjournalai.feature_dream.domain.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.repository.DreamRepository
import org.ballistic.dreamjournalai.feature_dream.domain.util.DreamOrder
import org.ballistic.dreamjournalai.feature_dream.domain.util.OrderType

class GetDreams (private val repository: DreamRepository){
    operator fun invoke(dreamOrder: DreamOrder = DreamOrder.Date(OrderType.Descending)): Flow<List<Dream>> {
        return repository.getDreams().map { dreams ->
            when(dreamOrder.orderType) {
                is OrderType.Ascending -> {
                    when(dreamOrder) {
                        is DreamOrder.Date -> {
                            dreams.sortedBy { it.timestamp }
                        }
                    }
                }
                is OrderType.Descending -> {
                    when(dreamOrder) {
                        is DreamOrder.Date -> {
                            dreams.sortedByDescending { it.timestamp }
                        }
                    }
                }
            }
        }
    }
}