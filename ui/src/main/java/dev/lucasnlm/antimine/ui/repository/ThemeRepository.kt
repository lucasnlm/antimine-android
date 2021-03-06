package dev.lucasnlm.antimine.ui.repository

import android.content.Context
import androidx.core.content.ContextCompat
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.AreaPalette

interface IThemeRepository {
    fun getCustomTheme(): AppTheme?
    fun getTheme(): AppTheme
    fun getAllThemes(): List<AppTheme>
    fun setTheme(themeId: Long)
    fun reset(): AppTheme
}

class ThemeRepository(
    private val context: Context,
    private val preferenceRepository: IPreferencesRepository,
) : IThemeRepository {
    override fun getCustomTheme(): AppTheme? {
        val targetThemeId = preferenceRepository.themeId()
        return getAllThemes().firstOrNull { it.id == targetThemeId }
    }

    override fun getTheme(): AppTheme {
        return getCustomTheme() ?: buildSystemTheme()
    }

    override fun getAllThemes(): List<AppTheme> =
        listOf(buildSystemTheme()) + Themes.getAllCustom()

    override fun setTheme(themeId: Long) {
        preferenceRepository.useTheme(themeId)
    }

    override fun reset(): AppTheme {
        preferenceRepository.useTheme(0L)
        return buildSystemTheme()
    }

    private fun buildSystemTheme(): AppTheme {
        return AppTheme(
            id = 0L,
            theme = R.style.AppTheme,
            palette = fromDefaultPalette(context),
            isPaid = false,
        )
    }

    private fun fromDefaultPalette(context: Context) =
        AreaPalette(
            accent = ContextCompat.getColor(context, R.color.accent),
            background = ContextCompat.getColor(context, R.color.background),
            covered = ContextCompat.getColor(context, R.color.view_cover),
            coveredOdd = ContextCompat.getColor(context, R.color.view_cover),
            uncovered = ContextCompat.getColor(context, R.color.view_clean),
            uncoveredOdd = ContextCompat.getColor(context, R.color.view_clean),
            minesAround1 = ContextCompat.getColor(context, R.color.mines_around_1),
            minesAround2 = ContextCompat.getColor(context, R.color.mines_around_2),
            minesAround3 = ContextCompat.getColor(context, R.color.mines_around_3),
            minesAround4 = ContextCompat.getColor(context, R.color.mines_around_4),
            minesAround5 = ContextCompat.getColor(context, R.color.mines_around_5),
            minesAround6 = ContextCompat.getColor(context, R.color.mines_around_6),
            minesAround7 = ContextCompat.getColor(context, R.color.mines_around_7),
            minesAround8 = ContextCompat.getColor(context, R.color.mines_around_8),
            highlight = ContextCompat.getColor(context, R.color.highlight),
            focus = ContextCompat.getColor(context, R.color.accent)
        )
}
