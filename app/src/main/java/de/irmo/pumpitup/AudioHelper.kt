package de.irmo.pumpitup

import android.media.AudioManager
import android.media.ToneGenerator

class AudioHelper {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playBeep() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
