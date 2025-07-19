package com.nexus.controllerhub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.controllerhub.data.model.Macro
import com.nexus.controllerhub.data.model.MacroAction
import com.nexus.controllerhub.data.repository.ControllerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MacroEditorViewModel(
    private val repository: ControllerRepository,
    private val macroId: Long
) : ViewModel() {
    
    private val _macro = MutableStateFlow<Macro?>(null)
    val macro: StateFlow<Macro?> = _macro.asStateFlow()
    
    init {
        loadMacro()
    }
    
    private fun loadMacro() {
        viewModelScope.launch {
            val loadedMacro = repository.getMacroById(macroId)
            _macro.value = loadedMacro
        }
    }
    
    fun editAction(index: Int, newAction: MacroAction) {
        val currentMacro = _macro.value ?: return
        val updatedActions = currentMacro.actions.toMutableList()
        
        if (index in updatedActions.indices) {
            updatedActions[index] = newAction
            val updatedMacro = currentMacro.copy(
                actions = updatedActions,
                updatedAt = System.currentTimeMillis(),
                totalDuration = updatedActions.lastOrNull()?.timestamp ?: 0L
            )
            _macro.value = updatedMacro
        }
    }
    
    fun deleteAction(index: Int) {
        val currentMacro = _macro.value ?: return
        val updatedActions = currentMacro.actions.toMutableList()
        
        if (index in updatedActions.indices) {
            updatedActions.removeAt(index)
            val updatedMacro = currentMacro.copy(
                actions = updatedActions,
                updatedAt = System.currentTimeMillis(),
                totalDuration = updatedActions.lastOrNull()?.timestamp ?: 0L
            )
            _macro.value = updatedMacro
        }
    }
    
    fun addAction(action: MacroAction) {
        val currentMacro = _macro.value ?: return
        val updatedActions = currentMacro.actions.toMutableList()
        updatedActions.add(action)
        
        val updatedMacro = currentMacro.copy(
            actions = updatedActions,
            updatedAt = System.currentTimeMillis(),
            totalDuration = updatedActions.lastOrNull()?.timestamp ?: 0L
        )
        _macro.value = updatedMacro
    }
    
    fun saveMacro() {
        viewModelScope.launch {
            val currentMacro = _macro.value ?: return@launch
            repository.updateMacro(currentMacro)
        }
    }
}

class MacroEditorViewModelFactory(
    private val repository: ControllerRepository,
    private val macroId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MacroEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MacroEditorViewModel(repository, macroId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}