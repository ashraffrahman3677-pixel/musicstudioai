package com.example

import android.app.Application
import com.example.data.audio.MusicPlayerManager
import com.example.data.audio.VoiceRecorderHelper
import com.example.data.local.AppDatabase
import com.example.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AIMusicApp : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var musicRepository: MusicRepository
        private set

    lateinit var voiceProfileRepository: VoiceProfileRepository
        private set

    lateinit var songDirectorRepository: SongDirectorRepository
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var generationRepository: GenerationRepository
        private set

    lateinit var playerManager: MusicPlayerManager
        private set

    lateinit var voiceRecorderHelper: VoiceRecorderHelper
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        musicRepository = MusicRepository(this, database.songDao())
        voiceProfileRepository = VoiceProfileRepository(this, database.voiceProfileDao())
        songDirectorRepository = SongDirectorRepository()
        authRepository = AuthRepository(database.creditDao())
        generationRepository = GenerationRepository(
            this,
            database.songDao(),
            database.generationJobDao(),
            database.creditDao(),
            authRepository
        )
        playerManager = MusicPlayerManager(this)
        voiceRecorderHelper = VoiceRecorderHelper(this)

        // Pre-seed sample track in background
        CoroutineScope(Dispatchers.IO).launch {
            musicRepository.seedInitialSampleTrackIfEmpty()
        }
    }
}
