package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "generation_jobs")
data class GenerationJobEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val songId: String,
    val title: String,
    val prompt: String,
    val structuredJson: String = "",
    val lyrics: String = "",
    val vocalType: String = "AI_VOCAL",
    val voiceProfileId: String? = null,
    val status: String = "QUEUED", // "QUEUED", "PREPARING", "GENERATING_INSTRUMENTAL", "GENERATING_VOCAL", "MIXING", "MASTERING", "COMPLETED", "FAILED"
    val progress: Float = 0f,
    val currentStage: String = "Queued",
    val musicProvider: String = "Gemini Lyria Engine",
    val voiceProvider: String = "VoiceClone DSP v2",
    val creditCost: Int = 10,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
