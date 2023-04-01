package dev.lucasnlm.antimine.core.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import dev.lucasnlm.antimine.preferences.IPreferencesRepository

class GameAudioManager(
    private val context: Context,
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

    private fun buildMusicMediaPlayer(assetFileDescriptor: AssetFileDescriptor): MediaPlayer {
        return playWithMediaPlayer(
            soundAsset = assetFileDescriptor,
            volume = MUSIC_MAX_VOLUME,
            repeat = true,
            releaseOnComplete = false,
            isMusic = true,
        )
    }

    override fun playMusic() {
        if (preferencesRepository.isMusicEnabled()) {
            if (musicMediaPlayer == null) {
                val musicName = musicFileName()
                tryOpenFd(musicName)?.use { musicFd ->
                    musicMediaPlayer = buildMusicMediaPlayer(musicFd)
                }
            } else {
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
            if (musicMediaPlayer?.isPlaying == false) {
                musicMediaPlayer?.start()
            }
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

    override fun playMonetization() {
        revealBombFiles().pickOneAndPlay()
    }

    override fun playRevealBombReloaded() {
        val fileName = revealBombReloadFile()
        playSoundFromAssets(fileName)
    }

    override fun free() {
        stopMusic()
    }

    override fun getComposerData(): List<ComposerData> {
        return listOf(
            ComposerData(
                composer = "Tatyana Jacques",
                composerLink = "https://open.spotify.com/artist/5Z1PXKko20wSH0yFr9HtNr",
            ),
        )
    }

    private fun getAudioAttributes(isMusic: Boolean): AudioAttributes {
        return AudioAttributes.Builder().apply {
            setUsage(AudioAttributes.USAGE_GAME)

            if (isMusic) {
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            } else {
                setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (isMusic) {
                    setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE)
                } else {
                    setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)
                }
            }
        }.build()
    }

    private fun playWithMediaPlayer(
        soundAsset: AssetFileDescriptor,
        volume: Float,
        repeat: Boolean,
        releaseOnComplete: Boolean,
        isMusic: Boolean = false,
        seekTo: Int? = null,
    ): MediaPlayer {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.run {
                setDataSource(soundAsset.fileDescriptor, soundAsset.startOffset, soundAsset.length)
                prepare()
                setAudioAttributes(getAudioAttributes(isMusic))
                setVolume(volume, volume)
                seekTo?.let(::seekTo)
                isLooping = repeat
                if (releaseOnComplete) {
                    setOnCompletionListener {
                        release()
                    }
                }
                start()
            }
        } catch (_: Exception) {
            // Fail to load or play file. Ignore.
            mediaPlayer.release()
        }
        return mediaPlayer
    }

    private fun playSoundFromAssets(fileName: String) {
        tryOpenFd(fileName)?.use { soundAsset ->
            playWithMediaPlayer(
                soundAsset = soundAsset,
                volume = SFX_MAX_VOLUME,
                repeat = false,
                releaseOnComplete = true,
                seekTo = 0,
                isMusic = false,
            )
        }
    }

    private fun tryOpenFd(fileName: String): AssetFileDescriptor? {
        return try {
            context.assets.openFd(fileName)
        } catch (e: Exception) {
            // Failed to load, file does not exist.
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
        const val MUSIC_MAX_VOLUME = 0.3f
        const val SFX_MAX_VOLUME = 0.7f

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
