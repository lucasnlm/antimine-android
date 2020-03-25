package dev.lucasnlm.antimine.about.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import dev.lucasnlm.antimine.BuildConfig
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.about.models.AboutEvent
import dev.lucasnlm.antimine.about.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.fragment_about_info.*

class AboutInfoFragment : Fragment() {
    private var aboutViewModel: AboutViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.run {
            aboutViewModel = ViewModelProviders.of(this).get(AboutViewModel::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_about_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        version.text = getString(R.string.version_s, BuildConfig.VERSION_NAME)

        thirdsParties.setOnClickListener {
            aboutViewModel?.eventObserver?.postValue(AboutEvent.ThirdPartyLicenses)
        }

        sourceCode.setOnClickListener {
            aboutViewModel?.eventObserver?.postValue(AboutEvent.SourceCode)
        }

        translation.setOnClickListener {
            aboutViewModel?.eventObserver?.postValue(AboutEvent.Translators)
        }
    }
}
