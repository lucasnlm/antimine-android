package dev.lucasnlm.antimine.core.audio

interface GameAudioManager {
    fun playWin()

    fun playBombExplosion()

    fun playMusic()

    fun isPlayingMusic(): Boolean

    fun pauseMusic()

    fun resumeMusic()

    fun stopMusic()

    fun playClickSound(index: Int = 0)

    fun playOpenArea()

    fun playPutFlag()

    fun playOpenMultipleArea()

    fun playRevealBomb()

    fun playMonetization()

    fun playRevealBombReloaded()

    fun playSwitchAction()

    fun free()

    fun getComposerData(): List<ComposerData>
}
