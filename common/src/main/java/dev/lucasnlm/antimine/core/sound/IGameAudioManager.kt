package dev.lucasnlm.antimine.core.sound

interface IGameAudioManager {
    fun playWin()
    fun playBombExplosion()
    fun playMusic()
    fun pauseMusic()
    fun resumeMusic()
    fun stopMusic()
    fun playClickSound(index: Int = 0)
    fun playOpenArea()
    fun playPutFlag()
    fun playOpenMultipleArea()
    fun playRevealBomb()
    fun free()
}
