package com.example.quicknote.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.quicknote.data.TodoItem
import com.example.quicknote.ui.components.SampleMoodboardThumbnail
import com.example.quicknote.ui.theme.DarkBackground
import com.example.quicknote.ui.theme.DarkBorder
import com.example.quicknote.ui.theme.DarkSurface
import com.example.quicknote.ui.theme.DarkSurfaceVariant
import com.example.quicknote.ui.theme.PrimaryCyan
import com.example.quicknote.ui.theme.TextMuted
import com.example.quicknote.ui.theme.TextPrimary
import com.example.quicknote.ui.theme.TextSecondary
import com.example.quicknote.ui.viewmodel.NotesViewModel
import java.util.UUID

@Composable
fun NoteDetailScreen(
    noteId: Long?,
    initialIsTask: Boolean = false,
    viewModel: NotesViewModel,
    onBackClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    var currentNoteId by remember(noteId) { mutableStateOf(noteId) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var isTask by remember { mutableStateOf(initialIsTask) }
    var isPinned by remember { mutableStateOf(false) }
    var dateFormatted by remember { mutableStateOf("Hoy, recién creado") }
    var todoItems by remember { mutableStateOf<List<TodoItem>>(emptyList()) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0) {
            val note = viewModel.getNoteById(noteId)
            if (note != null) {
                currentNoteId = note.id
                title = note.title
                content = note.content
                imageUri = note.imageUri
                isTask = note.isTask
                isPinned = note.isPinned
                dateFormatted = note.dateFormatted
                todoItems = note.todoItems
            }
        }
    }

    fun doSave() {
        isSaving = true
        viewModel.saveNote(
            existingNoteId = currentNoteId,
            title = title,
            content = content,
            imageUri = imageUri,
            isTask = isTask,
            isPinned = isPinned,
            todoItems = todoItems,
            onSaved = { saved ->
                currentNoteId = saved.id
                dateFormatted = saved.dateFormatted
                isSaving = false
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            doSave()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it.toString()
            doSave()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            NoteDetailTopBar(
                isTask = isTask,
                isPinned = isPinned,
                onBackClick = {
                    doSave()
                    onBackClick()
                },
                onToggleTaskMode = {
                    isTask = !isTask
                    doSave()
                },
                onAddImage = { galleryLauncher.launch("image/*") },
                onTogglePin = {
                    isPinned = !isPinned
                    doSave()
                },
                onDelete = {
                    val id = currentNoteId
                    if (id != null && id > 0) {
                        viewModel.deleteNoteById(id)
                    }
                    onBackClick()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = PrimaryCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(10.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PrimaryCyan)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSaving) "Guardando..." else "Guardado automático",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Editado $dateFormatted",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                if (title.isEmpty()) {
                    Text(
                        text = if (isTask) "Título de la lista..." else "Título de la nota...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        doSave()
                    },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(PrimaryCyan),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 260.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                ) {
                    if (imageUri == "sample_interior") {
                        SampleMoodboardThumbnail()
                    } else {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Imagen adjunta",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(
                        onClick = {
                            imageUri = null
                            doSave()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quitar foto",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .clickable { onImageClick(imageUri!!) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Toca para ampliar en pantalla completa",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                if (content.isEmpty()) {
                    Text(
                        text = "Escribe aquí tus notas o pensamientos...",
                        fontSize = 16.sp,
                        color = TextMuted,
                        lineHeight = 24.sp
                    )
                }
                BasicTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        doSave()
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = TextPrimary,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(PrimaryCyan),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isTask || todoItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "LISTA DE TAREAS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                todoItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isDone,
                            onCheckedChange = { isChecked ->
                                val updatedList = todoItems.toMutableList().apply {
                                    this[index] = item.copy(isDone = isChecked)
                                }
                                todoItems = updatedList
                                doSave()
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryCyan,
                                uncheckedColor = TextMuted,
                                checkmarkColor = DarkBackground
                            )
                        )
                        BasicTextField(
                            value = item.text,
                            onValueChange = { newText ->
                                val updatedList = todoItems.toMutableList().apply {
                                    this[index] = item.copy(text = newText)
                                }
                                todoItems = updatedList
                                doSave()
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (item.isDone) TextMuted else TextPrimary,
                                fontSize = 15.sp
                            ),
                            cursorBrush = SolidColor(PrimaryCyan),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val updatedList = todoItems.toMutableList().apply {
                                    removeAt(index)
                                }
                                todoItems = updatedList
                                doSave()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar tarea",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val newItem = TodoItem(UUID.randomUUID().toString(), "", false)
                            todoItems = todoItems + newItem
                            doSave()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir tarea",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Añadir ítem a la lista",
                        color = PrimaryCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteDetailTopBar(
    isTask: Boolean,
    isPinned: Boolean,
    onBackClick: () -> Unit,
    onToggleTaskMode: () -> Unit,
    onAddImage: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onToggleTaskMode,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isTask) PrimaryCyan else DarkSurfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = "Modo lista",
                tint = if (isTask) DarkBackground else TextPrimary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onAddImage) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Adjuntar imagen",
                tint = TextSecondary
            )
        }

        IconButton(onClick = onTogglePin) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Fijar nota",
                tint = if (isPinned) PrimaryCyan else TextSecondary
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar nota",
                tint = TextSecondary
            )
        }
    }
}
