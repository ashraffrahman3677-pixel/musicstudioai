package com.example.data.repository

import android.content.Context
import android.os.Environment
import com.example.data.audio.AudioSynthesisEngine
import com.example.data.local.SongDao
import com.example.data.model.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MusicRepository(
    private val context: Context,
    private val songDao: SongDao
) {
    val allSongs: Flow<List<SongEntity>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<SongEntity>> = songDao.getFavoriteSongs()
    val myVoiceSongs: Flow<List<SongEntity>> = songDao.getMyVoiceSongs()
    val instrumentalSongs: Flow<List<SongEntity>> = songDao.getInstrumentalSongs()

    suspend fun getSong(id: String): SongEntity? = withContext(Dispatchers.IO) {
        songDao.getSongById(id)
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        songDao.toggleFavorite(id, isFavorite)
    }

    suspend fun renameSong(id: String, newTitle: String) = withContext(Dispatchers.IO) {
        songDao.renameSong(id, newTitle)
    }

    suspend fun deleteSong(id: String) = withContext(Dispatchers.IO) {
        val song = songDao.getSongById(id)
        if (song != null) {
            try {
                val file = File(song.audioPath)
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            songDao.deleteSongById(id)
        }
    }

    suspend fun exportSongToDownloads(song: SongEntity): File? = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(song.audioPath)
            if (!sourceFile.exists()) return@withContext null

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val sanitizedTitle = song.title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
            val targetFile = File(downloadsDir, "$sanitizedTitle.wav")

            sourceFile.copyTo(targetFile, overwrite = true)
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun seedInitialSampleTrackIfEmpty() = withContext(Dispatchers.IO) {
        // Pre-seed an initial studio master song if user has no songs yet
        val songId = "initial_seed_track_01"
        val existing = songDao.getSongById(songId)
        if (existing == null) {
            val seedSong = SongEntity(
                id = songId,
                title = "Masih Berdiri (Still Standing)",
                prompt = "Buat lagu rap Melayu tentang depression, hidup susah tetapi akhirnya ada harapan",
                lyrics = """
[Intro]
Yeah... dari kegelapan, kita cari sinar.

[Verse 1]
Langkah kaki terasa berat, waktu terus berputar
Tiap malam mata terbuka, fikiran liar mengejar
Dunia kata aku lemah, tak mampu bertahan
Tapi dalam hati ini ada api takkan padam!

[Chorus]
Walau badai datang melanda jiwa
Ku tetap berdiri mendepani dunia!
Hilang rasa sakit, terbit kekuatan
Masih berdiri... oh masih berdiri!
                """.trimIndent(),
                language = "ms",
                genre = "Malay Rap",
                subgenre = "Emotional Rap",
                mood = "Reflective & Hopeful",
                bpm = 84,
                musicalKey = "A Minor",
                durationSeconds = 60,
                vocalType = "AI_VOCAL",
                audioPath = "",
                isFavorite = true
            )

            val renderedFile = AudioSynthesisEngine.renderSongAudio(context, seedSong) { _, _ -> }
            val completedSeedSong = seedSong.copy(audioPath = renderedFile.absolutePath)
            songDao.insertSong(completedSeedSong)
        }
    }
}
