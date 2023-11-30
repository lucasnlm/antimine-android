package dev.lucasnlm.antimine.preferences

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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
import dev.lucasnlm.antimine.common.auto.AutoExt.isAndroidAuto
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.databinding.ActivityPreferencesBinding
import dev.lucasnlm.antimine.ui.ext.SnackbarExt.showWarning
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.model.TopBarAction
import dev.lucasnlm.antimine.utils.BuildExt.androidMarshmallow
import dev.lucasnlm.antimine.utils.BuildExt.androidOreo
import dev.lucasnlm.external.PlayGamesManager
import org.koin.android.ext.android.inject
import dev.lucasnlm.antimine.i18n.R as i18n

class PreferencesActivity :
    ThemedActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val audioManager: GameAudioManager by inject()
    private val playGamesManager: PlayGamesManager by inject()
    private val preferencesRepository: PreferencesRepository by inject()
    private val cloudSaveManager by inject<CloudSaveManager>()
    private val settingsBackupManager: SettingsBackupManager by lazy {
        SettingsBackupManager(applicationContext)
    }
    private val binding: ActivityPreferencesBinding by lazy {
        ActivityPreferencesBinding.inflate(layoutInflater)
    }

    private lateinit var exportResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var importResultLauncher: ActivityResultLauncher<Intent>

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

        setContentView(binding.root)

        exportResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    var result = false
                    val target = it.data?.data
                    if (target != null) {
                        val data = preferencesRepository.exportData()
                        result = settingsBackupManager.exportSettings(target, data)
                    }

                    if (result) {
                        showWarning(
                            resId = i18n.string.exported_success,
                            container = binding.root,
                        )
                    } else {
                        showWarning(
                            resId = i18n.string.error,
                            container = binding.root,
                        )
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
                            preferencesRepository.importData(data)
                            result = true
                        }
                    }

                    if (result) {
                        showWarning(
                            resId = i18n.string.imported_success,
                            container = binding.root,
                        )
                        bindItems()
                    } else {
                        showWarning(
                            resId = i18n.string.error,
                            container = binding.root,
                        )
                    }
                }
            }

        bindToolbar(binding.toolbar)
        bindToolbarAction(preferencesRepository.hasCustomizations())
        bindItems()

        preferenceManager.registerOnSharedPreferenceChangeListener(this)
    }

    private fun bindItems() {
        binding.hapticFeedback.bindItem(
            initialValue = preferencesRepository.useHapticFeedback(),
            onChangeValue = {
                preferencesRepository.setHapticFeedback(it)
                if (it && preferencesRepository.getHapticFeedbackLevel() == 0) {
                    preferencesRepository.resetHapticFeedbackLevel()
                }
            },
        )

        binding.soundEffects.bindItem(
            visible = androidMarshmallow(),
            initialValue = preferencesRepository.isSoundEffectsEnabled(),
            onChangeValue = { preferencesRepository.setSoundEffectsEnabled(it) },
        )

        binding.music.bindItem(
            visible = androidMarshmallow(),
            initialValue = preferencesRepository.isMusicEnabled(),
            onChangeValue = { preferencesRepository.setMusicEnabled(it) },
        )

        binding.showWindows.bindItem(
            initialValue = preferencesRepository.showWindowsWhenFinishGame(),
            onChangeValue = { preferencesRepository.mustShowWindowsWhenFinishGame(it) },
        )

        binding.openDirectly.bindItem(
            initialValue = preferencesRepository.openGameDirectly(),
            onChangeValue = { preferencesRepository.setOpenGameDirectly(it) },
        )

        binding.useQuestionMark.bindItem(
            initialValue = preferencesRepository.useQuestionMark(),
            onChangeValue = { preferencesRepository.setQuestionMark(it) },
        )

        binding.showTimer.bindItem(
            initialValue = preferencesRepository.showTimer(),
            onChangeValue = { preferencesRepository.setTimerVisible(it) },
        )

        binding.automaticFlags.bindItem(
            initialValue = preferencesRepository.useFlagAssistant(),
            onChangeValue = { preferencesRepository.setFlagAssistant(it) },
        )

        binding.hint.bindItem(
            initialValue = preferencesRepository.useHelp(),
            onChangeValue = { preferencesRepository.setHelp(it) },
        )

        binding.noGuessingMode.bindItem(
            initialValue = preferencesRepository.useSimonTathamAlgorithm(),
            onChangeValue = { preferencesRepository.setSimonTathamAlgorithm(it) },
        )

        binding.allowClickNumber.bindItem(
            initialValue = preferencesRepository.allowTapOnNumbers(),
            onChangeValue = {
                preferencesRepository.setAllowTapOnNumbers(it)
                binding.flagWhenTapOnNumbers.isVisible = it
                binding.flagWhenTapOnNumbers.isVisible = it
            },
        )

        binding.flagWhenTapOnNumbers.bindItem(
            initialValue = preferencesRepository.letNumbersAutoFlag(),
            onChangeValue = { preferencesRepository.setNumbersAutoFlag(it) },
        )

        if (!preferencesRepository.allowTapOnNumbers()) {
            binding.flagWhenTapOnNumbers.isVisible = false
            binding.flagWhenTapOnNumbers.isVisible = false
        }

        binding.highlightUnsolvedNumbers.bindItem(
            initialValue = preferencesRepository.dimNumbers(),
            onChangeValue = { preferencesRepository.setDimNumbers(it) },
        )

        binding.immersiveMode.bindItem(
            initialValue = preferencesRepository.useImmersiveMode(),
            onChangeValue = { preferencesRepository.setImmersiveMode(it) },
        )

        if (playGamesManager.hasGooglePlayGames()) {
            binding.playGames.bindItem(
                initialValue = preferencesRepository.keepRequestPlayGames(),
                onChangeValue = {
                    preferencesRepository.setRequestPlayGames(it)
                },
            )
        } else {
            binding.playGames.isVisible = false
        }

        binding.exportSettings.apply {
            isVisible = !isAndroidAuto()
            setOnClickListener {
                val exportIntent =
                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(Intent.EXTRA_TITLE, SettingsBackupManager.FILE_NAME)
                        type = "application/json"

                        androidOreo {
                            val documentsDir =
                                Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOCUMENTS,
                                )
                            putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentsDir.toUri())
                        }
                    }

                exportResultLauncher.launch(exportIntent)
            }
        }

        binding.importSettings.apply {
            isVisible = !isAndroidAuto()
            setOnClickListener {
                val exportIntent =
                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(Intent.EXTRA_TITLE, SettingsBackupManager.FILE_NAME)
                        type = "application/json"

                        androidOreo {
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
                        preferencesRepository.reset()
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
        bindToolbarAction(preferencesRepository.hasCustomizations())
    }
}
