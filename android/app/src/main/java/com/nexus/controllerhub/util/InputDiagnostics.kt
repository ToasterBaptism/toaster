package com.nexus.controllerhub.util

import android.content.Context
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

/**
 * Utility class for diagnosing input device issues and ensuring
 * touch input remains functional while processing controller input
 */
object InputDiagnostics {
    private const val TAG = "InputDiagnostics"
    
    /**
     * Log detailed information about an input device
     */
    fun logDeviceInfo(device: InputDevice?) {
        if (device == null) {
            Log.d(TAG, "Device is null")
            return
        }
        
        Log.d(TAG, "Device Info:")
        Log.d(TAG, "  Name: ${device.name}")
        Log.d(TAG, "  ID: ${device.id}")
        Log.d(TAG, "  Sources: ${device.sources} (${getSourcesString(device.sources)})")
        Log.d(TAG, "  Vendor ID: ${device.vendorId}")
        Log.d(TAG, "  Product ID: ${device.productId}")
        Log.d(TAG, "  Controller Number: ${device.controllerNumber}")
        Log.d(TAG, "  Is Virtual: ${device.isVirtual}")
    }
    
    /**
     * Convert input source flags to human-readable string
     */
    private fun getSourcesString(sources: Int): String {
        val sourceList = mutableListOf<String>()
        
        if ((sources and InputDevice.SOURCE_KEYBOARD) != 0) sourceList.add("KEYBOARD")
        if ((sources and InputDevice.SOURCE_DPAD) != 0) sourceList.add("DPAD")
        if ((sources and InputDevice.SOURCE_GAMEPAD) != 0) sourceList.add("GAMEPAD")
        if ((sources and InputDevice.SOURCE_TOUCHSCREEN) != 0) sourceList.add("TOUCHSCREEN")
        if ((sources and InputDevice.SOURCE_MOUSE) != 0) sourceList.add("MOUSE")
        if ((sources and InputDevice.SOURCE_STYLUS) != 0) sourceList.add("STYLUS")
        if ((sources and InputDevice.SOURCE_TRACKBALL) != 0) sourceList.add("TRACKBALL")
        if ((sources and InputDevice.SOURCE_TOUCHPAD) != 0) sourceList.add("TOUCHPAD")
        if ((sources and InputDevice.SOURCE_JOYSTICK) != 0) sourceList.add("JOYSTICK")
        
        return sourceList.joinToString(", ")
    }
    
    /**
     * Check if a KeyEvent should be processed by the controller service
     */
    fun shouldProcessKeyEvent(event: KeyEvent): Boolean {
        val device = event.device
        if (device == null) {
            Log.d(TAG, "KeyEvent has no device - skipping")
            return false
        }
        
        val sources = device.sources
        
        // Explicitly reject touchscreen events
        if ((sources and InputDevice.SOURCE_TOUCHSCREEN) != 0) {
            Log.d(TAG, "Rejecting KeyEvent from touchscreen device: ${device.name}")
            return false
        }
        
        // Reject pure keyboard events (unless they're also gamepad)
        if ((sources and InputDevice.SOURCE_KEYBOARD) != 0 && 
            (sources and InputDevice.SOURCE_GAMEPAD) == 0) {
            Log.d(TAG, "Rejecting KeyEvent from pure keyboard device: ${device.name}")
            return false
        }
        
        // Accept gamepad/joystick events
        val isController = (sources and InputDevice.SOURCE_GAMEPAD) != 0 ||
                          (sources and InputDevice.SOURCE_JOYSTICK) != 0 ||
                          (sources and InputDevice.SOURCE_DPAD) != 0
        
        if (isController) {
            Log.d(TAG, "Accepting KeyEvent from controller: ${device.name}")
        } else {
            Log.d(TAG, "Rejecting KeyEvent from non-controller device: ${device.name}")
        }
        
        return isController
    }
    
    /**
     * Check if a MotionEvent should be processed by the controller service
     */
    fun shouldProcessMotionEvent(event: MotionEvent): Boolean {
        val device = event.device
        if (device == null) {
            Log.d(TAG, "MotionEvent has no device - skipping")
            return false
        }
        
        val sources = device.sources
        
        // Explicitly reject touchscreen events
        if ((sources and InputDevice.SOURCE_TOUCHSCREEN) != 0) {
            Log.d(TAG, "Rejecting MotionEvent from touchscreen device: ${device.name}")
            return false
        }
        
        // Explicitly reject mouse events
        if ((sources and InputDevice.SOURCE_MOUSE) != 0) {
            Log.d(TAG, "Rejecting MotionEvent from mouse device: ${device.name}")
            return false
        }
        
        // Accept gamepad/joystick events
        val isController = (sources and InputDevice.SOURCE_GAMEPAD) != 0 ||
                          (sources and InputDevice.SOURCE_JOYSTICK) != 0
        
        if (isController) {
            Log.d(TAG, "Accepting MotionEvent from controller: ${device.name}")
        } else {
            Log.d(TAG, "Rejecting MotionEvent from non-controller device: ${device.name}")
        }
        
        return isController
    }
    
    /**
     * List all connected input devices for debugging
     */
    fun listAllInputDevices(context: Context) {
        Log.d(TAG, "=== All Input Devices ===")
        val deviceIds = InputDevice.getDeviceIds()
        
        for (deviceId in deviceIds) {
            val device = InputDevice.getDevice(deviceId)
            if (device != null) {
                Log.d(TAG, "Device $deviceId:")
                logDeviceInfo(device)
                Log.d(TAG, "---")
            }
        }
        Log.d(TAG, "=== End Device List ===")
    }
    
    /**
     * Check if touch input should be working
     */
    fun verifyTouchInputAvailable(context: Context): Boolean {
        val deviceIds = InputDevice.getDeviceIds()
        
        for (deviceId in deviceIds) {
            val device = InputDevice.getDevice(deviceId)
            if (device != null && (device.sources and InputDevice.SOURCE_TOUCHSCREEN) != 0) {
                Log.d(TAG, "Touch input device found: ${device.name}")
                return true
            }
        }
        
        Log.w(TAG, "No touch input devices found!")
        return false
    }
}