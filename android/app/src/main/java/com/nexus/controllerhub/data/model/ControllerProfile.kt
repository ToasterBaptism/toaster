package com.nexus.controllerhub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "controller_profiles")
@Serializable
data class ControllerProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val buttonMappings: Map<String, String> = emptyMap(),
    val analogSettings: AnalogSettings = AnalogSettings(),
    val macroAssignments: Map<String, Long> = emptyMap() // Button to macro ID mapping
)

@Serializable
data class AnalogSettings(
    val leftStickDeadZoneInner: Float = 0.1f,
    val leftStickDeadZoneOuter: Float = 0.95f,
    val leftStickSensitivity: Float = 1.0f,
    val rightStickDeadZoneInner: Float = 0.1f,
    val rightStickDeadZoneOuter: Float = 0.95f,
    val rightStickSensitivity: Float = 1.0f,
    val leftTriggerActuation: Float = 0.5f,
    val rightTriggerActuation: Float = 0.5f,
    val leftStickCurve: List<Float> = listOf(0f, 0.25f, 0.5f, 0.75f, 1f),
    val rightStickCurve: List<Float> = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
)