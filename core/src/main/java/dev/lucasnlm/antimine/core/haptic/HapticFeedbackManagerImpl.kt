package dev.lucasnlm.antimine.core.haptic

import android.app.Application
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dev.lucasnlm.antimine.preferences.PreferencesRepositoryImpl

class HapticFeedbackManagerImpl(
    application: Application,
    private val preferencesRepository: PreferencesRepositoryImpl,
) : HapticFeedbackManager {

    private val vibrator by lazy {
        val context = application.applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

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

    private fun vibrateTo(
        time: Long,
        amplitude: Int,
    ) {
        runCatching {
            val feedbackLevel = preferencesRepository.getHapticFeedbackLevel().toDouble() / 100.0
            val realAmplitude = (feedbackLevel * amplitude).toInt()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(time, realAmplitude),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(time)
            }
        }
    }
}
