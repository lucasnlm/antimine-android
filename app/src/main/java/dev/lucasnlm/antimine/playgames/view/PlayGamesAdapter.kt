package dev.lucasnlm.antimine.playgames.view

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import dev.lucasnlm.antimine.playgames.viewmodel.PlayGamesViewModel

class PlayGamesAdapter(
    private val playGamesViewModel: PlayGamesViewModel,
) : BaseAdapter() {
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?,
    ): View {
        val view =
            if (convertView == null) {
                PlayGamesButtonView(parent!!.context)
            } else {
                (convertView as PlayGamesButtonView)
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
