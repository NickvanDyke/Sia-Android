/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.onboarding

import android.os.Bundle
import com.codemybrainsout.onboarder.AhoyOnboarderActivity
import com.codemybrainsout.onboarder.AhoyOnboarderCard
import com.vandyke.sia.R

class IntroActivity : AhoyOnboarderActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val card = AhoyOnboarderCard("Title", "Description", R.drawable.qr_image)
        val pages = listOf(card)
        pages.forEach {
            it.titleColor = android.R.color.white
            it.descriptionColor = android.R.color.white
            it.backgroundColor = R.color.colorPrimary
        }
        setColorBackground(android.R.color.white)
        setOnboardPages(pages)
    }

    override fun onFinishButtonPressed() {
        TODO("not implemented")
    }
}