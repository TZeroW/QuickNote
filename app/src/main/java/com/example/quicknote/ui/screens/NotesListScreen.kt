package com.example.quicknote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quicknote.ui.components.CategoryFilterChips
import com.example.quicknote.ui.components.QuickNoteHeader
import com.example.quicknote.ui.components.SwipeableNoteCard
import com.example.quicknote.ui.theme.DarkBackground
import com.example.quicknote.ui.theme.DarkSurface
import com.example.quicknote.ui.theme.DarkSurfaceVariant
import com.example.quicknote.ui.theme.OnPrimaryCyan
import com.example.quicknote.ui.theme.PrimaryCyan
import com.example.quicknote.ui.theme.TextMuted
import com.example.quicknote.ui.theme.TextPrimary
import com.example.quicknote.ui.theme.TextSecondary
import com.example.quicknote.ui.viewmodel.NotesViewModel

@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Long) -> Unit,
    onNewNoteClick: (isTask: Boolean) -> Unit,
    selectedNoteIdForMasterDetail: Long? = null,
    onNoteSelectedMasterDetail: ((Long) -> Unit)? = null
) {
    val notes by viewModel.allNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val totalCount by viewModel.totalCount.collectAsState()
    val notesCount by viewModel.notesCount.collectAsState()
    val tasksCount by viewModel.tasksCount.collectAsState()

    var isFabMenuExpanded by remember { mutableStateOf(false) }

    val fabRotation by animateFloatAsState(
        targetValue = if (isFabMenuExpanded) 45f else 0f,
        label = "fabRotation"
    )

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                // Expandable Options Menu
                AnimatedVisibility(
                    visible = isFabMenuExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Option 1: Nueva Lista de Tareas
                        FabMenuOption(
                            label = "Nueva Lista",
                            icon = Icons.Default.Checklist,
                            onClick = {
                                isFabMenuExpanded = false
                                onNewNoteClick(true)
                            }
                        )

                        // Option 2: Nueva Nota de Texto
                        FabMenuOption(
                            label = "Nueva Nota",
                            icon = Icons.Default.EditNote,
                            onClick = {
                                isFabMenuExpanded = false
                                onNewNoteClick(false)
                            }
                        )
                    }
                }

                // Main FAB (+) button with smooth rotation animation
                FloatingActionButton(
                    onClick = { isFabMenuExpanded = !isFabMenuExpanded },
                    containerColor = PrimaryCyan,
                    contentColor = OnPrimaryCyan,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar nota o tarea",
                        modifier = Modifier
                            .size(32.dp)
                            .rotate(fabRotation)
                    )
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth > 600.dp

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar (Title, Search)
                QuickNoteHeader(
                    searchQuery = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) }
                )

                // Filter Category Chips (Todas, Notas, Pendientes)
                CategoryFilterChips(
                    selectedCategory = selectedCategory,
                    totalCount = totalCount,
                    notesCount = notesCount,
                    tasksCount = tasksCount,
                    onCategorySelected = { viewModel.onCategorySelected(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (notes.isEmpty()) {
                    EmptyNotesState()
                } else if (isWideScreen) {
                    // Responsive Grid Layout for Tablets / Wide screen / Landscape
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 300.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(notes, key = { it.id }) { note ->
                            SwipeableNoteCard(
                                note = note,
                                onClick = {
                                    if (onNoteSelectedMasterDetail != null) {
                                        onNoteSelectedMasterDetail(note.id)
                                    } else {
                                        onNoteClick(note.id)
                                    }
                                },
                                onToggleCompleted = { viewModel.toggleTaskCompleted(note) },
                                onToggleTodoItem = { todoId -> viewModel.toggleSubTask(note, todoId) },
                                onDelete = { viewModel.deleteNote(note) }
                            )
                        }
                    }
                } else {
                    // 1-Column List for Mobile Smartphones
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(notes, key = { it.id }) { note ->
                            SwipeableNoteCard(
                                note = note,
                                onClick = { onNoteClick(note.id) },
                                onToggleCompleted = { viewModel.toggleTaskCompleted(note) },
                                onToggleTodoItem = { todoId -> viewModel.toggleSubTask(note, todoId) },
                                onDelete = { viewModel.deleteNote(note) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FabMenuOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(DarkSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PrimaryCyan),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = OnPrimaryCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyNotesState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NoteAdd,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay notas o tareas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Toca el botón + para crear tu primera nota o lista de pendientes.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
