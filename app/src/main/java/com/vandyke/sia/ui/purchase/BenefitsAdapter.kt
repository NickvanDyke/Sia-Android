package com.vandyke.sia.ui.purchase

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.vandyke.sia.R

class BenefitsAdapter : RecyclerView.Adapter<BenefitHolder>() {
    private val benefits = listOf(
            Benefit(R.drawable.ic_account_balance_wallet_white, "Send and receive Siacoin with your private wallet"),
            Benefit(R.drawable.ic_cloud_black, "Store your files on the Sia network"),
            Benefit(R.drawable.ic_code_black, "Support development")
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BenefitHolder {
        return BenefitHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_benefit, parent, false))
    }

    override fun onBindViewHolder(holder: BenefitHolder, position: Int) {
        holder.bind(benefits[position])
    }

    override fun getItemCount(): Int = benefits.size
}