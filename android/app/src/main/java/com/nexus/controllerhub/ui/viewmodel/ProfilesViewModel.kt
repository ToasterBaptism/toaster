package com.nexus.controllerhub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexus.controllerhub.data.model.ControllerProfile
import com.nexus.controllerhub.data.repository.ControllerRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfilesViewModel(
    private val repository: ControllerRepository
) : ViewModel() {
    
    val profiles = repository.getAllProfiles()
    val activeProfile = repository.getActiveProfileFlow()
    
    fun createProfile(name: String, description: String) {
        viewModelScope.launch {
            val profile = ControllerProfile(
                name = name,
                description = description,
                isActive = false
            )
            repository.insertProfile(profile)
        }
    }
    
    fun activateProfile(profileId: Long) {
        viewModelScope.launch {
            repository.setActiveProfile(profileId)
        }
    }
    
    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            repository.deleteProfileById(profileId)
        }
    }
    
    fun duplicateProfile(profileId: Long, newName: String) {
        viewModelScope.launch {
            repository.duplicateProfile(profileId, newName)
        }
    }
}

class ProfilesViewModelFactory(
    private val repository: ControllerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfilesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfilesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}