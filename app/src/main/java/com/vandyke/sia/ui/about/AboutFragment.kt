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
import com.vandyke.sia.ui.common.BaseFragment
import de.cketti.library.changelog.ChangeLog
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutFragment : BaseFragment() {
    override val layoutResId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val version = Element("Version ${BuildConfig.VERSION_NAME}", R.drawable.ic_format_list_bulleted)
                .setOnClickListener {
                    ChangeLog(context).fullLogDialog.show()
                }

        val siaHelp = Element("Help", R.drawable.ic_help_outline)
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("https://support.sia.tech/help_center")))

        val shareIntent = Intent.createChooser(
                Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "TODO"), "Share Sia") // TODO: some share sentence here, with the play store url
        val share = Element("Share", R.drawable.ic_share)
                .setIntent(shareIntent)

        // maybe I shouldn't have these links here? I don't want people going there for support, since this isn't an official Sia product
        // it's nice to have them other than that though. I'll see what ends up happening and remove if necessary.
        val discord = Element("Discord", R.drawable.discord_logo_black)
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/sFCT3Ar")))

        val reddit = Element("Reddit", R.drawable.reddit_logo)
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("https://reddit.com/r/siacoin")))

        /* creating my own email element because the default one opens a chooser. This one goes straight to email */
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
                .addItem(version)
                .addGitHub("NickvanDyke/Sia-Mobile", "GitHub")
                .addItem(reddit)
                .addItem(discord)
                .addItem(share)
                .addItem(email)
                .addItem(siaHelp)
                .addItem(youtube)
                .create()
    }
}