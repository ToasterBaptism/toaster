package com.nexus.controllerhub.core

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
 * Core controller input system that actually works with Android's input APIs
 * This is a singleton that manages all controller input across the app
 */
class ControllerInputSystem private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: ControllerInputSystem? = null
        
        fun getInstance(context: Context): ControllerInputSystem {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ControllerInputSystem(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
    
    // Controller state
    private val _connectedControllers = MutableStateFlow<List<DetectedController>>(emptyList())
    val connectedControllers: StateFlow<List<DetectedController>> = _connectedControllers.asStateFlow()
    
    private val _selectedController = MutableStateFlow<DetectedController?>(null)
    val selectedController: StateFlow<DetectedController?> = _selectedController.asStateFlow()
    
    // Real-time input state
    private val _buttonStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val buttonStates: StateFlow<Map<Int, Boolean>> = _buttonStates.asStateFlow()
    
    private val _analogStates = MutableStateFlow<Map<Int, Float>>(emptyMap())
    val analogStates: StateFlow<Map<Int, Float>> = _analogStates.asStateFlow()
    
    private val _inputEvents = MutableStateFlow<List<InputEventData>>(emptyList())
    val inputEvents: StateFlow<List<InputEventData>> = _inputEvents.asStateFlow()
    
    // Macro recording
    private val _isRecordingMacro = MutableStateFlow(false)
    val isRecordingMacro: StateFlow<Boolean> = _isRecordingMacro.asStateFlow()
    
    private val _recordedMacro = MutableStateFlow<List<MacroStep>>(emptyList())
    val recordedMacro: StateFlow<List<MacroStep>> = _recordedMacro.asStateFlow()
    
    // Internal state
    private val currentButtonStates = ConcurrentHashMap<Int, Boolean>()
    private val currentAnalogStates = ConcurrentHashMap<Int, Float>()
    private val inputEventHistory = mutableListOf<InputEventData>()
    private val macroSteps = mutableListOf<MacroStep>()
    private var macroStartTime = 0L
    private var isInputCaptureActive = false
    
    data class DetectedController(
        val deviceId: Int,
        val name: String,
        val vendorId: Int,
        val productId: Int,
        val descriptor: String,
        val isGamepad: Boolean,
        val hasVibrator: Boolean,
        val supportedAxes: List<AxisInfo>,
        val supportedKeys: List<Int>,
        val connectionType: String
    )
    
    data class AxisInfo(
        val axis: Int,
        val name: String,
        val min: Float,
        val max: Float,
        val flat: Float,
        val fuzz: Float,
        val resolution: Float
    )
    
    data class InputEventData(
        val timestamp: Long,
        val deviceId: Int,
        val type: String,
        val code: Int,
        val value: Float,
        val description: String
    )
    
    data class MacroStep(
        val timestamp: Long,
        val type: String,
        val keyCode: Int,
        val action: Int,
        val value: Float = 0f,
        val description: String
    )
    
    init {
        refreshControllerList()
        
        // Listen for controller connect/disconnect
        inputManager.registerInputDeviceListener(object : InputManager.InputDeviceListener {
            override fun onInputDeviceAdded(deviceId: Int) {
                refreshControllerList()
            }
            
            override fun onInputDeviceRemoved(deviceId: Int) {
                refreshControllerList()
                if (_selectedController.value?.deviceId == deviceId) {
                    _selectedController.value = null
                }
            }
            
            override fun onInputDeviceChanged(deviceId: Int) {
                refreshControllerList()
            }
        }, null)
    }
    
    /**
     * Set up input capture for the main activity
     * This MUST be called from MainActivity to capture controller input
     */
    fun setupInputCapture(activity: Activity) {
        this.activity = activity
        isInputCaptureActive = true
    }
    
    private var activity: Activity? = null
    
    /**
     * Handle key events - MUST be called from MainActivity's dispatchKeyEvent
     */
    fun handleKeyEventFromActivity(event: KeyEvent): Boolean {
        android.util.Log.d("ControllerInput", "Dispatch key event: keyCode=${event.keyCode}, action=${event.action}, device=${event.deviceId}, captureActive=$isInputCaptureActive")
        
        if (!isInputCaptureActive) {
            android.util.Log.d("ControllerInput", "Input capture not active, ignoring event")
            return false
        }
        
        if (isControllerEvent(event)) {
            android.util.Log.d("ControllerInput", "Controller event detected, handling...")
            handleKeyEvent(event)
            return true // Consume controller events
        } else {
            android.util.Log.d("ControllerInput", "Not a controller event, device=${event.deviceId}")
        }
        return false
    }
    
    /**
     * Handle motion events - MUST be called from MainActivity's dispatchGenericMotionEvent
     */
    fun handleMotionEventFromActivity(event: MotionEvent): Boolean {
        android.util.Log.d("ControllerInput", "Dispatch motion event: device=${event.deviceId}, source=${event.source}, captureActive=$isInputCaptureActive")
        
        if (!isInputCaptureActive) {
            android.util.Log.d("ControllerInput", "Input capture not active, ignoring motion event")
            return false
        }
        
        if (isControllerMotionEvent(event)) {
            android.util.Log.d("ControllerInput", "Controller motion event detected, handling...")
            handleMotionEvent(event)
            return true // Consume controller events
        } else {
            android.util.Log.d("ControllerInput", "Not a controller motion event, device=${event.deviceId}, source=${event.source}")
        }
        return false
    }
    
    private fun isControllerEvent(event: KeyEvent): Boolean {
        val device = inputManager.getInputDevice(event.deviceId)
        return device != null && isController(device)
    }
    
    private fun isControllerMotionEvent(event: MotionEvent): Boolean {
        val device = inputManager.getInputDevice(event.deviceId)
        return device != null && isController(device) && 
               (event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
    }
    
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val isPressed = event.action == KeyEvent.ACTION_DOWN
        val keyCode = event.keyCode
        val deviceId = event.deviceId
        
        // Debug logging
        android.util.Log.d("ControllerInput", "KEY EVENT: keyCode=$keyCode, action=${event.action}, device=$deviceId, pressed=$isPressed")
        
        // Update button state
        currentButtonStates[keyCode] = isPressed
        _buttonStates.value = currentButtonStates.toMap()
        
        // Create input event
        val inputEvent = InputEventData(
            timestamp = System.currentTimeMillis(),
            deviceId = deviceId,
            type = "BUTTON",
            code = keyCode,
            value = if (isPressed) 1f else 0f,
            description = "${getButtonName(keyCode)} ${if (isPressed) "PRESSED" else "RELEASED"}"
        )
        
        addInputEvent(inputEvent)
        
        // Record macro if recording
        if (_isRecordingMacro.value) {
            val macroStep = MacroStep(
                timestamp = System.currentTimeMillis() - macroStartTime,
                type = "BUTTON",
                keyCode = keyCode,
                action = event.action,
                description = inputEvent.description
            )
            macroSteps.add(macroStep)
            _recordedMacro.value = macroSteps.toList()
        }
        
        return isControllerEvent(event)
    }
    
    fun handleMotionEvent(event: MotionEvent): Boolean {
        val deviceId = event.deviceId
        val device = inputManager.getInputDevice(deviceId) ?: return false
        
        android.util.Log.d("ControllerInput", "MOTION EVENT: device=$deviceId, axes=${device.motionRanges.size}")
        
        // Process all motion axes
        for (range in device.motionRanges) {
            val axis = range.axis
            val value = event.getAxisValue(axis)
            
            // Only process if value changed significantly
            val oldValue = currentAnalogStates[axis] ?: 0f
            if (kotlin.math.abs(value - oldValue) > 0.01f) {
                android.util.Log.d("ControllerInput", "AXIS CHANGE: ${getAxisName(axis)} = $value (was $oldValue)")
                
                currentAnalogStates[axis] = value
                
                val inputEvent = InputEventData(
                    timestamp = System.currentTimeMillis(),
                    deviceId = deviceId,
                    type = "ANALOG",
                    code = axis,
                    value = value,
                    description = "${getAxisName(axis)}: ${String.format("%.3f", value)}"
                )
                
                addInputEvent(inputEvent)
                
                // Record macro if recording
                if (_isRecordingMacro.value) {
                    val macroStep = MacroStep(
                        timestamp = System.currentTimeMillis() - macroStartTime,
                        type = "ANALOG",
                        keyCode = axis,
                        action = MotionEvent.ACTION_MOVE,
                        value = value,
                        description = inputEvent.description
                    )
                    macroSteps.add(macroStep)
                    _recordedMacro.value = macroSteps.toList()
                }
            }
        }
        
        _analogStates.value = currentAnalogStates.toMap()
        
        return isControllerMotionEvent(event)
    }
    
    private fun addInputEvent(event: InputEventData) {
        inputEventHistory.add(0, event)
        if (inputEventHistory.size > 200) {
            inputEventHistory.removeAt(inputEventHistory.size - 1)
        }
        _inputEvents.value = inputEventHistory.toList()
    }
    
    private fun refreshControllerList() {
        val controllers = mutableListOf<DetectedController>()
        
        for (deviceId in inputManager.inputDeviceIds) {
            val device = inputManager.getInputDevice(deviceId)
            if (device != null && isController(device)) {
                
                // Get supported axes
                val axes = device.motionRanges.map { range ->
                    AxisInfo(
                        axis = range.axis,
                        name = getAxisName(range.axis),
                        min = range.min,
                        max = range.max,
                        flat = range.flat,
                        fuzz = range.fuzz,
                        resolution = range.resolution
                    )
                }
                
                // Get supported keys (approximate)
                val supportedKeys = listOf(
                    KeyEvent.KEYCODE_BUTTON_A,
                    KeyEvent.KEYCODE_BUTTON_B,
                    KeyEvent.KEYCODE_BUTTON_X,
                    KeyEvent.KEYCODE_BUTTON_Y,
                    KeyEvent.KEYCODE_BUTTON_L1,
                    KeyEvent.KEYCODE_BUTTON_R1,
                    KeyEvent.KEYCODE_BUTTON_L2,
                    KeyEvent.KEYCODE_BUTTON_R2,
                    KeyEvent.KEYCODE_BUTTON_THUMBL,
                    KeyEvent.KEYCODE_BUTTON_THUMBR,
                    KeyEvent.KEYCODE_BUTTON_START,
                    KeyEvent.KEYCODE_BUTTON_SELECT,
                    KeyEvent.KEYCODE_DPAD_UP,
                    KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    KeyEvent.KEYCODE_DPAD_RIGHT
                )
                
                controllers.add(DetectedController(
                    deviceId = deviceId,
                    name = device.name,
                    vendorId = device.vendorId,
                    productId = device.productId,
                    descriptor = device.descriptor,
                    isGamepad = (device.sources and InputDevice.SOURCE_GAMEPAD) != 0,
                    hasVibrator = device.vibrator?.hasVibrator() == true,
                    supportedAxes = axes,
                    supportedKeys = supportedKeys,
                    connectionType = if (device.isVirtual) "Virtual" else "Physical"
                ))
            }
        }
        
        _connectedControllers.value = controllers
        
        // Auto-select first controller if none selected
        if (_selectedController.value == null && controllers.isNotEmpty()) {
            _selectedController.value = controllers.first()
        }
    }
    
    private fun isController(device: InputDevice): Boolean {
        val sources = device.sources
        val isGamepad = (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
        val isJoystick = (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
        val isDpad = (sources and InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD
        
        android.util.Log.d("ControllerInput", "Device ${device.name} (${device.id}): sources=$sources, gamepad=$isGamepad, joystick=$isJoystick, dpad=$isDpad")
        
        return isGamepad || isJoystick || isDpad
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
            KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
            else -> "BUTTON_$keyCode"
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
            MotionEvent.AXIS_BRAKE -> "BRAKE"
            MotionEvent.AXIS_THROTTLE -> "THROTTLE"
            else -> "AXIS_$axis"
        }
    }
    
    // Public API methods
    fun selectController(controller: DetectedController) {
        _selectedController.value = controller
    }
    
    fun startMacroRecording() {
        macroSteps.clear()
        macroStartTime = System.currentTimeMillis()
        _isRecordingMacro.value = true
        _recordedMacro.value = emptyList()
    }
    
    fun stopMacroRecording() {
        _isRecordingMacro.value = false
    }
    
    fun clearMacro() {
        macroSteps.clear()
        _recordedMacro.value = emptyList()
    }
    
    fun clearInputHistory() {
        inputEventHistory.clear()
        _inputEvents.value = emptyList()
    }
    
    fun isButtonPressed(keyCode: Int): Boolean {
        return currentButtonStates[keyCode] == true
    }
    
    fun getAnalogValue(axis: Int): Float {
        return currentAnalogStates[axis] ?: 0f
    }
    
    fun enableInputCapture() {
        isInputCaptureActive = true
    }
    
    fun disableInputCapture() {
        isInputCaptureActive = false
    }
}