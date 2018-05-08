/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.purchase

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import com.android.billingclient.api.*
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.ui.main.MainActivity
import com.vandyke.sia.util.Analytics
import com.vandyke.sia.util.gone
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
                if (responseCode == BillingClient.BillingResponse.OK) {
                    val purchases = client.queryPurchases(BillingClient.SkuType.SUBS)
                    if (purchases.responseCode == BillingClient.BillingResponse.OK) {
                        val purchased = purchases.purchasesList?.any { it.sku == overall_sub_sku } == true
                        if (purchased) {
                            Prefs.requirePurchaseAt = 0
                            goToMainActivity()
                        } else if (pending) {
                            launchSubscriptionPurchase()
                        }
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

        if (Prefs.delayedPurchase && System.currentTimeMillis() > Prefs.requirePurchaseAt) {
            later.gone()
        } else {
            later.setOnClickListener {
                Prefs.delayedPurchase = true
                Prefs.requirePurchaseAt = System.currentTimeMillis() + 86400000 * 3 /* three days in the future */
                Analytics.subscribeLater()
                goToMainActivity()
            }
        }

        benefits_list.adapter = BenefitsAdapter()
        /* set benefits_list height to wrap_content if all items are fully visible, so that it'll center itself between the top and bottom */
        benefits_list.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if ((benefits_list.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == benefits_list.adapter.itemCount - 1) {
                    benefits_list.layoutParams = benefits_list.layoutParams.apply { height = ViewGroup.LayoutParams.WRAP_CONTENT }
                }
                benefits_list.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED
                || purchases?.any { it.sku == overall_sub_sku } == true
                || client.queryPurchases(BillingClient.SkuType.SUBS).purchasesList?.any { it.sku == overall_sub_sku } == true) {
            Prefs.requirePurchaseAt = 0
            Toast.makeText(this, "Thanks, enjoy! I look forward to bringing you updates.", Toast.LENGTH_LONG).show()
            Analytics.subscribe()
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