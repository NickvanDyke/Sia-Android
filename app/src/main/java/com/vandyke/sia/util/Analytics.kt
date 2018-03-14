/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import java.math.BigDecimal

// TODO: inject instead of being a global singleton
object Analytics {
    private lateinit var fb: FirebaseAnalytics

    fun init(application: Application) {
        fb = FirebaseAnalytics.getInstance(application)
    }

    fun setCurrentScreen(fragment: Fragment) {
        fb.setCurrentScreen(fragment.activity!!, fragment.javaClass.simpleName, fragment.javaClass.simpleName)
    }

    fun createWallet(fromSeed: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("from_seed", fromSeed)
        fb.logEvent("create_wallet", bundle)
    }

    fun sendSiacoin(amount: BigDecimal) {
        val bundle = Bundle()
        bundle.putString("amount", amount.toPlainString())
        fb.logEvent("send_siacoin", bundle)
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

    fun unsupportedDataSource(uri: Uri) {
        val bundle = Bundle()
        bundle.putString("uri_path", uri.path)
        fb.logEvent("unsupported_data_source", bundle)
    }

    fun likingSiaForAndroid(liking: Boolean, giveFeedback: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("liking", liking)
        bundle.putBoolean("give_feedback_or_review", giveFeedback)
        fb.logEvent("liking_sia_for_android", bundle)
    }
}