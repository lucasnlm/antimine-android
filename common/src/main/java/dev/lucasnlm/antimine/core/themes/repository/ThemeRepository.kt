package dev.lucasnlm.antimine.core.themes.repository

import android.content.Context
import androidx.core.content.ContextCompat
import dev.lucasnlm.antimine.common.R
import dev.lucasnlm.antimine.core.themes.model.AreaPalette
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.core.themes.model.AppTheme
import dev.lucasnlm.antimine.core.themes.model.Assets

interface IThemeRepository {
    fun getCustomTheme(): AppTheme?
    fun getTheme(): AppTheme
    fun getAllThemes(): List<AppTheme>
    fun setTheme(theme: AppTheme)
    fun reset(): AppTheme
}

class ThemeRepository(
    private val context: Context,
    private val preferenceRepository: IPreferencesRepository,
) : IThemeRepository {
    override fun getCustomTheme(): AppTheme? {
        return getAllThemes().firstOrNull { it.id == preferenceRepository.themeId() }
    }

    override fun getTheme(): AppTheme {
        return getCustomTheme() ?: buildSystemTheme()
    }

    override fun getAllThemes(): List<AppTheme> =
        listOf(buildSystemTheme()) + Themes.getAllCustom()

    override fun setTheme(theme: AppTheme) {
        preferenceRepository.useTheme(theme.id)
    }

    override fun reset(): AppTheme {
        preferenceRepository.useTheme(0L)
        return buildSystemTheme()
    }

    private fun buildSystemTheme(): AppTheme {
        return AppTheme(
            id = 0L,
            theme = R.style.AppTheme,
            themeNoActionBar = R.style.AppTheme_NoActionBar,
            palette = fromDefaultPalette(context),
            assets = fromDefaultAssets()
        )
    }

    private fun fromDefaultAssets() =
        Assets(
            wrongFlag = R.drawable.red_flag,
            flag = R.drawable.flag,
            questionMark = R.drawable.question,
            toolbarMine = R.drawable.mine,
            mine = R.drawable.mine,
            mineExploded = R.drawable.mine_exploded_red,
            mineLow = R.drawable.mine_low
        )

    private fun fromDefaultPalette(context: Context) =
        AreaPalette(
            border = ContextCompat.getColor(context, R.color.view_cover),
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
