package dev.lucasnlm.antimine.common.level.utils

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

interface IHapticFeedbackManager {
    fun longPressFeedback()
    fun explosionFeedback()
    fun tutorialErrorFeedback()
}

class HapticFeedbackManager(
    context: Context,
) : IHapticFeedbackManager {

    private val vibrator by lazy { context.getSystemService(VIBRATOR_SERVICE) as Vibrator }

    override fun longPressFeedback() {
        vibrateTo(70, 240)
        vibrateTo(10, 100)
    }

    override fun explosionFeedback() {
        vibrateTo(400, -1)
    }

    override fun tutorialErrorFeedback() {
        vibrateTo(70, 240)
        vibrateTo(10, 100)
        vibrateTo(70, 240)
    }

    private fun vibrateTo(time: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(time, amplitude)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(time)
        }
    }
}
