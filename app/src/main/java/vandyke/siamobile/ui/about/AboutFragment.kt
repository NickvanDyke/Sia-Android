/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.cketti.library.changelog.ChangeLog
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import vandyke.siamobile.BuildConfig
import vandyke.siamobile.R
import vandyke.siamobile.ui.common.BaseFragment

class AboutFragment : BaseFragment() {
    override val layoutResId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val version = Element("Version ${BuildConfig.VERSION_NAME}", null)
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
//        return AboutBuilder.with(context)
////                .setName("Nick van Dyke")
////                .setBrief("Sia supporter and Android developer")
////                .setLinksColumnsCount(2)
////                .addEmailLink("siamobiledev@gmail.com")
////                .addGitHubLink("NickvanDyke/Sia-Mobile")
//                .setShowDivider(false)
//                .setAppIcon(R.mipmap.ic_launcher)
//                .setAppName(R.string.app_name)
//                .setVersionNameAsAppSubTitle()
//                .addIntroduceAction(Intent(activity, AboutSiaActivity::class.java))
//                .addHelpAction(Intent(Intent.ACTION_VIEW, Uri.parse("https://support.sia.tech/help_center")))
//                .addFiveStarsAction()
//                .addShareAction("Store your Siacoins securely using Sia Mobile")
//                .addAction(R.drawable.github_logo, "Source code", Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/NickvanDyke/Sia-Mobile")))
//                .addChangeLogAction { ChangelogDialog.createAndShow(fragmentManager!!) }
//                .addFeedbackAction("siamobiledev@gmail.com")
//                .addDonateAction { DonateDialog.createAndShow(fragmentManager!!) }
//                .build()
    }
}