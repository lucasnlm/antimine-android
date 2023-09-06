package dev.lucasnlm.antimine.gameover.model

import android.os.Parcel
import android.os.Parcelable

data class CommonDialogState(
    val gameResult: GameResult,
    val showContinueButton: Boolean,
    val rightMines: Int,
    val totalMines: Int,
    val time: Long,
    val received: Int,
    val turn: Int,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        gameResult = parcel.readString()?.let { GameResult.valueOf(it) } ?: GameResult.GameOver,
        showContinueButton = parcel.readInt() != 0,
        rightMines = parcel.readInt(),
        totalMines = parcel.readInt(),
        time = parcel.readLong(),
        received = parcel.readInt(),
        turn = parcel.readInt(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeString(gameResult.name)
        parcel.writeInt(if (showContinueButton) 1 else 0)
        parcel.writeInt(rightMines)
        parcel.writeInt(totalMines)
        parcel.writeLong(time)
        parcel.writeInt(received)
        parcel.writeInt(turn)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommonDialogState> {
        override fun createFromParcel(parcel: Parcel): CommonDialogState {
            return CommonDialogState(parcel)
        }

        override fun newArray(size: Int): Array<CommonDialogState?> {
            return arrayOfNulls(size)
        }
    }
}
