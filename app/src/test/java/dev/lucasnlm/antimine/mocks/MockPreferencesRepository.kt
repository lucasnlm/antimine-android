package dev.lucasnlm.antimine.mocks

import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

class MockPreferencesRepository : IPreferencesRepository {
    override fun customGameMode(): Minefield = Minefield(9, 9, 9)

    override fun updateCustomGameMode(minefield: Minefield) { }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = false

    override fun getInt(key: String, defaultValue: Int): Int = 0

    override fun putBoolean(key: String, value: Boolean) { }

    override fun putInt(key: String, value: Int) { }

    override fun useFlagAssistant(): Boolean = true

    override fun useHapticFeedback(): Boolean = false

    override fun useLargeAreas(): Boolean = false

    override fun useAnimations(): Boolean = false

    override fun useDoubleClickToOpen(): Boolean = false
}
