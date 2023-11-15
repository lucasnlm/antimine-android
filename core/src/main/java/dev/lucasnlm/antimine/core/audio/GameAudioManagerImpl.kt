package dev.lucasnlm.antimine.core.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.utils.BuildExt.androidQuinceTart

class GameAudioManagerImpl(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository,
) : GameAudioManager {
    private var musicMediaPlayer: MediaPlayer? = null

    override fun playBombExplosion() {
        playSoundFromAssets(BOMB_EXPLOSION_FILE_NAME)
    }

    override fun playWin() {
        playSoundFromAssets(WIN_FILE_NAME)
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
                tryOpenFd(MUSIC_FILE_NAME)?.use { musicFd ->
                    musicMediaPlayer = buildMusicMediaPlayer(musicFd)
                }
            } else {
                musicMediaPlayer?.start()
            }
        } else {
            stopMusic()
        }
    }

    override fun isPlayingMusic(): Boolean {
        return musicMediaPlayer?.isPlaying == true
    }

    override fun pauseMusic() {
        runCatching {
            if (musicMediaPlayer?.isPlaying == true) {
                musicMediaPlayer?.pause()
            }
        }
    }

    override fun resumeMusic() {
        if (preferencesRepository.isMusicEnabled()) {
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
        clickFileName()
            .getOrNull(index)
            ?.let(::playSoundFromAssets)
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
        playSoundFromAssets(REVEAL_BOMB_RELOAD_FILE_NAME)
    }

    override fun playSwitchAction() {
        playSoundFromAssets(REVEAL_BOMB_RELOAD_FILE_NAME)
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

            androidQuinceTart {
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
        runCatching {
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
        }.onFailure {
            mediaPlayer.release()
        }
        return mediaPlayer
    }

    private fun playSoundFromAssets(fileName: String) {
        if (preferencesRepository.isSoundEffectsEnabled()) {
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
    }

    private fun tryOpenFd(fileName: String): AssetFileDescriptor? {
        return runCatching {
            context.assets.openFd(fileName)
        }.getOrNull()
    }

    private fun List<String>.pickOne() = this.shuffled().first()

    private fun List<String>.pickOneAndPlay() {
        pickOne().also(::playSoundFromAssets)
    }

    companion object {
        const val MUSIC_MAX_VOLUME = 0.3f
        const val SFX_MAX_VOLUME = 0.7f

        private fun filesCount(count: Int) = (0 until count)

        private const val OPEN_AREA_COUNT = 4
        private const val OPEN_MULTIPLE_COUNT = 3
        private const val PUT_FLAG_COUNT = 3
        private const val REVEAL_BOMB_COUNT = 3

        const val MUSIC_FILE_NAME = "music.ogg"
        const val WIN_FILE_NAME = "win.ogg"
        const val BOMB_EXPLOSION_FILE_NAME = "bomb_explosion.ogg"
        const val REVEAL_BOMB_RELOAD_FILE_NAME = "reveal_mine_reload.ogg"

        fun clickFileName() = listOf("menu_click.ogg", "menu_click_alt.ogg", "menu_click_back.ogg")

        fun openAreaFiles() = filesCount(OPEN_AREA_COUNT).map { "open_area_$it.ogg" }

        fun openMultipleFiles() = filesCount(OPEN_MULTIPLE_COUNT).map { "open_multiple_$it.ogg" }

        fun putFlagFiles() = filesCount(PUT_FLAG_COUNT).map { "put_flag_$it.ogg" }

        fun revealBombFiles() = filesCount(REVEAL_BOMB_COUNT).map { "reveal_mine_$it.ogg" }
    }
}
