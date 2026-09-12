package com.example.quicknote

import com.example.quicknote.data.Converters
import com.example.quicknote.data.Note
import com.example.quicknote.data.TodoItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteDataTest {

    @Test
    fun testNoteCreationAndDefaults() {
        val note = Note(
            title = "Test Note",
            content = "Sample content",
            dateFormatted = "Hoy, 12:00"
        )

        assertEquals("Test Note", note.title)
        assertEquals("Sample content", note.content)
        assertFalse(note.isTask)
        assertFalse(note.isCompleted)
        assertFalse(note.isPinned)
    }

    @Test
    fun testTodoItemConverters() {
        val converters = Converters()
        val items = listOf(
            TodoItem(id = "1", text = "Comprar plantas", isDone = false),
            TodoItem(id = "2", text = "Revisar entregables", isDone = true)
        )

        val jsonString = converters.fromTodoItemList(items)
        val deserialized = converters.toTodoItemList(jsonString)

        assertEquals(2, deserialized.size)
        assertEquals("Comprar plantas", deserialized[0].text)
        assertFalse(deserialized[0].isDone)
        assertEquals("Revisar entregables", deserialized[1].text)
        assertTrue(deserialized[1].isDone)
    }
}
