package com.example.quicknote.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTodoItemList(list: List<TodoItem>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val sb = StringBuilder("[")
        list.forEachIndexed { index, item ->
            val escapedText = item.text.replace("\\", "\\\\").replace("\"", "\\\"")
            sb.append("""{"id":"${item.id}","text":"$escapedText","isDone":${item.isDone}}""")
            if (index < list.size - 1) sb.append(",")
        }
        sb.append("]")
        return sb.toString()
    }

    @TypeConverter
    fun toTodoItemList(value: String?): List<TodoItem> {
        if (value.isNullOrEmpty() || value == "[]") return emptyList()
        val list = mutableListOf<TodoItem>()
        try {
            val content = value.trim().removePrefix("[").removeSuffix("]")
            if (content.isBlank()) return emptyList()
            val objects = content.split("},{")
            for (objStr in objects) {
                val cleanObj = objStr.removePrefix("{").removeSuffix("}")
                var id = ""
                var text = ""
                var isDone = false

                val fields = cleanObj.split("\",\"")
                for (field in fields) {
                    val parts = field.split("\":")
                    if (parts.size >= 2) {
                        val key = parts[0].replace("\"", "").trim()
                        val rawVal = parts.subList(1, parts.size).joinToString("\":").trim()
                        when (key) {
                            "id" -> id = rawVal.replace("\"", "")
                            "text" -> text = rawVal.removePrefix("\"").removeSuffix("\"").replace("\\\"", "\"").replace("\\\\", "\\")
                            "isDone" -> isDone = rawVal.toBoolean()
                        }
                    }
                }
                if (id.isNotBlank() || text.isNotBlank()) {
                    list.add(TodoItem(id, text, isDone))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
