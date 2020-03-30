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
        listOf(
            TranslationInfo(
                "Czech",
                sequenceOf("novas78@xda")
            ),
            TranslationInfo(
                "French",
                sequenceOf("Just Humeau")
            ),
            TranslationInfo(
                "German",
                sequenceOf("Oswald Boelcke", "wlls_ftn")
            ),
            TranslationInfo(
                "Portuguese (BR)",
                sequenceOf("Lucas Lima")
            ),
            TranslationInfo(
                "Russian",
                sequenceOf("gaich@xda", "ask45t")
            ),
            TranslationInfo(
                "Spanish",
                sequenceOf("Alfredo Jara")
            ),
            TranslationInfo(
                "Turkish",
                sequenceOf("Fatih Fırıncı")
            ),
            TranslationInfo(
                "Ukrainian",
                sequenceOf("dmitry.sh")
            ),
            TranslationInfo(
                "Vietnamese",
                sequenceOf("pnhpnh")
            )
        )
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
                "Dagger",
                R.raw.apache2
            ),
            ThirdParty(
                "Moshi",
                R.raw.apache2
            ),
            ThirdParty(
                "Mockito",
                R.raw.mockito
            ),
            ThirdParty(
                "Sounds",
                R.raw.sounds
            )
        )
    )
}
