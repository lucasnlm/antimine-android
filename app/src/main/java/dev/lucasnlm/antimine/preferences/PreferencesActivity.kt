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
import dev.lucasnlm.antimine.databinding.ActivityPreferencesBinding
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.ext.showWarning
import dev.lucasnlm.antimine.ui.model.TopBarAction
import dev.lucasnlm.external.IPlayGamesManager
import org.koin.android.ext.android.inject

class PreferencesActivity :
    ThemedActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val playGamesManager: IPlayGamesManager by inject()
    private val preferenceRepository: IPreferencesRepository by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()
    private val settingsBackupManager: SettingsBackupManager by lazy {
        SettingsBackupManager(applicationContext)
    }

    private lateinit var exportResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var importResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityPreferencesBinding

    private val preferenceManager by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private fun MaterialSwitch.bindItem(
        checked: Boolean,
        action: (Boolean) -> Unit,
    ) {
        isChecked = checked
        setOnCheckedChangeListener { _, newCheckedState ->
            action(newCheckedState)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        bindToolbar(binding.toolbar)
        bindToolbarAction(preferenceRepository.hasCustomizations())
        bindItems()

        preferenceManager.registerOnSharedPreferenceChangeListener(this)
    }

    private fun bindItems() {
        binding.hapticFeedback.bindItem(
            checked = preferenceRepository.useHapticFeedback(),
            action = {
                preferenceRepository.setHapticFeedback(it)
                if (it && preferenceRepository.getHapticFeedbackLevel() == 0) {
                    preferenceRepository.resetHapticFeedbackLevel()
                }
            },
        )

        binding.soundEffects.bindItem(
            checked = preferenceRepository.isSoundEffectsEnabled(),
            action = { preferenceRepository.setSoundEffectsEnabled(it) },
        )

        binding.music.bindItem(
            checked = preferenceRepository.isMusicEnabled(),
            action = { preferenceRepository.setMusicEnabled(it) }
        )

        binding.showWindows.bindItem(
            checked = preferenceRepository.showWindowsWhenFinishGame(),
            action = { preferenceRepository.mustShowWindowsWhenFinishGame(it) },
        )

        binding.openDirectly.bindItem(
            checked = preferenceRepository.openGameDirectly(),
            action = { preferenceRepository.setOpenGameDirectly(it) },
        )

        binding.useQuestionMark.bindItem(
            checked = preferenceRepository.useQuestionMark(),
            action = { preferenceRepository.setQuestionMark(it) },
        )

        binding.showTimer.bindItem(
            checked = preferenceRepository.showTimer(),
            action = { preferenceRepository.setTimerVisible(it) },
        )

        binding.automaticFlags.bindItem(
            checked = preferenceRepository.useFlagAssistant(),
            action = { preferenceRepository.setFlagAssistant(it) },
        )

        binding.hint.bindItem(
            checked = preferenceRepository.useHelp(),
            action = { preferenceRepository.setHelp(it) },
        )

        binding.allowClickNumber.bindItem(
            checked = preferenceRepository.allowTapOnNumbers(),
            action = {
                preferenceRepository.setAllowTapOnNumbers(it)

                if (it) {
                    binding.flagWhenTapOnNumbers.visibility = View.VISIBLE
                    binding.flagWhenTapOnNumbers.visibility = View.VISIBLE
                } else {
                    binding.flagWhenTapOnNumbers.visibility = View.GONE
                    binding.flagWhenTapOnNumbers.visibility = View.GONE
                }
            },
        )

        binding.flagWhenTapOnNumbers.bindItem(
            checked = preferenceRepository.letNumbersAutoFlag(),
            action = { preferenceRepository.setNumbersAutoFlag(it) },
        )

        if (!preferenceRepository.allowTapOnNumbers()) {
            binding.flagWhenTapOnNumbers.visibility = View.GONE
            binding.flagWhenTapOnNumbers.visibility = View.GONE
        }

        binding.highlightUnsolvedNumbers.bindItem(
            checked = preferenceRepository.dimNumbers(),
            action = { preferenceRepository.setDimNumbers(it) },
        )

        if (playGamesManager.hasGooglePlayGames()) {
            binding.playGames.bindItem(
                checked = preferenceRepository.keepRequestPlayGames(),
                action = {
                    preferenceRepository.setRequestPlayGames(it)
                },
            )
        } else {
            binding.playGames.visibility = View.GONE
        }

        binding.exportSettings.setOnClickListener {
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

        binding.importSettings.setOnClickListener {
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
