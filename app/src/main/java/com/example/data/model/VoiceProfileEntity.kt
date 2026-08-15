package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "voice_profiles")
data class VoiceProfileEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "user_default",
    val name: String,
    val language: String = "ms",
    val status: String = "READY", // "PENDING_RECORDING", "PROCESSING", "READY", "DELETED"
    val sampleCount: Int = 1,
    val challengePhrase: String = "Biru tujuh sungai empat puluh sembilan",
    val audioSamplePath: String? = null,
    val consentId: String = UUID.randomUUID().toString(),
    val consentHash: String = "SHA256-VERIFIED-OWNER-CONSENT",
    val consentTimestamp: Long = System.currentTimeMillis(),
    val qualityScore: Float = 0.96f,
    val createdAt: Long = System.currentTimeMillis()
)
