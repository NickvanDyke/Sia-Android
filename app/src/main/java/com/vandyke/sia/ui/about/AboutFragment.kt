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

        val whySia = Element("Why Sia?", R.drawable.ic_info_outline)
                .setIntent(Intent(context, AboutSiaActivity::class.java))

        val siaHelp = Element("Help", R.drawable.ic_help_outline)
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("https://support.sia.tech/help_center")))


        return AboutPage(context)
                .setImage(R.drawable.sia_logo_svg)
                .setDescription("Decentralized cloud storage of the future")
                .addItem(version)
                .addItem(whySia)
                .addItem(siaHelp)
                .addGitHub("NickvanDyke/Sia-Mobile", "GitHub")
                .addEmail("siamobiledev@gmail.com", "Email me")
                .create()
    }
}