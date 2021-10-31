package dev.lucasnlm.antimine.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.switchmaterial.SwitchMaterial
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.ui.ThematicActivity
import kotlinx.android.synthetic.main.activity_preferences.*
import org.koin.android.ext.android.inject

class PreferencesActivity :
    ThematicActivity(R.layout.activity_preferences),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val preferenceRepository: IPreferencesRepository by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()

    private fun bindItem(
        label: TextView,
        switch: SwitchMaterial,
        checked: Boolean,
        action: (Boolean) -> Unit
    ) {
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindToolbar(preferenceRepository.hasCustomizations())

        bindItem(
            label = hapticFeedbackLabel,
            switch = hapticFeedback,
            checked = preferenceRepository.useHapticFeedback(),
            action = {
                preferenceRepository.setHapticFeedback(it)
                if (it && preferenceRepository.getHapticFeedbackLevel() == 0) {
                    preferenceRepository.resetHapticFeedbackLevel()
                }
            }
        )

        bindItem(
            label = soundEffectsLabel,
            switch = soundEffects,
            checked = preferenceRepository.isSoundEffectsEnabled(),
            action = { preferenceRepository.setSoundEffectsEnabled(it) }
        )

        bindItem(
            label = showWindowsLabel,
            switch = showWindows,
            checked = preferenceRepository.showWindowsWhenFinishGame(),
            action = { preferenceRepository.mustShowWindowsWhenFinishGame(it) }
        )

        bindItem(
            label = openDirectlyLabel,
            switch = openDirectly,
            checked = preferenceRepository.openGameDirectly(),
            action = { preferenceRepository.setOpenGameDirectly(it) }
        )

        bindItem(
            label = useQuestionMarkLabel,
            switch = useQuestionMark,
            checked = preferenceRepository.useQuestionMark(),
            action = { preferenceRepository.setQuestionMark(it) }
        )

        bindItem(
            label = automaticFlagsLabel,
            switch = automaticFlags,
            checked = preferenceRepository.useFlagAssistant(),
            action = { preferenceRepository.setFlagAssistant(it) }
        )

        bindItem(
            label = helpLabel,
            switch = help,
            checked = preferenceRepository.useHelp(),
            action = { preferenceRepository.setHelp(it) }
        )

        bindItem(
            label = allowClickNumberLabel,
            switch = clickOnNumbers,
            checked = preferenceRepository.allowTapOnNumbers(),
            action = { preferenceRepository.setAllowTapOnNumbers(it) }
        )

        bindItem(
            label = highlightUnsolvedNumbersLabel,
            switch = highlightUnsolvedNumbers,
            checked = preferenceRepository.dimNumbers(),
            action = { preferenceRepository.setDimNumbers(it) }
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
