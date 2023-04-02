package dev.lucasnlm.antimine.wear.main

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import dev.lucasnlm.antimine.wear.databinding.ActivityThemesBinding
import dev.lucasnlm.antimine.wear.main.view.ThemeListAdapter
import org.koin.android.ext.android.inject

class WearThemeActivity : ThemedActivity() {
    private lateinit var binding: ActivityThemesBinding
    private val preferencesRepository: IPreferencesRepository by inject()
    private val themesRepository: IThemeRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThemesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refreshThemeList()
    }

    private fun refreshThemeList() {
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@WearThemeActivity)
            adapter = ThemeListAdapter(
                themes = themesRepository.getAllThemes(),
                preferencesRepository = preferencesRepository,
                onSelectTheme = { theme ->
                    preferencesRepository.useTheme(theme.id)
                    recreate()
                },
            )
        }
    }
}
