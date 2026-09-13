package com.example.quicknote.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quicknote.data.AppDatabase
import com.example.quicknote.data.Note
import com.example.quicknote.data.NoteRepository
import com.example.quicknote.data.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FilterCategory {
    ALL, NOTES, TASKS
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow(FilterCategory.ALL)

    val allNotes: StateFlow<List<Note>>

    val totalCount: StateFlow<Int>
    val notesCount: StateFlow<Int>
    val tasksCount: StateFlow<Int>

    init {
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)

        val rawNotesFlow = repository.allNotes

        totalCount = rawNotesFlow
            .combine(MutableStateFlow(Unit)) { list, _ -> list.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        notesCount = rawNotesFlow
            .combine(MutableStateFlow(Unit)) { list, _ -> list.count { !it.isTask } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        tasksCount = rawNotesFlow
            .combine(MutableStateFlow(Unit)) { list, _ -> list.count { it.isTask } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        allNotes = combine(rawNotesFlow, searchQuery, selectedCategory) { notes, query, category ->
            notes.filter { note ->
                val matchesCategory = when (category) {
                    FilterCategory.ALL -> true
                    FilterCategory.NOTES -> !note.isTask
                    FilterCategory.TASKS -> note.isTask
                }
                val matchesQuery = query.isBlank() ||
                        note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true) ||
                        note.todoItems.any { it.text.contains(query, ignoreCase = true) }

                matchesCategory && matchesQuery
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onCategorySelected(category: FilterCategory) {
        selectedCategory.value = category
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(note)
        }
    }

    fun deleteNoteById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(id)
        }
    }

    fun toggleTaskCompleted(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = note.copy(isCompleted = !note.isCompleted)
            repository.update(updated)
        }
    }

    fun toggleSubTask(note: Note, todoItemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTodoList = note.todoItems.map { item ->
                if (item.id == todoItemId) {
                    item.copy(isDone = !item.isDone)
                } else {
                    item
                }
            }
            val allDone = updatedTodoList.isNotEmpty() && updatedTodoList.all { it.isDone }
            val updated = note.copy(
                todoItems = updatedTodoList,
                isCompleted = allDone
            )
            repository.update(updated)
        }
    }

    fun togglePinned(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = note.copy(isPinned = !note.isPinned)
            repository.update(updated)
        }
    }

    fun saveNote(
        existingNoteId: Long?,
        title: String,
        content: String,
        imageUri: String?,
        isTask: Boolean,
        isPinned: Boolean,
        todoItems: List<TodoItem>,
        onSaved: ((Note) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val trimmedTitle = title.trim()
            val trimmedContent = content.trim()

            if (trimmedTitle.isBlank() && trimmedContent.isBlank() && imageUri == null && todoItems.isEmpty()) {
                if (existingNoteId != null && existingNoteId > 0L) {
                    repository.deleteById(existingNoteId)
                }
                return@launch
            }

            val formattedDate = getCurrentFormattedDate()
            val now = System.currentTimeMillis()

            val savedNote = if (existingNoteId == null || existingNoteId <= 0L) {
                val newNote = Note(
                    title = trimmedTitle.ifBlank { "Sin título" },
                    content = trimmedContent,
                    dateFormatted = formattedDate,
                    timestamp = now,
                    imageUri = imageUri,
                    isTask = isTask,
                    isCompleted = false,
                    isPinned = isPinned,
                    todoItems = todoItems
                )
                val generatedId = repository.insert(newNote)
                newNote.copy(id = generatedId)
            } else {
                val noteToUpdate = repository.getNoteByIdDirect(existingNoteId) ?: Note(id = existingNoteId, title = "")
                val updatedNote = noteToUpdate.copy(
                    title = trimmedTitle.ifBlank { "Sin título" },
                    content = trimmedContent,
                    dateFormatted = formattedDate,
                    timestamp = now,
                    imageUri = imageUri,
                    isTask = isTask,
                    isPinned = isPinned,
                    todoItems = todoItems
                )
                repository.update(updatedNote)
                updatedNote
            }

            withContext(Dispatchers.Main) {
                onSaved?.invoke(savedNote)
            }
        }
    }

    suspend fun getNoteById(id: Long): Note? {
        return repository.getNoteByIdDirect(id)
    }

    private fun getCurrentFormattedDate(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = sdf.format(Date())
        return "Hoy, $time"
    }
}
