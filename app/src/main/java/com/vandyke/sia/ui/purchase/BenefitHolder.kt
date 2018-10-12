package com.vandyke.sia.ui.purchase

import android.view.View
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_benefit.*

class BenefitHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), LayoutContainer {
    override val containerView: View = itemView

    fun bind(benefit: Benefit) {
        benefit_image.setImageResource(benefit.imageRes)
        benefit_desc.text = benefit.description
    }
}