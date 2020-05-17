package dev.lucasnlm.antimine.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import dev.lucasnlm.antimine.R

import kotlinx.android.synthetic.main.activity_text.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra(Constants.TEXT_TITLE)
        setContentView(R.layout.activity_text)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }

            withContext(Dispatchers.IO) {
                val rawPath = intent.getIntExtra(Constants.TEXT_PATH, -1)

                if (rawPath > 0) {
                    val result = resources.openRawResource(rawPath)
                        .bufferedReader()
                        .readLines()
                        .joinToString("\n")

                    withContext(Dispatchers.Main) {
                        textView.text = result
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }
}
