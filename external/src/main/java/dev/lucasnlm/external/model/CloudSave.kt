package dev.lucasnlm.external.model

data class CloudSave(
    val playId: String,
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
    val stats: List<HashMap<String, String>>,
    val premiumFeatures: Int,
    val controlStyle: Int,
    val noGuessing: Int,
)

fun CloudSave.toHashMap(): HashMap<String, Any> = hashMapOf(
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
    "premiumFeatures" to premiumFeatures,
    "controlStyle" to controlStyle,
    "noGuessing" to noGuessing,
)

@Suppress("UNCHECKED_CAST")
fun cloudSaveOf(id: String, data: Map<String, Any>) =
    CloudSave(
        id,
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
        data["stats"] as List<HashMap<String, String>>,
        data["premiumFeatures"].toString().toInt(),
        data["controlStyle"].toString().toInt(),
        (data["noGuessing"] ?: 1).toString().toInt(),
    )
