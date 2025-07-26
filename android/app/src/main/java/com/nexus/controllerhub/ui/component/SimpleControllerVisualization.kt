package com.nexus.controllerhub.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.controllerhub.core.ControllerInputSystem

@Composable
fun SimpleControllerVisualization(
    controller: ControllerInputSystem.DetectedController,
    buttonStates: Map<Int, Boolean>,
    analogStates: Map<Int, Float>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = controller.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple controller layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top row - shoulder buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(120.dp)
                ) {
                    ControllerButton("L1", buttonStates[102] == true) // KEYCODE_BUTTON_L1
                    ControllerButton("R1", buttonStates[103] == true) // KEYCODE_BUTTON_R1
                }
                
                // Middle row - sticks and face buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left stick area
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Left Stick", fontSize = 10.sp)
                        AnalogStick(
                            x = analogStates[0] ?: 0f, // AXIS_X
                            y = analogStates[1] ?: 0f  // AXIS_Y
                        )
                    }
                    
                    // Face buttons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ControllerButton("Y", buttonStates[100] == true) // KEYCODE_BUTTON_Y
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ControllerButton("X", buttonStates[99] == true)  // KEYCODE_BUTTON_X
                            ControllerButton("B", buttonStates[97] == true)  // KEYCODE_BUTTON_B
                        }
                        ControllerButton("A", buttonStates[96] == true) // KEYCODE_BUTTON_A
                    }
                }
                
                // Bottom row - D-pad and right stick
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // D-pad
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("D-Pad", fontSize = 10.sp)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ControllerButton("↑", buttonStates[19] == true) // KEYCODE_DPAD_UP
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ControllerButton("←", buttonStates[21] == true) // KEYCODE_DPAD_LEFT
                                ControllerButton("→", buttonStates[22] == true) // KEYCODE_DPAD_RIGHT
                            }
                            ControllerButton("↓", buttonStates[20] == true) // KEYCODE_DPAD_DOWN
                        }
                    }
                    
                    // Right stick
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Right Stick", fontSize = 10.sp)
                        AnalogStick(
                            x = analogStates[11] ?: 0f, // AXIS_Z
                            y = analogStates[14] ?: 0f  // AXIS_RZ
                        )
                    }
                }
                
                // Triggers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(120.dp)
                ) {
                    TriggerIndicator("L2", analogStates[17] ?: 0f) // AXIS_LTRIGGER
                    TriggerIndicator("R2", analogStates[18] ?: 0f) // AXIS_RTRIGGER
                }
            }
        }
    }
}

@Composable
private fun ControllerButton(
    label: String,
    isPressed: Boolean
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = if (isPressed) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = if (isPressed) Color(0xFF2E7D32) else Color(0xFFBDBDBD),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Normal,
            color = if (isPressed) Color.White else Color.Black
        )
    }
}

@Composable
private fun AnalogStick(
    x: Float,
    y: Float
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(
                color = Color(0xFFE0E0E0),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color(0xFFBDBDBD),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Stick position indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .offset(
                    x = (x * 20).dp,
                    y = (y * 20).dp
                )
                .background(
                    color = if (x != 0f || y != 0f) Color(0xFF2196F3) else Color(0xFF757575),
                    shape = CircleShape
                )
        )
        
        // Center dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    color = Color(0xFF424242),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun TriggerIndicator(
    label: String,
    value: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(8.dp)
                .background(
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value)
                    .background(
                        color = if (value > 0f) Color(0xFF4CAF50) else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
        Text(
            text = "%.2f".format(value),
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}