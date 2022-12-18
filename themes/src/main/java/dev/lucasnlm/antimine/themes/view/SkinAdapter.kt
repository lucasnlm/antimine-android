package dev.lucasnlm.antimine.themes.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.themes.R
import dev.lucasnlm.antimine.themes.viewmodel.ThemeViewModel
import dev.lucasnlm.antimine.ui.model.AppSkin
import kotlinx.android.synthetic.main.view_skin.view.*
import kotlinx.android.synthetic.main.view_theme.view.*

class SkinAdapter(
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

        holder.itemView.apply {
            val selected = (skin.id == themeViewModel.singleState().currentAppSkin.id)
            val alpha = if (selected) 127 else 255
            cardSkin.alpha = if (selected) 0.5f else 1.0f

            cardSkin.apply {
                setStrokeColor(
                    MaterialColors.getColorStateListOrNull(
                        context,
                        R.attr.backgroundColor,
                    ),
                )
                setOnClickListener {
                    if (preferencesRepository.isPremiumEnabled()) {
                        onSelectSkin(skin)
                    } else {
                        onRequestPurchase()
                    }
                }
            }
        }
    }
}

class SkinViewHolder(view: View) : RecyclerView.ViewHolder(view)
