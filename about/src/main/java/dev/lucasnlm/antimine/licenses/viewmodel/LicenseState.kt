package dev.lucasnlm.antimine.licenses.viewmodel

import androidx.annotation.RawRes

data class License(
    val name: String,
    @RawRes val licenseFileRes: Int,
)

data class LicenseState(
    val licenses: List<License>,
)
