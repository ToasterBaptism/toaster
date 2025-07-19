package com.nexus.controllerhub.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.controllerhub.data.model.ControllerLayout
import com.nexus.controllerhub.data.model.ControllerProfile
import com.nexus.controllerhub.util.ControllerDetector
import com.nexus.controllerhub.ui.theme.*

@Composable
fun ControllerVisualization(
    profile: ControllerProfile,
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    pressedButtons: Set<String> = emptySet(),
    leftStickPosition: Pair<Float, Float> = Pair(0f, 0f),
    rightStickPosition: Pair<Float, Float> = Pair(0f, 0f),
    leftTrigger: Float = 0f,
    rightTrigger: Float = 0f,
    controllerType: ControllerDetector.ControllerType = ControllerDetector.ControllerType.GENERIC
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp)
    ) {
        when (controllerType) {
            ControllerDetector.ControllerType.XBOX -> XboxControllerLayout(
                profile = profile,
                onButtonClick = onButtonClick,
                pressedButtons = pressedButtons,
                leftStickPosition = leftStickPosition,
                rightStickPosition = rightStickPosition,
                leftTrigger = leftTrigger,
                rightTrigger = rightTrigger
            )
            ControllerDetector.ControllerType.PLAYSTATION -> PlayStationControllerLayout(
                profile = profile,
                onButtonClick = onButtonClick,
                pressedButtons = pressedButtons,
                leftStickPosition = leftStickPosition,
                rightStickPosition = rightStickPosition,
                leftTrigger = leftTrigger,
                rightTrigger = rightTrigger
            )
            ControllerDetector.ControllerType.GAMESIR -> GameSirControllerLayout(
                profile = profile,
                onButtonClick = onButtonClick,
                pressedButtons = pressedButtons,
                leftStickPosition = leftStickPosition,
                rightStickPosition = rightStickPosition,
                leftTrigger = leftTrigger,
                rightTrigger = rightTrigger
            )
            else -> GenericControllerLayout(
                profile = profile,
                onButtonClick = onButtonClick,
                pressedButtons = pressedButtons,
                leftStickPosition = leftStickPosition,
                rightStickPosition = rightStickPosition,
                leftTrigger = leftTrigger,
                rightTrigger = rightTrigger
            )
        }
    }
}

@Composable
private fun XboxControllerLayout(
    profile: ControllerProfile,
    onButtonClick: (String) -> Unit,
    pressedButtons: Set<String>,
    leftStickPosition: Pair<Float, Float>,
    rightStickPosition: Pair<Float, Float>,
    leftTrigger: Float,
    rightTrigger: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Controller body background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.DarkGray,
                    RoundedCornerShape(24.dp)
                )
        )
        
        // Left analog stick
        AnalogStick(
            modifier = Modifier
                .offset(x = 60.dp, y = 180.dp)
                .size(80.dp),
            position = leftStickPosition,
            isPressed = pressedButtons.contains("BUTTON_THUMBL"),
            onClick = { onButtonClick("BUTTON_THUMBL") }
        )
        
        // Right analog stick
        AnalogStick(
            modifier = Modifier
                .offset(x = 220.dp, y = 220.dp)
                .size(80.dp),
            position = rightStickPosition,
            isPressed = pressedButtons.contains("BUTTON_THUMBR"),
            onClick = { onButtonClick("BUTTON_THUMBR") }
        )
        
        // D-Pad
        DPad(
            modifier = Modifier.offset(x = 40.dp, y = 100.dp),
            pressedButtons = pressedButtons,
            onButtonClick = onButtonClick
        )
        
        // Face buttons (A, B, X, Y)
        FaceButtons(
            modifier = Modifier.offset(x = 240.dp, y = 100.dp),
            pressedButtons = pressedButtons,
            onButtonClick = onButtonClick
        )
        
        // Shoulder buttons
        ShoulderButton(
            modifier = Modifier.offset(x = 40.dp, y = 20.dp),
            label = "LB",
            isPressed = pressedButtons.contains("BUTTON_L1"),
            onClick = { onButtonClick("BUTTON_L1") }
        )
        
        ShoulderButton(
            modifier = Modifier.offset(x = 260.dp, y = 20.dp),
            label = "RB",
            isPressed = pressedButtons.contains("BUTTON_R1"),
            onClick = { onButtonClick("BUTTON_R1") }
        )
        
        // Triggers
        Trigger(
            modifier = Modifier.offset(x = 60.dp, y = 0.dp),
            label = "LT",
            value = leftTrigger,
            onClick = { onButtonClick("BUTTON_L2") }
        )
        
        Trigger(
            modifier = Modifier.offset(x = 240.dp, y = 0.dp),
            label = "RT",
            value = rightTrigger,
            onClick = { onButtonClick("BUTTON_R2") }
        )
        
        // Center buttons
        CenterButton(
            modifier = Modifier.offset(x = 120.dp, y = 80.dp),
            label = "MENU",
            isPressed = pressedButtons.contains("BUTTON_START"),
            onClick = { onButtonClick("BUTTON_START") }
        )
        
        CenterButton(
            modifier = Modifier.offset(x = 180.dp, y = 80.dp),
            label = "VIEW",
            isPressed = pressedButtons.contains("BUTTON_SELECT"),
            onClick = { onButtonClick("BUTTON_SELECT") }
        )
    }
}

@Composable
private fun AnalogStick(
    modifier: Modifier = Modifier,
    position: Pair<Float, Float>,
    isPressed: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Stick base
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    if (isPressed) Color.Blue else Color.Gray,
                    CircleShape
                )
        )
        
        // Stick top (moves based on position)
        Box(
            modifier = Modifier
                .size(40.dp)
                .offset(
                    x = (position.first * 10).dp,
                    y = (position.second * 10).dp
                )
                .background(
                    if (isPressed) Color.LightGray else Color.White,
                    CircleShape
                )
        )
    }
}

@Composable
private fun DPad(
    modifier: Modifier = Modifier,
    pressedButtons: Set<String>,
    onButtonClick: (String) -> Unit
) {
    Box(modifier = modifier.size(80.dp)) {
        // Up
        ControllerButton(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(24.dp, 32.dp),
            label = "↑",
            isPressed = pressedButtons.contains("BUTTON_DPAD_UP"),
            onClick = { onButtonClick("BUTTON_DPAD_UP") }
        )
        
        // Down
        ControllerButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(24.dp, 32.dp),
            label = "↓",
            isPressed = pressedButtons.contains("BUTTON_DPAD_DOWN"),
            onClick = { onButtonClick("BUTTON_DPAD_DOWN") }
        )
        
        // Left
        ControllerButton(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(32.dp, 24.dp),
            label = "←",
            isPressed = pressedButtons.contains("BUTTON_DPAD_LEFT"),
            onClick = { onButtonClick("BUTTON_DPAD_LEFT") }
        )
        
        // Right
        ControllerButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(32.dp, 24.dp),
            label = "→",
            isPressed = pressedButtons.contains("BUTTON_DPAD_RIGHT"),
            onClick = { onButtonClick("BUTTON_DPAD_RIGHT") }
        )
    }
}

@Composable
private fun FaceButtons(
    modifier: Modifier = Modifier,
    pressedButtons: Set<String>,
    onButtonClick: (String) -> Unit
) {
    Box(modifier = modifier.size(80.dp)) {
        // Y (top)
        ControllerButton(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(32.dp),
            label = "Y",
            isPressed = pressedButtons.contains("BUTTON_Y"),
            onClick = { onButtonClick("BUTTON_Y") }
        )
        
        // A (bottom)
        ControllerButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(32.dp),
            label = "A",
            isPressed = pressedButtons.contains("BUTTON_A"),
            onClick = { onButtonClick("BUTTON_A") }
        )
        
        // X (left)
        ControllerButton(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(32.dp),
            label = "X",
            isPressed = pressedButtons.contains("BUTTON_X"),
            onClick = { onButtonClick("BUTTON_X") }
        )
        
        // B (right)
        ControllerButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(32.dp),
            label = "B",
            isPressed = pressedButtons.contains("BUTTON_B"),
            onClick = { onButtonClick("BUTTON_B") }
        )
    }
}

@Composable
private fun ControllerButton(
    modifier: Modifier = Modifier,
    label: String,
    isPressed: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .background(
                if (isPressed) Color.Green else Color.LightGray,
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) Color.White else Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ShoulderButton(
    modifier: Modifier = Modifier,
    label: String,
    isPressed: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(60.dp, 20.dp)
            .clickable { onClick() }
            .background(
                if (isPressed) Color.Green else Color.LightGray,
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) Color.White else Color.Black
        )
    }
}

@Composable
private fun Trigger(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp, 15.dp)
                .background(
                    if (value > 0.1f) Color.Red else Color.Gray,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Trigger value indicator
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.Gray, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value)
                    .background(Color.Red, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun CenterButton(
    modifier: Modifier = Modifier,
    label: String,
    isPressed: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(40.dp, 20.dp)
            .clickable { onClick() }
            .background(
                if (isPressed) Color.Green else Color.LightGray,
                RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) Color.White else Color.Black
        )
    }
}

@Composable
private fun PlayStationControllerLayout(
    profile: ControllerProfile,
    onButtonClick: (String) -> Unit,
    pressedButtons: Set<String>,
    leftStickPosition: Pair<Float, Float>,
    rightStickPosition: Pair<Float, Float>,
    leftTrigger: Float,
    rightTrigger: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Controller body background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black,
                    RoundedCornerShape(24.dp)
                )
        )
        
        // Left analog stick
        AnalogStick(
            modifier = Modifier
                .offset(x = 60.dp, y = 200.dp)
                .size(80.dp),
            position = leftStickPosition,
            isPressed = pressedButtons.contains("BUTTON_THUMBL"),
            onClick = { onButtonClick("BUTTON_THUMBL") }
        )
        
        // Right analog stick
        AnalogStick(
            modifier = Modifier
                .offset(x = 260.dp, y = 200.dp)
                .size(80.dp),
            position = rightStickPosition,
            isPressed = pressedButtons.contains("BUTTON_THUMBR"),
            onClick = { onButtonClick("BUTTON_THUMBR") }
        )
        
        // Face buttons (Cross, Circle, Square, Triangle)
        ControllerButton(
            modifier = Modifier.offset(x = 300.dp, y = 120.dp),
            label = "×",
            isPressed = pressedButtons.contains("BUTTON_A"),
            onClick = { onButtonClick("BUTTON_A") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 330.dp, y = 90.dp),
            label = "○",
            isPressed = pressedButtons.contains("BUTTON_B"),
            onClick = { onButtonClick("BUTTON_B") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 270.dp, y = 90.dp),
            label = "□",
            isPressed = pressedButtons.contains("BUTTON_X"),
            onClick = { onButtonClick("BUTTON_X") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 300.dp, y = 60.dp),
            label = "△",
            isPressed = pressedButtons.contains("BUTTON_Y"),
            onClick = { onButtonClick("BUTTON_Y") }
        )
        
        // D-Pad
        DPad(
            modifier = Modifier.offset(x = 40.dp, y = 80.dp),
            pressedButtons = pressedButtons,
            onButtonClick = onButtonClick
        )
        
        // Shoulder buttons
        ShoulderButton(
            modifier = Modifier.offset(x = 40.dp, y = 20.dp),
            label = "L1",
            isPressed = pressedButtons.contains("BUTTON_L1"),
            onClick = { onButtonClick("BUTTON_L1") }
        )
        ShoulderButton(
            modifier = Modifier.offset(x = 300.dp, y = 20.dp),
            label = "R1",
            isPressed = pressedButtons.contains("BUTTON_R1"),
            onClick = { onButtonClick("BUTTON_R1") }
        )
        
        // Triggers with pressure indicators
        TriggerIndicator(
            modifier = Modifier.offset(x = 20.dp, y = 10.dp),
            label = "L2",
            value = leftTrigger,
            isPressed = pressedButtons.contains("BUTTON_L2"),
            onClick = { onButtonClick("BUTTON_L2") }
        )
        TriggerIndicator(
            modifier = Modifier.offset(x = 320.dp, y = 10.dp),
            label = "R2",
            value = rightTrigger,
            isPressed = pressedButtons.contains("BUTTON_R2"),
            onClick = { onButtonClick("BUTTON_R2") }
        )
        
        // Center buttons
        ControllerButton(
            modifier = Modifier.offset(x = 140.dp, y = 100.dp),
            label = "SHARE",
            isPressed = pressedButtons.contains("BUTTON_SELECT"),
            onClick = { onButtonClick("BUTTON_SELECT") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 220.dp, y = 100.dp),
            label = "OPTIONS",
            isPressed = pressedButtons.contains("BUTTON_START"),
            onClick = { onButtonClick("BUTTON_START") }
        )
    }
}

@Composable
private fun GameSirControllerLayout(
    profile: ControllerProfile,
    onButtonClick: (String) -> Unit,
    pressedButtons: Set<String>,
    leftStickPosition: Pair<Float, Float>,
    rightStickPosition: Pair<Float, Float>,
    leftTrigger: Float,
    rightTrigger: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Controller body background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color(0xFF2E3440), // GameSir-like dark blue-gray
                    RoundedCornerShape(20.dp)
                )
        )
        
        // Left analog stick
        AnalogStick(
            modifier = Modifier
                .offset(x = 60.dp, y = 180.dp)
                .size(80.dp),
            position = leftStickPosition,
            isPressed = pressedButtons.contains("BUTTON_THUMBL"),
            onClick = { onButtonClick("BUTTON_THUMBL") }
        )
        
        // Right analog stick
        AnalogStick(
            modifier = Modifier
                .offset(x = 260.dp, y = 180.dp)
                .size(80.dp),
            position = rightStickPosition,
            isPressed = pressedButtons.contains("BUTTON_THUMBR"),
            onClick = { onButtonClick("BUTTON_THUMBR") }
        )
        
        // Face buttons (A, B, X, Y)
        ControllerButton(
            modifier = Modifier.offset(x = 300.dp, y = 120.dp),
            label = "A",
            isPressed = pressedButtons.contains("BUTTON_A"),
            onClick = { onButtonClick("BUTTON_A") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 330.dp, y = 90.dp),
            label = "B",
            isPressed = pressedButtons.contains("BUTTON_B"),
            onClick = { onButtonClick("BUTTON_B") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 270.dp, y = 90.dp),
            label = "X",
            isPressed = pressedButtons.contains("BUTTON_X"),
            onClick = { onButtonClick("BUTTON_X") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 300.dp, y = 60.dp),
            label = "Y",
            isPressed = pressedButtons.contains("BUTTON_Y"),
            onClick = { onButtonClick("BUTTON_Y") }
        )
        
        // D-Pad
        DPad(
            modifier = Modifier.offset(x = 40.dp, y = 80.dp),
            pressedButtons = pressedButtons,
            onButtonClick = onButtonClick
        )
        
        // Shoulder buttons
        ShoulderButton(
            modifier = Modifier.offset(x = 40.dp, y = 20.dp),
            label = "L1",
            isPressed = pressedButtons.contains("BUTTON_L1"),
            onClick = { onButtonClick("BUTTON_L1") }
        )
        ShoulderButton(
            modifier = Modifier.offset(x = 300.dp, y = 20.dp),
            label = "R1",
            isPressed = pressedButtons.contains("BUTTON_R1"),
            onClick = { onButtonClick("BUTTON_R1") }
        )
        
        // Triggers with pressure indicators
        TriggerIndicator(
            modifier = Modifier.offset(x = 20.dp, y = 10.dp),
            label = "L2",
            value = leftTrigger,
            isPressed = pressedButtons.contains("BUTTON_L2"),
            onClick = { onButtonClick("BUTTON_L2") }
        )
        TriggerIndicator(
            modifier = Modifier.offset(x = 320.dp, y = 10.dp),
            label = "R2",
            value = rightTrigger,
            isPressed = pressedButtons.contains("BUTTON_R2"),
            onClick = { onButtonClick("BUTTON_R2") }
        )
        
        // Center buttons
        ControllerButton(
            modifier = Modifier.offset(x = 140.dp, y = 100.dp),
            label = "SELECT",
            isPressed = pressedButtons.contains("BUTTON_SELECT"),
            onClick = { onButtonClick("BUTTON_SELECT") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 220.dp, y = 100.dp),
            label = "START",
            isPressed = pressedButtons.contains("BUTTON_START"),
            onClick = { onButtonClick("BUTTON_START") }
        )
        
        // GameSir specific buttons (if available)
        ControllerButton(
            modifier = Modifier.offset(x = 180.dp, y = 80.dp),
            label = "HOME",
            isPressed = pressedButtons.contains("BUTTON_MODE"),
            onClick = { onButtonClick("BUTTON_MODE") }
        )
    }
}

@Composable
private fun GenericControllerLayout(
    profile: ControllerProfile,
    onButtonClick: (String) -> Unit,
    pressedButtons: Set<String>,
    leftStickPosition: Pair<Float, Float>,
    rightStickPosition: Pair<Float, Float>,
    leftTrigger: Float,
    rightTrigger: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Controller body background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Gray,
                    RoundedCornerShape(16.dp)
                )
        )
        
        // Left analog stick
        AnalogStick(
            modifier = Modifier
                .offset(x = 60.dp, y = 180.dp)
                .size(80.dp),
            position = leftStickPosition,
            isPressed = pressedButtons.contains("BUTTON_THUMBL"),
            onClick = { onButtonClick("BUTTON_THUMBL") }
        )
        
        // Right analog stick
        AnalogStick(
            modifier = Modifier
                .offset(x = 260.dp, y = 180.dp)
                .size(80.dp),
            position = rightStickPosition,
            isPressed = pressedButtons.contains("BUTTON_THUMBR"),
            onClick = { onButtonClick("BUTTON_THUMBR") }
        )
        
        // Face buttons (generic layout)
        ControllerButton(
            modifier = Modifier.offset(x = 300.dp, y = 120.dp),
            label = "1",
            isPressed = pressedButtons.contains("BUTTON_A"),
            onClick = { onButtonClick("BUTTON_A") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 330.dp, y = 90.dp),
            label = "2",
            isPressed = pressedButtons.contains("BUTTON_B"),
            onClick = { onButtonClick("BUTTON_B") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 270.dp, y = 90.dp),
            label = "3",
            isPressed = pressedButtons.contains("BUTTON_X"),
            onClick = { onButtonClick("BUTTON_X") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 300.dp, y = 60.dp),
            label = "4",
            isPressed = pressedButtons.contains("BUTTON_Y"),
            onClick = { onButtonClick("BUTTON_Y") }
        )
        
        // D-Pad
        DPad(
            modifier = Modifier.offset(x = 40.dp, y = 80.dp),
            pressedButtons = pressedButtons,
            onButtonClick = onButtonClick
        )
        
        // Shoulder buttons
        ShoulderButton(
            modifier = Modifier.offset(x = 40.dp, y = 20.dp),
            label = "L1",
            isPressed = pressedButtons.contains("BUTTON_L1"),
            onClick = { onButtonClick("BUTTON_L1") }
        )
        ShoulderButton(
            modifier = Modifier.offset(x = 300.dp, y = 20.dp),
            label = "R1",
            isPressed = pressedButtons.contains("BUTTON_R1"),
            onClick = { onButtonClick("BUTTON_R1") }
        )
        
        // Triggers with pressure indicators
        TriggerIndicator(
            modifier = Modifier.offset(x = 20.dp, y = 10.dp),
            label = "L2",
            value = leftTrigger,
            isPressed = pressedButtons.contains("BUTTON_L2"),
            onClick = { onButtonClick("BUTTON_L2") }
        )
        TriggerIndicator(
            modifier = Modifier.offset(x = 320.dp, y = 10.dp),
            label = "R2",
            value = rightTrigger,
            isPressed = pressedButtons.contains("BUTTON_R2"),
            onClick = { onButtonClick("BUTTON_R2") }
        )
        
        // Center buttons
        ControllerButton(
            modifier = Modifier.offset(x = 140.dp, y = 100.dp),
            label = "SEL",
            isPressed = pressedButtons.contains("BUTTON_SELECT"),
            onClick = { onButtonClick("BUTTON_SELECT") }
        )
        ControllerButton(
            modifier = Modifier.offset(x = 220.dp, y = 100.dp),
            label = "START",
            isPressed = pressedButtons.contains("BUTTON_START"),
            onClick = { onButtonClick("BUTTON_START") }
        )
    }
}

@Composable
private fun TriggerIndicator(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    isPressed: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Trigger pressure bar
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(60.dp)
                .background(
                    Color.LightGray,
                    RoundedCornerShape(10.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(value)
                    .background(
                        if (isPressed) Color.Green else Color.Blue,
                        RoundedCornerShape(10.dp)
                    )
                    .align(Alignment.BottomCenter)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) Color.Green else Color.Black
        )
    }
}