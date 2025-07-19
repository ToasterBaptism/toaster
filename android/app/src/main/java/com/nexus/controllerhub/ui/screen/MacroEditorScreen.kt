package com.nexus.controllerhub.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexus.controllerhub.data.database.ControllerDatabase
import com.nexus.controllerhub.data.model.MacroAction
import com.nexus.controllerhub.data.model.MacroActionType
import com.nexus.controllerhub.data.repository.ControllerRepository
import com.nexus.controllerhub.ui.viewmodel.MacroEditorViewModel
import com.nexus.controllerhub.ui.viewmodel.MacroEditorViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroEditorScreen(
    macroId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = ControllerDatabase.getDatabase(context)
    val repository = ControllerRepository(database.profileDao(), database.macroDao())
    val viewModel: MacroEditorViewModel = viewModel(
        factory = MacroEditorViewModelFactory(repository, macroId)
    )
    
    val macro by viewModel.macro.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { 
                Text(
                    text = macro?.name ?: "Macro Editor",
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { viewModel.saveMacro() }) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                }
            }
        )
        
        if (macro != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Macro Info
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Macro Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Actions: ${macro!!.actions.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Duration: ${macro!!.totalDuration}ms",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Actions List
                Text(
                    text = "Macro Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (macro!!.actions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No actions recorded",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(macro!!.actions) { index, action ->
                            MacroActionCard(
                                action = action,
                                index = index,
                                onEdit = { viewModel.editAction(index, it) },
                                onDelete = { viewModel.deleteAction(index) }
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun MacroActionCard(
    action: MacroAction,
    index: Int,
    onEdit: (MacroAction) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Action Index
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Action Icon
            Icon(
                imageVector = when (action.type) {
                    MacroActionType.BUTTON_PRESS -> Icons.Default.TouchApp
                    MacroActionType.BUTTON_RELEASE -> Icons.Default.PanTool
                    MacroActionType.AXIS_MOVE -> Icons.Default.OpenWith
                    MacroActionType.DELAY -> Icons.Default.Schedule
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Action Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (action.type) {
                        MacroActionType.BUTTON_PRESS -> "Press ${action.buttonCode}"
                        MacroActionType.BUTTON_RELEASE -> "Release ${action.buttonCode}"
                        MacroActionType.AXIS_MOVE -> "${action.axisCode}: ${action.axisValue}"
                        MacroActionType.DELAY -> "Delay ${action.delayAfter}ms"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "At ${action.timestamp}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action Buttons
            IconButton(
                onClick = { onEdit(action) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp)
                )
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}