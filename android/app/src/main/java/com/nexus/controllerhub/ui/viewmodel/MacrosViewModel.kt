package com.nexus.controllerhub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.controllerhub.data.model.Macro
import com.nexus.controllerhub.data.repository.ControllerRepository
import com.nexus.controllerhub.service.ControllerAccessibilityService
import com.nexus.controllerhub.util.MacroPlayer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MacrosViewModel(
    private val repository: ControllerRepository
) : ViewModel() {
    
    val macros = repository.getAllMacros()
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    fun createEmptyMacro(name: String, description: String) {
        viewModelScope.launch {
            val macro = Macro(
                name = name,
                description = description
            )
            repository.insertMacro(macro)
        }
    }
    
    fun startRecording() {
        val service = ControllerAccessibilityService.instance
        if (service != null) {
            service.startMacroRecording()
            _isRecording.value = true
        }
    }
    
    fun stopRecording() {
        val service = ControllerAccessibilityService.instance
        if (service != null) {
            val actions = service.stopMacroRecording()
            _isRecording.value = false
            
            // Create macro with recorded actions
            viewModelScope.launch {
                val macro = Macro(
                    name = "Recorded Macro ${System.currentTimeMillis()}",
                    description = "Auto-recorded macro",
                    actions = actions,
                    totalDuration = actions.lastOrNull()?.timestamp ?: 0L
                )
                repository.insertMacro(macro)
            }
        }
    }
    
    fun deleteMacro(macroId: Long) {
        viewModelScope.launch {
            repository.deleteMacroById(macroId)
        }
    }
    
    fun testMacro(macroId: Long) {
        viewModelScope.launch {
            val macro = repository.getMacroById(macroId)
            if (macro != null) {
                // Test the macro using MacroPlayer
                // This would require a context, so we'll implement it in the service
                val service = ControllerAccessibilityService.instance
                // service?.testMacro(macro)
            }
        }
    }
}

class MacrosViewModelFactory(
    private val repository: ControllerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MacrosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MacrosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}