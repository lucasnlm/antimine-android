package dev.lucasnlm.antimine.core

interface AppVersionManager {
    fun isValid(): Boolean

    fun isWatch(): Boolean
}
