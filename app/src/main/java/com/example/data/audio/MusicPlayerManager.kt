package com.example.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.model.SongEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class MusicPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    val visualizerEngine = RealTimeAudioVisualizerEngine(context)

    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0)
    val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun playSong(song: SongEntity) {
        val audioFile = File(song.audioPath)
        if (!audioFile.exists()) {
            return
        }

        try {
            if (_currentSong.value?.id == song.id && mediaPlayer != null) {
                mediaPlayer?.start()
                _isPlaying.value = true
                visualizerEngine.onPlaybackStarted(mediaPlayer?.audioSessionId ?: 0, song.bpm, song.genre)
                startProgressTracker()
                return
            }

            mediaPlayer?.stop()
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(audioFile))
                isLooping = _isLooping.value
                prepare()
                start()
                setOnCompletionListener {
                    if (!_isLooping.value) {
                        _isPlaying.value = false
                        _currentPositionMs.value = 0
                        visualizerEngine.onPlaybackStopped()
                    }
                }
            }

            _currentSong.value = song
            _isPlaying.value = true
            _durationMs.value = mediaPlayer?.duration ?: (song.durationSeconds * 1000)
            _currentPositionMs.value = 0

            visualizerEngine.onPlaybackStarted(mediaPlayer?.audioSessionId ?: 0, song.bpm, song.genre)
            startProgressTracker()
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
            visualizerEngine.onPlaybackStopped()
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                visualizerEngine.onPlaybackPaused()
            } else {
                player.start()
                _isPlaying.value = true
                val song = _currentSong.value
                visualizerEngine.onPlaybackStarted(
                    player.audioSessionId,
                    song?.bpm ?: 120,
                    song?.genre ?: "Rap"
                )
                startProgressTracker()
            }
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs)
            _currentPositionMs.value = positionMs
        }
    }

    fun toggleLoop() {
        val newLoop = !_isLooping.value
        _isLooping.value = newLoop
        mediaPlayer?.isLooping = newLoop
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        _isPlaying.value = false
        _currentPositionMs.value = 0
        visualizerEngine.onPlaybackStopped()
        progressJob?.cancel()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPositionMs.value = player.currentPosition
                        _durationMs.value = player.duration
                    }
                }
                delay(200)
            }
        }
    }

    fun release() {
        stop()
        mediaPlayer?.release()
        mediaPlayer = null
        scope.cancel()
    }
}
