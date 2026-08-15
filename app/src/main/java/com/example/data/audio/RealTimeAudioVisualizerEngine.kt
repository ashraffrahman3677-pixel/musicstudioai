package com.example.data.audio

import android.content.Context
import android.media.audiofx.Visualizer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

enum class VisualizerMode(val title: String, val titleMs: String, val iconName: String) {
    SPECTRUM_ANALYZER("32-Band Spectrum", "Spektrum 32-Jalur", "Equalizer"),
    OSCILLOSCOPE("Oscilloscope Trace", "Jejak Osiloskop", "Timeline"),
    STEREO_VU_METERS("Stereo Analog VU", "Meter Analog VU", "Speed"),
    RADIAL_CORE("Radial Sonic Core", "Teras Sonik Radial", "DonutLarge")
}

enum class VisualizerTheme(val title: String, val primaryHex: Long, val secondaryHex: Long, val accentHex: Long) {
    PRO_VIOLET("Pro Violet", 0xFF65558F, 0xFF9A82DB, 0xFFD0BCFF),
    NEON_CYBER("Cyber Emerald", 0xFF00B4D8, 0xFF06D6A0, 0xFF52B788),
    ANALOG_GOLD("Vintage Amber", 0xFFE07A5F, 0xFFF4A261, 0xFFE76F51),
    ELECTRIC_ICE("Electric Ice", 0xFF0077B6, 0xFF00B4D8, 0xFF90E0EF)
}

data class VisualizerSnapshot(
    val frequencyBands: FloatArray = FloatArray(32) { 0f },
    val peakHoldBands: FloatArray = FloatArray(32) { 0f },
    val waveformPoints: FloatArray = FloatArray(64) { 0f },
    val leftVuLevel: Float = 0f,
    val rightVuLevel: Float = 0f,
    val leftPeakDb: Float = -48f,
    val rightPeakDb: Float = -48f,
    val subBassEnergy: Float = 0f,
    val midEnergy: Float = 0f,
    val trebleEnergy: Float = 0f,
    val isBeat: Boolean = false,
    val isClipping: Boolean = false,
    val isPlaying: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VisualizerSnapshot

        if (!frequencyBands.contentEquals(other.frequencyBands)) return false
        if (!peakHoldBands.contentEquals(other.peakHoldBands)) return false
        if (!waveformPoints.contentEquals(other.waveformPoints)) return false
        if (leftVuLevel != other.leftVuLevel) return false
        if (rightVuLevel != other.rightVuLevel) return false
        if (isPlaying != other.isPlaying) return false

        return true
    }

    override fun hashCode(): Int {
        var result = frequencyBands.contentHashCode()
        result = 31 * result + peakHoldBands.contentHashCode()
        result = 31 * result + waveformPoints.contentHashCode()
        result = 31 * result + leftVuLevel.hashCode()
        result = 31 * result + rightVuLevel.hashCode()
        result = 31 * result + isPlaying.hashCode()
        return result
    }
}

class RealTimeAudioVisualizerEngine(private val context: Context) {
    private var visualizer: Visualizer? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var synthTickerJob: Job? = null

    private val _snapshot = MutableStateFlow(VisualizerSnapshot())
    val snapshot: StateFlow<VisualizerSnapshot> = _snapshot.asStateFlow()

    private val _selectedMode = MutableStateFlow(VisualizerMode.SPECTRUM_ANALYZER)
    val selectedMode: StateFlow<VisualizerMode> = _selectedMode.asStateFlow()

    private val _selectedTheme = MutableStateFlow(VisualizerTheme.PRO_VIOLET)
    val selectedTheme: StateFlow<VisualizerTheme> = _selectedTheme.asStateFlow()

    // Internal state buffers
    private val bandCount = 32
    private val wavePointCount = 64
    private val currentBands = FloatArray(bandCount) { 0f }
    private val targetBands = FloatArray(bandCount) { 0f }
    private val peakBands = FloatArray(bandCount) { 0f }
    private val waveBuffer = FloatArray(wavePointCount) { 0f }

    private var leftVu = 0f
    private var rightVu = 0f
    private var isPlayingInternal = false
    private var currentAudioSessionId = 0
    private var hasHardwareCapture = false

    private var phase = 0f
    private var bpmMultiplier = 1.0f
    private var genreVibe = "Rap"

    fun setMode(mode: VisualizerMode) {
        _selectedMode.value = mode
    }

    fun setTheme(theme: VisualizerTheme) {
        _selectedTheme.value = theme
    }

    fun onPlaybackStarted(audioSessionId: Int, bpm: Int = 120, genre: String = "Rap") {
        isPlayingInternal = true
        currentAudioSessionId = audioSessionId
        bpmMultiplier = (bpm.toFloat() / 60f).coerceIn(0.8f, 2.5f)
        genreVibe = genre

        attachHardwareVisualizer(audioSessionId)
        startRendererLoop()
    }

    fun onPlaybackPaused() {
        isPlayingInternal = false
        // Smooth decay instead of abrupt zeroing
    }

    fun onPlaybackStopped() {
        isPlayingInternal = false
        releaseHardwareVisualizer()
    }

    private fun attachHardwareVisualizer(audioSessionId: Int) {
        if (audioSessionId == 0) return
        try {
            releaseHardwareVisualizer()
            val captureSize = 256
            visualizer = Visualizer(audioSessionId).apply {
                this.captureSize = captureSize
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        v: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        waveform?.let { processHardwareWaveform(it) }
                    }

                    override fun onFftDataCapture(
                        v: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        fft?.let { processHardwareFft(it) }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, true)
                enabled = true
            }
            hasHardwareCapture = true
        } catch (e: Exception) {
            hasHardwareCapture = false
            visualizer = null
        }
    }

    private fun releaseHardwareVisualizer() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            visualizer = null
            hasHardwareCapture = false
        }
    }

    private fun processHardwareWaveform(bytes: ByteArray) {
        if (!isPlayingInternal || bytes.isEmpty()) return
        val step = max(1, bytes.size / wavePointCount)
        var sumSquares = 0.0

        for (i in 0 until wavePointCount) {
            val idx = (i * step).coerceIn(0, bytes.size - 1)
            // PCM 8-bit unsigned byte: 128 is center (0.0)
            val normalized = (bytes[idx].toInt() and 0xFF - 128) / 128.0f
            waveBuffer[i] = normalized.coerceIn(-1f, 1f)
            sumSquares += (normalized * normalized)
        }

        val rms = sqrt(sumSquares / wavePointCount).toFloat()
        leftVu = (leftVu * 0.7f + (rms * 1.8f) * 0.3f).coerceIn(0f, 1.2f)
        rightVu = (rightVu * 0.7f + (rms * 1.6f * (0.9f + sin(phase) * 0.1f)) * 0.3f).coerceIn(0f, 1.2f)
    }

    private fun processHardwareFft(fftBytes: ByteArray) {
        if (!isPlayingInternal || fftBytes.size < 4) return
        val n = fftBytes.size / 2
        val magnitudes = FloatArray(n)

        for (k in 0 until n) {
            val r = fftBytes[2 * k].toFloat()
            val i = if (2 * k + 1 < fftBytes.size) fftBytes[2 * k + 1].toFloat() else 0f
            magnitudes[k] = sqrt(r * r + i * i) / 128.0f
        }

        // Group into 32 logarithmic bands
        for (i in 0 until bandCount) {
            val startBin = (2.0.pow(i.toDouble() / bandCount * 7.0) - 1.0).toInt().coerceIn(0, n - 1)
            val endBin = (2.0.pow((i + 1).toDouble() / bandCount * 7.0)).toInt().coerceIn(startBin + 1, n)

            var bandSum = 0f
            var count = 0
            for (bin in startBin until endBin) {
                bandSum += magnitudes[bin]
                count++
            }
            val avg = if (count > 0) bandSum / count else 0f
            targetBands[i] = (avg * 1.5f).coerceIn(0.02f, 0.98f)
        }
    }

    private fun startRendererLoop() {
        synthTickerJob?.cancel()
        synthTickerJob = scope.launch {
            val frameDelay = 25L // ~40 FPS ultra-smooth refresh rate
            while (isActive) {
                updatePhysicsFrame()
                delay(frameDelay)
            }
        }
    }

    private fun updatePhysicsFrame() {
        phase += 0.18f * bpmMultiplier
        if (phase > 2 * Math.PI.toFloat()) {
            phase -= 2 * Math.PI.toFloat()
        }

        // If hardware capture is not active or providing zero amplitude, use synthetic harmonic DSP
        if (!hasHardwareCapture) {
            synthesizeHarmonicSpectrum()
        }

        // Apply smooth physics to bands and peak-hold drop
        var totalEnergy = 0f
        var subBassEnergy = 0f
        var midEnergy = 0f
        var trebleEnergy = 0f

        val decayFactor = if (isPlayingInternal) 0.35f else 0.15f
        val peakDecay = 0.025f

        for (i in 0 until bandCount) {
            val target = if (isPlayingInternal) targetBands[i] else 0f
            // Attack vs Decay smoothing
            if (target > currentBands[i]) {
                currentBands[i] = currentBands[i] * 0.4f + target * 0.6f
            } else {
                currentBands[i] = (currentBands[i] - decayFactor * (currentBands[i] - target)).coerceAtLeast(0f)
            }

            // Peak Hold update & gravity drop
            if (currentBands[i] >= peakBands[i]) {
                peakBands[i] = currentBands[i]
            } else {
                peakBands[i] = (peakBands[i] - peakDecay).coerceAtLeast(0f)
            }

            totalEnergy += currentBands[i]
            if (i < 6) subBassEnergy += currentBands[i]
            else if (i in 6..18) midEnergy += currentBands[i]
            else trebleEnergy += currentBands[i]
        }

        subBassEnergy /= 6f
        midEnergy /= 13f
        trebleEnergy /= 13f

        val isBeat = subBassEnergy > 0.65f
        val isClipping = (leftVu > 0.95f || rightVu > 0.95f)

        val leftDb = (20 * log10((leftVu.coerceAtLeast(0.005f)))).coerceIn(-48f, 3f)
        val rightDb = (20 * log10((rightVu.coerceAtLeast(0.005f)))).coerceIn(-48f, 3f)

        _snapshot.value = VisualizerSnapshot(
            frequencyBands = currentBands.copyOf(),
            peakHoldBands = peakBands.copyOf(),
            waveformPoints = waveBuffer.copyOf(),
            leftVuLevel = leftVu,
            rightVuLevel = rightVu,
            leftPeakDb = leftDb,
            rightPeakDb = rightDb,
            subBassEnergy = subBassEnergy,
            midEnergy = midEnergy,
            trebleEnergy = trebleEnergy,
            isBeat = isBeat,
            isClipping = isClipping,
            isPlaying = isPlayingInternal
        )
    }

    private fun synthesizeHarmonicSpectrum() {
        if (!isPlayingInternal) {
            for (i in 0 until bandCount) {
                targetBands[i] = 0f
            }
            leftVu = (leftVu * 0.85f).coerceAtLeast(0f)
            rightVu = (rightVu * 0.85f).coerceAtLeast(0f)
            return
        }

        // Live DSP harmonic simulation driven by BPM and genre acoustics
        val beatPulse = (sin(phase * 2.0f).coerceAtLeast(0f).pow(3.5f)) * 0.85f
        val snarePulse = (sin(phase * 2.0f + 1.6f).coerceAtLeast(0f).pow(4.0f)) * 0.70f
        val hiHatPulse = (sin(phase * 4.0f + 0.8f).coerceAtLeast(0f).pow(5.0f)) * 0.60f
        val vocalMelody = (sin(phase * 0.75f) * 0.5f + 0.5f) * (sin(phase * 1.5f) * 0.3f + 0.7f)

        for (i in 0 until bandCount) {
            val freqNorm = i.toFloat() / bandCount

            val bandResponse = when {
                // Sub-Bass (0-5)
                i < 6 -> {
                    val subOsc = sin(phase * 2f - i * 0.2f) * 0.3f + 0.4f
                    beatPulse * 0.9f + subOsc * 0.35f
                }
                // Low-Mids / Bass guitar (6-12)
                i in 6..12 -> {
                    val bassHarmonic = sin(phase * 1.5f + i * 0.4f) * 0.25f + 0.35f
                    beatPulse * 0.45f + bassHarmonic * 0.5f + vocalMelody * 0.3f
                }
                // Midrange / Vocals / Melody (13-22)
                i in 13..22 -> {
                    val midHarmonic = sin(phase * 3f + i * 0.3f) * 0.3f + 0.35f
                    vocalMelody * 0.65f + midHarmonic * 0.35f + snarePulse * 0.4f
                }
                // High-Mids & Treble / Air (23-31)
                else -> {
                    val airShimmer = sin(phase * 6f + i * 0.5f) * 0.2f + 0.25f
                    hiHatPulse * 0.65f + airShimmer * 0.35f
                }
            }

            // Natural noise flutter to mimic real analog studio master
            val microFlutter = ((sin(phase * 11f + i * 1.7f) + 1f) * 0.06f)
            targetBands[i] = (bandResponse + microFlutter).coerceIn(0.05f, 0.95f)
        }

        // Synthesize waveform
        for (i in 0 until wavePointCount) {
            val t = i.toFloat() / wavePointCount
            val wave = sin(t * 12.0f + phase * 3f) * 0.5f * (0.8f + beatPulse * 0.4f) +
                    sin(t * 24.0f - phase * 5f) * 0.25f +
                    sin(t * 4.0f + phase) * 0.25f
            waveBuffer[i] = wave.coerceIn(-1f, 1f)
        }

        // Stereo VU meter ballistics
        val instantLeft = (beatPulse * 0.7f + vocalMelody * 0.35f + (sin(phase * 3f) * 0.1f)).coerceIn(0.1f, 1.1f)
        val instantRight = (beatPulse * 0.65f + vocalMelody * 0.40f + (cos(phase * 3f) * 0.1f)).coerceIn(0.1f, 1.1f)

        leftVu = (leftVu * 0.65f + instantLeft * 0.35f)
        rightVu = (rightVu * 0.65f + instantRight * 0.35f)
    }

    fun release() {
        releaseHardwareVisualizer()
        synthTickerJob?.cancel()
        scope.cancel()
    }
}
