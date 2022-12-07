package dev.lucasnlm.antimine.licenses.viewmodel

import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel

class LicenseViewModel : IntentViewModel<Void, LicenseState>() {
    private fun getLicensesList() = mapOf(
        "Android SDK License" to R.raw.android_sdk,
        "Koin" to R.raw.apache2,
        "LibGDX" to R.raw.apache2,
        "Material Design" to R.raw.apache2,
        "Moshi" to R.raw.apache2,
        "Mockk" to R.raw.apache2,
        "Noto Emoji" to R.raw.apache2,
        "Sounds" to R.raw.sounds,
    ).map {
        License(it.key, it.value)
    }.toList()

    override fun initialState(): LicenseState {
        return LicenseState(licenses = getLicensesList())
    }
}
