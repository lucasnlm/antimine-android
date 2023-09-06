package dev.lucasnlm.antimine.common.level.utils

import android.text.format.DateUtils
import java.util.Timer
import java.util.TimerTask

open class Clock {
    protected var elapsedTimeSeconds: Long = 0
    private var timer: Timer? = null

    val isStopped: Boolean
        get() = (timer == null)

    fun reset(initialValue: Long = 0L) {
        stop()
        this.elapsedTimeSeconds = initialValue
    }

    fun time() = elapsedTimeSeconds

    fun stop() {
        timer?.apply {
            cancel()
            purge()
        }
        timer = null
    }

    open fun provideTimer() = Timer()

    fun start(onTick: (seconds: Long) -> Unit) {
        stop()
        timer =
            provideTimer().apply {
                scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            elapsedTimeSeconds++
                            onTick(elapsedTimeSeconds)
                        }
                    },
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.SECOND_IN_MILLIS,
                )
            }
    }
}
