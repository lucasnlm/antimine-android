package dev.lucasnlm.antimine.common.level.data

import androidx.annotation.Keep

@Keep
enum class Mark {
    None,
    Flag,
    Question,
    PurposefulNone
}

fun Mark.isFlag(): Boolean = this == Mark.Flag

fun Mark.isQuestion(): Boolean = this == Mark.Question

fun Mark.isNone(): Boolean = this == Mark.None || this == Mark.PurposefulNone

fun Mark.isNotNone(): Boolean = this.isNone().not()
