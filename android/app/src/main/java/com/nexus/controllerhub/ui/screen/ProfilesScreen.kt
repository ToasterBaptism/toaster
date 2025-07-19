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
import com.nexus.controllerhub.ui.viewmodel.ProfilesViewModel
import com.nexus.controllerhub.ui.viewmodel.ProfilesViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToConfiguration: (Long) -> Unit
) {
    val context = LocalContext.current
    val database = ControllerDatabase.getDatabase(context)
    val repository = ControllerRepository(database.profileDao(), database.macroDao())
    val viewModel: ProfilesViewModel = viewModel(
        factory = ProfilesViewModelFactory(repository)
    )
    
    val profiles by viewModel.profiles.collectAsState(initial = emptyList())
    val activeProfile by viewModel.activeProfile.collectAsState(initial = null)
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.profiles_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Profile")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(profiles) { profile ->
                ProfileCard(
                    profile = profile,
                    isActive = profile.id == activeProfile?.id,
                    onActivate = { viewModel.activateProfile(profile.id) },
                    onEdit = { onNavigateToConfiguration(profile.id) },
                    onDelete = { showDeleteDialog = profile.id },
                    onDuplicate = { viewModel.duplicateProfile(profile.id, "${profile.name} Copy") }
                )
            }
        }
    }
    
    // Create Profile Dialog
    if (showCreateDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description ->
                viewModel.createProfile(name, description)
                showCreateDialog = false
            }
        )
    }
    
    // Delete Confirmation Dialog
    showDeleteDialog?.let { profileId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Profile") },
            text = { Text("Are you sure you want to delete this profile? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProfile(profileId)
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
private fun ProfileCard(
    profile: com.nexus.controllerhub.data.model.ControllerProfile,
    isActive: Boolean,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        )
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
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (profile.description.isNotEmpty()) {
                        Text(
                            text = profile.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isActive) {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Row {
                    if (!isActive) {
                        IconButton(onClick = onActivate) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Activate")
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate")
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
private fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
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