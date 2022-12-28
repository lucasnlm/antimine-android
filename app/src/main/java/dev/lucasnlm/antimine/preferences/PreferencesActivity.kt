package dev.lucasnlm.antimine.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import com.google.android.material.materialswitch.MaterialSwitch
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.model.TopBarAction
import kotlinx.android.synthetic.main.activity_preferences.*
import org.koin.android.ext.android.inject

class PreferencesActivity :
    ThemedActivity(R.layout.activity_preferences),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val preferenceRepository: IPreferencesRepository by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()

    private val preferenceManager by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private fun bindItem(
        switch: MaterialSwitch,
        checked: Boolean,
        action: (Boolean) -> Unit,
    ) {
        switch.apply {
            isChecked = checked
            setOnCheckedChangeListener { _, newCheckedState ->
                action(newCheckedState)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindToolbar(toolbar)
        bindToolbarAction(preferenceRepository.hasCustomizations())
        bindItems()

        preferenceManager.registerOnSharedPreferenceChangeListener(this)
    }

    private fun bindItems() {
        bindItem(
            switch = hapticFeedback,
            checked = preferenceRepository.useHapticFeedback(),
            action = {
                preferenceRepository.setHapticFeedback(it)
                if (it && preferenceRepository.getHapticFeedbackLevel() == 0) {
                    preferenceRepository.resetHapticFeedbackLevel()
                }
            },
        )

        bindItem(
            switch = soundEffects,
            checked = preferenceRepository.isSoundEffectsEnabled(),
            action = { preferenceRepository.setSoundEffectsEnabled(it) },
        )

        bindItem(
            switch = showWindows,
            checked = preferenceRepository.showWindowsWhenFinishGame(),
            action = { preferenceRepository.mustShowWindowsWhenFinishGame(it) },
        )

        bindItem(
            switch = openDirectly,
            checked = preferenceRepository.openGameDirectly(),
            action = { preferenceRepository.setOpenGameDirectly(it) },
        )

        bindItem(
            switch = useQuestionMark,
            checked = preferenceRepository.useQuestionMark(),
            action = { preferenceRepository.setQuestionMark(it) },
        )

        bindItem(
            switch = showTimer,
            checked = preferenceRepository.showTimer(),
            action = { preferenceRepository.setTimerVisible(it) },
        )

        bindItem(
            switch = automaticFlags,
            checked = preferenceRepository.useFlagAssistant(),
            action = { preferenceRepository.setFlagAssistant(it) },
        )

        bindItem(
            switch = hint,
            checked = preferenceRepository.useHelp(),
            action = { preferenceRepository.setHelp(it) },
        )

        bindItem(
            switch = allowClickNumber,
            checked = preferenceRepository.allowTapOnNumbers(),
            action = {
                preferenceRepository.setAllowTapOnNumbers(it)

                if (it) {
                    flagWhenTapOnNumbers.visibility = View.VISIBLE
                    flagWhenTapOnNumbers.visibility = View.VISIBLE
                } else {
                    flagWhenTapOnNumbers.visibility = View.GONE
                    flagWhenTapOnNumbers.visibility = View.GONE
                }
            },
        )

        bindItem(
            switch = flagWhenTapOnNumbers,
            checked = preferenceRepository.letNumbersAutoFlag(),
            action = { preferenceRepository.setNumbersAutoFlag(it) },
        )

        if (!preferenceRepository.allowTapOnNumbers()) {
            flagWhenTapOnNumbers.visibility = View.GONE
            flagWhenTapOnNumbers.visibility = View.GONE
        }

        bindItem(
            switch = highlightUnsolvedNumbers,
            checked = preferenceRepository.dimNumbers(),
            action = { preferenceRepository.setDimNumbers(it) },
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        cloudSaveManager.uploadSave()

        preferenceManager.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun bindToolbarAction(hasCustomizations: Boolean) {
        if (hasCustomizations) {
            setTopBarAction(
                TopBarAction(
                    name = R.string.delete_all,
                    icon = R.drawable.delete,
                    action = {
                        preferenceRepository.reset()
                        bindItems()
                        setTopBarAction(null)
                    },
                ),
            )
        } else {
            setTopBarAction(null)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        bindToolbarAction(preferenceRepository.hasCustomizations())
    }
}
