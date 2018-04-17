/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.vandyke.sia.R
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.Intents
import com.vandyke.sia.util.gone
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class HelpFragment : BaseFragment() {
    override val title: String = "Help"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val learnAboutSia = Element("Learn about the Sia project", R.drawable.sia_new_circle_logo_transparent_white)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://siasetup.info/learn/sia")
                }

        val buyingSiacoin = Element("How to buy Siacoin", R.drawable.sia_new_circle_logo_transparent_white)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://siasetup.info/guides/buying_siacoins")
                }

        val learnAboutRenting = Element("Learn about renting on Sia", R.drawable.ic_cloud_black)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://siasetup.info/learn/renting")
                }

        val rentingFaq = Element("Renting FAQ", R.drawable.ic_cloud_black)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://siasetup.info/faq/renting")
                }

        val rentingGuide = Element("Renting guide", R.drawable.ic_cloud_black)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://siasetup.info/faq/renting")
                }

        val hostScoring = Element("Learn how hosts are scored", R.drawable.ic_cloud_black)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://siasetup.info/learn/hosting#host_scoring")
                }

        val siaSupport = Element("Sia knowledge base", R.drawable.ic_info_outline_black)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://support.sia.tech/")
                }

        /* creating our own email element because the default one opens a chooser. This one goes straight to email */
        val email = Element("Email me for help", mehdi.sakout.aboutpage.R.drawable.about_icon_email)
                .setIntent(Intents.emailMe)

        val page = AboutPage(context)
                .setDescription("SiaSetup.info has a lot of helpful info regarding Sia. You'll find links to" +
                        " its most relevant articles below. Note that they're meant to be used with Sia-UI on your" +
                        " computer, but are still very applicable to the Android app.")
                .addItem(learnAboutSia)
                .addItem(buyingSiacoin)
                .addItem(hostScoring)
                .addItem(learnAboutRenting)
                .addItem(rentingFaq)
                .addItem(rentingGuide)
                .addItem(siaSupport)
                .addItem(email)
                .create()

        val root = page.rootView as ScrollView
        root.isVerticalScrollBarEnabled = false
        val rootLinear = root.getChildAt(0) as LinearLayout
        val topLinear = rootLinear.getChildAt(0) as LinearLayout
        /* hide the image at the top */
        topLinear.getChildAt(0).gone()

        return page
    }
}