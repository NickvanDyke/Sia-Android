package com.vandyke.sia.ui.purchase

import android.view.LayoutInflater
import android.view.ViewGroup
import com.vandyke.sia.R

class BenefitsAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<BenefitHolder>() {
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
    }

    override fun getItemCount(): Int = benefits.size
}