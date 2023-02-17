package dev.lucasnlm.antimine.donation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.os.LocaleListCompat
import dev.lucasnlm.antimine.donation.databinding.ActivityDonationBinding
import dev.lucasnlm.antimine.ui.ext.ThemedActivity

class DonationActivity : ThemedActivity() {
    private lateinit var binding: ActivityDonationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindToolbar(binding.toolbar)

        binding.paypalButton.setOnClickListener { openPayPal() }
        binding.githubButton.setOnClickListener { openGithub() }

        if (hasBrazilLocale()) {
            binding.pixButton.setOnClickListener { copyPixKey() }
        } else {
            binding.pixButton.visibility = View.GONE
        }
    }

    private fun openLink(link: String) {
        val context = application.applicationContext
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context.applicationContext, R.string.unknown_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPayPal() {
        val paypalLink = "https://www.paypal.com/donate?hosted_button_id=49XX9XDNUV4SW"
        openLink(paypalLink)
    }

    private fun openGithub() {
        val paypalLink = "https://github.com/sponsors/lucasnlm"
        openLink(paypalLink)
    }

    private fun hasBrazilLocale(): Boolean {
        return LocaleListCompat.getAdjustedDefault().run {
            var result = false
            for (i in 0..size()) {
                get(i)?.let {
                    if (it.country == "BR") {
                        result = true
                    }
                }
            }
            result
        }
    }

    private fun copyPixKey() {
        val pixKey = "1e91c4c3-e1b4-4aeb-a964-f2e07334b7dd"
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("pix", pixKey)
        clipboard.setPrimaryClip(clip)

        // Pix is only available on Brazil,
        // so there's no need to translate this.
        val text = "Chave Pix copiada"
        val title = "Enviar usando..."

        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()

        ShareCompat.IntentBuilder(this)
            .setText(pixKey)
            .setType("text/plain")
            .setChooserTitle(title)
            .startChooser()
    }
}
