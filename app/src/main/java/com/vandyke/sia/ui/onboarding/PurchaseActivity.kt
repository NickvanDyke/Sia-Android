/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.android.billingclient.api.*
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_purchase.*

class PurchaseActivity : AppCompatActivity(), PurchasesUpdatedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)

        subscribe.setOnClickListener {
            val client = BillingClient.newBuilder(this).setListener(this).build()

            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(responseCode: Int) {
                    val params = BillingFlowParams.newBuilder()
                            .setType(BillingClient.SkuType.INAPP)
                            .setSku(overall_sub_sku)
                            .build()
                    if (client.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS) == BillingClient.BillingResponse.OK) {
                        client.launchBillingFlow(this@PurchaseActivity, params)
                    } else {
                        AlertDialog.Builder(this@PurchaseActivity)
                                .setTitle("Unsupported")
                                .setMessage("Your device doesn't support subscriptions, sorry.")
                                .show()
                    }
                }

                override fun onBillingServiceDisconnected() {

                }
            })
        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED
                || purchases?.find { it.sku == overall_sub_sku } != null) {
            Prefs.cachedPurchased = true
            finish()
            startActivity(Intent(this@PurchaseActivity, MainActivity::class.java))
        }
    }

    companion object {
        const val overall_sub_sku = "overall_app_access"
    }
}