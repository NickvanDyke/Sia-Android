/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.onboarding

import android.os.Bundle
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import com.vandyke.sia.R

class IntroActivity : IntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: not-white background for every slide

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Android Sia client")
                .description("Sia for Android runs the Sia software (a 'Sia node') on your device, and provides an interface for you to interact with it.")
                .image(R.drawable.sia_new_circle_logo_transparent)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Blockchain syncing")
                .description("The Sia node will initially have to download, process, and store the Sia blockchain, which is about 11GB." +
                        " This can take a while. You can change the location under Node > Settings.")
                .image(R.drawable.ic_cloud_download_black)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Open source")
                .description("Sia for Android and Sia both have their source code available on GitHub, linked in the About page.")
                .image(mehdi.sakout.aboutpage.R.drawable.about_icon_github)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("In development")
                .description("Sia for Android and Sia are both still under heavy development, and will continue to improve.")
                .image(R.drawable.ic_code_black)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Independent")
                .description("I am not affiliated with Nebulous Labs, and neither is Sia for Android")
                .image(R.drawable.ic_person_outline_black)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Contact me!")
                .description("I respond to each and every email. Please email me from the About page if you " +
                                "have any feedback or questions.")
                .image(mehdi.sakout.aboutpage.R.drawable.about_icon_email)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Completely private")
                .description("Sia encrypts and distributes your files across a decentralized network. You control your private encryption keys and you own your data. No outside company or third party can access or control your files, unlike traditional cloud storage providers.")
                .image(R.drawable.sia_intro_image1)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Far more affordable")
                .description("On average, Sia's decentralized cloud storage costs significantly less than incumbent cloud storage providers. Storing 1TB of files on Sia costs about \$2 per month, compared to \$10 on Google Drive.")
                .image(R.drawable.sia_intro_image2)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Highly redundant")
                .description("Sia distributes and stores redundant file segments on nodes across the globe, eliminating any single point of failure and ensuring uptime that rivals traditional cloud storage providers.")
                .image(R.drawable.sia_intro_image3)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Blockchain marketplace")
                .description("Using the Sia blockchain, Sia creates a decentralized storage marketplace in which hosts compete for your business, which leads to the lowest possible prices. Renters pay using Siacoin, which can also be mined and traded.")
                .image(R.drawable.sia_intro_image4)
                .layout(R.layout.fragment_custom_slide)
                .build())

        addSlide(SimpleSlide.Builder()
                .background(android.R.color.white)
                .backgroundDark(R.color.colorPrimary)
                .title("Open source")
                .description("Sia’s software is completely open source, with contributions from leading software engineers and a thriving community of developers building innovative applications on the Sia API, such as this app!")
                .image(R.drawable.sia_intro_image5)
                .layout(R.layout.fragment_custom_slide)
                .build())

        isButtonBackVisible = false
        isButtonNextVisible = false
    }

    companion object {
        const val REQUEST_CODE_INTRO = 3754
    }
}