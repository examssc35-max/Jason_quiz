package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val theme: String = "system", // "system", "dark", "light"
    val blurIntensity: String = "medium", // "low", "medium", "high"
    val accentColorIndex: Int = 0, // 0 = Royal Blue, 1 = Cyan, 2 = Purple, 3 = Emerald
    val soundEffects: Boolean = true,
    val vibration: Boolean = true,
    val showExplanations: Boolean = true,
    val reduceMotion: Boolean = false,
    val autoAdvance: Boolean = false
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("quiz_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            theme = prefs.getString("theme", "system") ?: "system",
            blurIntensity = prefs.getString("blur_intensity", "medium") ?: "medium",
            accentColorIndex = prefs.getInt("accent_color", 0),
            soundEffects = prefs.getBoolean("sound_effects", true),
            vibration = prefs.getBoolean("vibration", true),
            showExplanations = prefs.getBoolean("show_explanations", true),
            reduceMotion = prefs.getBoolean("reduce_motion", false),
            autoAdvance = prefs.getBoolean("auto_advance", false)
        )
    }

    fun updateTheme(theme: String) {
        prefs.edit().putString("theme", theme).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun updateBlurIntensity(intensity: String) {
        prefs.edit().putString("blur_intensity", intensity).apply()
        _settings.value = _settings.value.copy(blurIntensity = intensity)
    }

    fun updateAccentColor(index: Int) {
        prefs.edit().putInt("accent_color", index).apply()
        _settings.value = _settings.value.copy(accentColorIndex = index)
    }

    fun toggleSoundEffects(enabled: Boolean) {
        prefs.edit().putBoolean("sound_effects", enabled).apply()
        _settings.value = _settings.value.copy(soundEffects = enabled)
    }

    fun toggleVibration(enabled: Boolean) {
        prefs.edit().putBoolean("vibration", enabled).apply()
        _settings.value = _settings.value.copy(vibration = enabled)
    }

    fun toggleShowExplanations(enabled: Boolean) {
        prefs.edit().putBoolean("show_explanations", enabled).apply()
        _settings.value = _settings.value.copy(showExplanations = enabled)
    }

    fun toggleReduceMotion(enabled: Boolean) {
        prefs.edit().putBoolean("reduce_motion", enabled).apply()
        _settings.value = _settings.value.copy(reduceMotion = enabled)
    }

    fun toggleAutoAdvance(enabled: Boolean) {
        prefs.edit().putBoolean("auto_advance", enabled).apply()
        _settings.value = _settings.value.copy(autoAdvance = enabled)
    }
}
