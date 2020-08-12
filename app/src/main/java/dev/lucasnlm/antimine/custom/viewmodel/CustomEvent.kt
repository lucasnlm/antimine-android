package dev.lucasnlm.antimine.custom.viewmodel

sealed class CustomEvent {
    data class ValidateInputEvent(
        val width: Int,
        val height: Int,
        val mines: Int
    ) : CustomEvent()
}

