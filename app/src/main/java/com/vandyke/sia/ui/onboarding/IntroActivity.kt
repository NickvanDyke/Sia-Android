/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.onboarding

import android.os.Bundle
import android.text.SpannableStringBuilder
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import com.vandyke.sia.R
import com.vandyke.sia.util.bullet

class IntroActivity : IntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: not-white background for every slide


        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Android Sia client")
                .description("This app runs the Sia software (a 'Sia node') on your device, and provides an interface for you to interact with it. So why use Sia?")
                .image(R.drawable.sia_new_circle_logo_transparent)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Completely private")
                .description(R.string.intro_completely_private_desc)
                .image(R.drawable.sia_intro_image1)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Far more affordable")
                .description(R.string.intro_far_more_affordable_desc)
                .image(R.drawable.sia_intro_image2)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Highly redundant")
                .description(R.string.intro_highly_redundant_desc)
                .image(R.drawable.sia_intro_image3)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Blockchain marketplace")
                .description(R.string.intro_blockchain_marketplace_desc)
                .image(R.drawable.sia_intro_image4)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Open source")
                .description(R.string.intro_open_source_desc)
                .image(R.drawable.sia_intro_image5)
                .layout(R.layout.fragment_custom_slide)
                .build())


        val str = SpannableStringBuilder()
        str.bullet("This app and Sia are both still under heavy development, and will continue to improve\n\n", 24)
        str.bullet("This app's source code is available on GitHub, so you can see everything it's doing\n\n", 24)
        str.bullet("I, the developer, am not affiliated with Nebulous Labs\n\n", 24)
        str.bullet("Feel free to send me an email, I respond to each and every one!", 24)
        addSlide(SimpleSlide.Builder()
                .background(R.color.colorPrimary)
                .backgroundDark(android.R.color.white)
                .title("Some notes before you begin")
                .description(str)
                .layout(R.layout.fragment_custom_slide_notes)
                .build())

        isButtonBackVisible = false
        isButtonNextVisible = false
    }

    companion object {
        const val REQUEST_CODE_INTRO = 3754
    }
}