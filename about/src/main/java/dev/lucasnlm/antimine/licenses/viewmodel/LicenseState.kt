package dev.lucasnlm.antimine.licenses.viewmodel

data class License(
    val name: String,
    val url: String,
)

data class LicenseState(
    val licenses: List<License>,
)
