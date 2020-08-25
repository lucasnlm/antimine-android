package dev.lucasnlm.antimine.playgames

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.playgames.viewmodel.PlayGamesEvent
import dev.lucasnlm.antimine.playgames.viewmodel.PlayGamesViewModel
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayGamesDialogFragment : AppCompatDialogFragment() {
    private val playGamesViewModel by viewModel<PlayGamesViewModel>()
    private val adapter by lazy { PlayGamesAdapter(playGamesViewModel) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
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
        private val playGamesViewModel: PlayGamesViewModel
    ) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = if (convertView == null) {
                PlayGamesButton(parent!!.context)
            } else {
                (convertView as PlayGamesButton)
            }

            val item = playGamesViewModel.playGamesItems[position]

            return view.apply {
                text.text = view.context.getString(item.stringRes)
                icon.setImageResource(item.iconRes)

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
        inflate(context, R.layout.view_play_games_button, this)

        icon = findViewById(R.id.icon)
        text = findViewById(R.id.text)
    }

    val icon: AppCompatImageView
    val text: TextView
}
