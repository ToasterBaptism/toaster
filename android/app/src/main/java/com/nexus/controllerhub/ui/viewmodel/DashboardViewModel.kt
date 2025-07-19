package com.nexus.controllerhub.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.controllerhub.data.model.ControllerProfile
import com.nexus.controllerhub.data.repository.ControllerRepository
import com.nexus.controllerhub.util.ControllerDetector
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: ControllerRepository,
    context: Context
) : ViewModel() {
    
    private val controllerDetector = ControllerDetector(context)
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    val connectedControllers = controllerDetector.connectedControllers
    val activeProfile = repository.getActiveProfileFlow()
    
    init {
        controllerDetector.startDetection()
        initializeDefaultProfile()
    }
    
    override fun onCleared() {
        super.onCleared()
        controllerDetector.stopDetection()
    }
    
    private fun initializeDefaultProfile() {
        viewModelScope.launch {
            // Create default profile if none exists
            val profiles = repository.getAllProfiles().first()
            if (profiles.isEmpty()) {
                repository.createDefaultProfile()
            }
        }
    }
    
    fun refreshControllers() {
        controllerDetector.startDetection()
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModelFactory(
    private val repository: ControllerRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}