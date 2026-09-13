package com.example.quicknote

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quicknote.ui.screens.ImageZoomScreen
import com.example.quicknote.ui.screens.NoteDetailScreen
import com.example.quicknote.ui.screens.NotesListScreen
import com.example.quicknote.ui.theme.DarkBackground
import com.example.quicknote.ui.theme.DarkBorder
import com.example.quicknote.ui.theme.QuickNoteTheme
import com.example.quicknote.ui.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickNoteTheme {
                QuickNoteApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun QuickNoteApp(viewModel: NotesViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth > 840.dp

        if (isTablet) {
            val notes by viewModel.allNotes.collectAsState()
            var selectedNoteId by remember { mutableStateOf<Long?>(null) }
            var initialIsTask by remember { mutableStateOf(false) }
            var zoomImageUri by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(notes) {
                if (selectedNoteId == null && notes.isNotEmpty()) {
                    selectedNoteId = notes.first().id
                }
            }

            if (zoomImageUri != null) {
                ImageZoomScreen(
                    imageUri = zoomImageUri!!,
                    onBackClick = { zoomImageUri = null }
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                ) {
                    Box(
                        modifier = Modifier
                            .width(380.dp)
                            .fillMaxHeight()
                    ) {
                        NotesListScreen(
                            viewModel = viewModel,
                            onNoteClick = { selectedNoteId = it },
                            onNewNoteClick = { isTask ->
                                selectedNoteId = -1L
                                initialIsTask = isTask
                            },
                            selectedNoteIdForMasterDetail = selectedNoteId,
                            onNoteSelectedMasterDetail = { selectedNoteId = it }
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(DarkBorder)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        NoteDetailScreen(
                            noteId = if (selectedNoteId != null && selectedNoteId!! > 0) selectedNoteId else null,
                            initialIsTask = initialIsTask,
                            viewModel = viewModel,
                            onBackClick = { selectedNoteId = null },
                            onImageClick = { zoomImageUri = it }
                        )
                    }
                }
            }
        } else {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "notes_list"
            ) {
                composable("notes_list") {
                    NotesListScreen(
                        viewModel = viewModel,
                        onNoteClick = { noteId ->
                            navController.navigate("note_detail/$noteId?isTask=false")
                        },
                        onNewNoteClick = { isTask ->
                            navController.navigate("note_detail/-1?isTask=$isTask")
                        }
                    )
                }

                composable(
                    route = "note_detail/{noteId}?isTask={isTask}",
                    arguments = listOf(
                        navArgument("noteId") { type = NavType.LongType },
                        navArgument("isTask") {
                            type = NavType.BoolType
                            defaultValue = false
                        }
                    )
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getLong("noteId")
                    val isTask = backStackEntry.arguments?.getBoolean("isTask") ?: false
                    val validNoteId = if (noteId != null && noteId > 0) noteId else null

                    NoteDetailScreen(
                        noteId = validNoteId,
                        initialIsTask = isTask,
                        viewModel = viewModel,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onImageClick = { imageUri ->
                            val encodedUri = Uri.encode(imageUri)
                            navController.navigate("image_zoom?uri=$encodedUri")
                        }
                    )
                }

                composable(
                    route = "image_zoom?uri={uri}",
                    arguments = listOf(
                        navArgument("uri") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val encodedUri = backStackEntry.arguments?.getString("uri") ?: ""
                    val decodedUri = Uri.decode(encodedUri)

                    ImageZoomScreen(
                        imageUri = decodedUri,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
