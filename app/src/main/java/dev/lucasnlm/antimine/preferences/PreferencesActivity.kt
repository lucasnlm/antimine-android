package dev.lucasnlm.antimine.preferences

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.google.android.material.materialswitch.MaterialSwitch
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.ext.showWarning
import dev.lucasnlm.antimine.ui.model.TopBarAction
import kotlinx.android.synthetic.main.activity_preferences.*
import org.koin.android.ext.android.inject

class PreferencesActivity :
    ThemedActivity(R.layout.activity_preferences),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val preferenceRepository: IPreferencesRepository by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()
    private val settingsBackupManager: SettingsBackupManager by lazy {
        SettingsBackupManager(applicationContext)
    }

    private lateinit var exportResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var importResultLauncher: ActivityResultLauncher<Intent>

    private val preferenceManager by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
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

        exportResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                var result = false
                val target = it.data?.data
                if (target != null) {
                    val data = preferenceRepository.exportData()
                    result = settingsBackupManager.exportSettings(target, data)
                }

                if (result) {
                    showWarning(R.string.exported_success)
                } else {
                    showWarning(R.string.error)
                }
            }
        }

        importResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                var result = false
                val target = it.data?.data
                if (target != null) {
                    val data = settingsBackupManager.importSettings(target)
                    if (data != null && data.isNotEmpty()) {
                        preferenceRepository.importData(data)
                        result = true
                    }
                }

                if (result) {
                    showWarning(R.string.imported_success)
                    bindItems()
                } else {
                    showWarning(R.string.error)
                }
            }
        }

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

        exportSettings.setOnClickListener {
            val exportIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_TITLE, SettingsBackupManager.FILE_NAME)
                type = "application/json"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentsDir.toUri())
                }
            }

            exportResultLauncher.launch(exportIntent)
        }

        importSettings.setOnClickListener {
            val exportIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_TITLE, SettingsBackupManager.FILE_NAME)
                type = "application/json"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentsDir.toUri())
                }
            }

            importResultLauncher.launch(exportIntent)
        }
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
