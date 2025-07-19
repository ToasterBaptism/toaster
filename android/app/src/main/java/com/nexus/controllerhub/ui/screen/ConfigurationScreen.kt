package com.nexus.controllerhub.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.nexus.controllerhub.ui.component.ControllerVisualization
import com.nexus.controllerhub.ui.viewmodel.ConfigurationViewModel
import com.nexus.controllerhub.ui.viewmodel.ConfigurationViewModelFactory
import com.nexus.controllerhub.util.ControllerInputState
import com.nexus.controllerhub.util.ControllerDetector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    profileId: Long,
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
    
    // Real-time controller input state
    val pressedButtons by ControllerInputState.pressedButtons.collectAsState()
    val leftStickPosition by ControllerInputState.leftStickPosition.collectAsState()
    val rightStickPosition by ControllerInputState.rightStickPosition.collectAsState()
    val leftTrigger by ControllerInputState.leftTrigger.collectAsState()
    val rightTrigger by ControllerInputState.rightTrigger.collectAsState()
    val selectedDeviceId by ControllerInputState.selectedDeviceId.collectAsState()
    
    // Controller detection for determining controller type
    val controllerDetector = remember { ControllerDetector(context) }
    val connectedControllers by controllerDetector.connectedControllers.collectAsState()
    
    LaunchedEffect(Unit) {
        controllerDetector.startDetection()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            controllerDetector.stopDetection()
        }
    }
    
    // Determine controller type based on selected device or first connected controller
    val controllerType = if (selectedDeviceId != null) {
        connectedControllers.find { it.deviceId == selectedDeviceId }?.controllerType
            ?: ControllerDetector.ControllerType.GENERIC
    } else {
        connectedControllers.firstOrNull()?.controllerType
            ?: ControllerDetector.ControllerType.GENERIC
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
                0 -> ButtonMappingTab(
                    profile = profile!!,
                    onUpdateMapping = viewModel::updateButtonMapping,
                    pressedButtons = pressedButtons,
                    leftStickPosition = leftStickPosition,
                    rightStickPosition = rightStickPosition,
                    leftTrigger = leftTrigger,
                    rightTrigger = rightTrigger,
                    controllerType = controllerType
                )
                1 -> CalibrationTab(
                    profile = profile!!,
                    onUpdateAnalogSettings = viewModel::updateAnalogSettings
                )
                2 -> MacroAssignmentTab(
                    profile = profile!!,
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
private fun ButtonMappingTab(
    profile: com.nexus.controllerhub.data.model.ControllerProfile,
    onUpdateMapping: (String, String) -> Unit,
    pressedButtons: Set<String>,
    leftStickPosition: Pair<Float, Float>,
    rightStickPosition: Pair<Float, Float>,
    leftTrigger: Float,
    rightTrigger: Float,
    controllerType: ControllerDetector.ControllerType
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tap a button to remap it",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Controller Visualization with real-time feedback
        ControllerVisualization(
            profile = profile,
            onButtonClick = { buttonCode ->
                // Show remapping dialog
                // This would open a dialog to select new mapping
            },
            pressedButtons = pressedButtons,
            leftStickPosition = leftStickPosition,
            rightStickPosition = rightStickPosition,
            leftTrigger = leftTrigger,
            rightTrigger = rightTrigger,
            controllerType = controllerType
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Current Mappings List
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Current Mappings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (profile.buttonMappings.isEmpty()) {
                    Text(
                        text = "No custom mappings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    profile.buttonMappings.forEach { (from, to) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = from)
                            Text(text = "→")
                            Text(text = to)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalibrationTab(
    profile: com.nexus.controllerhub.data.model.ControllerProfile,
    onUpdateAnalogSettings: (com.nexus.controllerhub.data.model.AnalogSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Analog Stick & Trigger Calibration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Left Stick Settings
        CalibrationSection(
            title = "Left Stick",
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Right Stick Settings
        CalibrationSection(
            title = "Right Stick",
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
    }
}

@Composable
private fun CalibrationSection(
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
            Slider(
                value = innerDeadZone,
                onValueChange = onInnerDeadZoneChange,
                valueRange = 0f..0.5f
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Outer Dead Zone
            Text(text = "Outer Dead Zone: ${(outerDeadZone * 100).toInt()}%")
            Slider(
                value = outerDeadZone,
                onValueChange = onOuterDeadZoneChange,
                valueRange = 0.5f..1f
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sensitivity
            Text(text = "Sensitivity: ${(sensitivity * 100).toInt()}%")
            Slider(
                value = sensitivity,
                onValueChange = onSensitivityChange,
                valueRange = 0.1f..2f
            )
        }
    }
}

@Composable
private fun MacroAssignmentTab(
    profile: com.nexus.controllerhub.data.model.ControllerProfile,
    onUpdateMacroAssignment: (String, Long?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Macro Assignments",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (profile.macroAssignments.isEmpty()) {
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
                        text = "No macro assignments",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Show assigned macros
            profile.macroAssignments.forEach { (button, macroId) ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
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
                            Icon(Icons.Default.Delete, contentDescription = "Remove Assignment")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}