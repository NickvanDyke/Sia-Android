package vandyke.siamobile.about

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import vandyke.siamobile.R

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val slideshow = Element()
        slideshow.intent = Intent(activity, AboutSiaActivity::class.java)
        slideshow.title = "Info"
        slideshow.iconDrawable = R.drawable.ic_info

        val donate = Element()
        donate.title = "Donate"
        donate.iconDrawable = R.drawable.ic_heart
        donate.setOnClickListener { DonateDialog.createAndShow(fragmentManager) }

        return AboutPage(activity)
                .setImage(R.drawable.sia_logo_svg)
                .setDescription("Thanks for using Sia Mobile!")
//                .addGroup("Sia Mobile")
                .addEmail("siamobiledev@gmail.com", "Contact")
                .addGitHub("NickvanDyke/Sia-Mobile", "GitHub")
                .addPlayStore("vandyke.siamobile", "Rate")
                .addItem(donate)
                .addGroup("Sia")
                .addItem(slideshow)
                .addGitHub("NebulousLabs/Sia", "GitHub")
                .addWebsite("sia.tech", "Homepage")
                .addWebsite("sia.tech/about", "About")
                .addWebsite("sia.tech/faq", "FAQ")
                .addWebsite("support.sia.tech/help_center", "Help")
                .addWebsite("siawiki.tech", "Wiki")
                .addWebsite("slackin.sia.tech", "Slack")
                .addWebsite("blog.sia.tech", "Blog")
                .addWebsite("sia.tech/get-siacoin", "How to buy Siacoin")
                .create()
    }
}