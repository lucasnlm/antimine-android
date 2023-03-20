package dev.lucasnlm.antimine.core.sound

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import androidx.annotation.RawRes
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.external.ICrashReporter

interface ISoundManager {
    fun play(@RawRes soundId: Int)
    fun playMusic()
    fun pauseMusic()
    fun resumeMusic()
    fun stopMusic()
    fun playClickSound(index: Int = 0)
    fun playOpenArea()
    fun free()
}

class SoundManager(
    private val context: Context,
    private val crashReporter: ICrashReporter,
    private val preferencesRepository: IPreferencesRepository,
) : ISoundManager {
    private var musicMediaPlayer: MediaPlayer? = null

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

    override fun playMusic() {
        if (preferencesRepository.isMusicEnabled()) {
            if (musicMediaPlayer == null) {
                val musicFd = tryOpenFd(MUSIC_FILE_NAME)

                musicFd?.use {
                    musicMediaPlayer = MediaPlayer().apply {
                        setDataSource(it.fileDescriptor, it.startOffset, it.length)
                        prepare()
                        setVolume(0.35f, 0.35f)
                        isLooping = true
                        start()
                    }
                }
            } else if (musicMediaPlayer?.isPlaying == false) {
                musicMediaPlayer?.start()
            }
        } else {
            stopMusic()
        }
    }

    override fun pauseMusic() {
        musicMediaPlayer?.pause()
    }

    override fun resumeMusic() {
        musicMediaPlayer?.start()
    }

    override fun stopMusic() {
        musicMediaPlayer?.run {
            stop()
            release()
        }
        musicMediaPlayer = null
    }

    override fun playClickSound(index: Int) {
        when (index) {
            0 -> playSoundFromAssets(SFX_CLICK_0_NAME)
            1 -> playSoundFromAssets(SFX_CLICK_1_NAME)
            2 -> playSoundFromAssets(SFX_CLICK_2_NAME)
        }
    }

    override fun playOpenArea() {
        listOf(
            SFX_OPEN_AREA_0,
            SFX_OPEN_AREA_1,
            SFX_OPEN_AREA_2,
            SFX_OPEN_AREA_3,
        ).shuffled().first().also(::playSoundFromAssets)
    }

    override fun free() {
        stopMusic()
    }

    private fun playSoundFromAssets(fileName: String) {
        try {
            tryOpenFd(fileName)?.use { soundAsset ->
                MediaPlayer().apply {
                    setDataSource(soundAsset.fileDescriptor, soundAsset.startOffset, soundAsset.length)
                    prepare()
                    setVolume(0.7f, 0.7f)
                    seekTo(0)
                    isLooping = false
                    setOnCompletionListener {
                        release()
                    }
                    start()
                }.start()
            }
        } catch (e: Exception) {
            // Some Huawei phones may fail to play sounds.
            // Adding this try catch to at lease to make they crash.
            crashReporter.sendError("Fail to play sound '$fileName'.")
        } finally {
        }
    }

    private fun tryOpenFd(fileName: String): AssetFileDescriptor? {
        return try {
            context.assets.openFd(fileName)
        } catch (e: Exception) {
            // Failed to load, file not exist.
            null
        }
    }

    companion object {
        const val MUSIC_FILE_NAME = "music.ogg"
        const val SFX_CLICK_0_NAME = "menu_click.ogg"
        const val SFX_CLICK_1_NAME = "menu_click_alt.ogg"
        const val SFX_CLICK_2_NAME = "menu_click_back.ogg"
        const val SFX_OPEN_AREA_0 = "open_area_0.ogg"
        const val SFX_OPEN_AREA_1 = "open_area_1.ogg"
        const val SFX_OPEN_AREA_2 = "open_area_2.ogg"
        const val SFX_OPEN_AREA_3 = "open_area_3.ogg"
    }
}
