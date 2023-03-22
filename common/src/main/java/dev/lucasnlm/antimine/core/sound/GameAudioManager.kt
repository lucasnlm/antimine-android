package dev.lucasnlm.antimine.core.sound

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.external.ICrashReporter

class GameAudioManager(
    private val context: Context,
    private val crashReporter: ICrashReporter,
    private val preferencesRepository: IPreferencesRepository,
) : IGameAudioManager {
    private var musicMediaPlayer: MediaPlayer? = null

    override fun playBombExplosion() {
        if (preferencesRepository.isSoundEffectsEnabled()) {
            val fileName = bombExplosionFileName()
            playSoundFromAssets(fileName)
        }
    }

    override fun playWin() {
        if (preferencesRepository.isSoundEffectsEnabled()) {
            val fileName = winFileName()
            playSoundFromAssets(fileName)
        }
    }

    override fun playMusic() {
        if (preferencesRepository.isMusicEnabled()) {
            if (musicMediaPlayer == null) {
                val musicName = musicFileName()
                val musicFd = tryOpenFd(musicName)

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
        try {
            if (musicMediaPlayer?.isPlaying == true) {
                musicMediaPlayer?.pause()
            }
        } catch (e: IllegalStateException) {
            // Ignore
        }
    }

    override fun resumeMusic() {
        if (preferencesRepository.isSoundEffectsEnabled()) {
            musicMediaPlayer?.start()
        }
    }

    override fun stopMusic() {
        musicMediaPlayer?.run {
            stop()
            release()
        }
        musicMediaPlayer = null
    }

    override fun playClickSound(index: Int) {
        if (preferencesRepository.isSoundEffectsEnabled()) {
            val clickFileNames = clickFileName()
            if (index < clickFileNames.size) {
                val fileClickName = clickFileNames[index]
                playSoundFromAssets(fileClickName)
            }
        }
    }

    override fun playOpenArea() {
        openAreaFiles().pickOneAndPlay()
    }

    override fun playPutFlag() {
        putFlagFiles().pickOneAndPlay()
    }

    override fun playOpenMultipleArea() {
        openMultipleFiles().pickOneAndPlay()
    }

    override fun playRevealBomb() {
        revealBombFiles().pickOneAndPlay()
    }

    override fun playRevealBombReloaded() {
        val fileName = revealBombReloadFile()
        playSoundFromAssets(fileName)
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

    private fun List<String>.pickOne() = this.shuffled().first()

    private fun List<String>.pickOneAndPlay() {
        if (preferencesRepository.isSoundEffectsEnabled()) {
            pickOne().also(::playSoundFromAssets)
        }
    }

    companion object {
        private fun filesCount(count: Int) = (0 until count)

        fun winFileName() = "win.ogg"
        fun bombExplosionFileName() = "bomb_explosion.ogg"
        fun clickFileName() = listOf("menu_click.ogg", "menu_click_alt.ogg", "menu_click_back.ogg")
        fun musicFileName() = "music.ogg"
        fun openAreaFiles() = filesCount(4).map { "open_area_$it.ogg" }
        fun openMultipleFiles() = filesCount(3).map { "open_multiple_$it.ogg" }
        fun putFlagFiles() = filesCount(3).map { "put_flag_$it.ogg" }
        fun revealBombFiles() = filesCount(3).map { "reveal_mine_$it.ogg" }
        fun revealBombReloadFile() = "reveal_mine_reload.ogg"
    }
}
