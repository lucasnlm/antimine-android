package dev.lucasnlm.external.model

data class CloudSave(
    val playId: String,
    val lastWidth: Int,
    val lastHeight: Int,
    val lastMines: Int,
    val completeTutorial: Int,
    val selectedTheme: Int,
    val squareRadius: Int,
    val squareSize: Int,
    val touchTiming: Int,
    val questionMark: Int,
    val gameAssistance: Int,
    val help: Int,
    val hapticFeedback: Int,
    val soundEffects: Int,
    val stats: List<Map<String, String>>
)

fun CloudSave.toHashMap(): HashMap<String, Any> = hashMapOf(
    "lastWidth" to lastWidth,
    "lastHeight" to lastHeight,
    "lastMines" to lastMines,
    "completeTutorial" to completeTutorial,
    "selectedTheme" to selectedTheme,
    "squareRadius" to squareRadius,
    "squareSize" to squareSize,
    "touchTiming" to touchTiming,
    "questionMark" to questionMark,
    "gameAssistance" to gameAssistance,
    "help" to help,
    "hapticFeedback" to hapticFeedback,
    "soundEffects" to soundEffects,
    "stats" to stats,
)

@Suppress("UNCHECKED_CAST")
fun cloudSaveOf(id: String, data: Map<String, Any>) =
    CloudSave(
        id,
        data["lastWidth"].toString().toInt(),
        data["lastHeight"].toString().toInt(),
        data["lastMines"].toString().toInt(),
        data["completeTutorial"].toString().toInt(),
        data["selectedTheme"].toString().toInt(),
        data["squareRadius"].toString().toInt(),
        data["squareSize"].toString().toInt(),
        data["touchTiming"].toString().toInt(),
        data["questionMark"].toString().toInt(),
        data["gameAssistance"].toString().toInt(),
        data["help"].toString().toInt(),
        data["hapticFeedback"].toString().toInt(),
        data["soundEffects"].toString().toInt(),
        data["stats"] as List<Map<String, String>>,
)

