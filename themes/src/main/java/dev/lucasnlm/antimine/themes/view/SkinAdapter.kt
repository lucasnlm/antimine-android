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
        val layout = if (viewType == WITH_PADDING) R.layout.view_skin else R.layout.view_skin_full

        val view = LayoutInflater
            .from(parent.context)
            .inflate(layout, parent, false)

        return SkinViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appSkins.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (appSkins[position].showPadding) WITH_PADDING else WITHOUT_PADDING
    }

    override fun onBindViewHolder(holder: SkinViewHolder, position: Int) {
        val skin = appSkins[position]
        val tintColor = themeRepository.getTheme().palette.covered.toAndroidColor()

        holder.itemView.apply {
            val selected = (skin.id == themeViewModel.singleState().currentAppSkin.id)

            val backgroundColor = MaterialColors.getColorStateListOrNull(
                context,
                R.attr.backgroundColor,
            )

            val strokeColor = MaterialColors.getColorStateListOrNull(
                context,
                if (selected) R.attr.colorTertiary else R.attr.backgroundColor,
            )

            cardSkin.apply {
                backgroundTintList = backgroundColor
                setStrokeColor(strokeColor)
                setOnClickListener {
                    if (preferencesRepository.isPremiumEnabled()) {
                        onSelectSkin(skin)
                    } else {
                        onRequestPurchase()
                    }
                }
            }

            skinImage.apply {
                val floatAlpha = 0.45f
                alpha = if (selected) 1.0f else floatAlpha
                setImageResource(skin.imageRes)
                if (skin.canTint) {
                    setColorFilter(tintColor, PorterDuff.Mode.MULTIPLY)
                } else {
                    setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
                }
            }
        }
    }

    private companion object {
        const val WITH_PADDING = 0
        const val WITHOUT_PADDING = 1
    }
}

class SkinViewHolder(view: View) : RecyclerView.ViewHolder(view)
