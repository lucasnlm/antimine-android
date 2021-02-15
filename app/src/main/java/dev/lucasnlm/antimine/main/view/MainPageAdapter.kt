package dev.lucasnlm.antimine.main.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPageAdapter(
    fragmentActivity: FragmentActivity,
    private val fragments: List<Fragment>,
) : FragmentStateAdapter(fragmentActivity) {

    init {
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
