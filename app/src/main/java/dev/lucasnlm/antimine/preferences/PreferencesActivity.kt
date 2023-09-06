package dev.lucasnlm.antimine.preferences

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.google.android.material.materialswitch.MaterialSwitch
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.databinding.ActivityPreferencesBinding
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.ext.showWarning
import dev.lucasnlm.antimine.ui.model.TopBarAction
import dev.lucasnlm.external.PlayGamesManager
import org.koin.android.ext.android.inject
import dev.lucasnlm.antimine.i18n.R as i18n

class PreferencesActivity :
    ThemedActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val audioManager: GameAudioManager by inject()
    private val playGamesManager: PlayGamesManager by inject()
    private val preferenceRepository: PreferencesRepository by inject()
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
        visible: Boolean = true,
        initialValue: Boolean,
        onChangeValue: (Boolean) -> Unit,
    ) {
        isVisible = visible
        isSoundEffectsEnabled = false
        isChecked = initialValue
        setOnCheckedChangeListener { _, newCheckedState ->
            onChangeValue(newCheckedState)
            audioManager.playClickSound(if (newCheckedState) 0 else 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exportResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    var result = false
                    val target = it.data?.data
                    if (target != null) {
                        val data = preferenceRepository.exportData()
                        result = settingsBackupManager.exportSettings(target, data)
                    }

                    if (result) {
                        showWarning(i18n.string.exported_success)
                    } else {
                        showWarning(i18n.string.error)
                    }
                }
            }

        importResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    var result = false
                    val target = it.data?.data
                    if (target != null) {
                        val data = settingsBackupManager.importSettings(target)
                        if (!data.isNullOrEmpty()) {
                            preferenceRepository.importData(data)
                            result = true
                        }
                    }

                    if (result) {
                        showWarning(i18n.string.imported_success)
                        bindItems()
                    } else {
                        showWarning(i18n.string.error)
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
            initialValue = preferenceRepository.useHapticFeedback(),
            onChangeValue = {
                preferenceRepository.setHapticFeedback(it)
                if (it && preferenceRepository.getHapticFeedbackLevel() == 0) {
                    preferenceRepository.resetHapticFeedbackLevel()
                }
            },
        )

        binding.soundEffects.bindItem(
            visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M,
            initialValue = preferenceRepository.isSoundEffectsEnabled(),
            onChangeValue = { preferenceRepository.setSoundEffectsEnabled(it) },
        )

        binding.music.bindItem(
            visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M,
            initialValue = preferenceRepository.isMusicEnabled(),
            onChangeValue = { preferenceRepository.setMusicEnabled(it) },
        )

        binding.showWindows.bindItem(
            initialValue = preferenceRepository.showWindowsWhenFinishGame(),
            onChangeValue = { preferenceRepository.mustShowWindowsWhenFinishGame(it) },
        )

        binding.openDirectly.bindItem(
            initialValue = preferenceRepository.openGameDirectly(),
            onChangeValue = { preferenceRepository.setOpenGameDirectly(it) },
        )

        binding.useQuestionMark.bindItem(
            initialValue = preferenceRepository.useQuestionMark(),
            onChangeValue = { preferenceRepository.setQuestionMark(it) },
        )

        binding.showTimer.bindItem(
            initialValue = preferenceRepository.showTimer(),
            onChangeValue = { preferenceRepository.setTimerVisible(it) },
        )

        binding.automaticFlags.bindItem(
            initialValue = preferenceRepository.useFlagAssistant(),
            onChangeValue = { preferenceRepository.setFlagAssistant(it) },
        )

        binding.hint.bindItem(
            initialValue = preferenceRepository.useHelp(),
            onChangeValue = { preferenceRepository.setHelp(it) },
        )

        binding.noGuessingMode.bindItem(
            initialValue = preferenceRepository.useSimonTathamAlgorithm(),
            onChangeValue = { preferenceRepository.setSimonTathamAlgorithm(it) },
        )

        binding.allowClickNumber.bindItem(
            initialValue = preferenceRepository.allowTapOnNumbers(),
            onChangeValue = {
                preferenceRepository.setAllowTapOnNumbers(it)
                binding.flagWhenTapOnNumbers.isVisible = it
                binding.flagWhenTapOnNumbers.isVisible = it
            },
        )

        binding.flagWhenTapOnNumbers.bindItem(
            initialValue = preferenceRepository.letNumbersAutoFlag(),
            onChangeValue = { preferenceRepository.setNumbersAutoFlag(it) },
        )

        if (!preferenceRepository.allowTapOnNumbers()) {
            binding.flagWhenTapOnNumbers.isVisible = false
            binding.flagWhenTapOnNumbers.isVisible = false
        }

        binding.highlightUnsolvedNumbers.bindItem(
            initialValue = preferenceRepository.dimNumbers(),
            onChangeValue = { preferenceRepository.setDimNumbers(it) },
        )

        if (playGamesManager.hasGooglePlayGames()) {
            binding.playGames.bindItem(
                initialValue = preferenceRepository.keepRequestPlayGames(),
                onChangeValue = {
                    preferenceRepository.setRequestPlayGames(it)
                },
            )
        } else {
            binding.playGames.isVisible = false
        }

        binding.exportSettings.setOnClickListener {
            val exportIntent =
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, SettingsBackupManager.FILE_NAME)
                    type = "application/json"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val documentsDir =
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOCUMENTS,
                            )
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentsDir.toUri())
                    }
                }

            exportResultLauncher.launch(exportIntent)
        }

        binding.importSettings.setOnClickListener {
            val exportIntent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, SettingsBackupManager.FILE_NAME)
                    type = "application/json"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val documentsDir =
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOCUMENTS,
                            )
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
                    name = i18n.string.delete_all,
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

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?,
    ) {
        bindToolbarAction(preferenceRepository.hasCustomizations())
    }
}
