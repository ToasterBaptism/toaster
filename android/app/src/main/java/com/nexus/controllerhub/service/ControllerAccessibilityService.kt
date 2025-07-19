package com.nexus.controllerhub.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import com.nexus.controllerhub.data.database.ControllerDatabase
import com.nexus.controllerhub.data.model.ControllerInput
import com.nexus.controllerhub.data.model.MacroAction
import com.nexus.controllerhub.data.model.MacroActionType
import com.nexus.controllerhub.data.repository.ControllerRepository
import com.nexus.controllerhub.util.ControllerInputProcessor
import com.nexus.controllerhub.util.ControllerInputState
import com.nexus.controllerhub.util.InputDiagnostics
import com.nexus.controllerhub.util.MacroPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class ControllerAccessibilityService : AccessibilityService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var repository: ControllerRepository
    private lateinit var inputProcessor: ControllerInputProcessor
    private lateinit var macroPlayer: MacroPlayer
    
    private var isRecordingMacro = false
    private val recordedActions = mutableListOf<MacroAction>()
    private var recordingStartTime = 0L
    
    companion object {
        private const val TAG = "ControllerAccessibilityService"
        var instance: ControllerAccessibilityService? = null
            private set
        
        fun isServiceEnabled(): Boolean = instance != null
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        val database = ControllerDatabase.getDatabase(this)
        repository = ControllerRepository(database.profileDao(), database.macroDao())
        inputProcessor = ControllerInputProcessor(repository)
        macroPlayer = MacroPlayer(this)
        
        Log.d(TAG, "ControllerAccessibilityService created - Touch input should remain functional")
        
        // Verify touch input is available
        InputDiagnostics.verifyTouchInputAvailable(this)
        
        // List all input devices for debugging
        InputDiagnostics.listAllInputDevices(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
        Log.d(TAG, "ControllerAccessibilityService destroyed")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We primarily handle input events through onKeyEvent and onGenericMotionEvent
        // This method can be used for additional accessibility features if needed
        // Touch events should pass through normally without interference
        Log.v(TAG, "Accessibility event: ${event?.eventType} - not processing to preserve touch input")
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }
    
    override fun onKeyEvent(event: KeyEvent): Boolean {
        // First check if this is a controller input
        if (!isControllerInput(event)) {
            // Not a controller input - let it pass through normally
            return false
        }
        
        Log.d(TAG, "Controller key event: ${event.keyCode}, action: ${event.action}, device: ${event.device?.name}")
        
        // Update visual feedback state
        ControllerInputState.updateButtonState(event)
        
        // Record macro if recording is active (only from selected device)
        if (isRecordingMacro && shouldProcessEvent(event)) {
            recordKeyEvent(event)
        }
        
        // Process input through our remapping system
        serviceScope.launch {
            val processedEvent = inputProcessor.processKeyEvent(event)
            if (processedEvent != null && processedEvent != event) {
                // Dispatch the remapped event
                dispatchKeyEvent(processedEvent)
                return@launch
            }
            
            // Check for macro triggers
            val activeProfile = repository.getActiveProfile()
            activeProfile?.let { profile ->
                val buttonCode = getButtonCode(event.keyCode)
                val macroId = profile.macroAssignments[buttonCode]
                if (macroId != null && event.action == KeyEvent.ACTION_DOWN) {
                    val macro = repository.getMacroById(macroId)
                    macro?.let { macroPlayer.playMacro(it) }
                    return@launch
                }
            }
        }
        
        // Return true to consume the controller event only
        return true
    }
    
    fun handleGenericMotionEvent(event: MotionEvent): Boolean {
        if (!isControllerInput(event)) {
            return false
        }
        
        Log.d(TAG, "Motion event: ${event.device?.name}")
        
        // Update visual feedback state
        ControllerInputState.updateMotionState(event)
        
        // Record macro if recording is active (only from selected device)
        if (isRecordingMacro && shouldProcessEvent(event)) {
            recordMotionEvent(event)
        }
        
        // Process analog input through our calibration system
        serviceScope.launch {
            val processedEvent = inputProcessor.processMotionEvent(event)
            if (processedEvent != null && processedEvent != event) {
                // Dispatch the calibrated event
                dispatchGenericMotionEvent(processedEvent)
            }
        }
        
        return true
    }
    
    private fun isControllerInput(event: KeyEvent): Boolean {
        return InputDiagnostics.shouldProcessKeyEvent(event)
    }
    
    private fun isControllerInput(event: MotionEvent): Boolean {
        return InputDiagnostics.shouldProcessMotionEvent(event)
    }
    
    private fun shouldProcessEvent(event: KeyEvent): Boolean {
        val selectedDevice = ControllerInputState.selectedDeviceId.value
        return selectedDevice == null || event.deviceId == selectedDevice
    }
    
    private fun shouldProcessEvent(event: MotionEvent): Boolean {
        val selectedDevice = ControllerInputState.selectedDeviceId.value
        return selectedDevice == null || event.deviceId == selectedDevice
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
            KeyEvent.KEYCODE_BUTTON_SELECT -> "BUTTON_SELECT"
            KeyEvent.KEYCODE_BUTTON_START -> "BUTTON_START"
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "BUTTON_THUMBL"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "BUTTON_THUMBR"
            KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
            else -> "UNKNOWN_$keyCode"
        }
    }
    
    // Macro recording functions
    fun startMacroRecording() {
        isRecordingMacro = true
        recordedActions.clear()
        recordingStartTime = System.currentTimeMillis()
        Log.d(TAG, "Started macro recording")
    }
    
    fun stopMacroRecording(): List<MacroAction> {
        isRecordingMacro = false
        val actions = recordedActions.toList()
        recordedActions.clear()
        Log.d(TAG, "Stopped macro recording, recorded ${actions.size} actions")
        return actions
    }
    
    private fun recordKeyEvent(event: KeyEvent) {
        val timestamp = System.currentTimeMillis() - recordingStartTime
        val buttonCode = getButtonCode(event.keyCode)
        val actionType = if (event.action == KeyEvent.ACTION_DOWN) MacroActionType.BUTTON_PRESS else MacroActionType.BUTTON_RELEASE
        
        val action = MacroAction(
            type = actionType,
            buttonCode = buttonCode,
            timestamp = timestamp
        )
        recordedActions.add(action)
        
        Log.d(TAG, "Recorded macro action: $actionType $buttonCode at ${timestamp}ms (total: ${recordedActions.size})")
    }
    
    private fun recordMotionEvent(event: MotionEvent) {
        val timestamp = System.currentTimeMillis() - recordingStartTime
        
        // Record significant axis movements
        val axes = listOf(
            MotionEvent.AXIS_X to "AXIS_X",
            MotionEvent.AXIS_Y to "AXIS_Y",
            MotionEvent.AXIS_Z to "AXIS_Z",
            MotionEvent.AXIS_RZ to "AXIS_RZ",
            MotionEvent.AXIS_LTRIGGER to "AXIS_LTRIGGER",
            MotionEvent.AXIS_RTRIGGER to "AXIS_RTRIGGER"
        )
        
        for ((axis, axisName) in axes) {
            val value = event.getAxisValue(axis)
            if (kotlin.math.abs(value) > 0.1f) { // Only record significant movements
                val action = MacroAction(
                    type = MacroActionType.AXIS_MOVE,
                    axisCode = axisName,
                    axisValue = value,
                    timestamp = timestamp
                )
                recordedActions.add(action)
            }
        }
    }
    
    private fun dispatchKeyEvent(event: KeyEvent) {
        // This would dispatch the remapped key event
        // Implementation depends on the specific remapping requirements
        Log.d(TAG, "Dispatching remapped key event: ${event.keyCode}")
    }
    
    private fun dispatchGenericMotionEvent(event: MotionEvent) {
        // This would dispatch the calibrated motion event
        // Implementation depends on the specific calibration requirements
        Log.d(TAG, "Dispatching calibrated motion event")
    }
}