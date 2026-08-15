package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CreditTransactionEntity
import com.example.data.model.GenerationJobEntity
import com.example.data.model.SongEntity
import com.example.data.model.VoiceProfileEntity

@Database(
    entities = [
        SongEntity::class,
        VoiceProfileEntity::class,
        GenerationJobEntity::class,
        CreditTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun voiceProfileDao(): VoiceProfileDao
    abstract fun generationJobDao(): GenerationJobDao
    abstract fun creditDao(): CreditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_music_studio.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
