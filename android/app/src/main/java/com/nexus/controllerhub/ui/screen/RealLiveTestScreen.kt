package com.nexus.controllerhub.ui.screen

import com.nexus.controllerhub.core.ControllerInputSystem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.controllerhub.controller.ControllerManager
import com.nexus.controllerhub.ui.component.ProperControllerVisualization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealLiveTestScreen(
    controllerManager: ControllerManager,
    onNavigateBack: () -> Unit
) {
    // State management using ControllerManager
    val connectedControllers by controllerManager.controllers.collectAsState()
    val activeController by controllerManager.activeController.collectAsState()
    val isRecording by controllerManager.isRecording.collectAsState()
    val recordedMacro by controllerManager.recordedMacro.collectAsState()
    val inputEvents by controllerManager.inputEvents.collectAsState()
    
    var isInputCaptureActive by remember { mutableStateOf(false) }
    var showInputDetails by remember { mutableStateOf(false) }
    var showMacroDetails by remember { mutableStateOf(false) }
    
    // Enable/disable input capture based on state
    LaunchedEffect(isInputCaptureActive) {
        if (isInputCaptureActive) {
            controllerManager.startTestMode()
        } else {
            controllerManager.stopTestMode()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧪 Live Controller Testing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            // Controller Status Section
            ControllerStatusSection(
                connectedControllers = connectedControllers,
                selectedController = activeController,
                onSelectController = { controllerManager.selectController(it) }
            )
            
            // Test Controls Section
            TestControlsSection(
                isInputCaptureActive = isInputCaptureActive,
                onToggleInputCapture = { isInputCaptureActive = !isInputCaptureActive },
                isRecording = isRecording,
                onStartRecording = { controllerManager.startMacroRecording() },
                onStopRecording = { controllerManager.stopMacroRecording() },
                onClearMacro = { controllerManager.clearMacro() },
                showInputDetails = showInputDetails,
                onToggleInputDetails = { showInputDetails = !showInputDetails },
                showMacroDetails = showMacroDetails,
                onToggleMacroDetails = { showMacroDetails = !showMacroDetails }
            )
            
            if (isInputCaptureActive && activeController != null) {
                // Live Controller Visualization
                ProperControllerVisualization(
                    controllerManager = controllerManager,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Input Details (if enabled)
                if (showInputDetails) {
                    InputDetailsSection(
                        inputEvents = inputEvents,
                        onClearLog = { controllerManager.clearInputHistory() }
                    )
                }
                
                // Macro Details (if enabled and macro exists)
                if (showMacroDetails && recordedMacro.isNotEmpty()) {
                    MacroDetailsSection(recordedMacro = recordedMacro)
                }
            } else if (!isInputCaptureActive) {
                // Setup Instructions
                SetupInstructionsSection()
            } else {
                // No controller selected
                NoControllerSelectedSection()
            }
        }
    }
}

@Composable
private fun ControllerStatusSection(
    connectedControllers: List<ControllerManager.Controller>,
    selectedController: ControllerManager.Controller?,
    onSelectController: (ControllerManager.Controller) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                selectedController != null -> MaterialTheme.colorScheme.primaryContainer
                connectedControllers.isNotEmpty() -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎮 Controller Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            when {
                selectedController != null -> {
                    Text(
                        text = "✅ Active Controller:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = selectedController.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip("ID: ${selectedController.deviceId}")
                        StatusChip(selectedController.type.name)
                        StatusChip("${selectedController.supportedAxes.size} Axes")
                        StatusChip("${selectedController.supportedButtons.size} Buttons")
                    }
                    
                    if (connectedControllers.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 ${connectedControllers.size - 1} other controller(s) available. Go to Device Selection to switch.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                connectedControllers.isNotEmpty() -> {
                    Text(
                        text = "⚠️ Controllers detected but none selected:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    for (controller in connectedControllers.take(3)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${controller.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            
                            TextButton(
                                onClick = { onSelectController(controller) }
                            ) {
                                Text("Select")
                            }
                        }
                    }
                    
                    if (connectedControllers.size > 3) {
                        Text(
                            text = "... and ${connectedControllers.size - 3} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                else -> {
                    Text(
                        text = "❌ No controllers detected",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Text(
                        text = "Please connect a controller via USB or Bluetooth to continue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TestControlsSection(
    isInputCaptureActive: Boolean,
    onToggleInputCapture: () -> Unit,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onClearMacro: () -> Unit,
    showInputDetails: Boolean,
    onToggleInputDetails: () -> Unit,
    showMacroDetails: Boolean,
    onToggleMacroDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎛️ Test Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Main control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleInputCapture,
                    modifier = Modifier.weight(1f),
                    colors = if (isInputCaptureActive) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Icon(
                        if (isInputCaptureActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isInputCaptureActive) "Stop Capture" else "Start Capture")
                }
            }
            
            // Macro controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = if (isRecording) onStopRecording else onStartRecording,
                    modifier = Modifier.weight(1f),
                    enabled = isInputCaptureActive,
                    colors = if (isRecording) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRecording) "Stop Recording" else "Record Macro")
                }
                
                Button(
                    onClick = onClearMacro,
                    modifier = Modifier.weight(1f),
                    enabled = isInputCaptureActive
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Macro")
                }
            }
            
            // View controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleInputDetails,
                    modifier = Modifier.weight(1f),
                    enabled = isInputCaptureActive
                ) {
                    Icon(
                        if (showInputDetails) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (showInputDetails) "Hide Input Log" else "Show Input Log")
                }
                
                Button(
                    onClick = onToggleMacroDetails,
                    modifier = Modifier.weight(1f),
                    enabled = isInputCaptureActive
                ) {
                    Icon(
                        if (showMacroDetails) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (showMacroDetails) "Hide Macro" else "Show Macro")
                }
            }
        }
    }
}

@Composable
private fun InputDetailsSection(
    inputEvents: List<ControllerManager.InputEvent>,
    onClearLog: () -> Unit
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
                Text(
                    text = "📊 Input Event Log (${inputEvents.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onClearLog) {
                    Text("Clear")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (inputEvents.isEmpty()) {
                Text(
                    text = "No input events recorded yet. Press buttons or move sticks to see data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val eventsToShow = inputEvents.take(50)
                    items(eventsToShow) { event ->
                        InputEventItem(event)
                    }
                }
                
                if (inputEvents.size > 50) {
                    Text(
                        text = "... and ${inputEvents.size - 50} more events",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InputEventItem(event: ControllerManager.InputEvent) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (event.type) {
                "BUTTON" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                "ANALOG" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = event.data,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${event.type} | Timestamp: ${event.timestamp}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MacroDetailsSection(
    recordedMacro: List<ControllerManager.MacroEvent>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📹 Recorded Macro (${recordedMacro.size} steps)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.height(150.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(recordedMacro) { step ->
                    MacroStepItem(step)
                }
            }
        }
    }
}

@Composable
private fun MacroStepItem(step: ControllerManager.MacroEvent) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = "${step.timestamp}ms: ${step.type.name} ${step.button ?: ""} ${step.value ?: ""}".trim(),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun SetupInstructionsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚀 Getting Started",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InstructionStep("1", "Connect a controller via USB or Bluetooth")
                InstructionStep("2", "Select the controller from the status section above")
                InstructionStep("3", "Tap 'Start Capture' to begin real-time input monitoring")
                InstructionStep("4", "Press buttons and move sticks to see live visualization")
                InstructionStep("5", "Use 'Record Macro' to capture input sequences")
                InstructionStep("6", "Enable input/macro logs for detailed analysis")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "⚠️ Important: Input capture requires the app to be in focus. Make sure this screen is active when testing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = androidx.compose.foundation.shape.CircleShape,
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun NoControllerSelectedSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎮 Controller Selection Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Controllers are connected but none is selected for input capture. Please select a controller from the status section above or go to Device Selection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}