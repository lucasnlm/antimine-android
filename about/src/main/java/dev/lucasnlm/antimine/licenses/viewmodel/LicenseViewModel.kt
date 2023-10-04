package dev.lucasnlm.antimine.licenses.viewmodel

import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel

class LicenseViewModel : IntentViewModel<Void, LicenseState>() {
    private fun getLicensesList() =
        mapOf(
            "Antimine" to "https://github.com/lucasnlm/antimine-android/blob/main/LICENSE",
            "Android SDK License" to "https://developer.android.com/studio/terms",
            "Koin" to "https://github.com/InsertKoinIO/koin/blob/main/LICENSE",
            "LibGDX" to "https://github.com/libgdx/libgdx/blob/master/LICENSE",
            "Material Design" to "https://github.com/material-components/material-components-android/",
            "Mockk" to "https://github.com/mockk/mockk/blob/master/LICENSE",
            "Noto Emoji" to "https://github.com/googlefonts/noto-emoji/blob/main/fonts/LICENSE",
            "kotlin" to "https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt",
            "kotlinx.coroutines" to "https://github.com/Kotlin/kotlinx.coroutines/blob/master/LICENSE.txt",
        ).map {
            License(it.key, it.value)
        }.sortedBy {
            it.name
        }.toList()

    override fun initialState(): LicenseState {
        return LicenseState(licenses = getLicensesList())
    }
}
