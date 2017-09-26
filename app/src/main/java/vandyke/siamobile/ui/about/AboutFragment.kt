/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vansuita.materialabout.builder.AboutBuilder
import vandyke.siamobile.R

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return AboutBuilder.with(activity)
//                .setName("Nick van Dyke")
//                .setBrief("Sia supporter and Android developer")
//                .setLinksColumnsCount(2)
//                .addEmailLink("siamobiledev@gmail.com")
//                .addGitHubLink("NickvanDyke/Sia-Mobile")
                .setShowDivider(false)
                .setAppIcon(R.mipmap.ic_launcher)
                .setAppName(R.string.app_name)
                .setVersionNameAsAppSubTitle()
                .addIntroduceAction(Intent(activity, AboutSiaActivity::class.java))
                .addHelpAction(Intent(Intent.ACTION_VIEW, Uri.parse("https://support.sia.tech/help_center")))
                .addFiveStarsAction()
                .addShareAction("Store your Siacoins securely using Sia Mobile")
                .addAction(R.drawable.github_logo, "Source code", Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/NickvanDyke/Sia-Mobile")))
                .addChangeLogAction { ChangelogDialog.createAndShow(fragmentManager) }
                .addFeedbackAction("siamobiledev@gmail.com")
                .addDonateAction { DonateDialog.createAndShow(fragmentManager) }
                .build()
    }
}