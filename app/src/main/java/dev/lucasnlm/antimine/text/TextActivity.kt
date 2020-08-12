package dev.lucasnlm.antimine.text

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RawRes
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.ThematicActivity
import dev.lucasnlm.antimine.text.viewmodel.TextEvent
import dev.lucasnlm.antimine.text.viewmodel.TextViewModel
import kotlinx.android.synthetic.main.activity_text.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class TextActivity : ThematicActivity(R.layout.activity_text) {
    private val viewModel: TextViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras ?: Bundle()

        title = bundle.getString(TEXT_TITLE)

        lifecycleScope.launchWhenCreated {
            viewModel.sendEvent(
                TextEvent.LoadText(
                    title = bundle.getString(TEXT_TITLE, ""),
                    rawFileRes = bundle.getInt(TEXT_PATH, -1)
                )
            )

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }

            viewModel.observeState().collect {
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
