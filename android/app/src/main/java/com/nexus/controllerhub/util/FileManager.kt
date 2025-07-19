package com.nexus.controllerhub.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.nexus.controllerhub.data.model.ControllerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileManager(private val context: Context) {
    
    private val profilesDir = File(context.filesDir, "profiles")
    
    init {
        if (!profilesDir.exists()) {
            profilesDir.mkdirs()
        }
    }
    
    suspend fun exportProfile(profile: ControllerProfile): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "${profile.name.replace("[^a-zA-Z0-9]".toRegex(), "_")}_${System.currentTimeMillis()}.json"
            val file = File(profilesDir, fileName)
            
            val json = Json.encodeToString(profile)
            FileOutputStream(file).use { output ->
                output.write(json.toByteArray())
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: IOException) {
            null
        }
    }
    
    suspend fun importProfile(uri: Uri): ControllerProfile? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader()?.use { it.readText() }
            
            if (json != null) {
                Json.decodeFromString<ControllerProfile>(json).copy(
                    id = 0, // Reset ID for new profile
                    isActive = false, // Don't activate imported profile automatically
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun getProfilesDirectory(): File = profilesDir
    
    suspend fun cleanupOldFiles(maxAgeMs: Long = 7 * 24 * 60 * 60 * 1000L) = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        profilesDir.listFiles()?.forEach { file ->
            if (currentTime - file.lastModified() > maxAgeMs) {
                file.delete()
            }
        }
    }
}