package com.nexus.controllerhub.data.database

import androidx.room.*
import com.nexus.controllerhub.data.model.Macro
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {
    
    @Query("SELECT * FROM macros ORDER BY updatedAt DESC")
    fun getAllMacros(): Flow<List<Macro>>
    
    @Query("SELECT * FROM macros WHERE id = :id")
    suspend fun getMacroById(id: Long): Macro?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: Macro): Long
    
    @Update
    suspend fun updateMacro(macro: Macro)
    
    @Delete
    suspend fun deleteMacro(macro: Macro)
    
    @Query("DELETE FROM macros WHERE id = :id")
    suspend fun deleteMacroById(id: Long)
    
    @Query("SELECT COUNT(*) FROM macros")
    suspend fun getMacroCount(): Int
    
    @Query("SELECT * FROM macros WHERE name LIKE :searchQuery ORDER BY updatedAt DESC")
    fun searchMacros(searchQuery: String): Flow<List<Macro>>
    
    @Query("SELECT * FROM macros WHERE id IN (:macroIds)")
    suspend fun getMacrosByIds(macroIds: List<Long>): List<Macro>
}