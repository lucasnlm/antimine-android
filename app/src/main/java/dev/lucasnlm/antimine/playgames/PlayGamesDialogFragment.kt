package dev.lucasnlm.antimine.playgames

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.databinding.ViewPlayGamesButtonBinding
import dev.lucasnlm.antimine.playgames.viewmodel.PlayGamesEvent
import dev.lucasnlm.antimine.playgames.viewmodel.PlayGamesViewModel
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayGamesDialogFragment : AppCompatDialogFragment() {
    private val playGamesViewModel by viewModel<PlayGamesViewModel>()
    private val adapter by lazy { PlayGamesAdapter(playGamesViewModel) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.google_play_games)
            setAdapter(adapter, null)
            setPositiveButton(R.string.ok, null)
        }.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenCreated {
            playGamesViewModel.observeEvent().collect {
                when (it) {
                    is PlayGamesEvent.OpenAchievements -> {
                        activity?.let { activity ->
                            playGamesViewModel.openAchievements(activity)
                        }
                    }
                    is PlayGamesEvent.OpenLeaderboards -> {
                        activity?.let { activity ->
                            playGamesViewModel.openLeaderboards(activity)
                        }
                    }
                }
            }
        }
    }

    private class PlayGamesAdapter(
        private val playGamesViewModel: PlayGamesViewModel,
    ) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = if (convertView == null) {
                PlayGamesButton(parent!!.context)
            } else {
                (convertView as PlayGamesButton)
            }

            val item = playGamesViewModel.playGamesItems[position]

            return view.apply {
                binding.text.text = view.context.getString(item.stringRes)
                binding.icon.setImageResource(item.iconRes)

                setOnClickListener {
                    playGamesViewModel.sendEvent(item.triggerEvent)
                }
            }
        }

        override fun hasStableIds(): Boolean = true

        override fun getItem(position: Int): Any = playGamesViewModel.playGamesItems[position]

        override fun getItemId(position: Int): Long = playGamesViewModel.playGamesItems[position].id.toLong()

        override fun getCount(): Int = playGamesViewModel.playGamesItems.count()
    }

    companion object {
        val TAG = PlayGamesDialogFragment::class.simpleName
    }
}

class PlayGamesButton : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = ViewPlayGamesButtonBinding.inflate(layoutInflater, this, false)
    }

    val binding: ViewPlayGamesButtonBinding
}
