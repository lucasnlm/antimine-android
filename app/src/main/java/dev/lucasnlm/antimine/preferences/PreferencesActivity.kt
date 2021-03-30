package dev.lucasnlm.antimine.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.switchmaterial.SwitchMaterial
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.core.isAndroidTv
import dev.lucasnlm.antimine.ui.ThematicActivity
import kotlinx.android.synthetic.main.activity_preferences.*
import org.koin.android.ext.android.inject

class PreferencesActivity :
    ThematicActivity(R.layout.activity_preferences),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val preferenceRepository: IPreferencesRepository by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()
    private val isAndroidTv: Boolean by lazy { isAndroidTv() }

    private fun bindItem(
        label: TextView,
        switch: SwitchMaterial,
        checked: Boolean,
        supportTv: Boolean,
        action: (Boolean) -> Unit) {
        if (isAndroidTv && supportTv || !isAndroidTv) {
            label.setOnClickListener {
                switch.apply {
                    isChecked = !switch.isChecked
                }
            }

            switch.apply {
                isChecked = checked
                jumpDrawablesToCurrentState()
            }

            switch.setOnCheckedChangeListener { _, newCheckedState ->
                action(newCheckedState)
            }
        } else {
            label.visibility = View.GONE
            switch.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        bindToolbar(preferenceRepository.hasCustomizations())

        bindItem(
            label = hapticFeedbackLabel,
            switch = hapticFeedback,
            supportTv = false,
            checked = preferenceRepository.useHapticFeedback(),
            action = { preferenceRepository.setHapticFeedback(it) }
        )

        bindItem(
            label = soundEffectsLabel,
            switch = soundEffects,
            supportTv = true,
            checked = preferenceRepository.isSoundEffectsEnabled(),
            action = { preferenceRepository.setSoundEffectsEnabled(it) }
        )

        bindItem(
            label = animationsLabel,
            switch = animations,
            supportTv = true,
            checked = preferenceRepository.useAnimations(),
            action = { preferenceRepository.setAnimations(it) }
        )

        bindItem(
            label = showWindowsLabel,
            switch = showWindows,
            supportTv = false,
            checked = preferenceRepository.showWindowsWhenFinishGame(),
            action = { preferenceRepository.mustShowWindowsWhenFinishGame(it) }
        )

        bindItem(
            label = openDirectlyLabel,
            switch = openDirectly,
            supportTv = false,
            checked = preferenceRepository.openGameDirectly(),
            action = { preferenceRepository.setOpenGameDirectly(it) }
        )

        bindItem(
            label = useQuestionMarkLabel,
            switch = useQuestionMark,
            supportTv = true,
            checked = preferenceRepository.useQuestionMark(),
            action = { preferenceRepository.setQuestionMark(it) }
        )

        bindItem(
            label = automaticFlagsLabel,
            switch = automaticFlags,
            supportTv = true,
            checked = preferenceRepository.useFlagAssistant(),
            action = { preferenceRepository.setFlagAssistant(it) }
        )

        bindItem(
            label = noGuessingLabel,
            switch = noGuessing,
            supportTv = true,
            checked = preferenceRepository.useNoGuessingAlgorithm(),
            action = { preferenceRepository.setNoGuessingAlgorithm(it) }
        )

        bindItem(
            label = helpLabel,
            switch = help,
            supportTv = false,
            checked = preferenceRepository.useHelp(),
            action = { preferenceRepository.setHelp(it) }
        )

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        cloudSaveManager.uploadSave()

        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun bindToolbar(hasCustomizations: Boolean) {
        if (hasCustomizations) {
            section.bind(
                text = R.string.settings,
                startButton = R.drawable.back_arrow,
                startDescription = R.string.back,
                startAction = {
                    finish()
                },
                endButton = R.drawable.delete,
                endDescription = R.string.delete_all,
                endAction = {
                    preferenceRepository.reset()
                    bindToolbar(false)
                }
            )
        } else {
            section.bind(
                text = R.string.settings,
                startButton = R.drawable.back_arrow,
                startDescription = R.string.back,
                startAction = {
                    finish()
                }
            )
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        bindToolbar(preferenceRepository.hasCustomizations())
    }
}
