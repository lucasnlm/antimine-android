package dev.lucasnlm.antimine.themes.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.themes.R
import dev.lucasnlm.antimine.themes.viewmodel.ThemeEvent
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.ext.toInvertedAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.external.IAdsManager
import kotlinx.android.synthetic.main.view_theme.view.*

class ThemeAdapter(
    private val activity: Activity,
    private val themeViewModel: ThemeViewModel,
    private val preferencesRepository: IPreferencesRepository,
    private val adsManager: IAdsManager,
) : RecyclerView.Adapter<ThemeViewHolder>() {

    private val themes: List<AppTheme> = themeViewModel.singleState().themes
    private val unlockedThemes = preferencesRepository.getUnlockedThemes()

    init {
        setHasStableIds(true)
        stateRestorationPolicy = StateRestorationPolicy.ALLOW
    }

    override fun getItemId(position: Int): Long = themes[position].id

    override fun getItemCount(): Int = themes.size

    override fun getItemViewType(position: Int): Int = themes[position].id.toInt()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_theme, parent, false)

        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themes[position]

        holder.itemView.apply {
            val selected = (theme.id == themeViewModel.singleState().current.id)
            val alpha = if (selected) 127 else 255
            cardTheme.alpha = if (selected) 0.5f else 1.0f

            covered.setBackgroundColor(theme.palette.covered.toAndroidColor(alpha))
            uncovered.setBackgroundColor(theme.palette.background.toAndroidColor(alpha))

            if (position == 0) {
                label.apply {
                    text = label.context.getString(R.string.system)
                    setTextColor(theme.palette.background.toInvertedAndroidColor(200))
                    setBackgroundResource(android.R.color.transparent)
                    setCompoundDrawables(null, null, null, null)
                    visibility = View.VISIBLE
                }
            } else if (
                theme.isPaid &&
                !preferencesRepository.isPremiumEnabled() &&
                !unlockedThemes.contains(theme.id.toInt())
            ) {
                label.apply {
                    text = label.context.getString(R.string.unlock)
                    setTextColor(theme.palette.background.toInvertedAndroidColor(200))
                    setBackgroundResource(android.R.color.transparent)
                    compoundDrawables.forEach {
                        it?.setTint(theme.palette.background.toInvertedAndroidColor(200))
                    }
                    visibility = View.VISIBLE
                }
            } else {
                label.apply {
                    setCompoundDrawables(null, null, null, null)
                    visibility = View.GONE
                }
            }

            cardTheme.apply {
                strokeColor = theme.palette.covered.toAndroidColor(alpha)
                setOnClickListener {
                    unlockThemeIfNeeded(theme)
                }
            }
        }
    }

    private fun unlockThemeIfNeeded(theme: AppTheme) {
        if (!theme.isPaid ||
            preferencesRepository.isPremiumEnabled() ||
            unlockedThemes.contains(theme.id.toInt())
        ) {
            themeViewModel.sendEvent(ThemeEvent.ChangeTheme(theme))
        } else {
            adsManager.showRewardedAd(
                activity,
                skipIfFrequent = false,
                onRewarded = {
                    themeViewModel.sendEvent(ThemeEvent.ChangeTheme(theme))
                },
                onFail = {
                    themeViewModel.sendEvent(ThemeEvent.ChangeTheme(theme))
                }
            )
        }
    }
}

class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view)
