package dev.lucasnlm.antimine.core

interface IAppVersionManager {
    fun isValid(): Boolean
    fun isWatch(): Boolean
}
