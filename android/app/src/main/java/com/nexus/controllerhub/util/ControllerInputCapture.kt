package com.nexus.controllerhub.util

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simplified controller input capture system that works without accessibility service
 * This demonstrates how controller input should be captured and processed
 */
class ControllerInputCapture(private val context: Context) {
    
    companion object {
        private const val TAG = "ControllerInputCapture"
    }
    
    // Real-time input state
    private val _pressedButtons = MutableStateFlow<Set<String>>(emptySet())
    val pressedButtons: StateFlow<Set<String>> = _pressedButtons.asStateFlow()
    
    private val _leftStickPosition = MutableStateFlow(Pair(0f, 0f))
    val leftStickPosition: StateFlow<Pair<Float, Float>> = _leftStickPosition.asStateFlow()
    
    private val _rightStickPosition = MutableStateFlow(Pair(0f, 0f))
    val rightStickPosition: StateFlow<Pair<Float, Float>> = _rightStickPosition.asStateFlow()
    
    private val _leftTrigger = MutableStateFlow(0f)
    val leftTrigger: StateFlow<Float> = _leftTrigger.asStateFlow()
    
    private val _rightTrigger = MutableStateFlow(0f)
    val rightTrigger: StateFlow<Float> = _rightTrigger.asStateFlow()
    
    // Test mode state
    private val _isTestMode = MutableStateFlow(false)
    val isTestMode: StateFlow<Boolean> = _isTestMode.asStateFlow()
    
    private val _rawInputLog = MutableStateFlow<List<String>>(emptyList())
    val rawInputLog: StateFlow<List<String>> = _rawInputLog.asStateFlow()
    
    private val _remappedOutputLog = MutableStateFlow<List<String>>(emptyList())
    val remappedOutputLog: StateFlow<List<String>> = _remappedOutputLog.asStateFlow()
    
    // Macro recording state
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _recordedEvents = MutableStateFlow<List<ControllerEvent>>(emptyList())
    val recordedEvents: StateFlow<List<ControllerEvent>> = _recordedEvents.asStateFlow()
    
    private var recordingStartTime = 0L
    
    // Button remapping
    private var buttonMappings = mutableMapOf<String, String>()
    
    data class ControllerEvent(
        val type: EventType,
        val buttonCode: String? = null,
        val keyCode: Int? = null,
        val action: Int? = null,
        val axisValues: Map<String, Float>? = null,
        val timestamp: Long,
        val deviceId: Int
    )
    
    enum class EventType {
        BUTTON_PRESS,
        BUTTON_RELEASE,
        MOTION,
        TRIGGER
    }
    
    /**
     * Process a key event from the controller
     */
    fun processKeyEvent(event: KeyEvent): Boolean {
        if (!isControllerEvent(event)) return false
        
        val buttonCode = getButtonCode(event.keyCode)
        val deviceName = event.device?.name ?: "Unknown"
        
        Log.d(TAG, "Controller key event: $buttonCode, action: ${event.action}, device: $deviceName")
        
        // Update button state
        updateButtonState(event)
        
        // Log for test mode
        if (_isTestMode.value) {
            logRawInput("Key: $buttonCode ${if (event.action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"} (Device: $deviceName)")
            
            // Check for remapping
            val remappedButton = buttonMappings[buttonCode]
            if (remappedButton != null && remappedButton != buttonCode) {
                logRemappedOutput("Remapped: $buttonCode -> $remappedButton")
            } else {
                logRemappedOutput("No remapping: $buttonCode")
            }
        }
        
        // Record for macro if recording
        if (_isRecording.value) {
            recordKeyEvent(event)
        }
        
        return true
    }
    
    /**
     * Process a motion event from the controller
     */
    fun processMotionEvent(event: MotionEvent): Boolean {
        if (!isControllerEvent(event)) return false
        
        val deviceName = event.device?.name ?: "Unknown"
        
        Log.d(TAG, "Controller motion event: device: $deviceName")
        
        // Update motion state
        updateMotionState(event)
        
        // Log for test mode
        if (_isTestMode.value) {
            val leftX = event.getAxisValue(MotionEvent.AXIS_X)
            val leftY = event.getAxisValue(MotionEvent.AXIS_Y)
            val rightX = event.getAxisValue(MotionEvent.AXIS_Z)
            val rightY = event.getAxisValue(MotionEvent.AXIS_RZ)
            val leftTrigger = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
            val rightTrigger = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
            
            logRawInput("Motion: L(%.2f,%.2f) R(%.2f,%.2f) LT:%.2f RT:%.2f".format(
                leftX, leftY, rightX, rightY, leftTrigger, rightTrigger
            ))
            logRemappedOutput("Motion processed - no remapping applied")
        }
        
        // Record for macro if recording
        if (_isRecording.value) {
            recordMotionEvent(event)
        }
        
        return true
    }
    
    private fun updateButtonState(event: KeyEvent) {
        val buttonCode = getButtonCode(event.keyCode)
        val currentButtons = _pressedButtons.value.toMutableSet()
        
        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                currentButtons.add(buttonCode)
                _pressedButtons.value = currentButtons
            }
            KeyEvent.ACTION_UP -> {
                currentButtons.remove(buttonCode)
                _pressedButtons.value = currentButtons
            }
        }
    }
    
    private fun updateMotionState(event: MotionEvent) {
        // Left stick
        val leftX = event.getAxisValue(MotionEvent.AXIS_X)
        val leftY = event.getAxisValue(MotionEvent.AXIS_Y)
        _leftStickPosition.value = Pair(leftX, leftY)
        
        // Right stick
        val rightX = event.getAxisValue(MotionEvent.AXIS_Z)
        val rightY = event.getAxisValue(MotionEvent.AXIS_RZ)
        _rightStickPosition.value = Pair(rightX, rightY)
        
        // Triggers
        val leftTriggerValue = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
        val rightTriggerValue = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        _leftTrigger.value = leftTriggerValue
        _rightTrigger.value = rightTriggerValue
        
        // Update button state for triggers
        val currentButtons = _pressedButtons.value.toMutableSet()
        
        if (leftTriggerValue > 0.1f) {
            currentButtons.add("BUTTON_L2")
        } else {
            currentButtons.remove("BUTTON_L2")
        }
        
        if (rightTriggerValue > 0.1f) {
            currentButtons.add("BUTTON_R2")
        } else {
            currentButtons.remove("BUTTON_R2")
        }
        
        _pressedButtons.value = currentButtons
    }
    
    private fun isControllerEvent(event: KeyEvent): Boolean {
        val device = event.device ?: return false
        val sources = device.sources
        
        return (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
               (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
               (sources and InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD
    }
    
    private fun isControllerEvent(event: MotionEvent): Boolean {
        val device = event.device ?: return false
        val sources = device.sources
        
        return (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
               (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
    }
    
    private fun getButtonCode(keyCode: Int): String {
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> "BUTTON_A"
            KeyEvent.KEYCODE_BUTTON_B -> "BUTTON_B"
            KeyEvent.KEYCODE_BUTTON_X -> "BUTTON_X"
            KeyEvent.KEYCODE_BUTTON_Y -> "BUTTON_Y"
            KeyEvent.KEYCODE_BUTTON_L1 -> "BUTTON_L1"
            KeyEvent.KEYCODE_BUTTON_R1 -> "BUTTON_R1"
            KeyEvent.KEYCODE_BUTTON_L2 -> "BUTTON_L2"
            KeyEvent.KEYCODE_BUTTON_R2 -> "BUTTON_R2"
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "BUTTON_THUMBL"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "BUTTON_THUMBR"
            KeyEvent.KEYCODE_BUTTON_START -> "BUTTON_START"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "BUTTON_SELECT"
            KeyEvent.KEYCODE_DPAD_UP -> "BUTTON_DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "BUTTON_DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "BUTTON_DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "BUTTON_DPAD_RIGHT"
            else -> "UNKNOWN_$keyCode"
        }
    }
    
    private fun logRawInput(message: String) {
        val currentLog = _rawInputLog.value.toMutableList()
        currentLog.add(0, "${System.currentTimeMillis()}: $message")
        if (currentLog.size > 20) {
            currentLog.removeAt(currentLog.size - 1)
        }
        _rawInputLog.value = currentLog
    }
    
    private fun logRemappedOutput(message: String) {
        val currentLog = _remappedOutputLog.value.toMutableList()
        currentLog.add(0, "${System.currentTimeMillis()}: $message")
        if (currentLog.size > 20) {
            currentLog.removeAt(currentLog.size - 1)
        }
        _remappedOutputLog.value = currentLog
    }
    
    private fun recordKeyEvent(event: KeyEvent) {
        val timestamp = if (recordingStartTime == 0L) {
            recordingStartTime = System.currentTimeMillis()
            0L
        } else {
            System.currentTimeMillis() - recordingStartTime
        }
        
        val controllerEvent = ControllerEvent(
            type = if (event.action == KeyEvent.ACTION_DOWN) EventType.BUTTON_PRESS else EventType.BUTTON_RELEASE,
            buttonCode = getButtonCode(event.keyCode),
            keyCode = event.keyCode,
            action = event.action,
            timestamp = timestamp,
            deviceId = event.deviceId
        )
        
        val currentEvents = _recordedEvents.value.toMutableList()
        currentEvents.add(controllerEvent)
        _recordedEvents.value = currentEvents
        
        Log.d(TAG, "Recorded key event: ${controllerEvent.buttonCode} at ${timestamp}ms")
    }
    
    private fun recordMotionEvent(event: MotionEvent) {
        val timestamp = if (recordingStartTime == 0L) {
            recordingStartTime = System.currentTimeMillis()
            0L
        } else {
            System.currentTimeMillis() - recordingStartTime
        }
        
        val axisValues = mapOf(
            "AXIS_X" to event.getAxisValue(MotionEvent.AXIS_X),
            "AXIS_Y" to event.getAxisValue(MotionEvent.AXIS_Y),
            "AXIS_Z" to event.getAxisValue(MotionEvent.AXIS_Z),
            "AXIS_RZ" to event.getAxisValue(MotionEvent.AXIS_RZ),
            "AXIS_LTRIGGER" to event.getAxisValue(MotionEvent.AXIS_LTRIGGER),
            "AXIS_RTRIGGER" to event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        )
        
        // Only record significant movements
        if (axisValues.values.any { kotlin.math.abs(it) > 0.1f }) {
            val controllerEvent = ControllerEvent(
                type = EventType.MOTION,
                axisValues = axisValues,
                timestamp = timestamp,
                deviceId = event.deviceId
            )
            
            val currentEvents = _recordedEvents.value.toMutableList()
            currentEvents.add(controllerEvent)
            _recordedEvents.value = currentEvents
            
            Log.d(TAG, "Recorded motion event at ${timestamp}ms")
        }
    }
    
    // Public API methods
    fun startTestMode() {
        _isTestMode.value = true
        _rawInputLog.value = listOf("Test mode started - press controller buttons")
        _remappedOutputLog.value = listOf("Waiting for input...")
        Log.d(TAG, "Started test mode")
    }
    
    fun stopTestMode() {
        _isTestMode.value = false
        _rawInputLog.value = emptyList()
        _remappedOutputLog.value = emptyList()
        Log.d(TAG, "Stopped test mode")
    }
    
    fun startRecording() {
        _isRecording.value = true
        _recordedEvents.value = emptyList()
        recordingStartTime = 0L
        Log.d(TAG, "Started macro recording")
    }
    
    fun stopRecording(): List<ControllerEvent> {
        _isRecording.value = false
        val events = _recordedEvents.value
        Log.d(TAG, "Stopped macro recording, recorded ${events.size} events")
        return events
    }
    
    fun setButtonMapping(originalButton: String, mappedButton: String) {
        buttonMappings[originalButton] = mappedButton
        Log.d(TAG, "Set button mapping: $originalButton -> $mappedButton")
    }
    
    fun clearButtonMappings() {
        buttonMappings.clear()
        Log.d(TAG, "Cleared all button mappings")
    }
    
    fun clearState() {
        _pressedButtons.value = emptySet()
        _leftStickPosition.value = Pair(0f, 0f)
        _rightStickPosition.value = Pair(0f, 0f)
        _leftTrigger.value = 0f
        _rightTrigger.value = 0f
    }
}