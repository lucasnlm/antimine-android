package dev.lucasnlm.antimine.ui.repository

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.AreaPalette
import dev.lucasnlm.antimine.utils.BuildExt.androidSnowCone
import dev.lucasnlm.antimine.i18n.R as i18n

class ThemeRepositoryImpl(
    private val context: Context,
    private val preferenceRepository: PreferencesRepository,
) : ThemeRepository {

    private fun getDefaultTheme(): AppTheme {
        return if (preferenceRepository.isPremiumEnabled()) {
            buildSystemTheme()
        } else {
            Themes.lightTheme()
        }
    }

    override fun getCustomTheme(): AppTheme? {
        val targetThemeId = preferenceRepository.themeId()
        return getAllThemes().firstOrNull { it.id == targetThemeId }
    }

    override fun getSkin(): AppSkin {
        val targetSkinId = preferenceRepository.skinId()
        val allSkins = getAllSkins()
        return allSkins.firstOrNull { it.id == targetSkinId } ?: allSkins.first()
    }

    override fun getTheme(): AppTheme {
        return getCustomTheme() ?: getDefaultTheme()
    }

    override fun getAllThemes(): List<AppTheme> {
        return listOf(buildSystemTheme()) + Themes.getAllCustom()
    }

    override fun getAllDarkThemes(): List<AppTheme> {
        return getAllThemes().filter { it.isDarkTheme }
    }

    override fun getAllSkins(): List<AppSkin> {
        return Skins.getAllSkins()
    }

    override fun setTheme(themeId: Long) {
        preferenceRepository.useTheme(themeId)
    }

    override fun setSkin(skinId: Long) {
        preferenceRepository.useSkin(skinId)
    }

    override fun reset(): AppTheme {
        val defaultTheme = getDefaultTheme()
        preferenceRepository.useTheme(defaultTheme.id)
        return defaultTheme
    }

    private fun buildSystemTheme(): AppTheme {
        return AppTheme(
            id = 0L,
            theme = R.style.AppTheme,
            palette =
                if (androidSnowCone()) {
                    fromMaterialYou(context)
                } else {
                    fromDefaultPalette(context)
                },
            isPremium = true,
            isDarkTheme = isDarkTheme(),
            name = i18n.string.system,
        )
    }

    private fun isDarkTheme(): Boolean {
        val mask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mask == Configuration.UI_MODE_NIGHT_YES
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun fromMaterialYou(context: Context): AreaPalette {
        val isDarkTheme = isDarkTheme()
        val background =
            if (isDarkTheme) {
                ContextCompat.getColor(context, R.color.background)
            } else {
                ContextCompat.getColor(context, android.R.color.background_light)
            }

        val coveredColor =
            if (isDarkTheme) {
                ContextCompat.getColor(context, android.R.color.system_accent1_300)
            } else {
                ContextCompat.getColor(context, android.R.color.system_accent1_600)
            }

        return AreaPalette(
            accent = ContextCompat.getColor(context, android.R.color.system_accent1_500),
            background = background,
            covered = coveredColor,
            coveredOdd = coveredColor,
            uncovered = background,
            uncoveredOdd = background,
            minesAround1 = ContextCompat.getColor(context, R.color.mines_around_1),
            minesAround2 = ContextCompat.getColor(context, R.color.mines_around_2),
            minesAround3 = ContextCompat.getColor(context, R.color.mines_around_3),
            minesAround4 = ContextCompat.getColor(context, R.color.mines_around_4),
            minesAround5 = ContextCompat.getColor(context, R.color.mines_around_5),
            minesAround6 = ContextCompat.getColor(context, R.color.mines_around_6),
            minesAround7 = ContextCompat.getColor(context, R.color.mines_around_7),
            minesAround8 = ContextCompat.getColor(context, R.color.mines_around_8),
            highlight = ContextCompat.getColor(context, R.color.highlight),
            focus = ContextCompat.getColor(context, android.R.color.system_accent1_500),
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
            focus = ContextCompat.getColor(context, R.color.accent),
        )
}
