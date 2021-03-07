package dev.lucasnlm.antimine.about.viewmodel

import androidx.annotation.RawRes

data class License(
    val name: String,
    @RawRes val licenseFileRes: Int,
)

data class AboutState(
    val licenses: List<License>,
)
