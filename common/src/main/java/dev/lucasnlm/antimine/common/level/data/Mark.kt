package dev.lucasnlm.antimine.common.level.data

enum class Mark {
    None,
    Flag,
    Question,
    PurposefulNone
}

fun Mark.isFlag(): Boolean = this == Mark.Flag

fun Mark.isQuestion(): Boolean = this == Mark.Question
