package com.example.data.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

class VoiceRecorderHelper(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRecording = false

    private val _amplitudeFlow = MutableStateFlow(0f)
    val amplitudeFlow: StateFlow<Float> = _amplitudeFlow.asStateFlow()

    private val _recordDurationSec = MutableStateFlow(0)
    val recordDurationSec: StateFlow<Int> = _recordDurationSec.asStateFlow()

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    fun startRecording(outputFile: File, onFinished: (Boolean, File?) -> Unit) {
        if (isRecording) return

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onFinished(false, null)
                return
            }

            audioRecord?.startRecording()
            isRecording = true
            _recordDurationSec.value = 0

            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val pcmBuffer = ShortArray(bufferSize / 2)
                val rawPcmFile = File(context.cacheDir, "temp_raw_voice.pcm")
                val fos = FileOutputStream(rawPcmFile)

                var totalSamplesWritten = 0
                val startTime = System.currentTimeMillis()

                try {
                    while (isRecording && isActive) {
                        val readShorts = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                        if (readShorts > 0) {
                            val byteBuffer = ByteBuffer.allocate(readShorts * 2).order(ByteOrder.LITTLE_ENDIAN)
                            var sum = 0.0
                            for (s in 0 until readShorts) {
                                val sampleVal = pcmBuffer[s]
                                byteBuffer.putShort(sampleVal)
                                sum += sampleVal * sampleVal
                            }
                            fos.write(byteBuffer.array())
                            totalSamplesWritten += readShorts

                            val rms = sqrt(sum / readShorts)
                            val normalizedAmp = (rms / Short.MAX_VALUE).toFloat().coerceIn(0f, 1f)
                            _amplitudeFlow.value = normalizedAmp

                            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                            _recordDurationSec.value = elapsedSec
                        }
                    }
                } finally {
                    fos.close()
                }

                // Convert raw PCM to standard WAV
                convertPcmToWav(rawPcmFile, outputFile, totalSamplesWritten, sampleRate)
                rawPcmFile.delete()

                withContext(Dispatchers.Main) {
                    onFinished(totalSamplesWritten > sampleRate * 2, outputFile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
            onFinished(false, null)
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            recordingJob?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun convertPcmToWav(pcmFile: File, wavFile: File, totalSamples: Int, sampleRate: Int) {
        val pcmData = pcmFile.readBytes()
        val totalFileSize = 36 + pcmData.size
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * (bitsPerSample / 8)
        val blockAlign = channels * (bitsPerSample / 8)

        FileOutputStream(wavFile).use { fos ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
                put("RIFF".toByteArray(Charsets.US_ASCII))
                putInt(totalFileSize)
                put("WAVE".toByteArray(Charsets.US_ASCII))
                put("fmt ".toByteArray(Charsets.US_ASCII))
                putInt(16)
                putShort(1) // PCM
                putShort(channels.toShort())
                putInt(sampleRate)
                putInt(byteRate)
                putShort(blockAlign.toShort())
                putShort(bitsPerSample.toShort())
                put("data".toByteArray(Charsets.US_ASCII))
                putInt(pcmData.size)
            }
            fos.write(header.array())
            fos.write(pcmData)
        }
    }
}
