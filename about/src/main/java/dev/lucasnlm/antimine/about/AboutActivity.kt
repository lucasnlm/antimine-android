package dev.lucasnlm.antimine.about

import android.os.Bundle
import androidx.fragment.app.commit
import dev.lucasnlm.antimine.about.views.AboutInfoFragment
import dev.lucasnlm.antimine.ui.ext.ThematicActivity
import kotlinx.android.synthetic.main.activity_container.*

class AboutActivity : ThematicActivity(R.layout.activity_container) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindToolbar(toolbar)

        supportFragmentManager.commit(allowStateLoss = true) {
            replace(R.id.content, AboutInfoFragment())
        }
    }
}
