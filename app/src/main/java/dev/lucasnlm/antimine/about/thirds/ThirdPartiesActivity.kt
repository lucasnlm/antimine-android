package dev.lucasnlm.antimine.about.thirds

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.thirds.data.ThirdParty
import dev.lucasnlm.antimine.about.thirds.view.ThirdPartyAdapter
import kotlinx.android.synthetic.main.activity_third_party.*

class ThirdPartiesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third_party)
        bindToolbar()

        licenses.apply {
            setHasFixedSize(true)
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
            layoutManager = LinearLayoutManager(context)
        }

        bindLicenses()
    }

    private fun bindLicenses() {
        licenses.adapter = ThirdPartyAdapter(
            listOf(
                ThirdParty(
                    "Android SDK License",
                    R.raw.android_sdk
                ),
                ThirdParty(
                    "Material Design Icons",
                    R.raw.apache2
                ),
                ThirdParty(
                    "Dagger",
                    R.raw.apache2
                ),
                ThirdParty(
                    "Moshi",
                    R.raw.apache2
                ),
                ThirdParty(
                    "Mockito",
                    R.raw.mockito
                ),
                ThirdParty(
                    "Sounds",
                    R.raw.sounds
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
