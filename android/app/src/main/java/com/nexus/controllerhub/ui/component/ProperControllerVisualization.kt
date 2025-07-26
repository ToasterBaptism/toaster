package com.nexus.controllerhub.ui.component

import com.nexus.controllerhub.core.ControllerInputSystem
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.KeyEvent
import android.view.MotionEvent
import com.nexus.controllerhub.controller.ControllerManager
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProperControllerVisualization(
    controllerManager: ControllerManager,
    modifier: Modifier = Modifier
) {
    val connectedControllers by controllerManager.controllers.collectAsState()
    val activeController by controllerManager.activeController.collectAsState()
    val buttonStates by controllerManager.buttonStates.collectAsState()
    val leftStick by controllerManager.leftStick.collectAsState()
    val rightStick by controllerManager.rightStick.collectAsState()
    val leftTrigger by controllerManager.leftTrigger.collectAsState()
    val rightTrigger by controllerManager.rightTrigger.collectAsState()

    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "🎮 Live Controller Visualization",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (connectedControllers.isEmpty()) {
                // No controllers
                NoControllersMessage()
            } else {
                activeController?.let { controller ->
                    // Controller info
                    ControllerInfoSection(controller)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Main controller visualization
                    ControllerVisualizationLayout(
                        buttonStates = buttonStates,
                        leftStick = leftStick,
                        rightStick = rightStick,
                        leftTrigger = leftTrigger,
                        rightTrigger = rightTrigger,
                        controller = controller
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Live input data
                    LiveInputDataSection(buttonStates, leftStick, rightStick, leftTrigger, rightTrigger)
                }
            }
        }
    }
}

@Composable
private fun NoControllersMessage() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "❌ No Controllers Detected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Please connect a controller via USB or Bluetooth",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Supported: Xbox, PlayStation, Generic HID controllers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ControllerInfoSection(controller: ControllerManager.Controller) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📋 Controller Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoItem("Name", controller.name)
                    InfoItem("Device ID", controller.deviceId.toString())
                    InfoItem("Type", controller.type.name)
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    InfoItem("Vendor ID", "0x${controller.vendorId.toString(16).uppercase()}")
                    InfoItem("Product ID", "0x${controller.productId.toString(16).uppercase()}")
                    InfoItem("Connection", "Connected")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                InfoChip("${controller.supportedAxes.size} Axes", controller.supportedAxes.isNotEmpty())
                Spacer(modifier = Modifier.width(8.dp))
                InfoChip("${controller.supportedButtons.size} Buttons", controller.supportedButtons.isNotEmpty())
                Spacer(modifier = Modifier.width(8.dp))
                InfoChip("Vibration", controller.hasVibrator)
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun InfoChip(text: String, enabled: Boolean) {
    Surface(
        color = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ControllerVisualizationLayout(
    buttonStates: Map<String, Boolean>,
    leftStick: ControllerManager.StickState,
    rightStick: ControllerManager.StickState,
    leftTrigger: Float,
    rightTrigger: Float,
    controller: ControllerManager.Controller
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(20.dp)
        ) {
            // Left side - D-Pad and Left Stick
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                // D-Pad
                DPadVisualization(
                    upPressed = buttonStates.getOrDefault("DPAD_UP", false),
                    downPressed = buttonStates.getOrDefault("DPAD_DOWN", false),
                    leftPressed = buttonStates.getOrDefault("DPAD_LEFT", false),
                    rightPressed = buttonStates.getOrDefault("DPAD_RIGHT", false)
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Left Analog Stick
                AnalogStickVisualization(
                    xValue = leftStick.x,
                    yValue = leftStick.y,
                    label = "L",
                    pressed = buttonStates.getOrDefault("LS", false)
                )
            }
            
            // Right side - Face Buttons and Right Stick
            Column(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                // Face Buttons (A, B, X, Y)
                FaceButtonsVisualization(
                    aPressed = buttonStates.getOrDefault("A", false),
                    bPressed = buttonStates.getOrDefault("B", false),
                    xPressed = buttonStates.getOrDefault("X", false),
                    yPressed = buttonStates.getOrDefault("Y", false)
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Right Analog Stick
                AnalogStickVisualization(
                    xValue = rightStick.x,
                    yValue = rightStick.y,
                    label = "R",
                    pressed = buttonStates.getOrDefault("RS", false)
                )
            }
            
            // Top - Shoulder Buttons and Triggers
            ShoulderButtonsVisualization(
                modifier = Modifier.align(Alignment.TopCenter),
                l1Pressed = buttonStates.getOrDefault("L1", false),
                r1Pressed = buttonStates.getOrDefault("R1", false),
                l2Value = leftTrigger,
                r2Value = rightTrigger,
                l2Pressed = buttonStates.getOrDefault("L2", false),
                r2Pressed = buttonStates.getOrDefault("R2", false)
            )
            
            // Center - Menu Buttons
            CenterButtonsVisualization(
                modifier = Modifier.align(Alignment.Center),
                startPressed = buttonStates.getOrDefault("START", false),
                selectPressed = buttonStates.getOrDefault("SELECT", false)
            )
        }
    }
}

@Composable
private fun DPadVisualization(
    upPressed: Boolean,
    downPressed: Boolean,
    leftPressed: Boolean,
    rightPressed: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "D-PAD",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Up
            DirectionalButton("↑", upPressed)
            
            Row {
                // Left
                DirectionalButton("←", leftPressed)
                Spacer(modifier = Modifier.width(32.dp))
                // Right
                DirectionalButton("→", rightPressed)
            }
            
            // Down
            DirectionalButton("↓", downPressed)
        }
    }
}

@Composable
private fun DirectionalButton(symbol: String, pressed: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                if (pressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                if (pressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (pressed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AnalogStickVisualization(
    xValue: Float,
    yValue: Float,
    label: String,
    pressed: Boolean
) {
    val density = LocalDensity.current
    val stickSize = 80.dp
    val stickRadius = with(density) { (stickSize / 2).toPx() }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$label STICK",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier.size(stickSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw outer circle (boundary)
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.4f),
                    radius = stickRadius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
                
                // Draw dead zone circle
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    radius = stickRadius * 0.2f,
                    center = center
                )
                
                // Calculate stick position
                val stickX = center.x + (xValue * stickRadius * 0.85f)
                val stickY = center.y + (yValue * stickRadius * 0.85f)
                
                // Draw stick position
                val stickColor = when {
                    pressed -> Color.Red
                    abs(xValue) > 0.1f || abs(yValue) > 0.1f -> Color.Blue.copy(alpha = 0.8f)
                    else -> Color.Gray
                }
                
                drawCircle(
                    color = stickColor,
                    radius = stickRadius * 0.25f,
                    center = Offset(stickX, stickY)
                )
                
                // Draw center dot
                drawCircle(
                    color = Color.White,
                    radius = stickRadius * 0.08f,
                    center = Offset(stickX, stickY)
                )
            }
            
            // Stick label
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Position values
        Text(
            text = "X: ${String.format("%.2f", xValue)}\nY: ${String.format("%.2f", yValue)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FaceButtonsVisualization(
    aPressed: Boolean,
    bPressed: Boolean,
    xPressed: Boolean,
    yPressed: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FACE BUTTONS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Y
            FaceButton("Y", yPressed, Color(0xFF4CAF50)) // Green
            
            Row {
                // X
                FaceButton("X", xPressed, Color(0xFF2196F3)) // Blue
                Spacer(modifier = Modifier.width(16.dp))
                // B
                FaceButton("B", bPressed, Color(0xFFF44336)) // Red
            }
            
            // A
            FaceButton("A", aPressed, Color(0xFF4CAF50)) // Green
        }
    }
}

@Composable
private fun FaceButton(label: String, pressed: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (pressed) color else color.copy(alpha = 0.3f)
            )
            .border(
                2.dp,
                if (pressed) color else color.copy(alpha = 0.6f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (pressed) Color.White else color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ShoulderButtonsVisualization(
    modifier: Modifier = Modifier,
    l1Pressed: Boolean,
    r1Pressed: Boolean,
    l2Value: Float,
    r2Value: Float,
    l2Pressed: Boolean,
    r2Pressed: Boolean
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left shoulder
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ShoulderButton("L1", l1Pressed)
            Spacer(modifier = Modifier.height(8.dp))
            TriggerVisualization("L2", l2Value, l2Pressed)
        }
        
        // Right shoulder
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ShoulderButton("R1", r1Pressed)
            Spacer(modifier = Modifier.height(8.dp))
            TriggerVisualization("R2", r2Value, r2Pressed)
        }
    }
}

@Composable
private fun ShoulderButton(label: String, pressed: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 50.dp, height = 24.dp)
            .background(
                if (pressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (pressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (pressed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TriggerVisualization(label: String, value: Float, pressed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(80.dp)
                .background(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(6.dp)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(6.dp)
                )
        ) {
            // Trigger fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(value.coerceIn(0f, 1f))
                    .align(Alignment.BottomCenter)
                    .background(
                        if (pressed || value > 0.1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        RoundedCornerShape(6.dp)
                    )
            )
            
            // Label
            Text(
                text = label,
                modifier = Modifier.align(Alignment.Center),
                color = if (value > 0.5f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CenterButtonsVisualization(
    modifier: Modifier = Modifier,
    startPressed: Boolean,
    selectPressed: Boolean
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        MenuButton("SELECT", selectPressed)
        MenuButton("START", startPressed)
    }
}

@Composable
private fun MenuButton(label: String, pressed: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 60.dp, height = 20.dp)
            .background(
                if (pressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (pressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (pressed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LiveInputDataSection(
    buttonStates: Map<String, Boolean>,
    leftStick: ControllerManager.StickState,
    rightStick: ControllerManager.StickState,
    leftTrigger: Float,
    rightTrigger: Float
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Live Input Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Active buttons
            val activeButtons = buttonStates.filter { it.value }
            if (activeButtons.isNotEmpty()) {
                Text(
                    text = "🔴 Active Buttons:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = activeButtons.keys.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Active analog inputs
            val hasAnalogInput = abs(leftStick.x) > 0.05f || abs(leftStick.y) > 0.05f ||
                               abs(rightStick.x) > 0.05f || abs(rightStick.y) > 0.05f ||
                               leftTrigger > 0.05f || rightTrigger > 0.05f
            
            if (hasAnalogInput) {
                Text(
                    text = "🎯 Active Analog:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                if (abs(leftStick.x) > 0.05f || abs(leftStick.y) > 0.05f) {
                    Text(
                        text = "Left Stick: X=${String.format("%.3f", leftStick.x)}, Y=${String.format("%.3f", leftStick.y)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                if (abs(rightStick.x) > 0.05f || abs(rightStick.y) > 0.05f) {
                    Text(
                        text = "Right Stick: X=${String.format("%.3f", rightStick.x)}, Y=${String.format("%.3f", rightStick.y)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                if (leftTrigger > 0.05f) {
                    Text(
                        text = "Left Trigger: ${String.format("%.3f", leftTrigger)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                if (rightTrigger > 0.05f) {
                    Text(
                        text = "Right Trigger: ${String.format("%.3f", rightTrigger)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            if (activeButtons.isEmpty() && !hasAnalogInput) {
                Text(
                    text = "⏸️ No input detected - press buttons or move sticks to see live data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun getButtonName(keyCode: Int): String {
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
        else -> "BTN_$keyCode"
    }
}

private fun getAxisName(axis: Int): String {
    return when (axis) {
        MotionEvent.AXIS_X -> "L-STICK-X"
        MotionEvent.AXIS_Y -> "L-STICK-Y"
        MotionEvent.AXIS_Z -> "R-STICK-X"
        MotionEvent.AXIS_RZ -> "R-STICK-Y"
        MotionEvent.AXIS_LTRIGGER -> "L-TRIGGER"
        MotionEvent.AXIS_RTRIGGER -> "R-TRIGGER"
        MotionEvent.AXIS_HAT_X -> "DPAD-X"
        MotionEvent.AXIS_HAT_Y -> "DPAD-Y"
        else -> "AXIS_$axis"
    }
}