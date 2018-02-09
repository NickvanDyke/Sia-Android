/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.logging

import android.app.Application
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import java.math.BigDecimal

object Analytics {
    private lateinit var fb: FirebaseAnalytics

    fun init(application: Application) {
        fb = FirebaseAnalytics.getInstance(application)
    }

    fun createWallet(fromSeed: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("from_seed", fromSeed)
        fb.logEvent("create_wallet", bundle)
    }

    fun sendSiacoin(amount: BigDecimal) {
        val bundle = Bundle()
        bundle.putString("amount", amount.toPlainString())
        fb.logEvent("send_siacoin", null)
    }

    fun viewAddress() {
        fb.logEvent("view_address", null)
    }

    fun sweepSeed() {
        fb.logEvent("sweep_seed", null)
    }

    fun subscribe() {
        fb.logEvent("subscribe", null)
    }

    fun subscribeLater() {
        fb.logEvent("subscribe_later", null)
    }
}