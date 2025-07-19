package com.nexus.controllerhub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "macros")
@Serializable
data class Macro(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val actions: List<MacroAction> = emptyList(),
    val totalDuration: Long = 0 // Total duration in milliseconds
)

@Serializable
data class MacroAction(
    val type: MacroActionType,
    val buttonCode: String = "",
    val axisCode: String = "",
    val axisValue: Float = 0f,
    val delayAfter: Long = 0, // Delay after this action in milliseconds
    val timestamp: Long = 0 // Relative timestamp from start of recording
)

@Serializable
enum class MacroActionType {
    BUTTON_PRESS,
    BUTTON_RELEASE,
    AXIS_MOVE,
    DELAY
}