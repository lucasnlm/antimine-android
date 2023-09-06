package dev.lucasnlm.external.model

import java.security.InvalidKeyException

data class CloudSave(
    val playId: String,
    val completeTutorial: Int,
    val selectedTheme: Int,
    val selectedSkin: Int,
    val touchTiming: Int,
    val questionMark: Int,
    val gameAssistance: Int,
    val help: Int,
    val hapticFeedback: Int,
    val soundEffects: Int,
    val music: Int,
    val stats: List<HashMap<String, String>>,
    val premiumFeatures: Int,
    val controlStyle: Int,
    val openDirectly: Int,
    val doubleClickTimeout: Int,
    val allowTapNumbers: Int,
    val hapticFeedbackLevel: Int,
    val highlightNumbers: Int,
    val leftHanded: Int,
    val dimNumbers: Int,
    val timerVisible: Int,
)

fun CloudSave.toHashMap(): HashMap<String, Any> =
    hashMapOf(
        "uid" to playId,
        "completeTutorial" to completeTutorial,
        "selectedTheme" to selectedTheme,
        "selectedSkin" to selectedSkin,
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
        "openDirectly" to openDirectly,
        "doubleClickTimeout" to doubleClickTimeout,
        "allowTapNumbers" to allowTapNumbers,
        "highlightNumbers" to highlightNumbers,
        "leftHanded" to leftHanded,
        "dimNumbers" to dimNumbers,
    )

private fun Any?.parseInt(): Int = this?.toString()?.toInt() ?: throw InvalidKeyException("Fail to parse Int")

private fun Any?.parseInt(default: Int): Int = this?.toString()?.toInt() ?: default

@Suppress("UNCHECKED_CAST")
fun cloudSaveOf(
    id: String,
    data: Map<String, Any>,
) = CloudSave(
    playId = id,
    completeTutorial = data["completeTutorial"].parseInt(),
    selectedTheme = data["selectedTheme"].parseInt(),
    selectedSkin = data["selectedSkin"].parseInt(0),
    touchTiming = data["touchTiming"].parseInt(),
    questionMark = data["questionMark"].parseInt(),
    gameAssistance = data["gameAssistance"].parseInt(),
    help = data["help"].parseInt(),
    hapticFeedback = data["hapticFeedback"].parseInt(),
    hapticFeedbackLevel = data["hapticFeedbackLevel"].parseInt(100),
    soundEffects = data["soundEffects"].parseInt(),
    music = data["music"].parseInt(1),
    stats = data["stats"] as List<HashMap<String, String>>,
    premiumFeatures = data["premiumFeatures"].parseInt(),
    controlStyle = data["controlStyle"].parseInt(),
    openDirectly = data["openDirectly"].parseInt(0),
    doubleClickTimeout = data["doubleClickTimeout"].parseInt(400),
    allowTapNumbers = data["allowTapNumbers"].parseInt(1),
    highlightNumbers = data["highlightNumbers"].parseInt(0),
    leftHanded = data["leftHanded"].parseInt(0),
    dimNumbers = data["dimNumbers"].parseInt(0),
    timerVisible = data["timerVisible"].parseInt(1),
)
