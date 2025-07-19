package com.nexus.controllerhub.util

import android.view.KeyEvent
import android.view.MotionEvent
import com.nexus.controllerhub.data.repository.ControllerRepository
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

class ControllerInputProcessor(
    private val repository: ControllerRepository
) {
    
    suspend fun processKeyEvent(event: KeyEvent): KeyEvent? {
        val activeProfile = repository.getActiveProfile() ?: return null
        
        val buttonCode = getButtonCode(event.keyCode)
        val mappedButton = activeProfile.buttonMappings[buttonCode]
        
        return if (mappedButton != null && mappedButton != buttonCode) {
            // Create new event with remapped key code
            val newKeyCode = getKeyCodeFromButton(mappedButton)
            if (newKeyCode != -1) {
                KeyEvent(event.downTime, event.eventTime, event.action, newKeyCode, 
                        event.repeatCount, event.metaState, event.deviceId, event.scanCode)
            } else null
        } else null
    }
    
    suspend fun processMotionEvent(event: MotionEvent): MotionEvent? {
        val activeProfile = repository.getActiveProfile() ?: return null
        val analogSettings = activeProfile.analogSettings
        
        // Create a new motion event with calibrated values
        val newEvent = MotionEvent.obtain(event)
        var modified = false
        
        // Process left stick
        val leftX = event.getAxisValue(MotionEvent.AXIS_X)
        val leftY = event.getAxisValue(MotionEvent.AXIS_Y)
        val (calibratedLeftX, calibratedLeftY) = calibrateStick(
            leftX, leftY,
            analogSettings.leftStickDeadZoneInner,
            analogSettings.leftStickDeadZoneOuter,
            analogSettings.leftStickSensitivity,
            analogSettings.leftStickCurve
        )
        
        if (leftX != calibratedLeftX || leftY != calibratedLeftY) {
            // TODO: Create new MotionEvent with modified axis values
            // This requires using MotionEvent.obtain() with all parameters
            modified = true
        }
        
        // Process right stick
        val rightX = event.getAxisValue(MotionEvent.AXIS_Z)
        val rightY = event.getAxisValue(MotionEvent.AXIS_RZ)
        val (calibratedRightX, calibratedRightY) = calibrateStick(
            rightX, rightY,
            analogSettings.rightStickDeadZoneInner,
            analogSettings.rightStickDeadZoneOuter,
            analogSettings.rightStickSensitivity,
            analogSettings.rightStickCurve
        )
        
        if (rightX != calibratedRightX || rightY != calibratedRightY) {
            // TODO: Create new MotionEvent with modified axis values
            // This requires using MotionEvent.obtain() with all parameters
            modified = true
        }
        
        // Process triggers
        val leftTrigger = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
        val rightTrigger = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        
        val calibratedLeftTrigger = calibrateTrigger(leftTrigger, analogSettings.leftTriggerActuation)
        val calibratedRightTrigger = calibrateTrigger(rightTrigger, analogSettings.rightTriggerActuation)
        
        if (leftTrigger != calibratedLeftTrigger) {
            // TODO: Create new MotionEvent with modified axis values
            modified = true
        }
        
        if (rightTrigger != calibratedRightTrigger) {
            // TODO: Create new MotionEvent with modified axis values
            modified = true
        }
        
        return if (modified) newEvent else null
    }
    
    private fun calibrateStick(
        x: Float, y: Float,
        innerDeadZone: Float,
        outerDeadZone: Float,
        sensitivity: Float,
        curve: List<Float>
    ): Pair<Float, Float> {
        val magnitude = kotlin.math.sqrt(x * x + y * y)
        
        // Apply inner dead zone
        if (magnitude < innerDeadZone) {
            return Pair(0f, 0f)
        }
        
        // Apply outer dead zone
        val clampedMagnitude = if (magnitude > outerDeadZone) outerDeadZone else magnitude
        
        // Normalize to 0-1 range after dead zones
        val normalizedMagnitude = (clampedMagnitude - innerDeadZone) / (outerDeadZone - innerDeadZone)
        
        // Apply sensitivity curve
        val curvedMagnitude = applyCurve(normalizedMagnitude, curve) * sensitivity
        
        // Maintain direction
        val angle = kotlin.math.atan2(y.toDouble(), x.toDouble())
        val newX = (curvedMagnitude * kotlin.math.cos(angle)).toFloat()
        val newY = (curvedMagnitude * kotlin.math.sin(angle)).toFloat()
        
        return Pair(
            newX.coerceIn(-1f, 1f),
            newY.coerceIn(-1f, 1f)
        )
    }
    
    private fun calibrateTrigger(value: Float, actuationPoint: Float): Float {
        return if (value >= actuationPoint) 1f else 0f
    }
    
    private fun applyCurve(input: Float, curve: List<Float>): Float {
        if (curve.size < 2) return input
        
        val segments = curve.size - 1
        val segmentSize = 1f / segments
        val segmentIndex = (input / segmentSize).toInt().coerceIn(0, segments - 1)
        val segmentProgress = (input - segmentIndex * segmentSize) / segmentSize
        
        val startValue = curve[segmentIndex]
        val endValue = curve[segmentIndex + 1]
        
        return startValue + (endValue - startValue) * segmentProgress
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
            // Android system keys
            "BACK" -> KeyEvent.KEYCODE_BACK
            "HOME" -> KeyEvent.KEYCODE_HOME
            "MENU" -> KeyEvent.KEYCODE_MENU
            else -> -1
        }
    }
}