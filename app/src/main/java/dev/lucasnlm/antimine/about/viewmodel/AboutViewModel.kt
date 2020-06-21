package dev.lucasnlm.antimine.about.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.models.AboutEvent
import dev.lucasnlm.antimine.about.models.ThirdParty
import dev.lucasnlm.antimine.about.models.TranslationInfo

class AboutViewModel : ViewModel() {
    val eventObserver = MutableLiveData<AboutEvent>()

    fun getTranslatorsList() = mapOf(
        "Chinese Simplified" to sequenceOf("linsui"),
        "Czech" to sequenceOf("novas78@xda"),
        "French" to sequenceOf("Just Humeau"),
        "German" to sequenceOf("Oswald Boelcke", "wlls_ftn", "Trafalgar-Square"),
        "Greek" to sequenceOf("Retrial"),
        "Japanese" to sequenceOf("Ryota Hasegawa"),
        "Portuguese (BR)" to sequenceOf("Lucas Lima"),
        "Russian" to sequenceOf("gaich@xda", "ask45t"),
        "Spanish" to sequenceOf("Alfredo Jara"),
        "Turkish" to sequenceOf("Fatih Fırıncı"),
        "Ukrainian" to sequenceOf("Dmitry Shuba"),
        "Vietnamese" to sequenceOf("pnhpnh")
    ).map {
        TranslationInfo(it.key, it.value)
    }.toList()

    fun getLicensesList() = mapOf(
        "Android SDK License" to R.raw.android_sdk,
        "Material Design Icons" to R.raw.apache2,
        "Dagger Hilt" to R.raw.apache2,
        "Moshi" to R.raw.apache2,
        "Mockito" to R.raw.mockito,
        "Sounds" to R.raw.sounds
    ).map {
        ThirdParty(it.key, it.value)
    }.toList()
}
