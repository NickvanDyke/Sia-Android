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

class IntroActivity : AhoyOnboarderActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val explanationCard = AhoyOnboarderCard("Android Sia client",
                "Sia for Android runs a Sia node - which interacts with the Sia network - on your device, and lets " +
                        "you interface with it, and therefore the network.",
                R.drawable.sia_new_circle_logo_transparent)
        val syncCard = AhoyOnboarderCard("Blockchain syncing",
                "The Sia node will initially have to download, process, and store the Sia blockchain, which is about 11GB." +
                        " This can take a while.",
                R.drawable.ic_cloud_download_black)
        val sourceCard = AhoyOnboarderCard("Open source",
                "Sia for Android and Sia both have their source code available on GitHub, linked in the About page.",
                mehdi.sakout.aboutpage.R.drawable.about_icon_github)
        val underDevCard = AhoyOnboarderCard("In development",
                "Sia for Android and Sia are both still under heavy development, and will continue to improve.",
                R.drawable.ic_code)
        val independentCard = AhoyOnboarderCard("Independent",
                "Sia for Android is developed independently by me, an individual, and is not affiliated with Nebulous Labs.",
                R.drawable.ic_person_outline)
        val emailMeCard = AhoyOnboarderCard("Contact me!",
                "I respond to each and every email. Please email me from the About page if you " +
                        "have any feedback or questions. Enjoy!",
                mehdi.sakout.aboutpage.R.drawable.about_icon_email)

        val pages = listOf(explanationCard, syncCard, sourceCard, underDevCard, independentCard, emailMeCard)
        pages.forEach {
            it.titleColor = android.R.color.primary_text_light
            it.descriptionColor = android.R.color.secondary_text_light
            it.backgroundColor = android.R.color.white
        }
        setOnboardPages(pages)

        setColorBackground(R.color.colorPrimary)

        showNavigationControls(false)
        setFinishButtonTitle("Get started")
        setFinishButtonDrawableStyle(getDrawable(R.drawable.onboarding_finish_button))
    }

    override fun onFinishButtonPressed() {
        Prefs.viewedOnboarding = true
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }
}