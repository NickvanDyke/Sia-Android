/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.ui.about

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import vandyke.siamobile.R
import vandyke.siamobile.util.StorageUtil

class ModesActivity : AppIntro() {

    private var currentSlide: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bgColor = ContextCompat.getColor(this, android.R.color.white)
        val titleColor = ContextCompat.getColor(this, android.R.color.black)
        val descColor = ContextCompat.getColor(this, android.R.color.darker_gray)

        addSlide(AppIntroFragment.newInstance("Modes",
                "Sia Mobile can operate in multiple modes, explained in the following slides. The modes are independent - changes "
                        + "made while in one mode will not affect other modes. You can change mode and view this again at any time in Settings.",
                R.drawable.sia_logo_svg, bgColor, titleColor, descColor))
        addSlide(AppIntroFragment.newInstance("Paper wallet",
                "Generates a fresh seed and addresses from it. You can send coins to any of the addresses, and later load" +
                        " the seed on a full node to access the coins. Sia Mobile does not save any of this" +
                        " info for you - record it elsewhere.",
                R.drawable.paper_wallet, bgColor, titleColor, descColor))
        addSlide(AppIntroFragment.newInstance("Cold storage",
                "Similar to a paper wallet, except Sia Mobile will store the generated seed and addresses for you. Only for receiving and storing coins. " +
                        "Completely offline the Sia network - like a paper wallet, to see your correct balance and transactions and access/send your coins, you'll" +
                        " have to load your seed on a full node.",
                R.drawable.safe_image, bgColor, titleColor, descColor))
        addSlide(AppIntroFragment.newInstance("Remote full node",
                "Run a full node on your computer, and control it from Sia Mobile. Allows all Sia features. Some setup required.",
                R.drawable.remote_node_graphic, bgColor, titleColor, descColor))
        addSlide(AppIntroFragment.newInstance("Local full node",
                "Run a full node on your device. Completely independent. Allows all Sia features. Must "
                        + "sync Sia blockchain, which uses significant storage and bandwidth - about 5GB.",
                R.drawable.local_node_graphic, bgColor, titleColor, descColor))

        setBarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        setDoneText("Close")

        showSkipButton(false)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        when (currentSlide) {
            1 -> setResult(PAPER_WALLET)
            2 -> setResult(COLD_STORAGE)
            3 -> setResult(REMOTE_FULL_NODE)
            4 -> if (StorageUtil.isSiadSupported) {
                setResult(LOCAL_FULL_NODE)
            } else {
                Toast.makeText(this, "Sorry, but your device's CPU architecture is not supported by Sia's full node", Toast.LENGTH_LONG).show()
                return
            }
        }
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        for (i in 0..slides.size) {
            if (newFragment === slides[i]) {
                currentSlide = i
                break
            }
        }
        showSkipButton(currentSlide != 0)
        when (currentSlide) {
            1 -> setSkipText("Generate")
            2 -> setSkipText("Create")
            3 -> setSkipText("Setup")
            4 -> {
                setSkipText("Start")
                showSkipButton(true)
            }
        }
    }

    companion object {
        var PAPER_WALLET = 1
        var COLD_STORAGE = 2
        var REMOTE_FULL_NODE = 3
        var LOCAL_FULL_NODE = 4
    }
}
