package com.nexus.controllerhub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.controllerhub.data.model.AnalogSettings
import com.nexus.controllerhub.data.model.ControllerProfile
import com.nexus.controllerhub.data.repository.ControllerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConfigurationViewModel(
    private val repository: ControllerRepository,
    private val profileId: Long
) : ViewModel() {
    
    private val _profile = MutableStateFlow<ControllerProfile?>(null)
    val profile: StateFlow<ControllerProfile?> = _profile.asStateFlow()
    
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            val loadedProfile = repository.getProfileById(profileId)
            _profile.value = loadedProfile
        }
    }
    
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }
    
    fun updateButtonMapping(fromButton: String, toButton: String) {
        viewModelScope.launch {
            val currentProfile = _profile.value ?: return@launch
            val updatedMappings = currentProfile.buttonMappings.toMutableMap()
            updatedMappings[fromButton] = toButton
            
            val updatedProfile = currentProfile.copy(
                buttonMappings = updatedMappings,
                updatedAt = System.currentTimeMillis()
            )
            
            repository.updateProfile(updatedProfile)
            _profile.value = updatedProfile
        }
    }
    
    fun removeButtonMapping(button: String) {
        viewModelScope.launch {
            val currentProfile = _profile.value ?: return@launch
            val updatedMappings = currentProfile.buttonMappings.toMutableMap()
            updatedMappings.remove(button)
            
            val updatedProfile = currentProfile.copy(
                buttonMappings = updatedMappings,
                updatedAt = System.currentTimeMillis()
            )
            
            repository.updateProfile(updatedProfile)
            _profile.value = updatedProfile
        }
    }
    
    fun updateAnalogSettings(analogSettings: AnalogSettings) {
        viewModelScope.launch {
            val currentProfile = _profile.value ?: return@launch
            val updatedProfile = currentProfile.copy(
                analogSettings = analogSettings,
                updatedAt = System.currentTimeMillis()
            )
            
            repository.updateProfile(updatedProfile)
            _profile.value = updatedProfile
        }
    }
    
    fun updateMacroAssignment(button: String, macroId: Long?) {
        viewModelScope.launch {
            val currentProfile = _profile.value ?: return@launch
            val updatedAssignments = currentProfile.macroAssignments.toMutableMap()
            
            if (macroId != null) {
                updatedAssignments[button] = macroId
            } else {
                updatedAssignments.remove(button)
            }
            
            val updatedProfile = currentProfile.copy(
                macroAssignments = updatedAssignments,
                updatedAt = System.currentTimeMillis()
            )
            
            repository.updateProfile(updatedProfile)
            _profile.value = updatedProfile
        }
    }
}

class ConfigurationViewModelFactory(
    private val repository: ControllerRepository,
    private val profileId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfigurationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConfigurationViewModel(repository, profileId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}