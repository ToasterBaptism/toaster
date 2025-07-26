package com.nexus.controllerhub.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nexus.controllerhub.controller.ControllerManager
import com.nexus.controllerhub.controller.ButtonMapping
import com.nexus.controllerhub.controller.RemappingProfile
import kotlinx.coroutines.delay
import android.view.KeyEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonRemappingScreen(
    controllerManager: ControllerManager,
    onNavigateBack: () -> Unit
) {
    val remappingManager = controllerManager.remappingManager
    val profiles by remappingManager.profiles.collectAsState()
    val activeProfile by remappingManager.activeProfile.collectAsState()
    val isRemappingEnabled by remappingManager.isRemappingEnabled.collectAsState()
    val connectedControllers by controllerManager.controllers.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var showRemapDialog by remember { mutableStateOf<ButtonMapping?>(null) }
    var capturedKeyCode by remember { mutableStateOf<Int?>(null) }
    
    // Start test mode to capture input for remapping
    LaunchedEffect(Unit) {
        controllerManager.startTestMode()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            controllerManager.stopTestMode()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Button Remapping") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Switch(
                        checked = isRemappingEnabled,
                        onCheckedChange = { remappingManager.toggleRemapping(it) }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Controller Status
            ControllerStatusCard(connectedControllers)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Current Profile") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Profiles") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (selectedTab) {
                0 -> CurrentProfileTab(
                    activeProfile = activeProfile,
                    onRemapButton = { mapping -> showRemapDialog = mapping }
                )
                1 -> ProfilesTab(
                    profiles = profiles,
                    activeProfile = activeProfile,
                    onCreateProfile = { showCreateProfileDialog = true },
                    onSelectProfile = { remappingManager.setActiveProfile(it.id) },
                    onDeleteProfile = { remappingManager.deleteProfile(it.id) }
                )
            }
        }
    }
    
    // Create Profile Dialog
    if (showCreateProfileDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateProfileDialog = false },
            onConfirm = { name, description ->
                remappingManager.createProfile(name, description)
                showCreateProfileDialog = false
            }
        )
    }
    
    // Remap Button Dialog
    showRemapDialog?.let { mapping ->
        // Listen for controller input when dialog is open
        LaunchedEffect(showRemapDialog) {
            val inputEvents = controllerManager.inputEvents
            inputEvents.collect { events ->
                events.firstOrNull()?.let { event ->
                    if (event.type == "BUTTON" && event.data.contains("PRESSED")) {
                        // Extract key code from button name
                        val buttonName = event.data.split(" ")[0].split("->").last()
                        val keyCode = getKeyCodeFromButtonName(buttonName)
                        if (keyCode != -1) {
                            capturedKeyCode = keyCode
                        }
                    }
                }
            }
        }
        
        RemapButtonDialog(
            mapping = mapping,
            capturedKeyCode = capturedKeyCode,
            onDismiss = { 
                showRemapDialog = null
                capturedKeyCode = null
            },
            onConfirm = { newKeyCode ->
                activeProfile?.let { profile ->
                    val newMapping = mapping.copy(
                        mappedKeyCode = newKeyCode,
                        mappedButtonName = remappingManager.getButtonName(newKeyCode)
                    )
                    remappingManager.updateProfileMapping(profile.id, newMapping)
                }
                showRemapDialog = null
                capturedKeyCode = null
            }
        )
    }
}

@Composable
private fun ControllerStatusCard(controllers: List<ControllerManager.Controller>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (controllers.isNotEmpty()) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (controllers.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (controllers.isNotEmpty()) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (controllers.isNotEmpty()) 
                        "Controller Connected" 
                    else 
                        "No Controller Connected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (controllers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                controllers.forEach { controller ->
                    Text(
                        text = "• ${controller.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentProfileTab(
    activeProfile: RemappingProfile?,
    onRemapButton: (ButtonMapping) -> Unit
) {
    if (activeProfile == null) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Active Profile",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Please select a profile from the Profiles tab",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = activeProfile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (activeProfile.description.isNotEmpty()) {
                        Text(
                            text = activeProfile.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        items(activeProfile.mappings) { mapping ->
            ButtonMappingCard(
                mapping = mapping,
                onRemap = { onRemapButton(mapping) }
            )
        }
    }
}

@Composable
private fun ButtonMappingCard(
    mapping: ButtonMapping,
    onRemap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mapping.originalButtonName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (mapping.originalKeyCode != mapping.mappedKeyCode) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "mapped to",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = mapping.mappedButtonName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (mapping.description.isNotEmpty()) {
                    Text(
                        text = mapping.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onRemap) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Remap button"
                )
            }
        }
    }
}

@Composable
private fun ProfilesTab(
    profiles: List<RemappingProfile>,
    activeProfile: RemappingProfile?,
    onCreateProfile: () -> Unit,
    onSelectProfile: (RemappingProfile) -> Unit,
    onDeleteProfile: (RemappingProfile) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Button(
                onClick = onCreateProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Profile")
            }
        }
        
        items(profiles) { profile ->
            ProfileCard(
                profile = profile,
                isActive = profile.id == activeProfile?.id,
                onSelect = { onSelectProfile(profile) },
                onDelete = if (profile.id != "default") {
                    { onDeleteProfile(profile) }
                } else null
            )
        }
    }
}

@Composable
private fun ProfileCard(
    profile: RemappingProfile,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isActive) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                if (profile.description.isNotEmpty()) {
                    Text(
                        text = profile.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "${profile.mappings.size} button mappings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete profile",
                        tint = MaterialTheme.colorScheme.error
                    )
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

@Composable
private fun RemapButtonDialog(
    mapping: ButtonMapping,
    capturedKeyCode: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remap ${mapping.originalButtonName}") },
        text = {
            Column {
                Text("Press the button you want to map to:")
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (capturedKeyCode != null) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (capturedKeyCode != null) {
                            val buttonName = getButtonNameFromKeyCode(capturedKeyCode)
                            Text(
                                text = "Captured: $buttonName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Waiting for input...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { capturedKeyCode?.let { onConfirm(it) } },
                enabled = capturedKeyCode != null
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getKeyCodeFromButtonName(buttonName: String): Int {
    return when (buttonName) {
        "A" -> KeyEvent.KEYCODE_BUTTON_A
        "B" -> KeyEvent.KEYCODE_BUTTON_B
        "X" -> KeyEvent.KEYCODE_BUTTON_X
        "Y" -> KeyEvent.KEYCODE_BUTTON_Y
        "L1" -> KeyEvent.KEYCODE_BUTTON_L1
        "R1" -> KeyEvent.KEYCODE_BUTTON_R1
        "L2" -> KeyEvent.KEYCODE_BUTTON_L2
        "R2" -> KeyEvent.KEYCODE_BUTTON_R2
        "LS" -> KeyEvent.KEYCODE_BUTTON_THUMBL
        "RS" -> KeyEvent.KEYCODE_BUTTON_THUMBR
        "START" -> KeyEvent.KEYCODE_BUTTON_START
        "SELECT" -> KeyEvent.KEYCODE_BUTTON_SELECT
        "DPAD_UP" -> KeyEvent.KEYCODE_DPAD_UP
        "DPAD_DOWN" -> KeyEvent.KEYCODE_DPAD_DOWN
        "DPAD_LEFT" -> KeyEvent.KEYCODE_DPAD_LEFT
        "DPAD_RIGHT" -> KeyEvent.KEYCODE_DPAD_RIGHT
        else -> -1
    }
}

private fun getButtonNameFromKeyCode(keyCode: Int): String {
    return when (keyCode) {
        KeyEvent.KEYCODE_BUTTON_A -> "A"
        KeyEvent.KEYCODE_BUTTON_B -> "B"
        KeyEvent.KEYCODE_BUTTON_X -> "X"
        KeyEvent.KEYCODE_BUTTON_Y -> "Y"
        KeyEvent.KEYCODE_BUTTON_L1 -> "L1"
        KeyEvent.KEYCODE_BUTTON_R1 -> "R1"
        KeyEvent.KEYCODE_BUTTON_L2 -> "L2"
        KeyEvent.KEYCODE_BUTTON_R2 -> "R2"
        KeyEvent.KEYCODE_BUTTON_THUMBL -> "LS"
        KeyEvent.KEYCODE_BUTTON_THUMBR -> "RS"
        KeyEvent.KEYCODE_BUTTON_START -> "START"
        KeyEvent.KEYCODE_BUTTON_SELECT -> "SELECT"
        KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
        KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
        KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
        KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
        else -> "UNKNOWN_$keyCode"
    }
}