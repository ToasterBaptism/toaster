package com.nexus.controllerhub.data.database

import androidx.room.*
import com.nexus.controllerhub.data.model.ControllerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ControllerProfileDao {
    
    @Query("SELECT * FROM controller_profiles ORDER BY updatedAt DESC")
    fun getAllProfiles(): Flow<List<ControllerProfile>>
    
    @Query("SELECT * FROM controller_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): ControllerProfile?
    
    @Query("SELECT * FROM controller_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfile(): ControllerProfile?
    
    @Query("SELECT * FROM controller_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfileFlow(): Flow<ControllerProfile?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ControllerProfile): Long
    
    @Update
    suspend fun updateProfile(profile: ControllerProfile)
    
    @Delete
    suspend fun deleteProfile(profile: ControllerProfile)
    
    @Query("DELETE FROM controller_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)
    
    @Query("UPDATE controller_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()
    
    @Query("UPDATE controller_profiles SET isActive = 1 WHERE id = :id")
    suspend fun activateProfile(id: Long)
    
    @Transaction
    suspend fun setActiveProfile(id: Long) {
        deactivateAllProfiles()
        activateProfile(id)
    }
    
    @Query("SELECT COUNT(*) FROM controller_profiles")
    suspend fun getProfileCount(): Int
    
    @Query("SELECT * FROM controller_profiles WHERE name LIKE :searchQuery ORDER BY updatedAt DESC")
    fun searchProfiles(searchQuery: String): Flow<List<ControllerProfile>>
}