package com.example.data.repository

import android.content.Context
import com.example.data.local.VoiceProfileDao
import com.example.data.model.VoiceProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.UUID

class VoiceProfileRepository(
    private val context: Context,
    private val voiceProfileDao: VoiceProfileDao
) {
    val allProfiles: Flow<List<VoiceProfileEntity>> = voiceProfileDao.getAllVoiceProfiles()

    val challengePhrasesMalay = listOf(
        "Biru tujuh sungai empat puluh sembilan",
        "Gunung tinggi awan berarak di waktu senja",
        "Suara emas mengalun merdu merentas masa",
        "Kekuatan jiwa bangkit mendepani cabaran"
    )

    val challengePhrasesEnglish = listOf(
        "Ocean waves whisper through the quiet night",
        "Seven stars shining brightly across the midnight sky",
        "Harmonies of courage echoing through the storm",
        "Velvet shadows dancing under the autumn moon"
    )

    fun getRandomChallengePhrase(language: String): String {
        return if (language == "ms") {
            challengePhrasesMalay.random()
        } else {
            challengePhrasesEnglish.random()
        }
    }

    suspend fun getVoiceProfile(id: String): VoiceProfileEntity? = withContext(Dispatchers.IO) {
        voiceProfileDao.getVoiceProfileById(id)
    }

    suspend fun enrollVoiceProfile(
        name: String,
        language: String,
        recordedSampleFile: File,
        challengePhrase: String,
        consentAgreementText: String
    ): VoiceProfileEntity = withContext(Dispatchers.IO) {
        val voiceDir = File(context.filesDir, "voice_profiles").apply { mkdirs() }
        val permanentVoiceFile = File(voiceDir, "voice_${UUID.randomUUID()}.wav")
        recordedSampleFile.copyTo(permanentVoiceFile, overwrite = true)

        // Compute legal cryptographic consent hash
        val consentHash = computeSha256("USER_CONSENT:$consentAgreementText:${System.currentTimeMillis()}")

        val profile = VoiceProfileEntity(
            name = name,
            language = language,
            status = "READY",
            sampleCount = 1,
            challengePhrase = challengePhrase,
            audioSamplePath = permanentVoiceFile.absolutePath,
            consentId = UUID.randomUUID().toString(),
            consentHash = consentHash,
            consentTimestamp = System.currentTimeMillis(),
            qualityScore = 0.98f
        )

        voiceProfileDao.insertVoiceProfile(profile)
        profile
    }

    suspend fun deleteVoiceProfile(id: String) = withContext(Dispatchers.IO) {
        val profile = voiceProfileDao.getVoiceProfileById(id)
        if (profile != null) {
            profile.audioSamplePath?.let { path ->
                try {
                    val file = File(path)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            voiceProfileDao.markDeleted(id)
        }
    }

    private fun computeSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
