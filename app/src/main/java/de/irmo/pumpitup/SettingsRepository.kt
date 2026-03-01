package de.irmo.pumpitup

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pumpitup_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_UPPER_THRESHOLD = "upper_threshold"
        private const val KEY_LOWER_THRESHOLD = "lower_threshold"
        private const val KEY_DEBOUNCE_TIME = "debounce_time"
        
        const val DEFAULT_UPPER_THRESHOLD = 0.35f
        const val DEFAULT_LOWER_THRESHOLD = 0.25f
        const val DEFAULT_DEBOUNCE_TIME = 500L
    }

    fun getUpperThreshold(): Float {
        return prefs.getFloat(KEY_UPPER_THRESHOLD, DEFAULT_UPPER_THRESHOLD)
    }

    fun setUpperThreshold(value: Float) {
        prefs.edit().putFloat(KEY_UPPER_THRESHOLD, value).apply()
    }

    fun getLowerThreshold(): Float {
        return prefs.getFloat(KEY_LOWER_THRESHOLD, DEFAULT_LOWER_THRESHOLD)
    }

    fun setLowerThreshold(value: Float) {
        prefs.edit().putFloat(KEY_LOWER_THRESHOLD, value).apply()
    }

    fun getDebounceTime(): Long {
        return prefs.getLong(KEY_DEBOUNCE_TIME, DEFAULT_DEBOUNCE_TIME)
    }

    fun setDebounceTime(value: Long) {
        prefs.edit().putLong(KEY_DEBOUNCE_TIME, value).apply()
    }
}
