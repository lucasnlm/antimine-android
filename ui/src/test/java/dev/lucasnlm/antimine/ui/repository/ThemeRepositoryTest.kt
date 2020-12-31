package dev.lucasnlm.antimine.ui.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeRepositoryTest {
    @Test
    fun getAllThemesMustNotHaveDuplicatedIds() {
        val customThemes = Themes.getAllCustom()
        customThemes.distinctBy { it.id }.count()
        assertEquals(customThemes.size, customThemes.distinctBy { it.id }.count())
    }
}
