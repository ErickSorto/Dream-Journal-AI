package org.ballistic.dreamcatcherai.feature_note.domain.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ballistic.dreamcatcherai.feature_note.domain.model.Note
import org.ballistic.dreamcatcherai.feature_note.domain.repository.NoteRepository
import org.ballistic.dreamcatcherai.feature_note.domain.util.NoteOrder
import org.ballistic.dreamcatcherai.feature_note.domain.util.OrderType

class GetNotes (private val repository: NoteRepository){
    operator fun invoke(noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending)): Flow<List<Note>> {
        return repository.getNotes().map { notes ->
            when(noteOrder.orderType) {
                is OrderType.Ascending -> {
                    when(noteOrder) {
                        is NoteOrder.Date -> {
                            notes.sortedBy { it.timestamp }
                        }
                    }
                }
                is OrderType.Descending -> {
                    when(noteOrder) {
                        is NoteOrder.Date -> {
                            notes.sortedByDescending { it.timestamp }
                        }
                    }
                }
            }
        }
    }
}