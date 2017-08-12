/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.about

import android.app.Fragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vansuita.materialabout.builder.AboutBuilder
import vandyke.siamobile.R

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return AboutBuilder.with(activity)
                .setName("Nick van Dyke")
                .setBrief("Passionate Android freelancer. Email me with an app you want developed and an offer!")
                .setLinksColumnsCount(2)
                .addEmailLink("siamobiledev@gmail.com")
                .addGitHubLink("NickvanDyke/Sia-Mobile")
                .setDividerHeight(5)
                .setAppIcon(R.mipmap.ic_launcher)
                .setAppName(R.string.app_name)
                .setVersionNameAsAppSubTitle()
                .addIntroduceAction(Intent(activity, AboutSiaActivity::class.java))
                .addHelpAction(Intent(Intent.ACTION_VIEW, Uri.parse("https://support.sia.tech/help_center")))
                .addFiveStarsAction()
                .addShareAction("Try the future of cloud storage with Sia Mobile")
                .addUpdateAction()
                .addChangeLogAction { ChangelogDialog.createAndShow(fragmentManager) }
                .addFeedbackAction("siamobiledev@gmail.com")
                .addDonateAction { DonateDialog.createAndShow(fragmentManager) }
                .build()
    }
}