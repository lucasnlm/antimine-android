package dev.lucasnlm.antimine.themes.view

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.core.dpToPx
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.themes.R
import dev.lucasnlm.antimine.themes.databinding.ViewSkinBinding
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.repository.IThemeRepository

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
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ViewSkinBinding.inflate(layoutInflater, parent, false)
        return SkinViewHolder(binding)
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
        val context = holder.itemView.context

        holder.itemView.apply {
            isSoundEffectsEnabled = false

            val selected = (skin.id == themeViewModel.singleState().currentAppSkin.id)

            val backgroundColor = MaterialColors.getColorStateListOrNull(
                context,
                R.attr.backgroundColor,
            )

            val strokeColor = MaterialColors.getColorStateListOrNull(
                context,
                if (selected) R.attr.colorTertiary else R.attr.backgroundColor,
            )

            holder.binding.cardSkin.apply {
                backgroundTintList = backgroundColor
                setStrokeColor(strokeColor)
                isSoundEffectsEnabled = false
                setOnClickListener {
                    if (preferencesRepository.isPremiumEnabled()) {
                        onSelectSkin(skin)
                    } else {
                        onRequestPurchase()
                    }
                }
            }

            holder.binding.skinImage.apply {
                val floatAlpha = 0.45f
                val paddingValue = context.dpToPx(8)
                alpha = if (selected) 1.0f else floatAlpha
                setImageResource(skin.imageRes)
                isSoundEffectsEnabled = false
                if (skin.canTint) {
                    setColorFilter(tintColor, PorterDuff.Mode.MULTIPLY)
                } else {
                    setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
                }
                if (!skin.hasPadding) {
                    setPadding(0, 0, 0, 0)
                } else {
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                }
            }
        }
    }

    private companion object {
        const val WITH_PADDING = 0
        const val WITHOUT_PADDING = 1
    }
}

class SkinViewHolder(
    val binding: ViewSkinBinding,
) : RecyclerView.ViewHolder(binding.root)
