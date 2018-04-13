/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.about

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.vandyke.sia.BuildConfig
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.Intents
import com.vandyke.sia.util.gone
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutFragment : BaseFragment() {
    override val title: String = "About"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val appVersion = Element("Version ${BuildConfig.VERSION_NAME}", R.drawable.ic_format_list_bulleted_black)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://github.com/NickvanDyke/Sia-Android/releases")
                }

        val siaVersion = Element("Version ${Prefs.siaVersion}", R.drawable.ic_format_list_bulleted_black)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://github.com/NebulousLabs/Sia/releases")
                }

        val appGithub = Element("GitHub", mehdi.sakout.aboutpage.R.drawable.about_icon_github)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://github.com/NickvanDyke/Sia-Android")
                }

        val siaGithub = Element("GitHub", mehdi.sakout.aboutpage.R.drawable.about_icon_github)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://github.com/NebulousLabs/Sia")
                }

        val siaHomepage = Element("Website", R.drawable.sia_new_circle_logo_transparent)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://sia.tech")
                }

        val shareIntent = Intent.createChooser(
                Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.vandyke.sia"), "Share Sia")
        val share = Element("Share", R.drawable.ic_share_black)
                .setIntent(shareIntent)

        // maybe I shouldn't have these links here? I don't want people going there for support, since this isn't an official Sia product.
        // It's nice to have them other than that though. I'll see what ends up happening and remove if necessary.
        val discord = Element("Discord", R.drawable.ic_discord_logo_black)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://discord.gg/sFCT3Ar")
                }

        val reddit = Element("Reddit", R.drawable.reddit_logo).setOnClickListener {
            GenUtil.launchCustomTabs(context!!, "https://reddit.com/r/siacoin")
        }

        /* creating our own email element because the default one opens a chooser. This one goes straight to email */
        val email = Element("Email me about anything", mehdi.sakout.aboutpage.R.drawable.about_icon_email)
                .setIntent(Intents.emailMe)

        val page = AboutPage(context)
                .addGroup("App")
                .addItem(appVersion)
                .addItem(appGithub)
                .addItem(share)
                .addItem(email)
                .addGroup("Sia")
                .addItem(siaVersion)
                .addItem(siaHomepage)
                .addItem(siaGithub)
                .addItem(reddit)
                .addItem(discord)
                .create()

        val root = page.rootView as ScrollView
        root.isVerticalScrollBarEnabled = false
        val linear = root.getChildAt(0) as LinearLayout
        /* hide the LinearLayout that holds the image and description, since we don't want them */
        linear.getChildAt(0).gone()
        /* hide the top divider line */
        linear.getChildAt(1).gone()

        return page
    }
}