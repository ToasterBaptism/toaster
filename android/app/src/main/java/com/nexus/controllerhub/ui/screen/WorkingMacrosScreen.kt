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
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.controllerhub.controller.ControllerManager
import com.nexus.controllerhub.controller.ControllerManager.Controller
import com.nexus.controllerhub.controller.ControllerManager.MacroEvent
import com.nexus.controllerhub.controller.ControllerManager.EventType
import com.nexus.controllerhub.ui.component.ProperControllerVisualization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingMacrosScreen(
    controllerManager: ControllerManager,
    onNavigateBack: () -> Unit,
    onNavigateToMacroEditor: (Long) -> Unit
) {
    // Collect real-time state from ControllerManager
    val connectedControllers by controllerManager.controllers.collectAsState()
    val selectedController by controllerManager.activeController.collectAsState()
    val isRecording by controllerManager.isRecording.collectAsState()
    val recordedMacro by controllerManager.recordedMacro.collectAsState()
    val buttonStates by controllerManager.buttonStates.collectAsState()
    val leftStick by controllerManager.leftStick.collectAsState()
    val rightStick by controllerManager.rightStick.collectAsState()
    val leftTrigger by controllerManager.leftTrigger.collectAsState()
    val rightTrigger by controllerManager.rightTrigger.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Record Macro", "Saved Macros", "Live Test")
    
    // Create analog states map for compatibility
    val analogStates = mapOf(
        "LEFT_STICK_X" to leftStick.x,
        "LEFT_STICK_Y" to leftStick.y,
        "RIGHT_STICK_X" to rightStick.x,
        "RIGHT_STICK_Y" to rightStick.y,
        "LEFT_TRIGGER" to leftTrigger,
        "RIGHT_TRIGGER" to rightTrigger
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Macro Controller") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        ) {
            // Controller Status
            ControllerStatusCard(
                connectedControllers = connectedControllers,
                activeController = selectedController,
                onSelectController = { controllerManager.selectController(it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tab Content
            when (selectedTab) {
                0 -> MacroRecordingTab(
                    controllerManager = controllerManager,
                    isRecording = isRecording,
                    recordedMacro = recordedMacro,
                    activeController = selectedController,
                    buttonStates = buttonStates,
                    analogStates = analogStates
                )
                1 -> SavedMacrosTab(
                    onNavigateToMacroEditor = onNavigateToMacroEditor
                )
                2 -> LiveTestTab(
                    controllerManager = controllerManager,
                    activeController = selectedController,
                    buttonStates = buttonStates,
                    analogStates = analogStates
                )
            }
        }
    }
}

@Composable
private fun ControllerStatusCard(
    connectedControllers: List<Controller>,
    activeController: Controller?,
    onSelectController: (Controller) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                activeController != null -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                connectedControllers.isNotEmpty() -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> Color(0xFFF44336).copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Gamepad,
                    contentDescription = null,
                    tint = when {
                        activeController != null -> Color(0xFF4CAF50)
                        connectedControllers.isNotEmpty() -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        activeController != null -> "Controller Active: ${activeController.name}"
                        connectedControllers.isNotEmpty() -> "${connectedControllers.size} Controller(s) Connected"
                        else -> "No Controllers Connected"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (connectedControllers.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Select Controller:", fontSize = 12.sp)
                connectedControllers.forEach { controller ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectController(controller) }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = controller == activeController,
                            onClick = { onSelectController(controller) }
                        )
                        Text("${controller.name} (ID: ${controller.deviceId})")
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroRecordingTab(
    controllerManager: ControllerManager,
    isRecording: Boolean,
    recordedMacro: List<MacroEvent>,
    activeController: Controller?,
    buttonStates: Map<String, Boolean>,
    analogStates: Map<String, Float>
) {
    Column {
        // Recording Controls
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Macro Recording",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { controllerManager.startMacroRecording() },
                        enabled = !isRecording && activeController != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start Recording")
                    }
                    
                    Button(
                        onClick = { controllerManager.stopMacroRecording() },
                        enabled = isRecording,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop Recording")
                    }
                    
                    Button(
                        onClick = { controllerManager.clearMacro() },
                        enabled = recordedMacro.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        )
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
                
                if (isRecording) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Recording... Press controller buttons to record macro",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Live Controller Visualization
        if (activeController != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Live Controller Input",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProperControllerVisualization(
                        controllerManager = controllerManager,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recorded Macro Display
        if (recordedMacro.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Recorded Macro (${recordedMacro.size} steps)",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(recordedMacro.take(20).size) { index ->
                            MacroStepDisplay(recordedMacro.take(20)[index])
                        }
                        if (recordedMacro.size > 20) {
                            item {
                                Text(
                                    "... and ${recordedMacro.size - 20} more steps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun MacroStepDisplay(step: MacroEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(
                Color.Gray.copy(alpha = 0.1f),
                RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${step.timestamp}ms",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(60.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (step.type) {
                EventType.BUTTON_DOWN -> "Button ${step.button} pressed"
                EventType.BUTTON_UP -> "Button ${step.button} released"
                EventType.STICK_MOVE -> "Stick ${step.button}: ${step.value}"
                EventType.TRIGGER_MOVE -> "Trigger ${step.button}: ${step.value}"
            },
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SavedMacrosTab(
    onNavigateToMacroEditor: (Long) -> Unit
) {
    Column {
        Text(
            "Saved Macros",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No Saved Macros",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Record a macro above to save it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LiveTestTab(
    controllerManager: ControllerManager,
    activeController: Controller?,
    buttonStates: Map<String, Boolean>,
    analogStates: Map<String, Float>
) {
    Column {
        Text(
            "Live Controller Test",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (activeController != null) {
            ProperControllerVisualization(
                controllerManager = controllerManager,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Button States Display
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Active Buttons",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val activeButtons = buttonStates.filter { it.value }
                    if (activeButtons.isNotEmpty()) {
                        activeButtons.forEach { (button, _) ->
                            Text(
                                "• $button",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            "No buttons pressed",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No Controller Selected",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Connect a controller to test input",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}