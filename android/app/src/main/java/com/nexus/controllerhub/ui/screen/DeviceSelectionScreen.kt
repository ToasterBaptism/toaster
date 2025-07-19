package com.nexus.controllerhub.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.controllerhub.util.ControllerDetector
import com.nexus.controllerhub.util.ControllerInputState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSelectionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val controllerDetector = remember { ControllerDetector(context) }
    val connectedControllers by controllerDetector.connectedControllers.collectAsState()
    val selectedDeviceId by ControllerInputState.selectedDeviceId.collectAsState()
    
    LaunchedEffect(Unit) {
        controllerDetector.startDetection()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            controllerDetector.stopDetection()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Controller Device") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Device Selection",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select a specific controller device to use with the app. If no device is selected, all connected controllers will be processed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            item {
                // Option to use all devices
                DeviceCard(
                    deviceName = "All Connected Controllers",
                    deviceType = "Use any connected controller",
                    controllerType = ControllerDetector.ControllerType.GENERIC,
                    connectionType = ControllerDetector.ConnectionType.UNKNOWN,
                    isSelected = selectedDeviceId == null,
                    onClick = {
                        ControllerInputState.setSelectedDevice(null)
                    }
                )
            }
            
            if (connectedControllers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Gamepad,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Controllers Detected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Connect a controller via Bluetooth or USB to see it here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                items(connectedControllers) { controller ->
                    DeviceCard(
                        deviceName = controller.name,
                        deviceType = "Device ID: ${controller.deviceId}",
                        controllerType = controller.controllerType,
                        connectionType = controller.connectionType,
                        isSelected = selectedDeviceId == controller.deviceId,
                        onClick = {
                            ControllerInputState.setSelectedDevice(controller.deviceId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    deviceName: String,
    deviceType: String,
    controllerType: ControllerDetector.ControllerType,
    connectionType: ControllerDetector.ConnectionType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Controller type icon
            Icon(
                imageVector = when (connectionType) {
                    ControllerDetector.ConnectionType.BLUETOOTH -> Icons.Default.Bluetooth
                    ControllerDetector.ConnectionType.USB -> Icons.Default.Usb
                    else -> Icons.Default.Gamepad
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = deviceType,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                // Controller type and connection type badges
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Badge(
                        text = controllerType.name,
                        color = getControllerTypeColor(controllerType)
                    )
                    
                    if (connectionType != ControllerDetector.ConnectionType.UNKNOWN) {
                        Badge(
                            text = connectionType.name,
                            color = getConnectionTypeColor(connectionType)
                        )
                    }
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun Badge(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

private fun getControllerTypeColor(type: ControllerDetector.ControllerType): Color {
    return when (type) {
        ControllerDetector.ControllerType.XBOX -> Color(0xFF107C10) // Xbox green
        ControllerDetector.ControllerType.PLAYSTATION -> Color(0xFF0070D1) // PlayStation blue
        ControllerDetector.ControllerType.GAMESIR -> Color(0xFFFF6B35) // GameSir orange
        ControllerDetector.ControllerType.GENERIC -> Color(0xFF6B73FF) // Generic purple
        ControllerDetector.ControllerType.UNKNOWN -> Color.Gray
    }
}

private fun getConnectionTypeColor(type: ControllerDetector.ConnectionType): Color {
    return when (type) {
        ControllerDetector.ConnectionType.BLUETOOTH -> Color(0xFF0082FC) // Bluetooth blue
        ControllerDetector.ConnectionType.USB -> Color(0xFF34A853) // USB green
        ControllerDetector.ConnectionType.UNKNOWN -> Color.Gray
    }
}