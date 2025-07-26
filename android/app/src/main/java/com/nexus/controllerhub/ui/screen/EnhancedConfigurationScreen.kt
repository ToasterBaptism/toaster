package com.nexus.controllerhub.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexus.controllerhub.controller.ControllerManager
import com.nexus.controllerhub.data.database.ControllerDatabase
import com.nexus.controllerhub.data.repository.ControllerRepository
import com.nexus.controllerhub.ui.component.ProperControllerVisualization
import com.nexus.controllerhub.ui.viewmodel.ConfigurationViewModel
import com.nexus.controllerhub.ui.viewmodel.ConfigurationViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedConfigurationScreen(
    profileId: Long,
    controllerManager: ControllerManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = ControllerDatabase.getDatabase(context)
    val repository = ControllerRepository(database.profileDao(), database.macroDao())
    val viewModel: ConfigurationViewModel = viewModel(
        factory = ConfigurationViewModelFactory(repository, profileId)
    )
    
    val profile by viewModel.profile.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    
    // ControllerManager state
    val connectedControllers by controllerManager.controllers.collectAsState()
    val activeController by controllerManager.activeController.collectAsState()
    val isRecording by controllerManager.isRecording.collectAsState()
    val recordedMacro by controllerManager.recordedMacro.collectAsState()
    val inputEvents by controllerManager.inputEvents.collectAsState()
    
    var isInputCaptureActive by remember { mutableStateOf(false) }
    
    // Enable/disable input capture based on state
    LaunchedEffect(isInputCaptureActive) {
        if (isInputCaptureActive) {
            controllerManager.startTestMode()
        } else {
            controllerManager.stopTestMode()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { 
                Text(
                    text = profile?.name ?: "Configuration",
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // Toggle input capture for real-time feedback
                IconButton(
                    onClick = { isInputCaptureActive = !isInputCaptureActive }
                ) {
                    Icon(
                        imageVector = if (isInputCaptureActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isInputCaptureActive) "Disable Live Input" else "Enable Live Input",
                        tint = if (isInputCaptureActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
        
        if (profile != null) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Button Mapping") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Calibration") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text("Macros") }
                )
            }
            
            // Tab Content
            when (selectedTab) {
                0 -> EnhancedButtonMappingTab(
                    profile = profile!!,
                    controllerManager = controllerManager,
                    isInputCaptureActive = isInputCaptureActive,
                    onUpdateMapping = viewModel::updateButtonMapping
                )
                1 -> EnhancedCalibrationTab(
                    profile = profile!!,
                    controllerManager = controllerManager,
                    isInputCaptureActive = isInputCaptureActive,
                    onUpdateAnalogSettings = viewModel::updateAnalogSettings
                )
                2 -> EnhancedMacroAssignmentTab(
                    profile = profile!!,
                    controllerManager = controllerManager,
                    isRecording = isRecording,
                    recordedMacro = recordedMacro,
                    onUpdateMacroAssignment = viewModel::updateMacroAssignment
                )
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
private fun EnhancedButtonMappingTab(
    profile: com.nexus.controllerhub.data.model.ControllerProfile,
    controllerManager: ControllerManager,
    isInputCaptureActive: Boolean,
    onUpdateMapping: (String, String) -> Unit
) {
    val activeController by controllerManager.activeController.collectAsState()
    val inputEvents by controllerManager.inputEvents.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isInputCaptureActive) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isInputCaptureActive) "🎮 Live Input Active" else "⏸️ Live Input Disabled",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isInputCaptureActive) 
                        "Press controller buttons to see them light up below" 
                    else 
                        "Enable live input (eye icon) to see real-time controller feedback",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (activeController != null) {
                    Text(
                        text = "Controller: ${activeController!!.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Controller Visualization with real-time feedback
        if (isInputCaptureActive && activeController != null) {
            ProperControllerVisualization(
                controllerManager = controllerManager
            )
        }
        
        // Recent Input Events
        if (isInputCaptureActive && inputEvents.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Recent Input Events",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(inputEvents.takeLast(10)) { event ->
                            Text(
                                text = "• ${event.data}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        
        // Current Mappings List
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Current Button Mappings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (profile.buttonMappings.isEmpty()) {
                    Text(
                        text = "No custom mappings configured",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    profile.buttonMappings.forEach { (from, to) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = from,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "maps to",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = to,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { onUpdateMapping(from, "") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove mapping",
                                    modifier = Modifier.size(16.dp)
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
private fun EnhancedCalibrationTab(
    profile: com.nexus.controllerhub.data.model.ControllerProfile,
    controllerManager: ControllerManager,
    isInputCaptureActive: Boolean,
    onUpdateAnalogSettings: (com.nexus.controllerhub.data.model.AnalogSettings) -> Unit
) {
    val activeController by controllerManager.activeController.collectAsState()
    val inputEvents by controllerManager.inputEvents.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isInputCaptureActive) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🎯 Analog Stick & Trigger Calibration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isInputCaptureActive) 
                        "Move sticks and triggers to see live values below" 
                    else 
                        "Enable live input to see real-time analog values",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (activeController != null) {
                    Text(
                        text = "Controller: ${activeController!!.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Live Analog Values
        if (isInputCaptureActive && inputEvents.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 Live Analog Values",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show recent analog events
                    val analogEvents = inputEvents.filter { 
                        it.data.contains("AXIS") || it.data.contains("stick") || it.data.contains("trigger")
                    }.takeLast(5)
                    
                    if (analogEvents.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 150.dp)
                        ) {
                            items(analogEvents) { event ->
                                Text(
                                    text = "• ${event.data}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Move analog sticks or triggers to see values",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Left Stick Settings
        EnhancedCalibrationSection(
            title = "🕹️ Left Stick",
            innerDeadZone = profile.analogSettings.leftStickDeadZoneInner,
            outerDeadZone = profile.analogSettings.leftStickDeadZoneOuter,
            sensitivity = profile.analogSettings.leftStickSensitivity,
            onInnerDeadZoneChange = { value ->
                onUpdateAnalogSettings(
                    profile.analogSettings.copy(leftStickDeadZoneInner = value)
                )
            },
            onOuterDeadZoneChange = { value ->
                onUpdateAnalogSettings(
                    profile.analogSettings.copy(leftStickDeadZoneOuter = value)
                )
            },
            onSensitivityChange = { value ->
                onUpdateAnalogSettings(
                    profile.analogSettings.copy(leftStickSensitivity = value)
                )
            }
        )
        
        // Right Stick Settings
        EnhancedCalibrationSection(
            title = "🕹️ Right Stick",
            innerDeadZone = profile.analogSettings.rightStickDeadZoneInner,
            outerDeadZone = profile.analogSettings.rightStickDeadZoneOuter,
            sensitivity = profile.analogSettings.rightStickSensitivity,
            onInnerDeadZoneChange = { value ->
                onUpdateAnalogSettings(
                    profile.analogSettings.copy(rightStickDeadZoneInner = value)
                )
            },
            onOuterDeadZoneChange = { value ->
                onUpdateAnalogSettings(
                    profile.analogSettings.copy(rightStickDeadZoneOuter = value)
                )
            },
            onSensitivityChange = { value ->
                onUpdateAnalogSettings(
                    profile.analogSettings.copy(rightStickSensitivity = value)
                )
            }
        )
        
        // Trigger Settings
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🎯 Trigger Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Left Trigger Actuation Point
                Text(text = "Left Trigger Actuation: ${(profile.analogSettings.leftTriggerActuation * 100).toInt()}%")
                Slider(
                    value = profile.analogSettings.leftTriggerActuation,
                    onValueChange = { value ->
                        onUpdateAnalogSettings(
                            profile.analogSettings.copy(leftTriggerActuation = value)
                        )
                    },
                    valueRange = 0f..1f
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Right Trigger Actuation Point
                Text(text = "Right Trigger Actuation: ${(profile.analogSettings.rightTriggerActuation * 100).toInt()}%")
                Slider(
                    value = profile.analogSettings.rightTriggerActuation,
                    onValueChange = { value ->
                        onUpdateAnalogSettings(
                            profile.analogSettings.copy(rightTriggerActuation = value)
                        )
                    },
                    valueRange = 0f..1f
                )
            }
        }
    }
}

@Composable
private fun EnhancedCalibrationSection(
    title: String,
    innerDeadZone: Float,
    outerDeadZone: Float,
    sensitivity: Float,
    onInnerDeadZoneChange: (Float) -> Unit,
    onOuterDeadZoneChange: (Float) -> Unit,
    onSensitivityChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Inner Dead Zone
            Text(text = "Inner Dead Zone: ${(innerDeadZone * 100).toInt()}%")
            Text(
                text = "Eliminates small unwanted movements",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = innerDeadZone,
                onValueChange = onInnerDeadZoneChange,
                valueRange = 0f..0.5f
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Outer Dead Zone
            Text(text = "Outer Dead Zone: ${(outerDeadZone * 100).toInt()}%")
            Text(
                text = "Ensures full range is reachable",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = outerDeadZone,
                onValueChange = onOuterDeadZoneChange,
                valueRange = 0.5f..1f
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sensitivity
            Text(text = "Sensitivity: ${(sensitivity * 100).toInt()}%")
            Text(
                text = "Controls response curve steepness",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = sensitivity,
                onValueChange = onSensitivityChange,
                valueRange = 0.1f..2f
            )
        }
    }
}

@Composable
private fun EnhancedMacroAssignmentTab(
    profile: com.nexus.controllerhub.data.model.ControllerProfile,
    controllerManager: ControllerManager,
    isRecording: Boolean,
    recordedMacro: List<ControllerManager.MacroEvent>,
    onUpdateMacroAssignment: (String, Long?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Macro Recording Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isRecording) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.primaryContainer
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
                    Text(
                        text = if (isRecording) "🔴 Recording Macro..." else "🎬 Macro Recorder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row {
                        if (isRecording) {
                            Button(
                                onClick = { controllerManager.stopMacroRecording() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Stop")
                            }
                        } else {
                            Button(
                                onClick = { controllerManager.startMacroRecording() }
                            ) {
                                Icon(Icons.Default.FiberManualRecord, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Record")
                            }
                        }
                        
                        if (recordedMacro.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { controllerManager.clearMacro() }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isRecording) 
                        "Press controller buttons to record a macro sequence" 
                    else if (recordedMacro.isNotEmpty())
                        "Macro recorded with ${recordedMacro.size} events"
                    else
                        "Click Record to start capturing controller input",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Recorded Macro Preview
        if (recordedMacro.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📝 Recorded Macro Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(recordedMacro.take(20)) { event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${event.type} ${event.button ?: ""} ${event.value?.let { "($it)" } ?: ""}".trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${event.timestamp}ms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                    
                    if (recordedMacro.size > 20) {
                        Text(
                            text = "... and ${recordedMacro.size - 20} more events",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Current Macro Assignments
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🎮 Current Macro Assignments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (profile.macroAssignments.isEmpty()) {
                    Text(
                        text = "No macro assignments configured",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    profile.macroAssignments.forEach { (button, macroId) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = button,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Macro ID: $macroId",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onUpdateMacroAssignment(button, null) }
                                ) {
                                    Icon(
                                        Icons.Default.Delete, 
                                        contentDescription = "Remove Assignment",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}