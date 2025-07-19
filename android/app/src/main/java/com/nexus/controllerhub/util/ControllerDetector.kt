package com.nexus.controllerhub.util

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ControllerDetector(private val context: Context) : InputManager.InputDeviceListener {
    
    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
    private val _connectedControllers = MutableStateFlow<List<ConnectedController>>(emptyList())
    val connectedControllers: StateFlow<List<ConnectedController>> = _connectedControllers.asStateFlow()
    
    data class ConnectedController(
        val deviceId: Int,
        val name: String,
        val descriptor: String,
        val vendorId: Int,
        val productId: Int,
        val isGamepad: Boolean,
        val hasVibrator: Boolean,
        val controllerType: ControllerType,
        val connectionType: ConnectionType
    )
    
    enum class ControllerType {
        XBOX,
        PLAYSTATION,
        GAMESIR,
        GENERIC,
        UNKNOWN
    }
    
    enum class ConnectionType {
        USB,
        BLUETOOTH,
        UNKNOWN
    }
    
    init {
        inputManager.registerInputDeviceListener(this, null)
        refreshControllerList()
    }
    
    fun startDetection() {
        refreshControllerList()
    }
    
    fun stopDetection() {
        inputManager.unregisterInputDeviceListener(this)
    }
    
    override fun onInputDeviceAdded(deviceId: Int) {
        refreshControllerList()
    }
    
    override fun onInputDeviceRemoved(deviceId: Int) {
        refreshControllerList()
    }
    
    override fun onInputDeviceChanged(deviceId: Int) {
        refreshControllerList()
    }
    
    private fun refreshControllerList() {
        val controllers = mutableListOf<ConnectedController>()
        
        for (deviceId in inputManager.inputDeviceIds) {
            val device = inputManager.getInputDevice(deviceId)
            if (device != null && isController(device)) {
                controllers.add(
                    ConnectedController(
                        deviceId = device.id,
                        name = device.name,
                        descriptor = device.descriptor,
                        vendorId = device.vendorId,
                        productId = device.productId,
                        isGamepad = device.sources and InputDevice.SOURCE_GAMEPAD != 0,
                        hasVibrator = device.vibrator?.hasVibrator() == true,
                        controllerType = detectControllerType(device),
                        connectionType = detectConnectionType(device)
                    )
                )
            }
        }
        
        _connectedControllers.value = controllers
    }
    
    private fun isController(device: InputDevice): Boolean {
        val sources = device.sources
        return (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
               (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
    }
    
    private fun detectControllerType(device: InputDevice): ControllerType {
        val name = device.name.lowercase()
        val vendorId = device.vendorId
        val productId = device.productId
        
        return when {
            // GameSir controllers
            name.contains("gamesir") || 
            vendorId == 0x3537 || // GameSir vendor ID
            name.contains("g3s") ||
            name.contains("g4s") ||
            name.contains("g7") ||
            name.contains("t1s") ||
            name.contains("t4") -> ControllerType.GAMESIR
            
            // Xbox controllers
            name.contains("xbox") ||
            name.contains("microsoft") ||
            vendorId == 0x045e || // Microsoft vendor ID
            (vendorId == 0x045e && productId in listOf(0x028e, 0x028f, 0x02d1, 0x02dd, 0x02e3, 0x02ea, 0x0719)) -> ControllerType.XBOX
            
            // PlayStation controllers
            name.contains("playstation") ||
            name.contains("dualshock") ||
            name.contains("dualsense") ||
            name.contains("ps3") ||
            name.contains("ps4") ||
            name.contains("ps5") ||
            vendorId == 0x054c -> ControllerType.PLAYSTATION // Sony vendor ID
            
            // Generic gamepad patterns
            name.contains("gamepad") ||
            name.contains("controller") ||
            name.contains("joystick") -> ControllerType.GENERIC
            
            else -> ControllerType.UNKNOWN
        }
    }
    
    private fun detectConnectionType(device: InputDevice): ConnectionType {
        val name = device.name.lowercase()
        val descriptor = device.descriptor.lowercase()
        
        return when {
            name.contains("bluetooth") ||
            descriptor.contains("bluetooth") ||
            name.contains("wireless") -> ConnectionType.BLUETOOTH
            
            name.contains("usb") ||
            descriptor.contains("usb") ||
            name.contains("wired") -> ConnectionType.USB
            
            else -> ConnectionType.UNKNOWN
        }
    }
    
    fun getControllerCapabilities(deviceId: Int): ControllerCapabilities? {
        val device = inputManager.getInputDevice(deviceId) ?: return null
        
        val buttons = mutableListOf<String>()
        val axes = mutableListOf<String>()
        
        // Check for standard gamepad buttons
        val buttonKeyCodes = listOf(
            KeyEvent.KEYCODE_BUTTON_A to "BUTTON_A",
            KeyEvent.KEYCODE_BUTTON_B to "BUTTON_B",
            KeyEvent.KEYCODE_BUTTON_X to "BUTTON_X",
            KeyEvent.KEYCODE_BUTTON_Y to "BUTTON_Y",
            KeyEvent.KEYCODE_BUTTON_L1 to "BUTTON_L1",
            KeyEvent.KEYCODE_BUTTON_R1 to "BUTTON_R1",
            KeyEvent.KEYCODE_BUTTON_L2 to "BUTTON_L2",
            KeyEvent.KEYCODE_BUTTON_R2 to "BUTTON_R2",
            KeyEvent.KEYCODE_BUTTON_SELECT to "BUTTON_SELECT",
            KeyEvent.KEYCODE_BUTTON_START to "BUTTON_START",
            KeyEvent.KEYCODE_BUTTON_THUMBL to "BUTTON_THUMBL",
            KeyEvent.KEYCODE_BUTTON_THUMBR to "BUTTON_THUMBR",
            KeyEvent.KEYCODE_DPAD_UP to "DPAD_UP",
            KeyEvent.KEYCODE_DPAD_DOWN to "DPAD_DOWN",
            KeyEvent.KEYCODE_DPAD_LEFT to "DPAD_LEFT",
            KeyEvent.KEYCODE_DPAD_RIGHT to "DPAD_RIGHT"
        )
        
        for ((keyCode, buttonName) in buttonKeyCodes) {
            if (device.hasKeys(keyCode)[0]) {
                buttons.add(buttonName)
            }
        }
        
        // Check for standard axes
        val axisMotionRanges = device.motionRanges
        for (range in axisMotionRanges) {
            when (range.axis) {
                MotionEvent.AXIS_X -> axes.add("LEFT_STICK_X")
                MotionEvent.AXIS_Y -> axes.add("LEFT_STICK_Y")
                MotionEvent.AXIS_Z -> axes.add("RIGHT_STICK_X")
                MotionEvent.AXIS_RZ -> axes.add("RIGHT_STICK_Y")
                MotionEvent.AXIS_LTRIGGER -> axes.add("LEFT_TRIGGER")
                MotionEvent.AXIS_RTRIGGER -> axes.add("RIGHT_TRIGGER")
            }
        }
        
        return ControllerCapabilities(
            deviceId = deviceId,
            deviceName = device.name,
            supportedButtons = buttons,
            supportedAxes = axes,
            hasVibration = device.vibrator?.hasVibrator() == true
        )
    }
    
    data class ControllerCapabilities(
        val deviceId: Int,
        val deviceName: String,
        val supportedButtons: List<String>,
        val supportedAxes: List<String>,
        val hasVibration: Boolean
    )
}