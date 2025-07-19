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
    rightTrigger: Float = 0f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp)
    ) {
        XboxControllerLayout(
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