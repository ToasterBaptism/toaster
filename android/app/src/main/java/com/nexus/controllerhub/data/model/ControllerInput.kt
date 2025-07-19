package com.nexus.controllerhub.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ControllerInput(
    val deviceId: Int,
    val deviceName: String,
    val buttonStates: Map<String, Boolean> = emptyMap(),
    val axisValues: Map<String, Float> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ControllerButton(
    val code: String,
    val displayName: String,
    val position: ButtonPosition,
    val isPressed: Boolean = false,
    val isMapped: Boolean = false,
    val mappedTo: String? = null
)

@Serializable
data class ButtonPosition(
    val x: Float,
    val y: Float,
    val width: Float = 40f,
    val height: Float = 40f
)

@Serializable
data class AnalogStick(
    val code: String,
    val displayName: String,
    val position: ButtonPosition,
    val xValue: Float = 0f,
    val yValue: Float = 0f,
    val deadZoneInner: Float = 0.1f,
    val deadZoneOuter: Float = 0.95f,
    val sensitivity: Float = 1.0f
)

@Serializable
data class Trigger(
    val code: String,
    val displayName: String,
    val position: ButtonPosition,
    val value: Float = 0f,
    val actuationPoint: Float = 0.5f
)

// Standard controller layout
object ControllerLayout {
    val STANDARD_BUTTONS = listOf(
        ControllerButton("BUTTON_A", "A", ButtonPosition(320f, 200f)),
        ControllerButton("BUTTON_B", "B", ButtonPosition(360f, 160f)),
        ControllerButton("BUTTON_X", "X", ButtonPosition(280f, 160f)),
        ControllerButton("BUTTON_Y", "Y", ButtonPosition(320f, 120f)),
        ControllerButton("BUTTON_L1", "L1", ButtonPosition(80f, 60f)),
        ControllerButton("BUTTON_R1", "R1", ButtonPosition(480f, 60f)),
        ControllerButton("BUTTON_L2", "L2", ButtonPosition(80f, 20f)),
        ControllerButton("BUTTON_R2", "R2", ButtonPosition(480f, 20f)),
        ControllerButton("BUTTON_SELECT", "Select", ButtonPosition(200f, 120f)),
        ControllerButton("BUTTON_START", "Start", ButtonPosition(360f, 120f)),
        ControllerButton("BUTTON_THUMBL", "L3", ButtonPosition(120f, 200f)),
        ControllerButton("BUTTON_THUMBR", "R3", ButtonPosition(440f, 200f)),
        ControllerButton("DPAD_UP", "D-Up", ButtonPosition(120f, 120f)),
        ControllerButton("DPAD_DOWN", "D-Down", ButtonPosition(120f, 200f)),
        ControllerButton("DPAD_LEFT", "D-Left", ButtonPosition(80f, 160f)),
        ControllerButton("DPAD_RIGHT", "D-Right", ButtonPosition(160f, 160f))
    )
    
    val ANALOG_STICKS = listOf(
        AnalogStick("LEFT_STICK", "Left Stick", ButtonPosition(120f, 200f)),
        AnalogStick("RIGHT_STICK", "Right Stick", ButtonPosition(440f, 200f))
    )
    
    val TRIGGERS = listOf(
        Trigger("LEFT_TRIGGER", "Left Trigger", ButtonPosition(80f, 20f)),
        Trigger("RIGHT_TRIGGER", "Right Trigger", ButtonPosition(480f, 20f))
    )
}