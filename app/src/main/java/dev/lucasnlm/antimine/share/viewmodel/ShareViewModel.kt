package dev.lucasnlm.antimine.share.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.share.ShareBuilder

class ShareViewModel @ViewModelInject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    suspend fun share(minefield: Minefield?, field: List<Area>?) {
        val result = if (minefield != null && field != null && field.count() != 0) {
            ShareBuilder(context).share(minefield, field)
        } else {
            false
        }

        if (!result) {
            Toast.makeText(context, context.getString(R.string.fail_to_share), Toast.LENGTH_SHORT).show()
        }
    }
}
