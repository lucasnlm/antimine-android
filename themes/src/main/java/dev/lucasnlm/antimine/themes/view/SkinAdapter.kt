package dev.lucasnlm.antimine.themes.view

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.themes.R
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import kotlinx.android.synthetic.main.view_skin.view.*
import kotlinx.android.synthetic.main.view_theme.view.*

class SkinAdapter(
    private val themeRepository: IThemeRepository,
    private val themeViewModel: ThemeViewModel,
    private val preferencesRepository: IPreferencesRepository,
    private val onSelectSkin: (AppSkin) -> Unit,
    private val onRequestPurchase: () -> Unit,
) : RecyclerView.Adapter<SkinViewHolder>() {
    private val appSkins: List<AppSkin> = themeViewModel.singleState().appSkins

    init {
        setHasStableIds(true)
        stateRestorationPolicy = StateRestorationPolicy.ALLOW
    }

    override fun getItemId(position: Int): Long = appSkins[position].id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkinViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_skin, parent, false)

        return SkinViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appSkins.size
    }

    override fun onBindViewHolder(holder: SkinViewHolder, position: Int) {
        val skin = appSkins[position]
        val tintColor = themeRepository.getTheme().palette.covered.toAndroidColor()

        holder.itemView.apply {
            val backgroundColor = MaterialColors.getColorStateListOrNull(
                context,
                R.attr.backgroundColor,
            )

            val selected = (skin.id == themeViewModel.singleState().currentAppSkin.id)

            cardSkin.apply {
                alpha = if (selected) 0.5f else 1.0f
                backgroundTintList = backgroundColor
                setStrokeColor(backgroundColor)
                setOnClickListener {
                    if (preferencesRepository.isPremiumEnabled()) {
                        onSelectSkin(skin)
                    } else {
                        onRequestPurchase()
                    }
                }
            }

            skinImage.apply {
                setImageResource(skin.imageRes)
                if (skin.canTint) {
                    setColorFilter(tintColor, PorterDuff.Mode.MULTIPLY)
                } else {
                    setColorFilter(Color.WHITE)
                }
            }
        }
    }
}

class SkinViewHolder(view: View) : RecyclerView.ViewHolder(view)
