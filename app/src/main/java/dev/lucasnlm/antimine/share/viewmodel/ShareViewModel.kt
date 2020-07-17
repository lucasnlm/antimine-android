package dev.lucasnlm.antimine.share.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.share.ShareBuilder
import dev.lucasnlm.antimine.common.level.models.Area
import dev.lucasnlm.antimine.common.level.models.Minefield

class ShareViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

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
