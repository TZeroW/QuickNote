package com.example.quicknote.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String = "",
    val dateFormatted: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val isTask: Boolean = false,
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val todoItems: List<TodoItem> = emptyList()
)

data class TodoItem(
    val id: String,
    val text: String,
    val isDone: Boolean = false
)
