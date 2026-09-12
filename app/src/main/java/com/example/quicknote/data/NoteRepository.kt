package com.example.quicknote.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAll()

    fun getNoteById(id: Long): Flow<Note?> = noteDao.getById(id)

    suspend fun getNoteByIdDirect(id: Long): Note? = noteDao.getNoteByIdDirect(id)

    suspend fun insert(note: Note): Long = noteDao.insert(note)

    suspend fun update(note: Note) = noteDao.update(note)

    suspend fun delete(note: Note) = noteDao.delete(note)

    suspend fun deleteById(id: Long) = noteDao.deleteById(id)
}
