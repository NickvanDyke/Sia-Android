/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.sia.BuildConfig
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.GenUtil
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutFragment : BaseFragment() {
    override val layoutResId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val appVersion = Element("Version ${BuildConfig.VERSION_NAME}", R.drawable.ic_format_list_bulleted)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://github.com/NickvanDyke/Sia-Android/releases")
                }

        val siaVersion = Element("Version ${Prefs.siaVersion}", R.drawable.siacoin_logo_svg)
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

        val siaHelp = Element("Help", R.drawable.ic_help_outline)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://support.sia.tech/help_center")
                }

        val shareIntent = Intent.createChooser(
                Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.vandyke.sia"), "Share Sia")
        val share = Element("Share", R.drawable.ic_share)
                .setIntent(shareIntent)

        // maybe I shouldn't have these links here? I don't want people going there for support, since this isn't an official Sia product.
        // It's nice to have them other than that though. I'll see what ends up happening and remove if necessary.
        val discord = Element("Discord", R.drawable.discord_logo_black)
                .setOnClickListener {
                    GenUtil.launchCustomTabs(context!!, "https://discord.gg/sFCT3Ar")
                }

        val reddit = Element("Reddit", R.drawable.reddit_logo).setOnClickListener {
            GenUtil.launchCustomTabs(context!!, "https://reddit.com/r/siacoin")
        }

        /* creating our own email element because the default one opens a chooser. This one goes straight to email */
        val emailIntent = Intent(Intent.ACTION_SENDTO)
                .setData(Uri.parse("mailto:"))
                .putExtra(Intent.EXTRA_EMAIL, "siamobiledev@gmail.com")
        val email = Element("Email me", mehdi.sakout.aboutpage.R.drawable.about_icon_email)
                .setIntent(emailIntent)

        val youtube = Element("Why Sia?", mehdi.sakout.aboutpage.R.drawable.about_icon_youtube)
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=B4YGpWxyn6Y")))

        return AboutPage(context)
                .setImage(R.drawable.sia_logo_svg) // TODO: image and text could probably be better
                .setDescription("Your private, decentralized cloud")
                .addGroup("App")
                .addItem(appVersion)
                .addItem(appGithub)
                .addItem(share)
                .addItem(email)
                .addGroup("Sia")
                .addItem(siaVersion)
                .addItem(siaGithub)
                .addItem(reddit)
                .addItem(discord)
                .addItem(youtube)
                .addItem(siaHelp)
                .create()
    }
}