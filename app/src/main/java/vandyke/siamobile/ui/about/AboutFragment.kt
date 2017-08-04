package vandyke.siamobile.ui.about

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vansuita.materialabout.builder.AboutBuilder
import vandyke.siamobile.R

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        val slideshow = Element()
//        slideshow.intent = Intent(activity, AboutSiaActivity::class.java)
//        slideshow.title = "Info"
//        slideshow.iconDrawable = R.drawable.ic_info
//
//        val donate = Element()
//        donate.title = "Donate"
//        donate.iconDrawable = R.drawable.ic_heart
//        donate.setOnClickListener { DonateDialog.createAndShow(fragmentManager) }
//
//        return AboutPage(activity)
//                .setImage(R.drawable.sia_logo_svg)
//                .setDescription("Thanks for using Sia Mobile!")
////                .addGroup("Sia Mobile")
//                .addEmail("siamobiledev@gmail.com", "Contact")
//                .addGitHub("NickvanDyke/Sia-Mobile", "GitHub")
//                .addPlayStore("vandyke.siamobile", "Rate")
//                .addItem(donate)
//                .addGroup("Sia")
//                .addItem(slideshow)
//                .addGitHub("NebulousLabs/Sia", "GitHub")
//                .addWebsite("sia.tech", "Homepage")
//                .addWebsite("sia.tech/about", "About")
//                .addWebsite("sia.tech/faq", "FAQ")
//                .addWebsite("support.sia.tech/help_center", "Help")
//                .addWebsite("siawiki.tech", "Wiki")
//                .addWebsite("slackin.sia.tech", "Slack")
//                .addWebsite("blog.sia.tech", "Blog")
//                .addWebsite("sia.tech/get-siacoin", "How to buy Siacoin")
//                .create()
        return AboutBuilder.with(activity)
                .setAppIcon(R.mipmap.ic_launcher)
                .setAppName(R.string.app_name)
                .setVersionNameAsAppSubTitle()
                .addEmailLink("siamobiledev@gmail.com")
                .addGitHubLink("NickvanDyke/Sia-Mobile")
                .addDonateAction { DonateDialog.createAndShow(fragmentManager) }
                .addChangeLogAction { ChangelogDialog.createAndShow(fragmentManager) }
                .build()
    }
}