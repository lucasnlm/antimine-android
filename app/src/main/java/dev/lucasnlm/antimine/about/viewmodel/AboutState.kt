package dev.lucasnlm.antimine.about.viewmodel

import androidx.annotation.RawRes

data class License(
    val name: String,
    @RawRes val licenseFileRes: Int,
)

data class TranslationInfo(
    val language: String,
    val translators: Sequence<String>,
)

data class AboutState(
    val translators: List<TranslationInfo>,
    val licenses: List<License>,
)
