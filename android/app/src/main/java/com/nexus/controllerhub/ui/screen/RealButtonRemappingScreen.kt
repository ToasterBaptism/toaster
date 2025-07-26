package com.nexus.controllerhub.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nexus.controllerhub.input.RealControllerInputManager
import kotlinx.coroutines.delay
import android.view.KeyEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealButtonRemappingScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val inputManager = remember { RealControllerInputManager(context) }
    
    // State management
    val connectedControllers by inputManager.connectedControllers.collectAsState()
    val buttonStates by inputManager.buttonStates.collectAsState()
    
    // Remapping state
    var buttonMappings by remember { mutableStateOf(getDefaultButtonMappings()) }
    var isRemappingMode by remember { mutableStateOf(false) }
    var selectedButton by remember { mutableStateOf<ButtonMapping?>(null) }
    var isWaitingForInput by remember { mutableStateOf(false) }
    var lastPressedButton by remember { mutableStateOf<Int?>(null) }
    
    // Listen for button presses when in remapping mode
    LaunchedEffect(buttonStates, isWaitingForInput) {
        if (isWaitingForInput) {
            val pressedButtons = buttonStates.filter { it.value }
            if (pressedButtons.isNotEmpty()) {
                val newKeyCode = pressedButtons.keys.first()
                selectedButton?.let { mapping ->
                    buttonMappings = buttonMappings.map { 
                        if (it.originalButton == mapping.originalButton) {
                            it.copy(mappedButton = newKeyCode, mappedButtonName = getKeyName(newKeyCode))
                        } else it
                    }
                }
                isWaitingForInput = false
                selectedButton = null
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎮 Button Remapping") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            isRemappingMode = !isRemappingMode
                            if (!isRemappingMode) {
                                isWaitingForInput = false
                                selectedButton = null
                            }
                        }
                    ) {
                        Icon(
                            if (isRemappingMode) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = if (isRemappingMode) "Save" else "Edit"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Controller Status
            ControllerStatusCard(connectedControllers)
            
            // Instructions
            InstructionsCard(isRemappingMode, isWaitingForInput)
            
            // Button Mappings List
            ButtonMappingsCard(
                buttonMappings = buttonMappings,
                isRemappingMode = isRemappingMode,
                buttonStates = buttonStates,
                onRemapButton = { mapping ->
                    selectedButton = mapping
                    isWaitingForInput = true
                },
                onResetButton = { mapping ->
                    buttonMappings = buttonMappings.map {
                        if (it.originalButton == mapping.originalButton) {
                            it.copy(
                                mappedButton = it.originalButton,
                                mappedButtonName = it.originalButtonName
                            )
                        } else it
                    }
                }
            )
            
            // Current Input Display
            if (isRemappingMode) {
                CurrentInputCard(buttonStates)
            }
        }
    }
    
    // Waiting for input dialog
    if (isWaitingForInput && selectedButton != null) {
        WaitingForInputDialog(
            buttonName = selectedButton!!.originalButtonName,
            onDismiss = {
                isWaitingForInput = false
                selectedButton = null
            }
        )
    }
}

data class ButtonMapping(
    val originalButton: Int,
    val originalButtonName: String,
    val mappedButton: Int,
    val mappedButtonName: String,
    val description: String
)

private fun getDefaultButtonMappings(): List<ButtonMapping> {
    return listOf(
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_A, "A", KeyEvent.KEYCODE_BUTTON_A, "A", "Primary action button"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_B, "B", KeyEvent.KEYCODE_BUTTON_B, "B", "Secondary action button"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_X, "X", KeyEvent.KEYCODE_BUTTON_X, "X", "Tertiary action button"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_Y, "Y", KeyEvent.KEYCODE_BUTTON_Y, "Y", "Quaternary action button"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_L1, "L1", KeyEvent.KEYCODE_BUTTON_L1, "L1", "Left shoulder button"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_R1, "R1", KeyEvent.KEYCODE_BUTTON_R1, "R1", "Right shoulder button"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_L2, "L2", KeyEvent.KEYCODE_BUTTON_L2, "L2", "Left trigger button"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_R2, "R2", KeyEvent.KEYCODE_BUTTON_R2, "R2", "Right trigger button"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_THUMBL, "L3", KeyEvent.KEYCODE_BUTTON_THUMBL, "L3", "Left stick click"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_THUMBR, "R3", KeyEvent.KEYCODE_BUTTON_THUMBR, "R3", "Right stick click"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_START, "START", KeyEvent.KEYCODE_BUTTON_START, "START", "Start/Menu button"),
        ButtonMapping(KeyEvent.KEYCODE_BUTTON_SELECT, "SELECT", KeyEvent.KEYCODE_BUTTON_SELECT, "SELECT", "Select/Back button"),
        ButtonMapping(KeyEvent.KEYCODE_DPAD_UP, "D-UP", KeyEvent.KEYCODE_DPAD_UP, "D-UP", "D-pad up"),
        ButtonMapping(KeyEvent.KEYCODE_DPAD_DOWN, "D-DOWN", KeyEvent.KEYCODE_DPAD_DOWN, "D-DOWN", "D-pad down"),
        ButtonMapping(KeyEvent.KEYCODE_DPAD_LEFT, "D-LEFT", KeyEvent.KEYCODE_DPAD_LEFT, "D-LEFT", "D-pad left"),
        ButtonMapping(KeyEvent.KEYCODE_DPAD_RIGHT, "D-RIGHT", KeyEvent.KEYCODE_DPAD_RIGHT, "D-RIGHT", "D-pad right")
    )
}

private fun getKeyName(keyCode: Int): String {
    return when (keyCode) {
        KeyEvent.KEYCODE_BUTTON_A -> "A"
        KeyEvent.KEYCODE_BUTTON_B -> "B"
        KeyEvent.KEYCODE_BUTTON_X -> "X"
        KeyEvent.KEYCODE_BUTTON_Y -> "Y"
        KeyEvent.KEYCODE_BUTTON_L1 -> "L1"
        KeyEvent.KEYCODE_BUTTON_R1 -> "R1"
        KeyEvent.KEYCODE_BUTTON_L2 -> "L2"
        KeyEvent.KEYCODE_BUTTON_R2 -> "R2"
        KeyEvent.KEYCODE_BUTTON_THUMBL -> "L3"
        KeyEvent.KEYCODE_BUTTON_THUMBR -> "R3"
        KeyEvent.KEYCODE_BUTTON_START -> "START"
        KeyEvent.KEYCODE_BUTTON_SELECT -> "SELECT"
        KeyEvent.KEYCODE_DPAD_UP -> "D-UP"
        KeyEvent.KEYCODE_DPAD_DOWN -> "D-DOWN"
        KeyEvent.KEYCODE_DPAD_LEFT -> "D-LEFT"
        KeyEvent.KEYCODE_DPAD_RIGHT -> "D-RIGHT"
        KeyEvent.KEYCODE_BACK -> "BACK"
        KeyEvent.KEYCODE_HOME -> "HOME"
        KeyEvent.KEYCODE_MENU -> "MENU"
        else -> "KEY_$keyCode"
    }
}

@Composable
private fun ControllerStatusCard(
    connectedControllers: List<RealControllerInputManager.ControllerInfo>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (connectedControllers.isNotEmpty()) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (connectedControllers.isEmpty()) {
                Text(
                    text = "❌ No Controllers Connected",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connect a controller to start remapping buttons.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "✅ Controller Connected: ${connectedControllers.first().name}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InstructionsCard(
    isRemappingMode: Boolean,
    isWaitingForInput: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isWaitingForInput) {
                    "🎯 Press the button you want to map to..."
                } else if (isRemappingMode) {
                    "✏️ Remapping Mode Active"
                } else {
                    "📋 Button Mapping Configuration"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when {
                    isWaitingForInput -> "Press any button on your controller to assign it to the selected mapping."
                    isRemappingMode -> "Tap any button mapping below to reassign it. Press the save icon when done."
                    else -> "Tap the edit icon to start remapping buttons. Each button can be mapped to any other button or system key."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ButtonMappingsCard(
    buttonMappings: List<ButtonMapping>,
    isRemappingMode: Boolean,
    buttonStates: Map<Int, Boolean>,
    onRemapButton: (ButtonMapping) -> Unit,
    onResetButton: (ButtonMapping) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎮 Button Mappings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(buttonMappings) { mapping ->
                    ButtonMappingItem(
                        mapping = mapping,
                        isPressed = buttonStates[mapping.originalButton] == true,
                        isRemappingMode = isRemappingMode,
                        onRemap = { onRemapButton(mapping) },
                        onReset = { onResetButton(mapping) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonMappingItem(
    mapping: ButtonMapping,
    isPressed: Boolean,
    isRemappingMode: Boolean,
    onRemap: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isRemappingMode) {
                    Modifier.clickable { onRemap() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                mapping.originalButton != mapping.mappedButton -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isPressed) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isPressed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRESSED",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    text = mapping.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (mapping.originalButton != mapping.mappedButton) {
                    Text(
                        text = "Mapped to: ${mapping.mappedButtonName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (isRemappingMode) {
                Row {
                    if (mapping.originalButton != mapping.mappedButton) {
                        IconButton(onClick = onReset) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Text(
                        text = "TAP TO REMAP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentInputCard(
    buttonStates: Map<Int, Boolean>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔴 Live Input Monitor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val activeButtons = buttonStates.filter { it.value }
            if (activeButtons.isNotEmpty()) {
                Text(
                    text = "Currently pressed: ${activeButtons.keys.joinToString(", ") { getKeyName(it) }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "No buttons currently pressed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WaitingForInputDialog(
    buttonName: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯 Waiting for Input",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Press the button you want to map to:",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = buttonName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}