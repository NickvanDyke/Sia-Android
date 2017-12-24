/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.about

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import vandyke.siamobile.R

class AboutSiaActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bgColor = ContextCompat.getColor(this, android.R.color.white)
        val titleColor = ContextCompat.getColor(this, android.R.color.black)
        val descColor = ContextCompat.getColor(this, android.R.color.darker_gray)

        addSlide(AppIntroFragment.newInstance("Completely private",
                "Sia splits apart, encrypts, and distributes your files across a decentralized network. Since you hold" +
                        " the keys, you own your data. No outside company can access or control your files, unlike traditional cloud storage providers.",
                R.drawable.siainfo1, bgColor, titleColor, descColor))
        addSlide(AppIntroFragment.newInstance("Far more affordable",
                "Sia's decentralized cloud is on average significantly less expensive than current cloud storage providers.",
                R.drawable.siainfo2, bgColor, titleColor, descColor))
        addSlide(AppIntroFragment.newInstance("Highly redundant",
                "Sia stores tiny pieces of your files on dozens of nodes across the globe. This eliminates any "
                        + "single point of failure and ensures highest possible uptime, superior to traditional cloud storage providers.",
                R.drawable.siainfo3, bgColor, titleColor, descColor))
        addSlide(AppIntroFragment.newInstance("Open source",
                "Sia is completely open source. Over a dozen individuals have contributed to Sia's software, and "
                        + "there is an active community building innovative applications on top of the Sia API.",
                R.drawable.siainfo4, bgColor, titleColor, descColor))
        addSlide(AppIntroFragment.newInstance("Blockchain marketplace",
                "Using the Sia blockchain, Sia creates a decentralized storage marketplace in which hosts compete for" +
                        " your business – this leads to the lowest possible prices. Renters pay using Siacoin, which can also be mined and traded.",
                R.drawable.siainfo5, bgColor, titleColor, descColor))

        setBarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        setSkipText("Close")
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }
}
