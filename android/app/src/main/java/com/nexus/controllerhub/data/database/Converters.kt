package com.nexus.controllerhub.data.database

import androidx.room.TypeConverter
import com.nexus.controllerhub.data.model.AnalogSettings
import com.nexus.controllerhub.data.model.MacroAction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    
    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return Json.encodeToString(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        return Json.decodeFromString(value)
    }
    
    @TypeConverter
    fun fromLongMap(value: Map<String, Long>): String {
        return Json.encodeToString(value)
    }
    
    @TypeConverter
    fun toLongMap(value: String): Map<String, Long> {
        return Json.decodeFromString(value)
    }
    
    @TypeConverter
    fun fromAnalogSettings(value: AnalogSettings): String {
        return Json.encodeToString(value)
    }
    
    @TypeConverter
    fun toAnalogSettings(value: String): AnalogSettings {
        return Json.decodeFromString(value)
    }
    
    @TypeConverter
    fun fromMacroActionList(value: List<MacroAction>): String {
        return Json.encodeToString(value)
    }
    
    @TypeConverter
    fun toMacroActionList(value: String): List<MacroAction> {
        return Json.decodeFromString(value)
    }
    
    @TypeConverter
    fun fromFloatList(value: List<Float>): String {
        return Json.encodeToString(value)
    }
    
    @TypeConverter
    fun toFloatList(value: String): List<Float> {
        return Json.decodeFromString(value)
    }
}