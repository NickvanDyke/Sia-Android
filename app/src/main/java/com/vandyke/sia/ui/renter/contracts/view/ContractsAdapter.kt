package com.vandyke.sia.ui.renter.contracts.view

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.ContractData

class ContractsAdapter : ListAdapter<ContractData, ContractHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractHolder {
        return ContractHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_contract, parent, false))
    }

    override fun onBindViewHolder(holder: ContractHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ContractData>() {
            override fun areItemsTheSame(oldItem: ContractData, newItem: ContractData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ContractData, newItem: ContractData): Boolean {
                return oldItem.goodforrenew == newItem.goodforrenew
                        && oldItem.goodforupload == newItem.goodforupload
                        && oldItem.netaddress == newItem.netaddress
                        && oldItem.endheight == newItem.endheight
            }
        }
    }
}