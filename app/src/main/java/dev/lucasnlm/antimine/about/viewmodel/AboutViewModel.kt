package dev.lucasnlm.antimine.about.viewmodel

import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.models.AboutState
import dev.lucasnlm.antimine.about.models.License
import dev.lucasnlm.antimine.about.models.TranslationInfo
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel

class AboutViewModel : IntentViewModel<AboutEvent, AboutState>() {

    private fun getTranslatorsList() = mapOf(
        "Arabic" to sequenceOf("Ahmad Alkurbi"),
        "Bulgarian" to sequenceOf("Georgi Eftimov"),
        "Chinese Simplified" to sequenceOf("linsui", "yilinzhao2020"),
        "Catalan" to sequenceOf("dmanye", "Archison"),
        "Czech" to sequenceOf("novas78@xda"),
        "Dutch" to sequenceOf("Max Pietersma"),
        "English" to sequenceOf("miguelsouza2212"),
        "Finnish" to sequenceOf("Topusku"),
        "French" to sequenceOf("Just Humeau"),
        "German" to sequenceOf("Oswald Boelcke", "wlls_ftn", "Trafalgar-Square"),
        "Greek" to sequenceOf("Retrial"),
        "Hungarian" to sequenceOf("Hermann Márk"),
        "Italian" to sequenceOf("Mattia - MisterWeeMan", "Nicola Lorenzetti"),
        "Japanese" to sequenceOf("Ryota Hasegawa"),
        "Norwegian" to sequenceOf("Eule Hecking"),
        "Polish" to sequenceOf("Sebastian Jasiński", "Sebastian Skibiński"),
        "Portuguese (BR)" to sequenceOf("Lucas Lima"),
        "Russian" to sequenceOf("gaich@xda", "ask45t", "Ekaterina543"),
        "Spanish" to sequenceOf("Alfredo Jara", "Aldo Rodriguez", "Inail"),
        "Turkish" to sequenceOf("Fatih Fırıncı"),
        "Ukrainian" to sequenceOf("Dmitry Shuba"),
        "Vietnamese" to sequenceOf("pnhpnh")
    ).map {
        TranslationInfo(it.key, it.value)
    }.toList()

    private fun getLicensesList() = mapOf(
        "Android SDK License" to R.raw.android_sdk,
        "Material Design Icons" to R.raw.apache2,
        "Dagger Hilt" to R.raw.apache2,
        "Moshi" to R.raw.apache2,
        "Mockito" to R.raw.mockito,
        "Sounds" to R.raw.sounds
    ).map {
        License(it.key, it.value)
    }.toList()

    override fun initialState(): AboutState = AboutState(
        translators = getTranslatorsList(),
        licenses = getLicensesList()
    )
}
