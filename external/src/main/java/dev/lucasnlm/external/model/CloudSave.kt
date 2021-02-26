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
    val noGuessing: Int,
    val language: String,
    val openDirectly: Int,
)

fun CloudSave.toHashMap(): HashMap<String, Any> = hashMapOf(
    "uid" to playId,
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
    "language" to language,
    "openDirectly" to openDirectly,
)

private fun Any?.parseInt(): Int = this?.toString()?.toInt() ?: throw Exception("Fail to parse Int")
private fun Any?.parseInt(default: Int): Int = this?.toString()?.toInt() ?: default
private fun Any?.parseString(default: String): String = this?.toString() ?: default

@Suppress("UNCHECKED_CAST")
fun cloudSaveOf(id: String, data: Map<String, Any>) =
    CloudSave(
        id,
        data["completeTutorial"].parseInt(),
        data["selectedTheme"].parseInt(),
        data["squareRadius"].parseInt(),
        data["squareSize"].parseInt(),
        data["touchTiming"].parseInt(),
        data["questionMark"].parseInt(),
        data["gameAssistance"].parseInt(),
        data["help"].parseInt(),
        data["hapticFeedback"].parseInt(),
        data["soundEffects"].parseInt(),
        data["stats"] as List<HashMap<String, String>>,
        data["premiumFeatures"].parseInt(),
        data["controlStyle"].parseInt(),
        (data["noGuessing"] ?: 1).parseInt(),
        data["language"].parseString(""),
        data["openDirectly"].parseInt(0),
    )
