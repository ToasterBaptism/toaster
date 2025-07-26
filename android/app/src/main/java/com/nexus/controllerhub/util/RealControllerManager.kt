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
 * Real controller manager that actually detects and processes controller input
 */
class RealControllerManager(private val context: Context) : InputManager.InputDeviceListener {
    
    companion object {
        private const val TAG = "RealControllerManager"
        
        @Volatile
        private var INSTANCE: RealControllerManager? = null
        
        fun getInstance(context: Context): RealControllerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RealControllerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
    
    // Real controller state
    private val _connectedControllers = MutableStateFlow<List<RealControllerInfo>>(emptyList())
    val connectedControllers: StateFlow<List<RealControllerInfo>> = _connectedControllers.asStateFlow()
    
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
    
    // Macro recording
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _recordedEvents = MutableStateFlow<List<ControllerEvent>>(emptyList())
    val recordedEvents: StateFlow<List<ControllerEvent>> = _recordedEvents.asStateFlow()
    
    private var recordingStartTime = 0L
    
    // Test mode
    private val _isTestMode = MutableStateFlow(false)
    val isTestMode: StateFlow<Boolean> = _isTestMode.asStateFlow()
    
    private val _inputLog = MutableStateFlow<List<String>>(emptyList())
    val inputLog: StateFlow<List<String>> = _inputLog.asStateFlow()
    
    data class RealControllerInfo(
        val deviceId: Int,
        val name: String,
        val descriptor: String,
        val vendorId: Int,
        val productId: Int,
        val sources: Int,
        val keyboardType: Int,
        val hasVibrator: Boolean,
        val hasKeys: List<Int>,
        val motionRanges: List<MotionRangeInfo>,
        val controllerType: ControllerType
    )
    
    data class MotionRangeInfo(
        val axis: Int,
        val source: Int,
        val min: Float,
        val max: Float,
        val flat: Float,
        val fuzz: Float,
        val resolution: Float
    )
    
    data class ControllerEvent(
        val timestamp: Long,
        val deviceId: Int,
        val type: EventType,
        val keyCode: Int? = null,
        val action: Int? = null,
        val axisValues: Map<Int, Float>? = null
    )
    
    enum class EventType {
        KEY_DOWN,
        KEY_UP,
        MOTION
    }
    
    enum class ControllerType {
        XBOX_360,
        XBOX_ONE,
        XBOX_SERIES,
        PS3,
        PS4,
        PS5,
        NINTENDO_SWITCH_PRO,
        GENERIC_GAMEPAD,
        UNKNOWN
    }
    
    init {
        inputManager.registerInputDeviceListener(this, null)
        scanForControllers()
        Log.d(TAG, "RealControllerManager initialized")
    }
    
    private fun scanForControllers() {
        val deviceIds = inputManager.inputDeviceIds
        val controllers = mutableListOf<RealControllerInfo>()
        
        Log.d(TAG, "Scanning for controllers, found ${deviceIds.size} input devices")
        
        for (deviceId in deviceIds) {
            val device = inputManager.getInputDevice(deviceId)
            if (device != null && isGameController(device)) {
                val controllerInfo = createControllerInfo(device)
                controllers.add(controllerInfo)
                Log.d(TAG, "Found controller: ${controllerInfo.name} (ID: $deviceId)")
            }
        }
        
        _connectedControllers.value = controllers
        Log.d(TAG, "Total controllers found: ${controllers.size}")
    }
    
    private fun isGameController(device: InputDevice): Boolean {
        val sources = device.sources
        
        // Check if device has gamepad or joystick sources
        val isGamepad = (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
        val isJoystick = (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
        val isDpad = (sources and InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD
        
        // Additional check for common controller buttons
        val hasControllerButtons = device.hasKeys(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y
        ).any { it }
        
        val result = isGamepad || isJoystick || (isDpad && hasControllerButtons)
        
        Log.d(TAG, "Device ${device.name}: gamepad=$isGamepad, joystick=$isJoystick, dpad=$isDpad, hasButtons=$hasControllerButtons, isController=$result")
        
        return result
    }
    
    private fun createControllerInfo(device: InputDevice): RealControllerInfo {
        val motionRanges = mutableListOf<MotionRangeInfo>()
        
        // Get all motion ranges
        for (range in device.motionRanges) {
            motionRanges.add(
                MotionRangeInfo(
                    axis = range.axis,
                    source = range.source,
                    min = range.min,
                    max = range.max,
                    flat = range.flat,
                    fuzz = range.fuzz,
                    resolution = range.resolution
                )
            )
        }
        
        // Check which keys the device has
        val hasKeys = mutableListOf<Int>()
        val commonKeys = listOf(
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
        
        for (keyCode in commonKeys) {
            if (device.hasKeys(keyCode)[0]) {
                hasKeys.add(keyCode)
            }
        }
        
        return RealControllerInfo(
            deviceId = device.id,
            name = device.name,
            descriptor = device.descriptor,
            vendorId = device.vendorId,
            productId = device.productId,
            sources = device.sources,
            keyboardType = device.keyboardType,
            hasVibrator = device.vibrator?.hasVibrator() ?: false,
            hasKeys = hasKeys,
            motionRanges = motionRanges,
            controllerType = determineControllerType(device)
        )
    }
    
    private fun determineControllerType(device: InputDevice): ControllerType {
        val name = device.name.lowercase()
        val vendorId = device.vendorId
        val productId = device.productId
        
        return when {
            // Xbox controllers
            vendorId == 0x045e -> when (productId) {
                0x028e -> ControllerType.XBOX_360
                0x02d1, 0x02dd -> ControllerType.XBOX_ONE
                0x0b12, 0x0b13 -> ControllerType.XBOX_SERIES
                else -> ControllerType.XBOX_360
            }
            
            // PlayStation controllers
            vendorId == 0x054c -> when (productId) {
                0x0268 -> ControllerType.PS3
                0x05c4, 0x09cc -> ControllerType.PS4
                0x0ce6 -> ControllerType.PS5
                else -> ControllerType.PS4
            }
            
            // Nintendo controllers
            vendorId == 0x057e && productId == 0x2009 -> ControllerType.NINTENDO_SWITCH_PRO
            
            // Name-based detection
            "xbox" in name -> when {
                "360" in name -> ControllerType.XBOX_360
                "one" in name -> ControllerType.XBOX_ONE
                "series" in name -> ControllerType.XBOX_SERIES
                else -> ControllerType.XBOX_360
            }
            
            "playstation" in name || "ps" in name || "dualshock" in name || "dualsense" in name -> when {
                "3" in name || "dualshock 3" in name -> ControllerType.PS3
                "4" in name || "dualshock 4" in name -> ControllerType.PS4
                "5" in name || "dualsense" in name -> ControllerType.PS5
                else -> ControllerType.PS4
            }
            
            "nintendo" in name || "switch" in name -> ControllerType.NINTENDO_SWITCH_PRO
            
            // Generic gamepad
            device.sources and InputDevice.SOURCE_GAMEPAD != 0 -> ControllerType.GENERIC_GAMEPAD
            
            else -> ControllerType.UNKNOWN
        }
    }
    
    /**
     * Process key events from controllers
     */
    fun processKeyEvent(event: KeyEvent): Boolean {
        val device = event.device
        if (device == null || !isGameController(device)) {
            return false
        }
        
        val buttonName = getButtonName(event.keyCode)
        val isPressed = event.action == KeyEvent.ACTION_DOWN
        
        Log.d(TAG, "Controller key event: $buttonName ${if (isPressed) "DOWN" else "UP"} from ${device.name}")
        
        // Update button state
        val currentButtons = _pressedButtons.value.toMutableSet()
        if (isPressed) {
            currentButtons.add(buttonName)
        } else {
            currentButtons.remove(buttonName)
        }
        _pressedButtons.value = currentButtons
        
        // Log for test mode
        if (_isTestMode.value) {
            logInput("Key: $buttonName ${if (isPressed) "DOWN" else "UP"} (${device.name})")
        }
        
        // Record for macro
        if (_isRecording.value) {
            recordKeyEvent(event)
        }
        
        return true
    }
    
    /**
     * Process motion events from controllers
     */
    fun processMotionEvent(event: MotionEvent): Boolean {
        val device = event.device
        if (device == null || !isGameController(device)) {
            return false
        }
        
        Log.d(TAG, "Controller motion event from ${device.name}")
        
        // Update analog stick positions
        val leftX = event.getAxisValue(MotionEvent.AXIS_X)
        val leftY = event.getAxisValue(MotionEvent.AXIS_Y)
        _leftStickPosition.value = Pair(leftX, leftY)
        
        val rightX = event.getAxisValue(MotionEvent.AXIS_Z)
        val rightY = event.getAxisValue(MotionEvent.AXIS_RZ)
        _rightStickPosition.value = Pair(rightX, rightY)
        
        // Update trigger values
        val leftTriggerValue = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
        val rightTriggerValue = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        _leftTrigger.value = leftTriggerValue
        _rightTrigger.value = rightTriggerValue
        
        // Update trigger button states
        val currentButtons = _pressedButtons.value.toMutableSet()
        if (leftTriggerValue > 0.1f) {
            currentButtons.add("L2")
        } else {
            currentButtons.remove("L2")
        }
        
        if (rightTriggerValue > 0.1f) {
            currentButtons.add("R2")
        } else {
            currentButtons.remove("R2")
        }
        _pressedButtons.value = currentButtons
        
        // Log for test mode
        if (_isTestMode.value) {
            logInput("Motion: L(%.2f,%.2f) R(%.2f,%.2f) LT:%.2f RT:%.2f".format(
                leftX, leftY, rightX, rightY, leftTriggerValue, rightTriggerValue
            ))
        }
        
        // Record for macro
        if (_isRecording.value) {
            recordMotionEvent(event)
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
            else -> "KEY_$keyCode"
        }
    }
    
    private fun logInput(message: String) {
        val currentLog = _inputLog.value.toMutableList()
        currentLog.add(0, "${System.currentTimeMillis()}: $message")
        if (currentLog.size > 50) {
            currentLog.removeAt(currentLog.size - 1)
        }
        _inputLog.value = currentLog
    }
    
    private fun recordKeyEvent(event: KeyEvent) {
        val timestamp = if (recordingStartTime == 0L) {
            recordingStartTime = System.currentTimeMillis()
            0L
        } else {
            System.currentTimeMillis() - recordingStartTime
        }
        
        val controllerEvent = ControllerEvent(
            timestamp = timestamp,
            deviceId = event.deviceId,
            type = if (event.action == KeyEvent.ACTION_DOWN) EventType.KEY_DOWN else EventType.KEY_UP,
            keyCode = event.keyCode,
            action = event.action
        )
        
        val currentEvents = _recordedEvents.value.toMutableList()
        currentEvents.add(controllerEvent)
        _recordedEvents.value = currentEvents
        
        Log.d(TAG, "Recorded key event: ${getButtonName(event.keyCode)} at ${timestamp}ms")
    }
    
    private fun recordMotionEvent(event: MotionEvent) {
        val timestamp = if (recordingStartTime == 0L) {
            recordingStartTime = System.currentTimeMillis()
            0L
        } else {
            System.currentTimeMillis() - recordingStartTime
        }
        
        val axisValues = mutableMapOf<Int, Float>()
        for (range in event.device.motionRanges) {
            val value = event.getAxisValue(range.axis)
            if (kotlin.math.abs(value) > 0.1f) {
                axisValues[range.axis] = value
            }
        }
        
        if (axisValues.isNotEmpty()) {
            val controllerEvent = ControllerEvent(
                timestamp = timestamp,
                deviceId = event.deviceId,
                type = EventType.MOTION,
                axisValues = axisValues
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
        _inputLog.value = listOf("Test mode started - connect a controller and press buttons")
        Log.d(TAG, "Started test mode")
    }
    
    fun stopTestMode() {
        _isTestMode.value = false
        _inputLog.value = emptyList()
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
    
    fun refreshControllers() {
        scanForControllers()
    }
    
    // InputManager.InputDeviceListener implementation
    override fun onInputDeviceAdded(deviceId: Int) {
        Log.d(TAG, "Input device added: $deviceId")
        scanForControllers()
    }
    
    override fun onInputDeviceRemoved(deviceId: Int) {
        Log.d(TAG, "Input device removed: $deviceId")
        scanForControllers()
    }
    
    override fun onInputDeviceChanged(deviceId: Int) {
        Log.d(TAG, "Input device changed: $deviceId")
        scanForControllers()
    }
}