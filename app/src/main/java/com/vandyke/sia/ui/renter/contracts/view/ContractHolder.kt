package com.vandyke.sia.ui.renter.contracts.view

import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.ContractData
import com.vandyke.sia.util.format
import com.vandyke.sia.util.getAttrColor
import com.vandyke.sia.util.getColorRes
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_contract.*

class ContractHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), LayoutContainer {
    override val containerView: View? = itemView

    fun bind(contract: ContractData) {
        contract_hostaddress.text = contract.netaddress
        contract_endheight.text = contract.endheight.format()

        contract_hostaddress.setTextColor(when {
            contract.goodforupload -> itemView.context.getAttrColor(R.attr.colorPrimaryDark)
            else -> itemView.context.getColorRes(R.color.negative)
        })

        contract_endheight.setTextColor(when {
            contract.goodforrenew -> itemView.context.getAttrColor(R.attr.colorPrimaryDark)
            else -> itemView.context.getColorRes(R.color.negative)
        })
    }
}