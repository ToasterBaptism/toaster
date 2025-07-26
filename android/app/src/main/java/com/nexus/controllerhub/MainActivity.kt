package com.nexus.controllerhub

import android.os.Bundle
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
    }
    
    // CRITICAL: Override these methods to capture controller input
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Try to handle as controller input first
        if (controllerManager.handleKeyEvent(event)) {
            return true // Controller event was handled
        }
        // Let the system handle non-controller events
        return super.dispatchKeyEvent(event)
    }
    
    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        // Try to handle as controller motion first
        if (controllerManager.handleMotionEvent(event)) {
            return true // Controller motion was handled
        }
        // Let the system handle non-controller events
        return super.dispatchGenericMotionEvent(event)
    }
    
    override fun onResume() {
        super.onResume()
        // Re-enable test mode when app comes to foreground
        controllerManager.startTestMode()
    }
    
    override fun onPause() {
        super.onPause()
        // Disable test mode when app goes to background
        controllerManager.stopTestMode()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up controller manager if needed
        controllerManager.stopTestMode()
    }
}