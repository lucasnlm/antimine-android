package dev.lucasnlm.antimine.core.sound

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

interface ISoundManager {
    fun play(@RawRes soundId: Int)
}

class SoundManager(
    private val context: Context,
) : ISoundManager {

    override fun play(@RawRes soundId: Int) {
        MediaPlayer.create(context, soundId).apply {
            setVolume(0.7f, 0.7f)
            setOnCompletionListener {
                release()
            }
        }.start()
    }
}
