package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundAndHapticManager(private val context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    fun playClickSound(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            playTone(frequency = 880.0, durationMs = 35, volume = 0.25f)
        }
    }

    fun playCorrectSound(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            playTone(frequency = 587.33, durationMs = 60, volume = 0.35f) // D5
            playTone(frequency = 880.0, durationMs = 120, volume = 0.4f)  // A5
        }
    }

    fun playWrongSound(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            playTone(frequency = 311.13, durationMs = 90, volume = 0.35f) // Eb4
            playTone(frequency = 246.94, durationMs = 140, volume = 0.35f) // B3
        }
    }

    fun playCelebrationSound(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            for (note in notes) {
                playTone(frequency = note, durationMs = 80, volume = 0.4f)
            }
        }
    }

    fun triggerLightHaptic(enabled: Boolean) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20L)
            }
        } catch (_: Exception) {}
    }

    fun triggerSuccessHaptic(enabled: Boolean) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 30, 40, 50), -1)
            }
        } catch (_: Exception) {}
    }

    fun triggerErrorHaptic(enabled: Boolean) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 50, 60), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(70L)
            }
        } catch (_: Exception) {}
    }

    private fun playTone(frequency: Double, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 44100
            val numSamples = (durationMs * sampleRate) / 1000
            val generatedSnd = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val envelope = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1)
                    i > numSamples * 0.7 -> (numSamples - i) / (numSamples * 0.3)
                    else -> 1.0
                }
                val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
                val sample = (sin(angle) * 32767 * volume * envelope).toInt()
                generatedSnd[i] = sample.coerceIn(-32768, 32767).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 10)
            audioTrack.release()
        } catch (_: Exception) {}
    }
}
