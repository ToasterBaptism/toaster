package com.nexus.controllerhub

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.nexus.controllerhub.controller.ControllerManager
import com.nexus.controllerhub.ui.screen.MainNavigation
import com.nexus.controllerhub.ui.theme.NexusControllerHubTheme

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private lateinit var controllerManager: ControllerManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize the controller manager
        controllerManager = ControllerManager.getInstance(this)
        
        enableEdgeToEdge()
        setContent {
            NexusControllerHubTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainNavigation(
                        controllerManager = controllerManager,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
        
        // CRITICAL: Request focus to receive controller input
        setupInputFocus()
    }
    
    private fun setupInputFocus() {
        // Ensure the activity can receive input events
        window.decorView.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
        }
        
        // Set up input capture at the root view level
        window.decorView.setOnKeyListener { view, keyCode, event ->
            Log.d("MainActivity", "OnKeyListener: keyCode=$keyCode, action=${event.action}, device=${event.device?.name}")
            // Let the controller manager handle it first
            try {
                if (controllerManager.handleKeyEvent(event)) {
                    Log.d("MainActivity", "Event handled by controller manager")
                    true // Event was handled by controller manager
                } else {
                    Log.d("MainActivity", "Event not handled by controller manager")
                    false // Let the system handle it
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error handling key event", e)
                false
            }
        }
        
        window.decorView.setOnGenericMotionListener { view, event ->
            Log.d("MainActivity", "OnGenericMotionListener: device=${event.device?.name}")
            // Let the controller manager handle it first
            try {
                if (controllerManager.handleMotionEvent(event)) {
                    Log.d("MainActivity", "Motion event handled by controller manager")
                    true // Event was handled by controller manager
                } else {
                    Log.d("MainActivity", "Motion event not handled by controller manager")
                    false // Let the system handle it
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error handling motion event", e)
                false
            }
        }
        
        Log.d("MainActivity", "Input focus setup completed")
    }
    
    // CRITICAL: Override these methods to capture controller input
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        Log.d(TAG, "MainActivity received KeyEvent: action=${event.action}, keyCode=${event.keyCode}, deviceId=${event.deviceId}")
        
        // Try to handle as controller input first
        if (controllerManager.handleKeyEvent(event)) {
            Log.d(TAG, "KeyEvent handled by ControllerManager")
            return true // Controller event was handled
        }
        
        Log.d(TAG, "KeyEvent not handled by ControllerManager, passing to system")
        // Let the system handle non-controller events
        return super.dispatchKeyEvent(event)
    }
    
    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "MainActivity received MotionEvent: action=${event.action}, deviceId=${event.deviceId}")
        
        // Try to handle as controller motion first
        if (controllerManager.handleMotionEvent(event)) {
            Log.d(TAG, "MotionEvent handled by ControllerManager")
            return true // Controller motion was handled
        }
        
        Log.d(TAG, "MotionEvent not handled by ControllerManager, passing to system")
        // Let the system handle non-controller events
        return super.dispatchGenericMotionEvent(event)
    }
    
    override fun onResume() {
        super.onResume()
        // Controllers are automatically refreshed by the controller manager
        controllerManager.startTestMode()
        
        // Ensure we have focus for input capture
        window.decorView.requestFocus()
    }
    
    override fun onPause() {
        super.onPause()
        // Stop any ongoing macro recording when app goes to background
        if (controllerManager.isRecording.value) {
            controllerManager.stopMacroRecording()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up controller manager if needed
        if (controllerManager.isRecording.value) {
            controllerManager.stopMacroRecording()
        }
        controllerManager.stopTestMode()
    }
}