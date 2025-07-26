package com.nexus.controllerhub.controller

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class ButtonMapping(
    val originalKeyCode: Int,
    val originalButtonName: String,
    val mappedKeyCode: Int,
    val mappedButtonName: String,
    val description: String = "",
    val isEnabled: Boolean = true
)

@Serializable
data class RemappingProfile(
    val id: String,
    val name: String,
    val description: String,
    val mappings: List<ButtonMapping>,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
)

class ButtonRemappingManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "ButtonRemappingManager"
        private const val PREFS_NAME = "button_remapping_prefs"
        private const val KEY_ACTIVE_PROFILE = "active_profile"
        private const val KEY_PROFILES = "profiles"
        
        @Volatile
        private var INSTANCE: ButtonRemappingManager? = null
        
        fun getInstance(context: Context): ButtonRemappingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ButtonRemappingManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _profiles = MutableStateFlow<List<RemappingProfile>>(emptyList())
    val profiles: StateFlow<List<RemappingProfile>> = _profiles.asStateFlow()
    
    private val _activeProfile = MutableStateFlow<RemappingProfile?>(null)
    val activeProfile: StateFlow<RemappingProfile?> = _activeProfile.asStateFlow()
    
    private val _isRemappingEnabled = MutableStateFlow(true)
    val isRemappingEnabled: StateFlow<Boolean> = _isRemappingEnabled.asStateFlow()
    
    init {
        loadProfiles()
        loadActiveProfile()
        
        // Create default profile if none exist
        if (_profiles.value.isEmpty()) {
            createDefaultProfile()
        }
        
        Log.i(TAG, "ButtonRemappingManager initialized with ${_profiles.value.size} profiles")
    }
    
    private fun loadProfiles() {
        try {
            val profilesJson = prefs.getString(KEY_PROFILES, null)
            if (profilesJson != null) {
                val profiles = json.decodeFromString<List<RemappingProfile>>(profilesJson)
                _profiles.value = profiles
                Log.d(TAG, "Loaded ${profiles.size} profiles from storage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profiles", e)
            _profiles.value = emptyList()
        }
    }
    
    private fun loadActiveProfile() {
        try {
            val activeProfileId = prefs.getString(KEY_ACTIVE_PROFILE, null)
            if (activeProfileId != null) {
                val profile = _profiles.value.find { it.id == activeProfileId }
                _activeProfile.value = profile
                Log.d(TAG, "Loaded active profile: ${profile?.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading active profile", e)
        }
    }
    
    private fun saveProfiles() {
        try {
            val profilesJson = json.encodeToString(_profiles.value)
            prefs.edit().putString(KEY_PROFILES, profilesJson).apply()
            Log.d(TAG, "Saved ${_profiles.value.size} profiles to storage")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profiles", e)
        }
    }
    
    private fun saveActiveProfile() {
        try {
            val activeProfileId = _activeProfile.value?.id
            prefs.edit().putString(KEY_ACTIVE_PROFILE, activeProfileId).apply()
            Log.d(TAG, "Saved active profile: ${_activeProfile.value?.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving active profile", e)
        }
    }
    
    private fun createDefaultProfile() {
        val defaultMappings = getDefaultButtonMappings()
        val defaultProfile = RemappingProfile(
            id = "default",
            name = "Default",
            description = "Standard controller mapping",
            mappings = defaultMappings,
            isActive = true
        )
        
        _profiles.value = listOf(defaultProfile)
        _activeProfile.value = defaultProfile
        saveProfiles()
        saveActiveProfile()
        
        Log.i(TAG, "Created default remapping profile")
    }
    
    private fun getDefaultButtonMappings(): List<ButtonMapping> {
        return listOf(
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_A, "A", KeyEvent.KEYCODE_BUTTON_A, "A", "Primary action button"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_B, "B", KeyEvent.KEYCODE_BUTTON_B, "B", "Secondary action button"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_X, "X", KeyEvent.KEYCODE_BUTTON_X, "X", "Tertiary action button"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_Y, "Y", KeyEvent.KEYCODE_BUTTON_Y, "Y", "Quaternary action button"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_L1, "L1", KeyEvent.KEYCODE_BUTTON_L1, "L1", "Left shoulder button"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_R1, "R1", KeyEvent.KEYCODE_BUTTON_R1, "R1", "Right shoulder button"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_L2, "L2", KeyEvent.KEYCODE_BUTTON_L2, "L2", "Left trigger"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_R2, "R2", KeyEvent.KEYCODE_BUTTON_R2, "R2", "Right trigger"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_THUMBL, "LS", KeyEvent.KEYCODE_BUTTON_THUMBL, "LS", "Left stick click"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_THUMBR, "RS", KeyEvent.KEYCODE_BUTTON_THUMBR, "RS", "Right stick click"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_START, "START", KeyEvent.KEYCODE_BUTTON_START, "START", "Start/Menu button"),
            ButtonMapping(KeyEvent.KEYCODE_BUTTON_SELECT, "SELECT", KeyEvent.KEYCODE_BUTTON_SELECT, "SELECT", "Select/Back button"),
            ButtonMapping(KeyEvent.KEYCODE_DPAD_UP, "DPAD_UP", KeyEvent.KEYCODE_DPAD_UP, "DPAD_UP", "D-pad up"),
            ButtonMapping(KeyEvent.KEYCODE_DPAD_DOWN, "DPAD_DOWN", KeyEvent.KEYCODE_DPAD_DOWN, "DPAD_DOWN", "D-pad down"),
            ButtonMapping(KeyEvent.KEYCODE_DPAD_LEFT, "DPAD_LEFT", KeyEvent.KEYCODE_DPAD_LEFT, "DPAD_LEFT", "D-pad left"),
            ButtonMapping(KeyEvent.KEYCODE_DPAD_RIGHT, "DPAD_RIGHT", KeyEvent.KEYCODE_DPAD_RIGHT, "DPAD_RIGHT", "D-pad right")
        )
    }
    
    // Core remapping functionality
    fun remapKeyCode(originalKeyCode: Int): Int {
        if (!_isRemappingEnabled.value) return originalKeyCode
        
        val activeProfile = _activeProfile.value ?: return originalKeyCode
        val mapping = activeProfile.mappings.find { 
            it.originalKeyCode == originalKeyCode && it.isEnabled 
        }
        
        return mapping?.mappedKeyCode ?: originalKeyCode
    }
    
    fun getButtonName(keyCode: Int): String {
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> "A"
            KeyEvent.KEYCODE_BUTTON_B -> "B"
            KeyEvent.KEYCODE_BUTTON_X -> "X"
            KeyEvent.KEYCODE_BUTTON_Y -> "Y"
            KeyEvent.KEYCODE_BUTTON_L1 -> "L1"
            KeyEvent.KEYCODE_BUTTON_R1 -> "R1"
            KeyEvent.KEYCODE_BUTTON_L2 -> "L2"
            KeyEvent.KEYCODE_BUTTON_R2 -> "R2"
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "LS"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "RS"
            KeyEvent.KEYCODE_BUTTON_START -> "START"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "SELECT"
            KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
            else -> "UNKNOWN_$keyCode"
        }
    }
    
    // Profile management
    fun createProfile(name: String, description: String = ""): RemappingProfile {
        val newProfile = RemappingProfile(
            id = "profile_${System.currentTimeMillis()}",
            name = name,
            description = description,
            mappings = getDefaultButtonMappings()
        )
        
        val updatedProfiles = _profiles.value.toMutableList()
        updatedProfiles.add(newProfile)
        _profiles.value = updatedProfiles
        saveProfiles()
        
        Log.i(TAG, "Created new profile: $name")
        return newProfile
    }
    
    fun deleteProfile(profileId: String): Boolean {
        if (profileId == "default") {
            Log.w(TAG, "Cannot delete default profile")
            return false
        }
        
        val updatedProfiles = _profiles.value.filter { it.id != profileId }
        _profiles.value = updatedProfiles
        
        // If deleted profile was active, switch to default
        if (_activeProfile.value?.id == profileId) {
            val defaultProfile = updatedProfiles.find { it.id == "default" }
            _activeProfile.value = defaultProfile
            saveActiveProfile()
        }
        
        saveProfiles()
        Log.i(TAG, "Deleted profile: $profileId")
        return true
    }
    
    fun setActiveProfile(profileId: String) {
        val profile = _profiles.value.find { it.id == profileId }
        if (profile != null) {
            _activeProfile.value = profile
            saveActiveProfile()
            Log.i(TAG, "Set active profile: ${profile.name}")
        } else {
            Log.w(TAG, "Profile not found: $profileId")
        }
    }
    
    fun updateProfileMapping(profileId: String, mapping: ButtonMapping) {
        val updatedProfiles = _profiles.value.map { profile ->
            if (profile.id == profileId) {
                val updatedMappings = profile.mappings.map { existingMapping ->
                    if (existingMapping.originalKeyCode == mapping.originalKeyCode) {
                        mapping
                    } else {
                        existingMapping
                    }
                }
                profile.copy(mappings = updatedMappings)
            } else {
                profile
            }
        }
        
        _profiles.value = updatedProfiles
        
        // Update active profile if it's the one being modified
        if (_activeProfile.value?.id == profileId) {
            _activeProfile.value = updatedProfiles.find { it.id == profileId }
        }
        
        saveProfiles()
        Log.d(TAG, "Updated mapping for profile $profileId: ${mapping.originalButtonName} -> ${mapping.mappedButtonName}")
    }
    
    fun addCustomMapping(profileId: String, originalKeyCode: Int, mappedKeyCode: Int) {
        val originalName = getButtonName(originalKeyCode)
        val mappedName = getButtonName(mappedKeyCode)
        
        val newMapping = ButtonMapping(
            originalKeyCode = originalKeyCode,
            originalButtonName = originalName,
            mappedKeyCode = mappedKeyCode,
            mappedButtonName = mappedName,
            description = "Custom mapping: $originalName -> $mappedName"
        )
        
        updateProfileMapping(profileId, newMapping)
    }
    
    fun toggleRemapping(enabled: Boolean) {
        _isRemappingEnabled.value = enabled
        Log.i(TAG, "Button remapping ${if (enabled) "enabled" else "disabled"}")
    }
    
    fun exportProfile(profileId: String): String? {
        val profile = _profiles.value.find { it.id == profileId }
        return if (profile != null) {
            try {
                json.encodeToString(profile)
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting profile", e)
                null
            }
        } else {
            null
        }
    }
    
    fun importProfile(profileJson: String): Boolean {
        return try {
            val profile = json.decodeFromString<RemappingProfile>(profileJson)
            val newProfile = profile.copy(
                id = "imported_${System.currentTimeMillis()}",
                isActive = false
            )
            
            val updatedProfiles = _profiles.value.toMutableList()
            updatedProfiles.add(newProfile)
            _profiles.value = updatedProfiles
            saveProfiles()
            
            Log.i(TAG, "Imported profile: ${newProfile.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing profile", e)
            false
        }
    }
}