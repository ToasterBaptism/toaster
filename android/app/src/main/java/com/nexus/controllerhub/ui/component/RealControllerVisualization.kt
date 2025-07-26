package com.nexus.controllerhub.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.KeyEvent
import com.nexus.controllerhub.input.RealControllerInputManager
import kotlin.math.abs

@Composable
fun RealControllerVisualization(
    inputManager: RealControllerInputManager,
    modifier: Modifier = Modifier
) {
    val buttonStates by inputManager.buttonStates.collectAsState()
    val analogStates by inputManager.analogStates.collectAsState()
    val connectedControllers by inputManager.connectedControllers.collectAsState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎮 Real-Time Controller Visualization",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (connectedControllers.isEmpty()) {
                Text(
                    text = "No controllers detected. Please connect a controller.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Show controller info
                connectedControllers.firstOrNull()?.let { controller ->
                    Text(
                        text = "Connected: ${controller.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Device ID: ${controller.deviceId} | Vendor: 0x${controller.vendorId.toString(16).uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Controller visualization
                ControllerLayout(
                    buttonStates = buttonStates,
                    analogStates = analogStates
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Real-time data display
                RealTimeDataDisplay(
                    buttonStates = buttonStates,
                    analogStates = analogStates
                )
            }
        }
    }
}

@Composable
private fun ControllerLayout(
    buttonStates: Map<Int, Boolean>,
    analogStates: Map<String, Float>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                Color.Gray.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Left analog stick
        AnalogStickVisualization(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 40.dp, y = 40.dp),
            xValue = analogStates["LEFT_STICK_X"] ?: 0f,
            yValue = analogStates["LEFT_STICK_Y"] ?: 0f,
            label = "L"
        )
        
        // Right analog stick
        AnalogStickVisualization(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-40).dp, y = 40.dp),
            xValue = analogStates["RIGHT_STICK_X"] ?: 0f,
            yValue = analogStates["RIGHT_STICK_Y"] ?: 0f,
            label = "R"
        )
        
        // D-Pad
        DPadVisualization(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 40.dp, y = (-40).dp),
            upPressed = buttonStates[KeyEvent.KEYCODE_DPAD_UP] == true,
            downPressed = buttonStates[KeyEvent.KEYCODE_DPAD_DOWN] == true,
            leftPressed = buttonStates[KeyEvent.KEYCODE_DPAD_LEFT] == true,
            rightPressed = buttonStates[KeyEvent.KEYCODE_DPAD_RIGHT] == true
        )
        
        // Face buttons (A, B, X, Y)
        FaceButtonsVisualization(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-40).dp, y = (-40).dp),
            aPressed = buttonStates[KeyEvent.KEYCODE_BUTTON_A] == true,
            bPressed = buttonStates[KeyEvent.KEYCODE_BUTTON_B] == true,
            xPressed = buttonStates[KeyEvent.KEYCODE_BUTTON_X] == true,
            yPressed = buttonStates[KeyEvent.KEYCODE_BUTTON_Y] == true
        )
        
        // Shoulder buttons
        ShoulderButtonsVisualization(
            modifier = Modifier.align(Alignment.TopCenter),
            l1Pressed = buttonStates[KeyEvent.KEYCODE_BUTTON_L1] == true,
            r1Pressed = buttonStates[KeyEvent.KEYCODE_BUTTON_R1] == true,
            l2Value = analogStates["LEFT_TRIGGER"] ?: 0f,
            r2Value = analogStates["RIGHT_TRIGGER"] ?: 0f
        )
        
        // Center buttons
        CenterButtonsVisualization(
            modifier = Modifier.align(Alignment.Center),
            startPressed = buttonStates[KeyEvent.KEYCODE_BUTTON_START] == true,
            selectPressed = buttonStates[KeyEvent.KEYCODE_BUTTON_SELECT] == true
        )
    }
}

@Composable
private fun AnalogStickVisualization(
    modifier: Modifier = Modifier,
    xValue: Float,
    yValue: Float,
    label: String
) {
    val density = LocalDensity.current
    val stickSize = 60.dp
    val stickRadius = with(density) { (stickSize / 2).toPx() }
    
    Box(
        modifier = modifier.size(stickSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw outer circle (dead zone)
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = stickRadius,
                center = center
            )
            
            // Draw inner circle (stick position)
            val stickX = center.x + (xValue * stickRadius * 0.8f)
            val stickY = center.y + (yValue * stickRadius * 0.8f)
            
            drawCircle(
                color = if (abs(xValue) > 0.1f || abs(yValue) > 0.1f) 
                    Color.Red else Color.Blue,
                radius = stickRadius * 0.3f,
                center = Offset(stickX, stickY)
            )
        }
        
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DPadVisualization(
    modifier: Modifier = Modifier,
    upPressed: Boolean,
    downPressed: Boolean,
    leftPressed: Boolean,
    rightPressed: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up
        Box(
            modifier = Modifier
                .size(24.dp, 16.dp)
                .background(
                    if (upPressed) Color.Red else Color.Gray.copy(alpha = 0.3f),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("↑", color = Color.White, fontSize = 10.sp)
        }
        
        Row {
            // Left
            Box(
                modifier = Modifier
                    .size(16.dp, 24.dp)
                    .background(
                        if (leftPressed) Color.Red else Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("←", color = Color.White, fontSize = 10.sp)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Right
            Box(
                modifier = Modifier
                    .size(16.dp, 24.dp)
                    .background(
                        if (rightPressed) Color.Red else Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("→", color = Color.White, fontSize = 10.sp)
            }
        }
        
        // Down
        Box(
            modifier = Modifier
                .size(24.dp, 16.dp)
                .background(
                    if (downPressed) Color.Red else Color.Gray.copy(alpha = 0.3f),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("↓", color = Color.White, fontSize = 10.sp)
        }
    }
}

@Composable
private fun FaceButtonsVisualization(
    modifier: Modifier = Modifier,
    aPressed: Boolean,
    bPressed: Boolean,
    xPressed: Boolean,
    yPressed: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Y
        CircleButton("Y", yPressed)
        
        Row {
            // X
            CircleButton("X", xPressed)
            Spacer(modifier = Modifier.width(8.dp))
            // B
            CircleButton("B", bPressed)
        }
        
        // A
        CircleButton("A", aPressed)
    }
}

@Composable
private fun CircleButton(
    label: String,
    pressed: Boolean
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                if (pressed) Color.Red else Color.Gray.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
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
    r2Value: Float
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // L1
            Box(
                modifier = Modifier
                    .size(40.dp, 20.dp)
                    .background(
                        if (l1Pressed) Color.Red else Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("L1", color = Color.White, fontSize = 10.sp)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // L2 (trigger)
            TriggerVisualization("L2", l2Value)
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // R1
            Box(
                modifier = Modifier
                    .size(40.dp, 20.dp)
                    .background(
                        if (r1Pressed) Color.Red else Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("R1", color = Color.White, fontSize = 10.sp)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // R2 (trigger)
            TriggerVisualization("R2", r2Value)
        }
    }
}

@Composable
private fun TriggerVisualization(
    label: String,
    value: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(60.dp)
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(value.coerceIn(0f, 1f))
                    .align(Alignment.BottomCenter)
                    .background(
                        if (value > 0.1f) Color.Red else Color.Blue,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
        
        Text(
            text = "$label\n${(value * 100).toInt()}%",
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onSurface
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
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp, 16.dp)
                .background(
                    if (selectPressed) Color.Red else Color.Gray.copy(alpha = 0.3f),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("SEL", color = Color.White, fontSize = 8.sp)
        }
        
        Box(
            modifier = Modifier
                .size(24.dp, 16.dp)
                .background(
                    if (startPressed) Color.Red else Color.Gray.copy(alpha = 0.3f),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("START", color = Color.White, fontSize = 6.sp)
        }
    }
}

@Composable
private fun RealTimeDataDisplay(
    buttonStates: Map<Int, Boolean>,
    analogStates: Map<String, Float>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Real-Time Input Data",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Active buttons
            val activeButtons = buttonStates.filter { it.value }
            if (activeButtons.isNotEmpty()) {
                Text(
                    text = "Active Buttons: ${activeButtons.keys.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
            
            // Active analog inputs
            val activeAnalog = analogStates.filter { abs(it.value) > 0.1f }
            if (activeAnalog.isNotEmpty()) {
                for ((axis, value) in activeAnalog) {
                    Text(
                        text = "$axis: ${String.format("%.3f", value)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Blue
                    )
                }
            }
            
            if (activeButtons.isEmpty() && activeAnalog.isEmpty()) {
                Text(
                    text = "No input detected - press buttons or move sticks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}