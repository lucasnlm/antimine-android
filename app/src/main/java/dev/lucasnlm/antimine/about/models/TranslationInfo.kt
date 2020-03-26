package dev.lucasnlm.antimine.about.models

data class TranslationInfo(
    val language: String,
    val translators: Sequence<String>
)
