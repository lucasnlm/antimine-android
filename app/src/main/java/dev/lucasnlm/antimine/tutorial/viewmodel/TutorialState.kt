package dev.lucasnlm.antimine.tutorial.viewmodel

data class TutorialState(
    val step: Int,
    val topMessage: String,
    val bottomMessage: String,
    val completed: Boolean,
)
