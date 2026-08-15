package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val prompt: String,
    val lyrics: String,
    val language: String = "ms", // "ms", "en", "mixed"
    val genre: String = "Malay Rap",
    val subgenre: String = "Emotional Rap",
    val mood: String = "Reflective & Hopeful",
    val bpm: Int = 82,
    val musicalKey: String = "A Minor",
    val durationSeconds: Int = 180,
    val vocalType: String = "AI_VOCAL", // "AI_VOCAL", "USER_VOICE", "INSTRUMENTAL"
    val voiceProfileId: String? = null,
    val voiceProfileName: String? = null,
    val audioPath: String,
    val coverArtUrl: String? = null,
    val isFavorite: Boolean = false,
    val isInstrumental: Boolean = false,
    val productionStyle: String = "Studio Mastered",
    val playCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
