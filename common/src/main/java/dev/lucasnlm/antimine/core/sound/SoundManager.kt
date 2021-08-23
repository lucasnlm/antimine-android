package dev.lucasnlm.antimine.core.sound

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import dev.lucasnlm.external.ICrashReporter

interface ISoundManager {
    fun play(@RawRes soundId: Int)
}

class SoundManager(
    private val context: Context,
    private val crashReporter: ICrashReporter,
) : ISoundManager {

    override fun play(@RawRes soundId: Int) {
        try {
            MediaPlayer.create(context, soundId).apply {
                setVolume(0.7f, 0.7f)
                setOnCompletionListener {
                    release()
                }
            }.start()
        } catch (e: Exception) {
            // Some Huawei phones may fail to play sounds.
            // Adding this try catch to at lease to make they crash.
            crashReporter.sendError("Fail to play sound.\n${e.message}")
        }
    }
}
