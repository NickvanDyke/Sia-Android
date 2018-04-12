/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.onboarding

import android.content.Intent
import android.os.Bundle
import com.codemybrainsout.onboarder.AhoyOnboarderActivity
import com.codemybrainsout.onboarder.AhoyOnboarderCard
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.ui.main.MainActivity

class SiaPositivesActivity : AhoyOnboarderActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pages = listOf(
                AhoyOnboarderCard("Completely private",
                        "Sia encrypts and distributes your files across a decentralized network. You control your private encryption keys and you own your data. No outside company or third party can access or control your files, unlike traditional cloud storage providers.",
                        R.drawable.sia_intro_image1),

                AhoyOnboarderCard("Far more affordable",
                        "On average, Sia's decentralized cloud storage costs significantly less than incumbent cloud storage providers. Storing 1TB of files on Sia costs about \$2 per month, compared to \$10 on Google Drive.",
                        R.drawable.sia_intro_image2),

                AhoyOnboarderCard("Highly redundant",
                        "Sia distributes and stores redundant file segments on nodes across the globe, eliminating any single point of failure and ensuring uptime that rivals traditional cloud storage providers.",
                        R.drawable.sia_intro_image3),

                AhoyOnboarderCard("Blockchain marketplace",
                        "Using the Sia blockchain, Sia creates a decentralized storage marketplace in which hosts compete for your business, which leads to the lowest possible prices. Renters pay using Siacoin, which can also be mined and traded.",
                        R.drawable.sia_intro_image4),

                AhoyOnboarderCard("Open source",
                        "Sia’s software is completely open source, with contributions from leading software engineers and a thriving community of developers building innovative applications on the Sia API, such as this app!",
                        R.drawable.sia_intro_image5)
        )

        pages.forEach {
            it.titleColor = android.R.color.primary_text_light
            it.descriptionColor = android.R.color.secondary_text_light
            it.backgroundColor = android.R.color.white
        }
        setOnboardPages(pages)

        setColorBackground(if (Prefs.oldSiaColors) R.color.colorPrimaryOld else R.color.colorPrimary)

        showNavigationControls(false)
        setFinishButtonTitle("Sounds great!")
        setFinishButtonDrawableStyle(getDrawable(if (Prefs.oldSiaColors) R.drawable.onboarding_finish_button_oldcolors else R.drawable.onboarding_finish_button))
    }

    override fun onFinishButtonPressed() {
        Prefs.viewedOnboarding = true
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }
}