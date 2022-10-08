package org.ballistic.dreamcatcherai.feature_note.domain.use_case

import kotlinx.coroutines.flow.Flow
import org.ballistic.dreamcatcherai.feature_note.domain.model.Note
import org.ballistic.dreamcatcherai.feature_note.domain.repository.NoteRepository

class GetNotes (private val repository: NoteRepository){
    operator fun invoke(): Flow<List<Note>> {

    }
}