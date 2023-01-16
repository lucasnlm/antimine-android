package dev.lucasnlm.antimine.licenses

import android.os.Bundle
import androidx.fragment.app.commit
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.licenses.views.LicensesFragment
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import kotlinx.android.synthetic.main.activity_container.*

class LicenseActivity : ThemedActivity(R.layout.activity_container) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindToolbar(toolbar)
        toolbar.setTitle(R.string.licenses)

        supportFragmentManager.commit(allowStateLoss = true) {
            replace(R.id.content, LicensesFragment())
        }
    }
}
