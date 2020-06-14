package dev.lucasnlm.antimine.about.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.models.AboutEvent
import dev.lucasnlm.antimine.about.models.ThirdParty
import dev.lucasnlm.antimine.about.models.TranslationInfo
import dev.lucasnlm.antimine.about.views.thirds.ThirdPartyAdapter
import dev.lucasnlm.antimine.about.views.translators.TranslatorsAdapter

class AboutViewModel : ViewModel() {
    val eventObserver = MutableLiveData<AboutEvent>()

    fun getTranslators() = TranslatorsAdapter(
        sequenceOf(
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
        ).map { TranslationInfo(it.first, it.second) }.toList()
    )

    fun getLicenses() = ThirdPartyAdapter(
        listOf(
            ThirdParty(
                "Android SDK License",
                R.raw.android_sdk
            ),
            ThirdParty(
                "Material Design Icons",
                R.raw.apache2
            ),
            ThirdParty(
                "Dagger Hilt",
                R.raw.apache2
            ),
            ThirdParty(
                "Moshi",
                R.raw.apache2
            ),
            ThirdParty(
                "Mockito",
                R.raw.mockito
            )
        )
    )
}
