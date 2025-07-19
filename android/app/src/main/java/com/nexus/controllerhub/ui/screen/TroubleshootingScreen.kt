package com.nexus.controllerhub.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.InputDevice
import com.nexus.controllerhub.service.ControllerAccessibilityService
import com.nexus.controllerhub.util.InputDiagnostics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TroubleshootingScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var inputDevices by remember { mutableStateOf<List<InputDevice>>(emptyList()) }
    var touchInputAvailable by remember { mutableStateOf(false) }
    var serviceEnabled by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Get all input devices
        val deviceIds = InputDevice.getDeviceIds()
        inputDevices = deviceIds.toList().mapNotNull { deviceId -> InputDevice.getDevice(deviceId) }
        
        // Check touch input availability
        touchInputAvailable = InputDiagnostics.verifyTouchInputAvailable(context)
        
        // Check service status
        serviceEnabled = ControllerAccessibilityService.isServiceEnabled()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Input Troubleshooting") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ServiceStatusCard(serviceEnabled = serviceEnabled)
            }
            
            item {
                TouchInputStatusCard(touchInputAvailable = touchInputAvailable)
            }
            
            item {
                Text(
                    text = "Connected Input Devices",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(inputDevices) { device ->
                InputDeviceCard(device = device)
            }
            
            item {
                TroubleshootingTipsCard()
            }
        }
    }
}

@Composable
private fun ServiceStatusCard(serviceEnabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (serviceEnabled) 
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
                    Icons.Default.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Accessibility Service Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (serviceEnabled) "✅ Service is running" else "❌ Service is not enabled",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (!serviceEnabled) {
                Text(
                    text = "Go to Settings → Accessibility to enable the service",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun TouchInputStatusCard(touchInputAvailable: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (touchInputAvailable) 
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
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Touch Input Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (touchInputAvailable) 
                    "✅ Touch input devices detected" 
                else 
                    "❌ No touch input devices found",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Touch input should work normally even when the accessibility service is enabled",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InputDeviceCard(device: InputDevice) {
    val isController = (device.sources and InputDevice.SOURCE_GAMEPAD) != 0 ||
                      (device.sources and InputDevice.SOURCE_JOYSTICK) != 0
    val isTouchscreen = (device.sources and InputDevice.SOURCE_TOUCHSCREEN) != 0
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isController) Icons.Default.Gamepad else Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isController) MaterialTheme.colorScheme.primary 
                          else MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = device.name ?: "Unknown Device",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "ID: ${device.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Sources: ${getSourcesString(device.sources)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isController) {
                Text(
                    text = "🎮 Will be processed by the app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (isTouchscreen) {
                Text(
                    text = "👆 Touch input - will pass through normally",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun TroubleshootingTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Troubleshooting Tips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val tips = listOf(
                "If touch input stops working, disable and re-enable the accessibility service",
                "The app only processes controller inputs - touch events are passed through",
                "Check Android logs (adb logcat) for detailed input event information",
                "Restart the app if controller detection isn't working properly",
                "Some devices may require specific controller drivers or apps"
            )
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun getSourcesString(sources: Int): String {
    val sourceList = mutableListOf<String>()
    
    if ((sources and InputDevice.SOURCE_KEYBOARD) != 0) sourceList.add("KEYBOARD")
    if ((sources and InputDevice.SOURCE_DPAD) != 0) sourceList.add("DPAD")
    if ((sources and InputDevice.SOURCE_GAMEPAD) != 0) sourceList.add("GAMEPAD")
    if ((sources and InputDevice.SOURCE_TOUCHSCREEN) != 0) sourceList.add("TOUCHSCREEN")
    if ((sources and InputDevice.SOURCE_MOUSE) != 0) sourceList.add("MOUSE")
    if ((sources and InputDevice.SOURCE_JOYSTICK) != 0) sourceList.add("JOYSTICK")
    
    return sourceList.joinToString(", ")
}