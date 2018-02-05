/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.android.billingclient.api.*
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_purchase.*

class PurchaseActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var client: BillingClient
    private var pending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)

        client = BillingClient.newBuilder(this).setListener(this).build()
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(responseCode: Int) {
                val purchases = client.queryPurchases(BillingClient.SkuType.SUBS)
                if (purchases.responseCode == BillingClient.BillingResponse.OK) {
                    val purchased = purchases.purchasesList?.find { it.sku == overall_sub_sku } != null
                    Prefs.cachedPurchased = purchased
                    if (purchased) {
                        goToMainActivity()
                    } else if (pending) {
                        launchSubscriptionPurchase()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
            }
        })

        subscribe.setOnClickListener {
            if (client.isReady) {
                launchSubscriptionPurchase()
            } else {
                pending = true
                Toast.makeText(this, "Google Play Billing isn't connected", Toast.LENGTH_LONG).show()
            }
        }

        if (Prefs.requirePurchaseAt != 0L && System.currentTimeMillis() > Prefs.requirePurchaseAt) {
            later.visibility = View.GONE
        } else {
            later.setOnClickListener {
                Prefs.requirePurchaseAt = System.currentTimeMillis() + 86400000 /* one day in the future */
                Toast.makeText(this, "Delayed for one day", Toast.LENGTH_LONG).show()
                goToMainActivity()
            }
        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED
                || purchases?.find { it.sku == overall_sub_sku } != null
                || client.queryPurchases(BillingClient.SkuType.SUBS).purchasesList.find { it.sku == overall_sub_sku } != null) {
            Prefs.cachedPurchased = true
            goToMainActivity()
        }
    }

    private fun launchSubscriptionPurchase() {
        pending = false
        if (client.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS) == BillingClient.BillingResponse.OK) {
            val params = BillingFlowParams.newBuilder()
                    .setType(BillingClient.SkuType.INAPP)
                    .setSku(overall_sub_sku)
                    .build()
            client.launchBillingFlow(this, params)
        } else {
            AlertDialog.Builder(this)
                    .setTitle("Unsupported")
                    .setMessage("Your device doesn't support subscriptions, sorry.")
                    .setPositiveButton("Close", null)
                    .show()
        }
    }

    private fun goToMainActivity() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (client.isReady)
            client.endConnection()
    }

    companion object {
        const val overall_sub_sku = "overall_app_access"
    }
}