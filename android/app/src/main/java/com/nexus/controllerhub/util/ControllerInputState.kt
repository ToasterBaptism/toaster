package com.nexus.controllerhub.util

import android.view.KeyEvent
import android.view.MotionEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages real-time controller input state for visual feedback
 */
object ControllerInputState {
    
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
    
    // Selected controller device
    private val _selectedDeviceId = MutableStateFlow<Int?>(null)
    val selectedDeviceId: StateFlow<Int?> = _selectedDeviceId.asStateFlow()
    
    fun setSelectedDevice(deviceId: Int?) {
        _selectedDeviceId.value = deviceId
    }
    
    /**
     * Update button state from KeyEvent
     */
    fun updateButtonState(event: KeyEvent) {
        // Only process events from selected device (or any device if none selected)
        val selectedDevice = _selectedDeviceId.value
        if (selectedDevice != null && event.deviceId != selectedDevice) {
            return
        }
        
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
    
    /**
     * Update analog stick and trigger state from MotionEvent
     */
    fun updateMotionState(event: MotionEvent) {
        // Only process events from selected device (or any device if none selected)
        val selectedDevice = _selectedDeviceId.value
        if (selectedDevice != null && event.deviceId != selectedDevice) {
            return
        }
        
        // Left stick (X and Y axes)
        val leftX = event.getAxisValue(MotionEvent.AXIS_X)
        val leftY = event.getAxisValue(MotionEvent.AXIS_Y)
        _leftStickPosition.value = Pair(leftX, leftY)
        
        // Right stick (Z and RZ axes)
        val rightX = event.getAxisValue(MotionEvent.AXIS_Z)
        val rightY = event.getAxisValue(MotionEvent.AXIS_RZ)
        _rightStickPosition.value = Pair(rightX, rightY)
        
        // Triggers (LTRIGGER and RTRIGGER axes)
        val leftTriggerValue = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
        val rightTriggerValue = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        _leftTrigger.value = leftTriggerValue
        _rightTrigger.value = rightTriggerValue
        
        // Handle trigger buttons (some controllers send triggers as buttons)
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
    
    /**
     * Clear all input state
     */
    fun clearState() {
        _pressedButtons.value = emptySet()
        _leftStickPosition.value = Pair(0f, 0f)
        _rightStickPosition.value = Pair(0f, 0f)
        _leftTrigger.value = 0f
        _rightTrigger.value = 0f
    }
    
    /**
     * Convert KeyEvent keyCode to button string
     */
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
}