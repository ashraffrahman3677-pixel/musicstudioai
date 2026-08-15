package com.example.data.repository

import android.content.Context
import com.example.data.audio.AudioSynthesisEngine
import com.example.data.local.CreditDao
import com.example.data.local.GenerationJobDao
import com.example.data.local.SongDao
import com.example.data.model.CreditTransactionEntity
import com.example.data.model.GenerationJobEntity
import com.example.data.model.SongEntity
import com.example.data.model.VoiceProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

sealed class GenerationUiState {
    object Idle : GenerationUiState()
    data class Generating(val job: GenerationJobEntity, val progress: Float, val stageText: String) : GenerationUiState()
    data class Success(val song: SongEntity) : GenerationUiState()
    data class Error(val message: String) : GenerationUiState()
}

class GenerationRepository(
    private val context: Context,
    private val songDao: SongDao,
    private val generationJobDao: GenerationJobDao,
    private val creditDao: CreditDao,
    private val authRepository: AuthRepository
) {
    val activeJobs: Flow<List<GenerationJobEntity>> = generationJobDao.getActiveJobs()
    val allJobs: Flow<List<GenerationJobEntity>> = generationJobDao.getAllJobs()

    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationState: StateFlow<GenerationUiState> = _generationState.asStateFlow()

    fun resetState() {
        _generationState.value = GenerationUiState.Idle
    }

    suspend fun startSongGeneration(
        title: String,
        prompt: String,
        lyrics: String,
        language: String,
        genre: String,
        subgenre: String,
        mood: String,
        bpm: Int,
        musicalKey: String,
        durationSeconds: Int,
        vocalType: String,
        voiceProfile: VoiceProfileEntity? = null,
        productionStyle: String = "Studio Mastered"
    ): SongEntity? = withContext(Dispatchers.IO) {
        val cost = if (vocalType == "USER_VOICE") 15 else if (vocalType == "INSTRUMENTAL") 8 else 10
        val currentBalance = authRepository.creditsBalance.value

        if (currentBalance < cost) {
            _generationState.value = GenerationUiState.Error("Baki kredit tidak mencukupi ($currentBalance / $cost diperlukan).")
            return@withContext null
        }

        val songId = UUID.randomUUID().toString()
        val jobId = UUID.randomUUID().toString()

        var job = GenerationJobEntity(
            id = jobId,
            songId = songId,
            title = title,
            prompt = prompt,
            lyrics = lyrics,
            vocalType = vocalType,
            voiceProfileId = voiceProfile?.id,
            status = "QUEUED",
            progress = 0.05f,
            currentStage = "Queued in Studio Engine",
            creditCost = cost
        )
        generationJobDao.insertJob(job)
        _generationState.value = GenerationUiState.Generating(job, 0.05f, "Queued in Studio Engine")

        // Deduct credits with transaction record
        authRepository.deductCredits(cost, "Song Generation: $title ($genre)")

        try {
            delay(500) // Brief preparation
            job = job.copy(status = "PREPARING", progress = 0.15f, currentStage = "Preparing tracks and instruments")
            generationJobDao.updateJob(job)
            _generationState.value = GenerationUiState.Generating(job, 0.15f, "Preparing tracks and instruments")

            val provisionalSong = SongEntity(
                id = songId,
                title = title,
                prompt = prompt,
                lyrics = lyrics,
                language = language,
                genre = genre,
                subgenre = subgenre,
                mood = mood,
                bpm = bpm,
                musicalKey = musicalKey,
                durationSeconds = durationSeconds,
                vocalType = vocalType,
                voiceProfileId = voiceProfile?.id,
                voiceProfileName = voiceProfile?.name,
                audioPath = "",
                isInstrumental = (vocalType == "INSTRUMENTAL"),
                productionStyle = productionStyle
            )

            val voiceSampleFile = if (voiceProfile?.audioSamplePath != null) {
                File(voiceProfile.audioSamplePath)
            } else null

            val renderedAudioFile = AudioSynthesisEngine.renderSongAudio(
                context = context,
                song = provisionalSong,
                userVoiceSampleFile = voiceSampleFile
            ) { prog, stageText ->
                val updatedJob = job.copy(
                    progress = prog,
                    currentStage = stageText,
                    status = when {
                        prog < 0.35f -> "GENERATING_INSTRUMENTAL"
                        prog < 0.70f -> "GENERATING_VOCAL"
                        prog < 0.90f -> "MIXING"
                        else -> "MASTERING"
                    }
                )
                _generationState.value = GenerationUiState.Generating(updatedJob, prog, stageText)
            }

            val completedSong = provisionalSong.copy(
                audioPath = renderedAudioFile.absolutePath
            )

            songDao.insertSong(completedSong)

            job = job.copy(
                status = "COMPLETED",
                progress = 1.0f,
                currentStage = "Generation Completed",
                completedAt = System.currentTimeMillis()
            )
            generationJobDao.updateJob(job)

            _generationState.value = GenerationUiState.Success(completedSong)
            completedSong
        } catch (e: Exception) {
            e.printStackTrace()
            job = job.copy(
                status = "FAILED",
                errorMessage = e.message ?: "Unknown generation failure",
                completedAt = System.currentTimeMillis()
            )
            generationJobDao.updateJob(job)

            // Refund credits on failure
            authRepository.addCredits(cost, "Refund: Generation Failed for $title")
            _generationState.value = GenerationUiState.Error("Generation failed: ${e.message}")
            null
        }
    }
}
