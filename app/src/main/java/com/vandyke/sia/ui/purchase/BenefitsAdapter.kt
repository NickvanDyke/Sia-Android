package com.vandyke.sia.ui.purchase

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.sia.R
import com.vandyke.sia.util.gone
import com.vandyke.sia.util.visible

class BenefitsAdapter : RecyclerView.Adapter<BenefitHolder>() {
    private val benefits = listOf(
            Benefit(R.drawable.ic_account_balance_wallet_white, "Manage your private Siacoin wallet"),
            Benefit(R.drawable.ic_cloud_outline_black, "Store your data reliably on the Sia network"),
            Benefit(R.drawable.ic_money_white, "Pay less than traditional cloud storage"),
            Benefit(R.drawable.ic_lock_outline_white, "Enjoy complete control and privacy over your data"),
            Benefit(R.drawable.ic_people_outline_black, "Participate in a blockchain marketplace"),
            Benefit(R.drawable.ic_code_black, "Support further development"),
            Benefit(R.drawable.ic_free_breakfast_black, "All for less than your morning coffee!"),
            Benefit(R.drawable.ic_sentiment_satisfied_white, "7 day free trial"),
            Benefit(R.drawable.ic_cancel_black, "Easily cancel at any time from settings")
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BenefitHolder {
        return BenefitHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_benefit, parent, false))
    }

    override fun onBindViewHolder(holder: BenefitHolder, position: Int) {
        holder.bind(benefits[position])
        if (position == benefits.size - 1) {
            holder.itemView.findViewById<View>(R.id.divider).gone()
        } else {
            holder.itemView.findViewById<View>(R.id.divider).visible()
        }
    }

    override fun getItemCount(): Int = benefits.size
}