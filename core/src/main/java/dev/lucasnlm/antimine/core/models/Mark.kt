package dev.lucasnlm.antimine.core.models

import androidx.annotation.Keep

@Keep
enum class Mark(
    val ligatureMask: Int,
) {
    None(0),
    Flag(1),
    Question(1),
    PurposefulNone(0),
    ;

    fun isFlag(): Boolean = this == Flag

    fun isQuestion(): Boolean = this == Question

    fun isPureNone(): Boolean = this == None

    fun isNone(): Boolean = this == None || this == PurposefulNone

    fun isNotNone(): Boolean = this.isNone().not()
}
