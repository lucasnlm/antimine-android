package dev.lucasnlm.antimine.text

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RawRes
import androidx.lifecycle.Observer
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.text.viewmodel.TextViewModel

import kotlinx.android.synthetic.main.activity_text.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TextActivity : AppCompatActivity(R.layout.activity_text) {
    private val viewModel: TextViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra(TEXT_TITLE)

        viewModel.text.observe(
            this,
            Observer { loadedText ->
                textView.text = loadedText
                progressBar.visibility = View.GONE
            }
        )

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }

            withContext(Dispatchers.IO) {
                viewModel.loadText(intent.getIntExtra(TEXT_PATH, -1))
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
