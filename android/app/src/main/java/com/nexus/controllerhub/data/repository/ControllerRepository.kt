package com.nexus.controllerhub.data.repository

import com.nexus.controllerhub.data.database.ControllerProfileDao
import com.nexus.controllerhub.data.database.MacroDao
import com.nexus.controllerhub.data.model.ControllerProfile
import com.nexus.controllerhub.data.model.Macro
import kotlinx.coroutines.flow.Flow

class ControllerRepository(
    private val profileDao: ControllerProfileDao,
    private val macroDao: MacroDao
) {
    
    // Profile operations
    fun getAllProfiles(): Flow<List<ControllerProfile>> = profileDao.getAllProfiles()
    
    suspend fun getProfileById(id: Long): ControllerProfile? = profileDao.getProfileById(id)
    
    suspend fun getActiveProfile(): ControllerProfile? = profileDao.getActiveProfile()
    
    fun getActiveProfileFlow(): Flow<ControllerProfile?> = profileDao.getActiveProfileFlow()
    
    suspend fun insertProfile(profile: ControllerProfile): Long = profileDao.insertProfile(profile)
    
    suspend fun updateProfile(profile: ControllerProfile) = profileDao.updateProfile(profile)
    
    suspend fun deleteProfile(profile: ControllerProfile) = profileDao.deleteProfile(profile)
    
    suspend fun deleteProfileById(id: Long) = profileDao.deleteProfileById(id)
    
    suspend fun setActiveProfile(id: Long) = profileDao.setActiveProfile(id)
    
    suspend fun deactivateAllProfiles() = profileDao.deactivateAllProfiles()
    
    fun searchProfiles(query: String): Flow<List<ControllerProfile>> = 
        profileDao.searchProfiles("%$query%")
    
    // Macro operations
    fun getAllMacros(): Flow<List<Macro>> = macroDao.getAllMacros()
    
    suspend fun getMacroById(id: Long): Macro? = macroDao.getMacroById(id)
    
    suspend fun insertMacro(macro: Macro): Long = macroDao.insertMacro(macro)
    
    suspend fun updateMacro(macro: Macro) = macroDao.updateMacro(macro)
    
    suspend fun deleteMacro(macro: Macro) = macroDao.deleteMacro(macro)
    
    suspend fun deleteMacroById(id: Long) = macroDao.deleteMacroById(id)
    
    fun searchMacros(query: String): Flow<List<Macro>> = 
        macroDao.searchMacros("%$query%")
    
    suspend fun getMacrosByIds(macroIds: List<Long>): List<Macro> = 
        macroDao.getMacrosByIds(macroIds)
    
    // Utility functions
    suspend fun createDefaultProfile(): Long {
        val defaultProfile = ControllerProfile(
            name = "Default Profile",
            description = "Default controller configuration",
            isActive = true
        )
        return insertProfile(defaultProfile)
    }
    
    suspend fun duplicateProfile(originalId: Long, newName: String): Long? {
        val original = getProfileById(originalId) ?: return null
        val duplicate = original.copy(
            id = 0,
            name = newName,
            isActive = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return insertProfile(duplicate)
    }
}