package dev.lucasnlm.antimine.about.translators

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.translators.model.TranslationInfo
import dev.lucasnlm.antimine.about.translators.view.TranslatorsAdapter
import kotlinx.android.synthetic.main.activity_translators.*

class TranslatorsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translators)
        bindToolbar()

        translators.apply {
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
            layoutManager = LinearLayoutManager(context)
        }

        bindTranslationInfo()
    }

    private fun bindTranslationInfo() {
        translators.adapter = TranslatorsAdapter(
            listOf(
                TranslationInfo(
                    "Brazilian Portuguese",
                    sequenceOf("Lucas Lima")
                ),
                TranslationInfo(
                    "Czech",
                    sequenceOf("Lukas Novotny")
                ),
                TranslationInfo(
                    "Turkish",
                    sequenceOf("Fatih Fırıncı")
                )
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun bindToolbar() {
        supportActionBar?.apply {
            setTitle(R.string.licenses)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }
}
