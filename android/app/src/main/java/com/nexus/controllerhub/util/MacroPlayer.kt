package com.nexus.controllerhub.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import com.nexus.controllerhub.data.model.Macro
import com.nexus.controllerhub.data.model.MacroAction
import com.nexus.controllerhub.data.model.MacroActionType
import kotlinx.coroutines.*

class MacroPlayer(private val context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    private var currentPlaybackJob: Job? = null
    
    companion object {
        private const val TAG = "MacroPlayer"
    }
    
    fun playMacro(macro: Macro) {
        // Cancel any currently playing macro
        stopMacro()
        
        Log.d(TAG, "Playing macro: ${macro.name} with ${macro.actions.size} actions")
        
        currentPlaybackJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                playMacroActions(macro.actions)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing macro: ${e.message}")
            }
        }
    }
    
    fun stopMacro() {
        currentPlaybackJob?.cancel()
        currentPlaybackJob = null
        Log.d(TAG, "Macro playback stopped")
    }
    
    fun isPlaying(): Boolean = currentPlaybackJob?.isActive == true
    
    private suspend fun playMacroActions(actions: List<MacroAction>) {
        var lastTimestamp = 0L
        
        for (action in actions) {
            // Calculate delay from previous action
            val delay = if (lastTimestamp == 0L) 0L else action.timestamp - lastTimestamp
            
            if (delay > 0) {
                delay(delay)
            }
            
            // Execute the action
            when (action.type) {
                MacroActionType.BUTTON_PRESS -> {
                    executeButtonPress(action.buttonCode)
                }
                MacroActionType.BUTTON_RELEASE -> {
                    executeButtonRelease(action.buttonCode)
                }
                MacroActionType.AXIS_MOVE -> {
                    executeAxisMove(action.axisCode, action.axisValue)
                }
                MacroActionType.DELAY -> {
                    delay(action.delayAfter)
                }
            }
            
            // Add any additional delay specified for this action
            if (action.delayAfter > 0) {
                delay(action.delayAfter)
            }
            
            lastTimestamp = action.timestamp
        }
        
        Log.d(TAG, "Macro playback completed")
    }
    
    private fun executeButtonPress(buttonCode: String) {
        val keyCode = getKeyCodeFromButton(buttonCode)
        if (keyCode != -1) {
            val event = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            dispatchKeyEvent(event)
            Log.d(TAG, "Executed button press: $buttonCode")
        }
    }
    
    private fun executeButtonRelease(buttonCode: String) {
        val keyCode = getKeyCodeFromButton(buttonCode)
        if (keyCode != -1) {
            val event = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            dispatchKeyEvent(event)
            Log.d(TAG, "Executed button release: $buttonCode")
        }
    }
    
    private fun executeAxisMove(axisCode: String, value: Float) {
        val axis = getAxisFromCode(axisCode)
        if (axis != -1) {
            // Create a motion event with the specified axis value
            // This is a simplified implementation - in practice, you'd need to
            // create a proper MotionEvent with all required parameters
            Log.d(TAG, "Executed axis move: $axisCode = $value")
        }
    }
    
    private fun dispatchKeyEvent(event: KeyEvent) {
        // In a real implementation, this would dispatch the event through
        // the accessibility service or input injection mechanism
        handler.post {
            // Dispatch the event
            Log.d(TAG, "Dispatching key event: ${event.keyCode}, action: ${event.action}")
        }
    }
    
    private fun getKeyCodeFromButton(buttonCode: String): Int {
        return when (buttonCode) {
            "BUTTON_A" -> KeyEvent.KEYCODE_BUTTON_A
            "BUTTON_B" -> KeyEvent.KEYCODE_BUTTON_B
            "BUTTON_X" -> KeyEvent.KEYCODE_BUTTON_X
            "BUTTON_Y" -> KeyEvent.KEYCODE_BUTTON_Y
            "BUTTON_L1" -> KeyEvent.KEYCODE_BUTTON_L1
            "BUTTON_R1" -> KeyEvent.KEYCODE_BUTTON_R1
            "BUTTON_L2" -> KeyEvent.KEYCODE_BUTTON_L2
            "BUTTON_R2" -> KeyEvent.KEYCODE_BUTTON_R2
            "BUTTON_SELECT" -> KeyEvent.KEYCODE_BUTTON_SELECT
            "BUTTON_START" -> KeyEvent.KEYCODE_BUTTON_START
            "BUTTON_THUMBL" -> KeyEvent.KEYCODE_BUTTON_THUMBL
            "BUTTON_THUMBR" -> KeyEvent.KEYCODE_BUTTON_THUMBR
            "DPAD_UP" -> KeyEvent.KEYCODE_DPAD_UP
            "DPAD_DOWN" -> KeyEvent.KEYCODE_DPAD_DOWN
            "DPAD_LEFT" -> KeyEvent.KEYCODE_DPAD_LEFT
            "DPAD_RIGHT" -> KeyEvent.KEYCODE_DPAD_RIGHT
            else -> -1
        }
    }
    
    private fun getAxisFromCode(axisCode: String): Int {
        return when (axisCode) {
            "AXIS_X" -> MotionEvent.AXIS_X
            "AXIS_Y" -> MotionEvent.AXIS_Y
            "AXIS_Z" -> MotionEvent.AXIS_Z
            "AXIS_RZ" -> MotionEvent.AXIS_RZ
            "AXIS_LTRIGGER" -> MotionEvent.AXIS_LTRIGGER
            "AXIS_RTRIGGER" -> MotionEvent.AXIS_RTRIGGER
            else -> -1
        }
    }
}