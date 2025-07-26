package com.nexus.controllerhub.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.controllerhub.util.RealControllerManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealConfigurationScreen(
    profileId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val controllerManager = remember { RealControllerManager.getInstance(context) }
    
    // Collect real-time state
    val connectedControllers by controllerManager.connectedControllers.collectAsState()
    val pressedButtons by controllerManager.pressedButtons.collectAsState()
    
    var selectedController by remember { mutableStateOf<RealControllerManager.RealControllerInfo?>(null) }
    var remappingMode by remember { mutableStateOf(false) }
    var selectedButton by remember { mutableStateOf<String?>(null) }
    
    // Auto-select first controller if available
    LaunchedEffect(connectedControllers) {
        if (selectedController == null && connectedControllers.isNotEmpty()) {
            selectedController = connectedControllers.first()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Real Controller Configuration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { controllerManager.refreshControllers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Controller Selection
            ControllerSelectionCard(
                controllers = connectedControllers,
                selectedController = selectedController,
                onControllerSelected = { selectedController = it }
            )
            
            selectedController?.let { controller ->
                // Real Controller Visualization
                RealControllerVisualizationCard(
                    controller = controller,
                    pressedButtons = pressedButtons,
                    remappingMode = remappingMode,
                    selectedButton = selectedButton,
                    onButtonSelected = { buttonName ->
                        selectedButton = buttonName
                        // Here you would implement the remapping logic
                    }
                )
                
                // Button Remapping Controls
                ButtonRemappingCard(
                    controller = controller,
                    remappingMode = remappingMode,
                    selectedButton = selectedButton,
                    onToggleRemapping = { remappingMode = !remappingMode },
                    onClearSelection = { selectedButton = null }
                )
                
                // Controller Capabilities
                ControllerCapabilitiesCard(controller)
            }
        }
    }
}

@Composable
private fun ControllerSelectionCard(
    controllers: List<RealControllerManager.RealControllerInfo>,
    selectedController: RealControllerManager.RealControllerInfo?,
    onControllerSelected: (RealControllerManager.RealControllerInfo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Controller",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (controllers.isEmpty()) {
                Text(
                    text = "❌ No controllers detected",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Connect a controller via USB or Bluetooth to configure it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                for (controller in controllers) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onControllerSelected(controller) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedController?.deviceId == controller.deviceId)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (selectedController?.deviceId == controller.deviceId)
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Gamepad,
                                contentDescription = null,
                                tint = if (selectedController?.deviceId == controller.deviceId)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = controller.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Type: ${controller.controllerType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "ID: ${controller.deviceId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selectedController?.deviceId == controller.deviceId) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealControllerVisualizationCard(
    controller: RealControllerManager.RealControllerInfo,
    pressedButtons: Set<String>,
    remappingMode: Boolean,
    selectedButton: String?,
    onButtonSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Controller Visualization",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (remappingMode) {
                Text(
                    text = "🎯 Remapping Mode: Click a button below to remap it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Visual representation of controller buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Face buttons (A, B, X, Y)
                Text(
                    text = "Face Buttons",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Y", "X", "B", "A").forEach { button ->
                        ControllerButton(
                            buttonName = button,
                            isPressed = pressedButtons.contains(button),
                            isSelected = selectedButton == button,
                            remappingMode = remappingMode,
                            onClick = { onButtonSelected(button) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Shoulder buttons
                Text(
                    text = "Shoulder Buttons",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("L1", "L2", "R1", "R2").forEach { button ->
                        ControllerButton(
                            buttonName = button,
                            isPressed = pressedButtons.contains(button),
                            isSelected = selectedButton == button,
                            remappingMode = remappingMode,
                            onClick = { onButtonSelected(button) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // D-Pad
                Text(
                    text = "D-Pad",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ControllerButton(
                        buttonName = "DPAD_UP",
                        isPressed = pressedButtons.contains("DPAD_UP"),
                        isSelected = selectedButton == "DPAD_UP",
                        remappingMode = remappingMode,
                        onClick = { onButtonSelected("DPAD_UP") }
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ControllerButton(
                            buttonName = "DPAD_LEFT",
                            isPressed = pressedButtons.contains("DPAD_LEFT"),
                            isSelected = selectedButton == "DPAD_LEFT",
                            remappingMode = remappingMode,
                            onClick = { onButtonSelected("DPAD_LEFT") }
                        )
                        Spacer(modifier = Modifier.width(32.dp))
                        ControllerButton(
                            buttonName = "DPAD_RIGHT",
                            isPressed = pressedButtons.contains("DPAD_RIGHT"),
                            isSelected = selectedButton == "DPAD_RIGHT",
                            remappingMode = remappingMode,
                            onClick = { onButtonSelected("DPAD_RIGHT") }
                        )
                    }
                    ControllerButton(
                        buttonName = "DPAD_DOWN",
                        isPressed = pressedButtons.contains("DPAD_DOWN"),
                        isSelected = selectedButton == "DPAD_DOWN",
                        remappingMode = remappingMode,
                        onClick = { onButtonSelected("DPAD_DOWN") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stick buttons and system buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("LS", "RS", "START", "SELECT").forEach { button ->
                        ControllerButton(
                            buttonName = button,
                            isPressed = pressedButtons.contains(button),
                            isSelected = selectedButton == button,
                            remappingMode = remappingMode,
                            onClick = { onButtonSelected(button) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ControllerButton(
    buttonName: String,
    isPressed: Boolean,
    isSelected: Boolean,
    remappingMode: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isPressed -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        isPressed -> MaterialTheme.colorScheme.onPrimary
        isSelected -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.secondary
                       else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = remappingMode) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buttonName.replace("DPAD_", "").replace("_", ""),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (isPressed || isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ButtonRemappingCard(
    controller: RealControllerManager.RealControllerInfo,
    remappingMode: Boolean,
    selectedButton: String?,
    onToggleRemapping: () -> Unit,
    onClearSelection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Button Remapping",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onToggleRemapping,
                    modifier = Modifier.weight(1f),
                    colors = if (remappingMode) 
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    else 
                        ButtonDefaults.buttonColors()
                ) {
                    Text(if (remappingMode) "Exit Remapping" else "Start Remapping")
                }
                
                if (selectedButton != null) {
                    Button(
                        onClick = onClearSelection,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Clear Selection")
                    }
                }
            }
            
            if (selectedButton != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Selected Button: $selectedButton",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Press a different controller button to remap $selectedButton to that button.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Or select a target action from the list below:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Common remapping targets
                        val remapTargets = listOf(
                            "Android Back",
                            "Android Home",
                            "Android Menu",
                            "Volume Up",
                            "Volume Down",
                            "Media Play/Pause",
                            "Screenshot"
                        )
                        
                        for (target in remapTargets) {
                            TextButton(
                                onClick = { 
                                    // Here you would implement the actual remapping
                                    // For now, just show a message
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Remap to: $target",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            
            if (remappingMode && selectedButton == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Click on any button in the controller visualization above to select it for remapping.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ControllerCapabilitiesCard(controller: RealControllerManager.RealControllerInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Controller Capabilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Basic info
            InfoRow("Name", controller.name)
            InfoRow("Type", controller.controllerType.toString())
            InfoRow("Device ID", controller.deviceId.toString())
            InfoRow("Vendor ID", "0x${controller.vendorId.toString(16).uppercase()}")
            InfoRow("Product ID", "0x${controller.productId.toString(16).uppercase()}")
            InfoRow("Has Vibrator", if (controller.hasVibrator) "Yes" else "No")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Supported Buttons (${controller.hasKeys.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            if (controller.hasKeys.isNotEmpty()) {
                Text(
                    text = controller.hasKeys.joinToString(", ") { keyCode ->
                        when (keyCode) {
                            96 -> "A"
                            97 -> "B"
                            99 -> "X"
                            100 -> "Y"
                            102 -> "L1"
                            103 -> "R1"
                            104 -> "L2"
                            105 -> "R2"
                            106 -> "LS"
                            107 -> "RS"
                            108 -> "START"
                            109 -> "SELECT"
                            19 -> "DPAD_UP"
                            20 -> "DPAD_DOWN"
                            21 -> "DPAD_LEFT"
                            22 -> "DPAD_RIGHT"
                            else -> "KEY_$keyCode"
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Motion Axes (${controller.motionRanges.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            if (controller.motionRanges.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.height(100.dp)
                ) {
                    items(controller.motionRanges) { range ->
                        Text(
                            text = "Axis ${range.axis}: ${range.min} to ${range.max}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}