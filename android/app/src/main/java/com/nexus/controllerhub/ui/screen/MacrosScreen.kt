package com.nexus.controllerhub.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexus.controllerhub.R
import com.nexus.controllerhub.data.database.ControllerDatabase
import com.nexus.controllerhub.data.repository.ControllerRepository
import com.nexus.controllerhub.service.ControllerAccessibilityService
import com.nexus.controllerhub.ui.component.ControllerVisualization
import com.nexus.controllerhub.ui.viewmodel.MacrosViewModel
import com.nexus.controllerhub.ui.viewmodel.MacrosViewModelFactory
import com.nexus.controllerhub.util.ControllerInputState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacrosScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMacroEditor: (Long) -> Unit
) {
    val context = LocalContext.current
    val database = ControllerDatabase.getDatabase(context)
    val repository = ControllerRepository(database.profileDao(), database.macroDao())
    val viewModel: MacrosViewModel = viewModel(
        factory = MacrosViewModelFactory(repository)
    )
    
    val macros by viewModel.macros.collectAsState(initial = emptyList())
    val isRecording by viewModel.isRecording.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    
    // Real-time controller input state for testing
    val pressedButtons by ControllerInputState.pressedButtons.collectAsState()
    val leftStickPosition by ControllerInputState.leftStickPosition.collectAsState()
    val rightStickPosition by ControllerInputState.rightStickPosition.collectAsState()
    val leftTrigger by ControllerInputState.leftTrigger.collectAsState()
    val rightTrigger by ControllerInputState.rightTrigger.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.macro_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (isRecording) {
                    IconButton(onClick = { viewModel.stopRecording() }) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop Recording")
                    }
                } else {
                    IconButton(onClick = { viewModel.startRecording() }) {
                        Icon(Icons.Default.FiberManualRecord, contentDescription = "Start Recording")
                    }
                }
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Macro")
                }
            }
        )
        
        if (isRecording) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.macro_recording_active),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // Controller visualization for testing during recording
        if (isRecording) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Controller Test - Press buttons to see feedback",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Create a dummy profile for visualization
                    val dummyProfile = remember {
                        com.nexus.controllerhub.data.model.ControllerProfile(
                            name = "Test Profile",
                            description = "For testing"
                        )
                    }
                    
                    ControllerVisualization(
                        profile = dummyProfile,
                        onButtonClick = { },
                        pressedButtons = pressedButtons,
                        leftStickPosition = leftStickPosition,
                        rightStickPosition = rightStickPosition,
                        leftTrigger = leftTrigger,
                        rightTrigger = rightTrigger,
                        modifier = Modifier.height(300.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (macros.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No macros created yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap the record button to create your first macro",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(macros) { macro ->
                    MacroCard(
                        macro = macro,
                        onEdit = { onNavigateToMacroEditor(macro.id) },
                        onDelete = { showDeleteDialog = macro.id },
                        onTest = { viewModel.testMacro(macro.id) }
                    )
                }
            }
        }
    }
    
    // Create Macro Dialog
    if (showCreateDialog) {
        CreateMacroDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description ->
                viewModel.createEmptyMacro(name, description)
                showCreateDialog = false
            }
        )
    }
    
    // Delete Confirmation Dialog
    showDeleteDialog?.let { macroId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Macro") },
            text = { Text("Are you sure you want to delete this macro? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMacro(macroId)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MacroCard(
    macro: com.nexus.controllerhub.data.model.Macro,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = macro.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (macro.description.isNotEmpty()) {
                        Text(
                            text = macro.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${macro.actions.size} actions • ${macro.totalDuration}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onTest) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Test Macro")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateMacroDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Macro") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Macro Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}