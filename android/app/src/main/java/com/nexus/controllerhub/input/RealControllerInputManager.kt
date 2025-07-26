package com.nexus.controllerhub.input

import android.app.Activity
import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Real controller input manager that actually captures controller input
 * This replaces the mock implementation with actual Android input system integration
 */
class RealControllerInputManager(private val context: Context) {
    
    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
    
    // Real-time input state
    private val _buttonStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val buttonStates: StateFlow<Map<Int, Boolean>> = _buttonStates.asStateFlow()
    
    private val _analogStates = MutableStateFlow<Map<String, Float>>(emptyMap())
    val analogStates: StateFlow<Map<String, Float>> = _analogStates.asStateFlow()
    
    private val _connectedControllers = MutableStateFlow<List<ControllerInfo>>(emptyList())
    val connectedControllers: StateFlow<List<ControllerInfo>> = _connectedControllers.asStateFlow()
    
    private val _inputEvents = MutableStateFlow<List<InputEvent>>(emptyList())
    val inputEvents: StateFlow<List<InputEvent>> = _inputEvents.asStateFlow()
    
    // Internal state tracking
    private val currentButtonStates = ConcurrentHashMap<Int, Boolean>()
    private val currentAnalogStates = ConcurrentHashMap<String, Float>()
    private val inputEventHistory = mutableListOf<InputEvent>()
    
    // Macro recording
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _recordedMacro = MutableStateFlow<List<MacroEvent>>(emptyList())
    val recordedMacro: StateFlow<List<MacroEvent>> = _recordedMacro.asStateFlow()
    
    private val macroEvents = mutableListOf<MacroEvent>()
    private var recordingStartTime = 0L
    
    data class ControllerInfo(
        val deviceId: Int,
        val name: String,
        val vendorId: Int,
        val productId: Int,
        val descriptor: String,
        val isGamepad: Boolean,
        val hasVibrator: Boolean,
        val axes: List<AxisInfo>
    )
    
    data class AxisInfo(
        val axis: Int,
        val name: String,
        val min: Float,
        val max: Float,
        val flat: Float,
        val fuzz: Float
    )
    
    data class InputEvent(
        val timestamp: Long,
        val deviceId: Int,
        val type: String,
        val code: Int,
        val value: Float,
        val description: String
    )
    
    data class MacroEvent(
        val timestamp: Long,
        val type: String,
        val keyCode: Int,
        val action: Int,
        val value: Float = 0f
    )
    
    init {
        refreshControllerList()
        inputManager.registerInputDeviceListener(object : InputManager.InputDeviceListener {
            override fun onInputDeviceAdded(deviceId: Int) {
                refreshControllerList()
            }
            
            override fun onInputDeviceRemoved(deviceId: Int) {
                refreshControllerList()
            }
            
            override fun onInputDeviceChanged(deviceId: Int) {
                refreshControllerList()
            }
        }, null)
    }
    
    /**
     * Set up input capture for an activity
     * This must be called from the activity to capture controller input
     */
    fun setupInputCapture(activity: Activity) {
        val rootView = activity.findViewById<View>(android.R.id.content)
        
        // Make the view focusable to receive input events
        rootView.isFocusable = true
        rootView.isFocusableInTouchMode = true
        rootView.requestFocus()
        
        // Set up key event listener
        rootView.setOnKeyListener { _, keyCode, event ->
            handleKeyEvent(event)
            true // Consume the event
        }
        
        // Set up generic motion event listener
        rootView.setOnGenericMotionListener { _, event ->
            handleMotionEvent(event)
            true // Consume the event
        }
    }
    
    private fun refreshControllerList() {
        val controllers = mutableListOf<ControllerInfo>()
        
        for (deviceId in inputManager.inputDeviceIds) {
            val device = inputManager.getInputDevice(deviceId)
            if (device != null && isController(device)) {
                val axes = mutableListOf<AxisInfo>()
                
                // Get all available axes for this controller
                for (range in device.motionRanges) {
                    axes.add(AxisInfo(
                        axis = range.axis,
                        name = getAxisName(range.axis),
                        min = range.min,
                        max = range.max,
                        flat = range.flat,
                        fuzz = range.fuzz
                    ))
                }
                
                controllers.add(ControllerInfo(
                    deviceId = deviceId,
                    name = device.name,
                    vendorId = device.vendorId,
                    productId = device.productId,
                    descriptor = device.descriptor,
                    isGamepad = (device.sources and InputDevice.SOURCE_GAMEPAD) != 0,
                    hasVibrator = device.vibrator?.hasVibrator() == true,
                    axes = axes
                ))
            }
        }
        
        _connectedControllers.value = controllers
    }
    
    private fun isController(device: InputDevice): Boolean {
        val sources = device.sources
        return (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
               (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
    }
    
    private fun handleKeyEvent(event: KeyEvent): Boolean {
        val isPressed = event.action == KeyEvent.ACTION_DOWN
        val keyCode = event.keyCode
        
        // Update button state
        currentButtonStates[keyCode] = isPressed
        _buttonStates.value = currentButtonStates.toMap()
        
        // Add to input event history
        val inputEvent = InputEvent(
            timestamp = System.currentTimeMillis(),
            deviceId = event.deviceId,
            type = "KEY",
            code = keyCode,
            value = if (isPressed) 1f else 0f,
            description = "${getKeyName(keyCode)} ${if (isPressed) "PRESSED" else "RELEASED"}"
        )
        
        addInputEvent(inputEvent)
        
        // Record macro if recording
        if (_isRecording.value) {
            recordMacroEvent(MacroEvent(
                timestamp = System.currentTimeMillis() - recordingStartTime,
                type = "KEY",
                keyCode = keyCode,
                action = event.action
            ))
        }
        
        return true
    }
    
    private fun handleMotionEvent(event: MotionEvent): Boolean {
        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) {
            // Handle analog sticks and triggers
            val device = inputManager.getInputDevice(event.deviceId)
            device?.let {
                for (range in it.motionRanges) {
                    val axis = range.axis
                    val value = event.getAxisValue(axis)
                    val axisName = getAxisName(axis)
                    
                    // Update analog state
                    currentAnalogStates[axisName] = value
                    
                    // Add to input event history
                    val inputEvent = InputEvent(
                        timestamp = System.currentTimeMillis(),
                        deviceId = event.deviceId,
                        type = "MOTION",
                        code = axis,
                        value = value,
                        description = "$axisName: ${String.format("%.3f", value)}"
                    )
                    
                    addInputEvent(inputEvent)
                    
                    // Record macro if recording
                    if (_isRecording.value) {
                        recordMacroEvent(MacroEvent(
                            timestamp = System.currentTimeMillis() - recordingStartTime,
                            type = "MOTION",
                            keyCode = axis,
                            action = MotionEvent.ACTION_MOVE,
                            value = value
                        ))
                    }
                }
            }
            
            _analogStates.value = currentAnalogStates.toMap()
        }
        
        return true
    }
    
    private fun addInputEvent(event: InputEvent) {
        inputEventHistory.add(0, event)
        if (inputEventHistory.size > 100) {
            inputEventHistory.removeAt(inputEventHistory.size - 1)
        }
        _inputEvents.value = inputEventHistory.toList()
    }
    
    private fun recordMacroEvent(event: MacroEvent) {
        macroEvents.add(event)
        _recordedMacro.value = macroEvents.toList()
    }
    
    fun startMacroRecording() {
        macroEvents.clear()
        recordingStartTime = System.currentTimeMillis()
        _isRecording.value = true
        _recordedMacro.value = emptyList()
    }
    
    fun stopMacroRecording() {
        _isRecording.value = false
    }
    
    fun clearMacro() {
        macroEvents.clear()
        _recordedMacro.value = emptyList()
    }
    
    fun clearInputHistory() {
        inputEventHistory.clear()
        _inputEvents.value = emptyList()
    }
    
    private fun getKeyName(keyCode: Int): String {
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
            KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
            else -> "KEY_$keyCode"
        }
    }
    
    private fun getAxisName(axis: Int): String {
        return when (axis) {
            MotionEvent.AXIS_X -> "LEFT_STICK_X"
            MotionEvent.AXIS_Y -> "LEFT_STICK_Y"
            MotionEvent.AXIS_Z -> "RIGHT_STICK_X"
            MotionEvent.AXIS_RZ -> "RIGHT_STICK_Y"
            MotionEvent.AXIS_LTRIGGER -> "LEFT_TRIGGER"
            MotionEvent.AXIS_RTRIGGER -> "RIGHT_TRIGGER"
            MotionEvent.AXIS_HAT_X -> "DPAD_X"
            MotionEvent.AXIS_HAT_Y -> "DPAD_Y"
            else -> "AXIS_$axis"
        }
    }
    
    /**
     * Get current button state
     */
    fun isButtonPressed(keyCode: Int): Boolean {
        return currentButtonStates[keyCode] == true
    }
    
    /**
     * Get current analog value
     */
    fun getAnalogValue(axisName: String): Float {
        return currentAnalogStates[axisName] ?: 0f
    }
    
    /**
     * Get controller information by device ID
     */
    fun getControllerInfo(deviceId: Int): ControllerInfo? {
        return _connectedControllers.value.find { it.deviceId == deviceId }
    }
}