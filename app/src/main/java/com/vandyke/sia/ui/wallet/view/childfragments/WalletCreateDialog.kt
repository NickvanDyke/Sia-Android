/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.vandyke.sia.R
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.SnackbarUtil
import kotlinx.android.synthetic.main.fragment_wallet_create.*

class WalletCreateDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_create

    override fun create(view: View, savedInstanceState: Bundle?) {
        walletCreateSeed.visibility = View.GONE
        walletCreateFromSeed.setOnClickListener {
            if (walletCreateFromSeed.isChecked)
                walletCreateSeed.visibility = View.VISIBLE
            else
                walletCreateSeed.visibility = View.GONE
        }

        walletCreateForceWarning.visibility = View.GONE
        walletCreateForce.setOnClickListener {
            if (walletCreateForce.isChecked)
                walletCreateForceWarning.visibility = View.VISIBLE
            else
                walletCreateForceWarning.visibility = View.GONE
        }

        walletCreateButton.setOnClickListener {
            val password = newPasswordCreate.text.toString()
            if (password != confirmNewPasswordCreate.text.toString()) {
                SnackbarUtil.showSnackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT)
                return@setOnClickListener
            }
            val force = walletCreateForce.isChecked
            if (!walletCreateFromSeed.isChecked) {
                viewModel.create(password, force)
            } else {
                viewModel.create(password, force, walletCreateSeed.text.toString())
            }
        }
    }


    companion object {
        fun showSeed(seed: String, context: Context) {
            val msg = "Below is your wallet seed. Your wallet's addresses are generated using this seed. Therefore, any coins you " +
                    "send to this wallet and its addresses will \"belong\" to this seed. It's what you will need" +
                    " in order to recover your coins if something happens to your wallet, or to load your wallet on another device. " +
                    "Record it elsewhere, and keep it safe."
            AlertDialog.Builder(context)
                    .setTitle("Wallet seed")
                    .setMessage("$msg\n\n$seed")
                    .setPositiveButton("Copy seed", { _, _ ->
                        GenUtil.copyToClipboard(context, seed)
                        Toast.makeText(context, "Copied seed. Please store it safely.", Toast.LENGTH_LONG).show()
                    })
                    .show()
        }
    }
}
