package dev.lucasnlm.antimine.about.translators.model

data class TranslationInfo(
    val language: String,
    val translators: Sequence<String>
)
