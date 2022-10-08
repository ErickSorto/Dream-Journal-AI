package org.ballistic.dreamcatcherai.feature_note.data.repository

import kotlinx.coroutines.flow.Flow
import org.ballistic.dreamcatcherai.feature_note.data.data_source.NoteDao
import org.ballistic.dreamcatcherai.feature_note.domain.model.Note
import org.ballistic.dreamcatcherai.feature_note.domain.repository.NoteRepository

class NoteRepositoryImplementation (private val dao: NoteDao) : NoteRepository {
    override fun getNOtes(): Flow<List<Note>> {
        return dao.getNotes()
    }

    override suspend fun getNoteById(id: Int): Note? {
        return dao.getNoteById(id)
    }

    override suspend fun insertNote(note: Note) {
        dao.insertNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        dao.deleteNote(note)
    }


}