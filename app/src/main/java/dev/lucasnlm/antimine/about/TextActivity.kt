package dev.lucasnlm.antimine.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import dev.lucasnlm.antimine.R

import kotlinx.android.synthetic.main.activity_text.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class TextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)
        bindToolbar()

        progressBar.isIndeterminate = true

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }

            withContext(Dispatchers.IO) {
                val rawPath = intent.getIntExtra(Constants.TEXT_PATH, -1)
                var result: String? = null

                if (rawPath > 0) {
                    resources.openRawResource(rawPath).use { inputStream ->

                        result = readTextFile(inputStream)
                    }
                }

                withContext(Dispatchers.Main) {
                    textView.text = result
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun readTextFile(inputStream: InputStream): String {
        var result = ""
        ByteArrayOutputStream().use { outputStream ->
            val buf = ByteArray(4096)
            var len: Int
            try {
                while (true) {
                    len = inputStream.read(buf)
                    if (len != -1) {
                        outputStream.write(buf, 0, len)
                    } else {
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Fail to read file.", e)
            }
            result = outputStream.toString()
        }
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var handled = false

        if (item.itemId == android.R.id.home) {
            onBackPressed()
            handled = true
        }

        return handled || super.onOptionsItemSelected(item)
    }

    private fun bindToolbar() {
        supportActionBar?.apply {
            title = intent.getStringExtra(Constants.TEXT_TITLE)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    companion object {
        private const val TAG = "TextActivity"
    }
}
