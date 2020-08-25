package dev.lucasnlm.antimine.text.viewmodel

import androidx.annotation.RawRes

sealed class TextEvent {
    data class LoadText(
        val title: String,
        @RawRes val rawFileRes: Int,
    ) : TextEvent()
}
