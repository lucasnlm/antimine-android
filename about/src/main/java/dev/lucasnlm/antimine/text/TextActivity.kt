package dev.lucasnlm.antimine.text

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.RawRes
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.text.viewmodel.TextEvent
import dev.lucasnlm.antimine.text.viewmodel.TextViewModel
import dev.lucasnlm.antimine.ui.ThematicActivity
import kotlinx.android.synthetic.main.activity_text.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class TextActivity : ThematicActivity(R.layout.activity_text) {
    private val textViewModel by viewModel<TextViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras ?: Bundle()

        title = bundle.getString(TEXT_TITLE)

        lifecycleScope.launchWhenCreated {
            textViewModel.sendEvent(
                TextEvent.LoadText(
                    title = bundle.getString(TEXT_TITLE, ""),
                    rawFileRes = bundle.getInt(TEXT_PATH, -1)
                )
            )

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }

            textViewModel.observeState().collect {
                if (it.body == null) {
                    // The target resource doesn't exist.
                    finish()
                }

                textView.text = it.body
                progressBar.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val TEXT_TITLE = "third_title"
        private const val TEXT_PATH = "third_path"

        fun getIntent(context: Context, title: String, @RawRes textRes: Int): Intent {
            return Intent(context, TextActivity::class.java).apply {
                putExtra(TEXT_TITLE, title)
                putExtra(TEXT_PATH, textRes)
            }
        }
    }
}
