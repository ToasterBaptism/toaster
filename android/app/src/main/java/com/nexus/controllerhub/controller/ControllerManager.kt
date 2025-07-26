package com.nexus.controllerhub.controller

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
 * Singleton controller manager that actually works
 */
class ControllerManager private constructor(private val context: Context) : InputManager.InputDeviceListener {
    
    companion object {
        private const val TAG = "ControllerManager"
        
        @Volatile
        private var INSTANCE: ControllerManager? = null
        
        fun getInstance(context: Context): ControllerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ControllerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
    
    // Controller state
    private val _controllers = MutableStateFlow<List<Controller>>(emptyList())
    val controllers: StateFlow<List<Controller>> = _controllers.asStateFlow()
    
    private val _activeController = MutableStateFlow<Controller?>(null)
    val activeController: StateFlow<Controller?> = _activeController.asStateFlow()
    
    // Input state
    private val _buttonStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val buttonStates: StateFlow<Map<String, Boolean>> = _buttonStates.asStateFlow()
    
    private val _leftStick = MutableStateFlow(StickState(0f, 0f))
    val leftStick: StateFlow<StickState> = _leftStick.asStateFlow()
    
    private val _rightStick = MutableStateFlow(StickState(0f, 0f))
    val rightStick: StateFlow<StickState> = _rightStick.asStateFlow()
    
    private val _leftTrigger = MutableStateFlow(0f)
    val leftTrigger: StateFlow<Float> = _leftTrigger.asStateFlow()
    
    private val _rightTrigger = MutableStateFlow(0f)
    val rightTrigger: StateFlow<Float> = _rightTrigger.asStateFlow()
    
    // Testing state
    private val _isTestMode = MutableStateFlow(false)
    val isTestMode: StateFlow<Boolean> = _isTestMode.asStateFlow()
    
    private val _inputEvents = MutableStateFlow<List<InputEvent>>(emptyList())
    val inputEvents: StateFlow<List<InputEvent>> = _inputEvents.asStateFlow()
    
    // Macro recording
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _recordedMacro = MutableStateFlow<List<MacroEvent>>(emptyList())
    val recordedMacro: StateFlow<List<MacroEvent>> = _recordedMacro.asStateFlow()
    
    private var recordingStartTime = 0L
    
    data class Controller(
        val deviceId: Int,
        val name: String,
        val type: ControllerType,
        val vendorId: Int,
        val productId: Int,
        val hasVibrator: Boolean,
        val supportedButtons: List<String>,
        val supportedAxes: List<String>
    )
    
    data class StickState(
        val x: Float,
        val y: Float
    )
    
    data class InputEvent(
        val timestamp: Long,
        val type: String,
        val data: String
    )
    
    data class MacroEvent(
        val timestamp: Long,
        val type: EventType,
        val button: String? = null,
        val value: Float? = null
    )
    
    enum class ControllerType {
        XBOX, PLAYSTATION, NINTENDO, GENERIC, UNKNOWN
    }
    
    enum class EventType {
        BUTTON_DOWN, BUTTON_UP, STICK_MOVE, TRIGGER_MOVE
    }
    
    init {
        inputManager.registerInputDeviceListener(this, null)
        scanControllers()
        Log.d(TAG, "ControllerManager initialized")
    }
    
    private fun scanControllers() {
        val deviceIds = inputManager.inputDeviceIds
        val foundControllers = mutableListOf<Controller>()
        
        Log.d(TAG, "Scanning ${deviceIds.size} input devices")
        
        for (deviceId in deviceIds) {
            val device = inputManager.getInputDevice(deviceId) ?: continue
            
            if (isController(device)) {
                val controller = createController(device)
                foundControllers.add(controller)
                Log.d(TAG, "Found controller: ${controller.name}")
            }
        }
        
        _controllers.value = foundControllers
        
        // Auto-select first controller
        if (foundControllers.isNotEmpty() && _activeController.value == null) {
            _activeController.value = foundControllers.first()
            Log.d(TAG, "Auto-selected controller: ${foundControllers.first().name}")
        }
        
        Log.d(TAG, "Total controllers found: ${foundControllers.size}")
    }
    
    private fun isController(device: InputDevice): Boolean {
        val sources = device.sources
        
        // Check for gamepad or joystick sources
        val hasGamepadSource = (sources and InputDevice.SOURCE_GAMEPAD) != 0
        val hasJoystickSource = (sources and InputDevice.SOURCE_JOYSTICK) != 0
        val hasDpadSource = (sources and InputDevice.SOURCE_DPAD) != 0
        
        // Check for common controller buttons
        val hasControllerButtons = listOf(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y
        ).any { keyCode ->
            device.hasKeys(keyCode)[0]
        }
        
        val isController = hasGamepadSource || hasJoystickSource || (hasDpadSource && hasControllerButtons)
        
        Log.d(TAG, "Device ${device.name}: gamepad=$hasGamepadSource, joystick=$hasJoystickSource, dpad=$hasDpadSource, buttons=$hasControllerButtons -> isController=$isController")
        
        return isController
    }
    
    private fun createController(device: InputDevice): Controller {
        val supportedButtons = mutableListOf<String>()
        val buttonKeyCodes = mapOf(
            KeyEvent.KEYCODE_BUTTON_A to "A",
            KeyEvent.KEYCODE_BUTTON_B to "B",
            KeyEvent.KEYCODE_BUTTON_X to "X",
            KeyEvent.KEYCODE_BUTTON_Y to "Y",
            KeyEvent.KEYCODE_BUTTON_L1 to "L1",
            KeyEvent.KEYCODE_BUTTON_R1 to "R1",
            KeyEvent.KEYCODE_BUTTON_L2 to "L2",
            KeyEvent.KEYCODE_BUTTON_R2 to "R2",
            KeyEvent.KEYCODE_BUTTON_THUMBL to "LS",
            KeyEvent.KEYCODE_BUTTON_THUMBR to "RS",
            KeyEvent.KEYCODE_BUTTON_START to "START",
            KeyEvent.KEYCODE_BUTTON_SELECT to "SELECT",
            KeyEvent.KEYCODE_DPAD_UP to "DPAD_UP",
            KeyEvent.KEYCODE_DPAD_DOWN to "DPAD_DOWN",
            KeyEvent.KEYCODE_DPAD_LEFT to "DPAD_LEFT",
            KeyEvent.KEYCODE_DPAD_RIGHT to "DPAD_RIGHT"
        )
        
        for ((keyCode, buttonName) in buttonKeyCodes) {
            if (device.hasKeys(keyCode)[0]) {
                supportedButtons.add(buttonName)
            }
        }
        
        val supportedAxes = mutableListOf<String>()
        for (range in device.motionRanges) {
            when (range.axis) {
                MotionEvent.AXIS_X -> supportedAxes.add("LEFT_STICK_X")
                MotionEvent.AXIS_Y -> supportedAxes.add("LEFT_STICK_Y")
                MotionEvent.AXIS_Z -> supportedAxes.add("RIGHT_STICK_X")
                MotionEvent.AXIS_RZ -> supportedAxes.add("RIGHT_STICK_Y")
                MotionEvent.AXIS_LTRIGGER -> supportedAxes.add("LEFT_TRIGGER")
                MotionEvent.AXIS_RTRIGGER -> supportedAxes.add("RIGHT_TRIGGER")
            }
        }
        
        return Controller(
            deviceId = device.id,
            name = device.name,
            type = determineControllerType(device),
            vendorId = device.vendorId,
            productId = device.productId,
            hasVibrator = device.vibrator?.hasVibrator() ?: false,
            supportedButtons = supportedButtons,
            supportedAxes = supportedAxes
        )
    }
    
    private fun determineControllerType(device: InputDevice): ControllerType {
        val name = device.name.lowercase()
        val vendorId = device.vendorId
        
        return when {
            vendorId == 0x045e || "xbox" in name -> ControllerType.XBOX
            vendorId == 0x054c || "playstation" in name || "ps" in name || "dualshock" in name || "dualsense" in name -> ControllerType.PLAYSTATION
            vendorId == 0x057e || "nintendo" in name || "switch" in name -> ControllerType.NINTENDO
            device.sources and InputDevice.SOURCE_GAMEPAD != 0 -> ControllerType.GENERIC
            else -> ControllerType.UNKNOWN
        }
    }
    
    // Public API
    fun selectController(controller: Controller) {
        _activeController.value = controller
        Log.d(TAG, "Selected controller: ${controller.name}")
    }
    
    fun startTestMode() {
        _isTestMode.value = true
        _inputEvents.value = emptyList()
        Log.d(TAG, "Started test mode")
    }
    
    fun stopTestMode() {
        _isTestMode.value = false
        Log.d(TAG, "Stopped test mode")
    }
    
    fun startMacroRecording() {
        _isRecording.value = true
        _recordedMacro.value = emptyList()
        recordingStartTime = System.currentTimeMillis()
        Log.d(TAG, "Started macro recording")
    }
    
    fun stopMacroRecording(): List<MacroEvent> {
        _isRecording.value = false
        val macro = _recordedMacro.value
        Log.d(TAG, "Stopped macro recording, captured ${macro.size} events")
        return macro
    }
    
    fun refreshControllers() {
        scanControllers()
    }
    
    fun clearMacro() {
        _recordedMacro.value = emptyList()
        Log.d(TAG, "Cleared recorded macro")
    }
    
    fun clearInputHistory() {
        _inputEvents.value = emptyList()
        Log.d(TAG, "Cleared input history")
    }
    
    // Input processing
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val device = event.device ?: return false
        if (!isController(device)) return false
        
        val buttonName = getButtonName(event.keyCode)
        val isPressed = event.action == KeyEvent.ACTION_DOWN
        
        Log.d(TAG, "Key event: $buttonName ${if (isPressed) "DOWN" else "UP"}")
        
        // Update button state
        val currentStates = _buttonStates.value.toMutableMap()
        currentStates[buttonName] = isPressed
        _buttonStates.value = currentStates
        
        // Log event if in test mode
        if (_isTestMode.value) {
            logInputEvent("BUTTON", "$buttonName ${if (isPressed) "PRESSED" else "RELEASED"}")
        }
        
        // Record macro event
        if (_isRecording.value) {
            recordMacroEvent(
                if (isPressed) EventType.BUTTON_DOWN else EventType.BUTTON_UP,
                buttonName
            )
        }
        
        return true
    }
    
    fun handleMotionEvent(event: MotionEvent): Boolean {
        val device = event.device ?: return false
        if (!isController(device)) return false
        
        // Update stick positions
        val leftX = event.getAxisValue(MotionEvent.AXIS_X)
        val leftY = event.getAxisValue(MotionEvent.AXIS_Y)
        _leftStick.value = StickState(leftX, leftY)
        
        val rightX = event.getAxisValue(MotionEvent.AXIS_Z)
        val rightY = event.getAxisValue(MotionEvent.AXIS_RZ)
        _rightStick.value = StickState(rightX, rightY)
        
        // Update trigger values
        val leftTriggerValue = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
        val rightTriggerValue = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        _leftTrigger.value = leftTriggerValue
        _rightTrigger.value = rightTriggerValue
        
        // Log significant motion events
        if (_isTestMode.value) {
            if (kotlin.math.abs(leftX) > 0.1f || kotlin.math.abs(leftY) > 0.1f) {
                logInputEvent("STICK", "LEFT (%.2f, %.2f)".format(leftX, leftY))
            }
            if (kotlin.math.abs(rightX) > 0.1f || kotlin.math.abs(rightY) > 0.1f) {
                logInputEvent("STICK", "RIGHT (%.2f, %.2f)".format(rightX, rightY))
            }
            if (leftTriggerValue > 0.1f) {
                logInputEvent("TRIGGER", "LEFT %.2f".format(leftTriggerValue))
            }
            if (rightTriggerValue > 0.1f) {
                logInputEvent("TRIGGER", "RIGHT %.2f".format(rightTriggerValue))
            }
        }
        
        // Record macro events for significant movements
        if (_isRecording.value) {
            if (kotlin.math.abs(leftX) > 0.1f || kotlin.math.abs(leftY) > 0.1f) {
                recordMacroEvent(EventType.STICK_MOVE, "LEFT_STICK", kotlin.math.sqrt(leftX * leftX + leftY * leftY))
            }
            if (kotlin.math.abs(rightX) > 0.1f || kotlin.math.abs(rightY) > 0.1f) {
                recordMacroEvent(EventType.STICK_MOVE, "RIGHT_STICK", kotlin.math.sqrt(rightX * rightX + rightY * rightY))
            }
            if (leftTriggerValue > 0.1f) {
                recordMacroEvent(EventType.TRIGGER_MOVE, "LEFT_TRIGGER", leftTriggerValue)
            }
            if (rightTriggerValue > 0.1f) {
                recordMacroEvent(EventType.TRIGGER_MOVE, "RIGHT_TRIGGER", rightTriggerValue)
            }
        }
        
        return true
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
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "LS"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "RS"
            KeyEvent.KEYCODE_BUTTON_START -> "START"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "SELECT"
            KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
            else -> "UNKNOWN_$keyCode"
        }
    }
    
    private fun logInputEvent(type: String, data: String) {
        val event = InputEvent(
            timestamp = System.currentTimeMillis(),
            type = type,
            data = data
        )
        
        val currentEvents = _inputEvents.value.toMutableList()
        currentEvents.add(0, event) // Add to beginning
        if (currentEvents.size > 100) {
            currentEvents.removeAt(currentEvents.size - 1)
        }
        _inputEvents.value = currentEvents
    }
    
    private fun recordMacroEvent(type: EventType, button: String, value: Float? = null) {
        val timestamp = System.currentTimeMillis() - recordingStartTime
        val event = MacroEvent(timestamp, type, button, value)
        
        val currentMacro = _recordedMacro.value.toMutableList()
        currentMacro.add(event)
        _recordedMacro.value = currentMacro
    }
    
    // InputManager.InputDeviceListener
    override fun onInputDeviceAdded(deviceId: Int) {
        Log.d(TAG, "Device added: $deviceId")
        scanControllers()
    }
    
    override fun onInputDeviceRemoved(deviceId: Int) {
        Log.d(TAG, "Device removed: $deviceId")
        scanControllers()
        
        // Clear active controller if it was removed
        if (_activeController.value?.deviceId == deviceId) {
            _activeController.value = null
        }
    }
    
    override fun onInputDeviceChanged(deviceId: Int) {
        Log.d(TAG, "Device changed: $deviceId")
        scanControllers()
    }
}