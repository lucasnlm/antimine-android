package dev.lucasnlm.antimine.common.level.utils

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

interface IHapticFeedbackInteractor {
    fun toggleFlagFeedback()
    fun explosionFeedback()
}

class HapticFeedbackInteractor(
    context: Context,
    private val preferencesRepository: IPreferencesRepository
) : IHapticFeedbackInteractor {
    private val vibrator: Vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator

    override fun toggleFlagFeedback() {
        if (preferencesRepository.useHapticFeedback()) {
            vibrateTo(70, 240)
            vibrateTo(10, 100)
        }
    }

    override fun explosionFeedback() {
        if (preferencesRepository.useHapticFeedback()) {
            vibrateTo(400, -1)
        }
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

class DisabledIHapticFeedbackInteractor : IHapticFeedbackInteractor {
    override fun toggleFlagFeedback() { }

    override fun explosionFeedback() { }
}
