package dev.lucasnlm.antimine.licenses

import android.os.Bundle
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import dev.lucasnlm.antimine.about.R
import dev.lucasnlm.antimine.licenses.views.LicensesFragment
import dev.lucasnlm.antimine.ui.ext.ThematicActivity

class LicenseActivity : ThematicActivity(R.layout.activity_container) {
    private val toolbar: MaterialToolbar by lazy {
        findViewById(R.id.toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindToolbar(toolbar)
        toolbar.setTitle(R.string.licenses)

        supportFragmentManager.commit(allowStateLoss = true) {
            replace(R.id.content, LicensesFragment())
        }
    }
}
