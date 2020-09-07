package dev.lucasnlm.antimine.text.viewmodel

import android.content.Context
import android.content.res.Resources
import androidx.annotation.RawRes
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.text.models.TextState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class TextViewModel(
    private val context: Context,
) : IntentViewModel<TextEvent, TextState>() {

    private suspend fun loadText(@RawRes rawFile: Int): String? {
        return try {
            withContext(Dispatchers.IO) {
                context.resources.openRawResource(rawFile)
                    .bufferedReader()
                    .readLines()
                    .joinToString("\n")
            }
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    override fun initialState(): TextState = TextState("", "")

    override suspend fun mapEventToState(event: TextEvent) = flow {
        if (event is TextEvent.LoadText) {
            emit(TextState(event.title, loadText(event.rawFileRes)))
        }
    }
}
