package dev.lucasnlm.external.model

import java.lang.Exception

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
    val language: String,
    val openDirectly: Int,
    val unlockedThemes: String,
    val squareDivider: Int,
    val doubleClickTimeout: Int,
    val allowTapNumbers: Int,
    val hapticFeedbackLevel: Int,
    val highlightNumbers: Int,
)

fun CloudSave.toHashMap(): HashMap<String, Any> = hashMapOf(
    "uid" to playId,
    "completeTutorial" to completeTutorial,
    "selectedTheme" to selectedTheme,
    "newSquareRadius" to squareRadius,
    "newSquareSize" to squareSize,
    "touchTiming" to touchTiming,
    "questionMark" to questionMark,
    "gameAssistance" to gameAssistance,
    "help" to help,
    "hapticFeedback" to hapticFeedback,
    "hapticFeedbackLevel" to hapticFeedbackLevel,
    "soundEffects" to soundEffects,
    "stats" to stats,
    "premiumFeatures" to premiumFeatures,
    "controlStyle" to controlStyle,
    "language" to language,
    "openDirectly" to openDirectly,
    "unlockedThemes" to unlockedThemes,
    "newSquareDivider" to squareDivider,
    "doubleClickTimeout" to doubleClickTimeout,
    "allowTapNumbers" to allowTapNumbers,
    "highlightNumbers" to highlightNumbers,
)

private fun Any?.parseInt(): Int = this?.toString()?.toInt() ?: throw Exception("Fail to parse Int")
private fun Any?.parseInt(default: Int): Int = this?.toString()?.toInt() ?: default
private fun Any?.parseString(default: String): String = this?.toString() ?: default

@Suppress("UNCHECKED_CAST")
fun cloudSaveOf(id: String, data: Map<String, Any>) =
    CloudSave(
        playId = id,
        completeTutorial = data["completeTutorial"].parseInt(),
        selectedTheme = data["selectedTheme"].parseInt(),
        squareRadius = data["newSquareRadius"].parseInt(3),
        squareSize = data["newSquareSize"].parseInt(50),
        touchTiming = data["touchTiming"].parseInt(),
        questionMark = data["questionMark"].parseInt(),
        gameAssistance = data["gameAssistance"].parseInt(),
        help = data["help"].parseInt(),
        hapticFeedback = data["hapticFeedback"].parseInt(),
        hapticFeedbackLevel = data["hapticFeedbackLevel"].parseInt(100),
        soundEffects = data["soundEffects"].parseInt(),
        stats = data["stats"] as List<HashMap<String, String>>,
        premiumFeatures = data["premiumFeatures"].parseInt(),
        controlStyle = data["controlStyle"].parseInt(),
        language = data["language"].parseString(""),
        openDirectly = data["openDirectly"].parseInt(0),
        unlockedThemes = data["unlockedThemes"].parseString(""),
        squareDivider = data["newSquareDivider"].parseInt(0),
        doubleClickTimeout = data["doubleClickTimeout"].parseInt(400),
        allowTapNumbers = data["allowTapNumbers"].parseInt(1),
        highlightNumbers = data["highlightNumbers"].parseInt(0),
    )
